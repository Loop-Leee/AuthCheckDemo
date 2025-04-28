package com.lloop.authcheckdemo.utils;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @Author lloop
 * @Create 2024/12/26 15:06
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis prefix
    public static final String USER_REFRESH_TOKEN = "user:login:refresh:";
    public static final String TOKEN_BLACKLIST = "user:blacklist:";

    private static final String ERROR_KEY_NULL = "key不能为空";
    private static final String ERROR_VALUE_NULL = "value不能为空";
    private static final String ERROR_TIME_NEGATIVE = "time必须大于0";

    /**
     * 执行Redis操作的模板方法
     * @param operation Redis操作
     * @param defaultValue 默认返回值
     * @return 操作结果
     */
    private <T> T executeWithConnection(Supplier<T> operation, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        } finally {
            RedisConnectionUtils.unbindConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        }
    }

    /**
     * 移除Redis前缀
     * @param key
     * @param prefix
     * @return
     */
    public String removePrefix(String key, String prefix){
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(prefix, "prefix不能为空");
        return key.substring(prefix.length());
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(() -> {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        }, false);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public Long getExpire(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.getExpire(key, TimeUnit.SECONDS),
            null
        );
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.hasKey(key),
            false
        );
    }

    /**
     * 删除缓存
     *
     * @param keys 可以传一个值 或多个
     */
    public void delete(String... keys) {
        Assert.notEmpty(keys, "keys不能为空");
        executeWithConnection(() -> {
            if (keys.length == 1) {
                redisTemplate.delete(keys[0]);
            } else {
                redisTemplate.delete(Arrays.asList(keys));
            }
            return null;
        }, null);
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public <T> T get(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                return (T) ops.get(key);
            },
            null
        );
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForValue().set(key, value);
                return true;
            },
            false
        );
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        
        return executeWithConnection(
            () -> {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
                return true;
            },
            false
        );
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return long
     */
    public Long incr(String key, long delta) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.isTrue(delta > 0, "递增因子必须大于0");
        return executeWithConnection(
            () -> redisTemplate.opsForValue().increment(key, delta),
            0L
        );
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return long
     */
    public Long decr(String key, long delta) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.isTrue(delta > 0, "递减因子必须大于0");
        return executeWithConnection(
            () -> redisTemplate.opsForValue().increment(key, -delta),
            0L
        );
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hGet(String key, String item) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForHash().get(key, item),
            null
        );
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hMGet(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForHash().entries(key),
            Collections.emptyMap()
        );
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 需要输入的多个 <field, value>
     * @return boolean 输入结果
     */
    public boolean hMSet(String key, Map<String, Object> map) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(map, "map不能为空");
        return executeWithConnection(
            () -> {
                redisTemplate.opsForHash().putAll(key, map);
                return true;
            },
            false
        );
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hMSet(String key, Map<String, Object> map, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(map, "map不能为空");
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForHash().putAll(key, map);
                expire(key, time);
                return true;
            },
            false
        );
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hSet(String key, String item, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        Assert.notNull(value, ERROR_VALUE_NULL);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForHash().put(key, item, value);
                return true;
            },
            false
        );
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hSet(String key, String item, Object value, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        Assert.notNull(value, ERROR_VALUE_NULL);
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForHash().put(key, item, value);
                expire(key, time);
                return true;
            },
            false
        );
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hDel(String key, Object... items) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(items, "items不能为空");
        executeWithConnection(
            () -> {
                redisTemplate.opsForHash().delete(key, items);
                return null;
            },
            null
        );
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForHash().hasKey(key, item),
            false
        );
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return Double
     */
    public Double hIncr(String key, String item, double by) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        Assert.isTrue(by > 0, "递增因子必须大于0");
        return executeWithConnection(
            () -> redisTemplate.opsForHash().increment(key, item, by),
            null
        );
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return Double
     */
    public Double hDecr(String key, String item, double by) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.hasText(item, "item不能为空");
        Assert.isTrue(by > 0, "递减因子必须大于0");
        return executeWithConnection(
            () -> redisTemplate.opsForHash().increment(key, item, -by),
            null
        );
    }

    /**
     * 获取所有值
     * @param key key
     * @param options options 查询条件 ScanOptions.scanOptions().count(2l).match("space*").build()
     * @return Cursor 迭代器方式获取数据 while(cursor.hasNext()){ Map.Entry<HK, HV> map = cursor.next(); HK key = map.getKey()
     * ; HV value = map.getValue();}
     */
    public Cursor<Map.Entry<Object, Object>> hScan(String key, ScanOptions options) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(options, "options不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForHash().scan(key, options),
            null
        );
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return 结果集合
     */
    public Set<Object> sGet(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForSet().members(key),
            Collections.emptySet()
        );
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean sHasKey(String key, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForSet().isMember(key, value),
            false
        );
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sSet(String key, Object... values) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(values, "values不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForSet().add(key, values),
            0L
        );
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sSetAndTime(String key, long time, Object... values) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(values, "values不能为空");
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(
            () -> {
                Long count = redisTemplate.opsForSet().add(key, values);
                expire(key, time);
                return count;
            },
            0L
        );
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return Long
     */
    public Long sGetSetSize(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForSet().size(key),
            null
        );
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long sRem(String key, Object... values) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(values, "values不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForSet().remove(key, values),
            0L
        );
    }

    // ============================zSet=============================

    /**
     * zSet集合元素个数
     *
     * @param key key
     * @return 个数
     */
    public Long zSetSize(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForZSet().size(key),
            0L
        );
    }

    /**
     * zSet添加元素,分值默认为当前时间戳
     *
     * @param key    key
     * @param values field
     * @return 添加是否成功
     */
    public Boolean zSetAdd(String key, Object value) {
        return zSetAdd(key, value, System.currentTimeMillis());
    }

    /**
     * zSet添加元素
     *
     * @param key   key 键
     * @param value field 元素
     * @param score score 分值
     * @return 添加是否成功
     */
    public Boolean zSetAdd(String key, Object value, Long score) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        Assert.notNull(score, "score不能为空");
        return executeWithConnection(
            () -> redisTemplate.opsForZSet().add(key, value, score),
            false
        );
    }

    /**
     * 获取所有值及其分数
     *
     * @param key 键
     * @return 值集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> getZSetWithScores(String key) {
        return getZSetWithScores(key, 0, -1);
    }

    /**
     * 获取范围内值及其分数（索引从 0 位置开始）
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return 值集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> getZSetWithScores(String key, long start, long end) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForZSet().rangeWithScores(key, start, end),
            Collections.emptySet()
        );
    }

    /**
     * 获取指定类型的zSet正序结果集合
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @param <R>   结果类型 <warn> 注意：redis序列化 使得Long存储为字符串类型 ，取出时不能通过 R直接转换为Long类型结合</warn>
     * @return 集合结果
     */
    public List<Object> getZSetResults(String key, long start, long end) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> {
                if (zSetSize(key) == 0) {
                    return new ArrayList<>(0);
                }
                return new ArrayList<>(redisTemplate.opsForZSet().range(key, start, end));
            },
            Collections.emptyList()
        );
    }

    /**
     * 获取指定类型的zSet倒序结果集合 （排行使用）
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @param <R>   结果类型 <warn> 注意：redis序列化 使得Long存储为字符串类型 ，取出时不能通过 R直接转换为Long类型结合</warn>
     * @return 集合结果
     */
    public List<Object> getReverseZSetResults(String key, long start, long end) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> {
                if (zSetSize(key) == 0) {
                    return new ArrayList<>(0);
                }
                return new ArrayList<>(redisTemplate.opsForZSet().reverseRange(key, start, end));
            },
            Collections.emptyList()
        );
    }

    /**
     * 获取倒序zSet
     */
    public Set<Object> getReverseZSet(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForZSet().reverseRange(key, 0, -1),
            Collections.emptySet()
        );
    }

    /**
     * 移除zSet中的值
     */
    public void removeZSet(String key, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        executeWithConnection(
            () -> {
                redisTemplate.opsForZSet().remove(key, value);
                return null;
            },
            null
        );
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForList().range(key, start, end),
            Collections.emptyList()
        );
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return long
     */
    public long lGetListSize(String key) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForList().size(key),
            0L
        );
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return Object
     */
    public Object lGetIndex(String key, long index) {
        Assert.hasText(key, ERROR_KEY_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForList().index(key, index),
            null
        );
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForList().rightPush(key, value);
                return true;
            },
            false
        );
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return boolean
     */
    public boolean lSet(String key, Object value, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForList().rightPush(key, value);
                expire(key, time);
                return true;
            },
            false
        );
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public boolean lSet(String key, List<Object> value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(value, "value不能为空");
        return executeWithConnection(
            () -> {
                redisTemplate.opsForList().rightPushAll(key, value);
                return true;
            },
            false
        );
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return Boolean
     */
    public boolean lSet(String key, List<Object> value, long time) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notEmpty(value, "value不能为空");
        Assert.isTrue(time > 0, ERROR_TIME_NEGATIVE);
        return executeWithConnection(
            () -> {
                redisTemplate.opsForList().rightPushAll(key, value);
                expire(key, time);
                return true;
            },
            false
        );
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lUpdateIndex(String key, long index, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        executeWithConnection(
            () -> {
                redisTemplate.opsForList().set(key, index, value);
                return null;
            },
            null
        );
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public Long lRemove(String key, long count, Object value) {
        Assert.hasText(key, ERROR_KEY_NULL);
        Assert.notNull(value, ERROR_VALUE_NULL);
        return executeWithConnection(
            () -> redisTemplate.opsForList().remove(key, count, value),
            null
        );
    }

    /**
     * 重命名
     *
     * @param oldKey oldKey
     * @param newKey newKey
     */
    public void renameKey(String oldKey, String newKey) {
        Assert.hasText(oldKey, "oldKey不能为空");
        Assert.hasText(newKey, "newKey不能为空");
        executeWithConnection(
            () -> {
                redisTemplate.rename(oldKey, newKey);
                return null;
            },
            null
        );
    }

    /**
     * 缓存预热
     */
//    @Scheduled(cron = "0 3 0 * * *")
//    public void cacheRecommendForUsers(Map<String, Object> map) {
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
//            String redisKey = UserService.CACHE_RECOMMEND_PREFIX + user.getId();
//            try {
//                redisTemplate.opsForValue().set(redisKey, userPage, 60 * 60 * 24, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            String s = "abc";
//            int length = s.length();
//        }
}

