package com.lloop.authcheckdemo.utils;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.Cursor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author lloop
 * @Create 2024/12/26 15:06
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    @Resource
    private RedisTemplate redisTemplate;

    // Redis prefix
    public static final String USER_REFRESH_TOKEN = "user:login:refresh:";
    public static final String TOKEN_BLACKLIST = "user:blacklist:";


    /**
     * 移除Redis前缀
     * @param key
     * @param prefix
     * @return
     */
    public String removePrefix(String key, String prefix){
        return key.substring(prefix.length());
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public void expire(String key, long time) {

        if (time > 0) {
            try {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                extracted();
            }
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                try {
                    redisTemplate.delete(key[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    extracted();
                }
            } else {
                try {
                    redisTemplate.delete(CollectionUtils.arrayToList(key));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    extracted();
                }
            }
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void del(Collection<Object> key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(String key) {
        if (key != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                extracted();
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public <T> T get(String key) {
        if (key == null) {
            return null;
        } else {
            ValueOperations<String, T> operation;
            try {
                operation = redisTemplate.opsForValue();
                return operation.get(key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                extracted();
            }
        }
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
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

        if (time > 0) {
            try {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                extracted();
            }
        } else {
            set(key, value);
        }
        return true;
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return long
     */
    public Long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return long
     */
    public Long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, -delta);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
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
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hMGet(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        } finally {
            extracted();
        }
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hMSet(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
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
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
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
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
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
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hDel(String key, Object... item) {
        try {
            redisTemplate.opsForHash().delete(key, item);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
        return false;
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
        Double increment = null;
        try {
            increment = redisTemplate.opsForHash().increment(key, item, by);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
        return increment;
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
        try {
            return redisTemplate.opsForHash().increment(key, item, -by);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /***
     * 获取所有值
     * @param key key
     * @param options options 查询条件 ScanOptions.scanOptions().count(2l).match("space*").build()
     * @return Cursor 迭代器方式获取数据 while(cursor.hasNext()){ Map.Entry<HK, HV> map = cursor.next(); HK key = map.getKey()
     * ; HV value = map.getValue();}
     */
    public <HK, HV> Cursor<Map.Entry<HK, HV>> hScan(String key, ScanOptions options) {
        try {
            return redisTemplate.opsForHash().scan(key, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return 结果集合
     */
    public <T> Set<T> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, Objects.requireNonNull(values));
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
    }

    private void extracted() {
        RedisConnectionUtils.unbindConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
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
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return Long
     */
    public Long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long sRem(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
    }

    // ============================zSet=============================

    /**
     * zSet集合元素个数
     *
     * @param key key
     * @return 个数
     */
    public Long zSetSize(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            extracted();
        }
    }

    /**
     * zSet添加元素,分值默认为当前时间戳
     *
     * @param key    key
     * @param values field
     * @return 添加是否成功
     */
    public Boolean zSetAdd(String key, Object values) {
        return zSetAdd(key, values, System.currentTimeMillis());
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
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 获取所有值及其分数
     *
     * @param key 键
     * @return 值集合
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> getZSetWithScores(String key) {
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
    public <T> Set<ZSetOperations.TypedTuple<T>> getZSetWithScores(String key, long start, long end) {
        try {
            return (Set<ZSetOperations.TypedTuple<T>>) redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
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
    public <R> List<R> getZSetResults(String key, long start, long end) {
        try {
            if (0 == zSetSize(key)) {
                return new ArrayList<R>(0);
            }
            return new ArrayList<R>
                    (Objects.requireNonNull(redisTemplate.opsForZSet().range(key, start, end)));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        } finally {
            extracted();
        }
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
    public <R> List<R> getReverseZSetResults(String key, long start, long end) {
        try {
            if (0 == zSetSize(key)) {
                return new ArrayList<R>(0);
            }
            return new ArrayList<R>
                    (Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(key, start, end)));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        } finally {
            extracted();
        }
    }


    public <T> Set<T> getReverseZSet(String key) {
        try {
            return (Set<T>) redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    public void removeZSet(String key, Object values) {
        redisTemplate.opsForZSet().remove(key, values);
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
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return long
     */
    public long lGetListSize(String key) {
        try {
            return Objects.requireNonNull(redisTemplate.opsForList().size(key));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            extracted();
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return Object
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
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
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return Boolean
     */
    public Boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            extracted();
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
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
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            extracted();
        }
    }

    /**
     * 重命名
     *
     * @param oldKey oldKey
     * @param newKey newKey
     */
    public void renamKey(String oldKey, String newKey) {
        try {
            redisTemplate.rename(oldKey, newKey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extracted();
        }
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

