package com.lloop.authcheckdemo.controller;

import com.lloop.authcheckdemo.annotation.IsAdmin;
import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.model.request.UserEditRequest;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.utils.ResultUtils;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @Author lloop
 * @Create 2024/12/24 23:06
 */
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/user/register")
    public BaseResponse<UserToken> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userRegisterRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        String account = userRegisterRequest.getAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        ThrowUtils.throwIf(StringUtils.isEmpty(account), ErrorCode.PARAMS_ERROR, "用户名不能为空");

        return ResultUtils.success(userService.userRegister(account, userPassword, checkPassword));
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/user/login")
    public BaseResponse<UserToken> userLogin(@RequestBody UserLoginRequest userLoginRequest){
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userLoginRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        String account = userLoginRequest.getAccount();
        String userPassword = userLoginRequest.getPassword();
        ThrowUtils.throwIf(StringUtils.isEmpty(account), ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(StringUtils.isEmpty(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        return ResultUtils.success(userService.userLogin(account, userPassword));
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken
     * @return
     */
    @PostMapping("/user/refreshToken/{refreshToken}")
    public BaseResponse<UserToken> refreshToken(@PathVariable("refreshToken") String refreshToken) {
        ThrowUtils.throwIf(StringUtils.isEmpty(refreshToken), ErrorCode.PARAMS_ERROR, "刷新令牌不能为空");
        return ResultUtils.success(userService.refreshToken(refreshToken));
    }


    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/user/logout")
    public BaseResponse<String> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token.substring(7));
        return ResultUtils.success("登出成功");
    }


    /**
     * 修改用户信息
     *
     * @param userEditRequest
     * @return
     */
    @PostMapping("/user/edit")
    public BaseResponse<String> editUser(@RequestBody UserEditRequest userEditRequest) {
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userEditRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        userService.editUser(userEditRequest);
        return ResultUtils.success("修改成功");
    }


    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/user/info")
    public BaseResponse<UserEditRequest> getUserInfo(@RequestHeader("Authorization") String token) {
        return ResultUtils.success(userService.getUserInfo());
    }

    /**
     * 测试管理员注解
     *
     * @return
     */
    @IsAdmin
    @GetMapping("/user/testAdmin")
    public BaseResponse<String> testAdmin() {
        return ResultUtils.success("管理员, 您好!");
    }

    @GetMapping("/user/homepage/info")
    public BaseResponse<UserEditRequest> homepage(@RequestHeader("Authorization") String token) {
        return ResultUtils.success(userService.getUserInfo());
    }

}
