package com.lloop.authcheckdemo.service;

import com.lloop.authcheckdemo.common.BaseResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;

/**
* @author lloop
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-24 21:53:21
*/
public interface UserService extends IService<User> {

    String userRegister(UserRegisterRequest userRegisterRequest);

    String userLogin(UserLoginRequest userLoginRequest);
}
