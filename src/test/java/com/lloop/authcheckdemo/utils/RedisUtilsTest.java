package com.lloop.authcheckdemo.utils;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author lloop
 * @Create 2024/12/28 18:40
 */
@SpringBootTest
class RedisUtilsTest {

    @Resource
    RedisUtils redisUtils;

    @Test
    void removePrefix() {
        String token = "123";
        String withPrefix = RedisUtils.USER_REFRESH_TOKEN + token;
        String removePrefix = redisUtils.removePrefix(withPrefix, RedisUtils.USER_REFRESH_TOKEN);
        assertEquals(removePrefix, token);
    }
}