package com.lloop.authcheckdemo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author lloop
 * @Create 2024/12/28 18:48
 */
@SpringBootTest
class JwtUtilsTest {

    @Resource
    JwtUtils jwtUtils;

    @Value("${jwt.secret}")
    public String secret;

    @Test
    void getTokenClaim() {

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJpZFwiOjE1LFwiYWNjb3VudFwiOlwibGxvb3BcIixcInVzZXJuYW1lXCI6XCLnk6Lnk6JcIixcInJvbGVcIjoxfSIsImlhdCI6MTczNTM4MzU1OCwiZXhwIjoxNzM1MzkwNzU4fQ.am-OUPBMLaAidZ1kmAQ9F3cDaqiRZeuYyZMOhtLbQoo";
        Claims body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        System.out.println("getTokenClaim :" + jwtUtils.getTokenClaim(token));
        System.out.println("body :" + body);

    }
}