package com.lloop.authcheckdemo.controller;

import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.utils.ResultUtils;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userRegisterRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        String account = userRegisterRequest.getAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        ThrowUtils.throwIf(StringUtils.isEmpty(account), ErrorCode.PARAMS_ERROR, "用户名不能为空");

        return ResultUtils.success(userService.userRegister(account, userPassword, checkPassword));
    }

    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest){
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userLoginRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        String account = userLoginRequest.getAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isEmpty(account), ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(StringUtils.isEmpty(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        return ResultUtils.success(userService.userLogin(account, userPassword));
    }

}