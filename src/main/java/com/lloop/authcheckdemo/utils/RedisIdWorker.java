package com.lloop.authcheckdemo.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @Author lloop
 * @Create 2024/12/25 15:11
 */
@Component
public class RedisIdWorker {

    // 开始时间戳
    private static final long BEGIN_TIMESTAMP = 1640995200L;

    // 序列号的位数
    private static final int COUNT_BITS = 32;

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 获取下一个自动生成的 id
    public String nextId(String keyPrefix){
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 3.获取当前日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 4.获取自增长值：生成一个递增计数值。每次调用 increment 方法时，它会在这个key之前的自增值的基础上+1（第一次为0）
        String key = "icr:" + keyPrefix + ":" + date;
        long count = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
        // 5.拼接并返回
        return String.valueOf(timestamp << COUNT_BITS | count);
    }
}