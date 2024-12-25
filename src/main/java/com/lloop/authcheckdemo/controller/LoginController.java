package com.lloop.authcheckdemo.controller;

import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

/**
 * @Author lloop
 * @Create 2024/12/24 23:06
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(ObjectUtils.isEmpty(userRegisterRequest)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String user = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(user);
    }

    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest){
        if(ObjectUtils.isEmpty(user)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.userLogin(userLoginRequest));
    }

}
