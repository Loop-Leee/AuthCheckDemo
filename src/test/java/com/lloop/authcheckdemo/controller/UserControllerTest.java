package com.lloop.authcheckdemo.controller;

import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author lloop
 * @Create 2024/12/27 19:55
 */
@SpringBootTest
class UserControllerTest {

    @Resource
    UserService userService;

    @Test
    void userLoginAndRefreshTokenAndLogOut() {
        UserToken userToken = userService.userLogin("lloop", "123");
        Assertions.assertNotNull(userToken.getRefreshToken(), "refreshToken 获取失败");
        Assertions.assertNotNull(userToken.getAccessToken(), "accessToken 获取失败");

        // UserToken refreshed = userService.refreshToken(userToken.getRefreshToken());
        // Assertions.assertNotEquals(userToken.getAccessToken(), refreshed.getAccessToken(), "accessToken 刷新失败");

        userService.logOut(userToken.getAccessToken());
    }

}