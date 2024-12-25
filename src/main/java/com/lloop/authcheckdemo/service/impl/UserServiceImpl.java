package com.lloop.authcheckdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.mapper.UserMapper;
import com.lloop.authcheckdemo.utils.RedisIdWorker;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
* @author lloop
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-24 21:53:21
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String USER_PREFIX = "user";

    // TODO 用户注册
    @Override
    public String userRegister(UserRegisterRequest userRegisterRequest) {
        String account = userRegisterRequest.getAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        ThrowUtils.throwIf(StringUtils.isEmpty(account), ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(userMapper.selectByAccount(account) != null, ErrorCode.PARAMS_ERROR, "用户名已存在");

        isValidPassword(userPassword, checkPassword);

        User user = new User();
        user.setAccount(account);
        user.setPassword(userPassword);
        userMapper.insert(user);
        user = userMapper.selectById(user.getId());

        String cookieId = redisIdWorker.nextId(USER_PREFIX);
        stringRedisTemplate.opsForValue().set(cookieId, String.valueOf(user.getId()));
        stringRedisTemplate.expire(cookieId, 30, TimeUnit.MINUTES);
        return cookieId;
    }

    private void isValidPassword(String userPassword, String checkPassword){
        ThrowUtils.throwIf(StringUtils.isEmpty(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,12}$";
        boolean matches = userPassword.matches(pattern);
        ThrowUtils.throwIf(!matches, ErrorCode.PARAMS_ERROR, "密码必须包含数字、大小写字母、特殊字符，长度在6-12位");
    }

    @Override
    public String userLogin(UserLoginRequest userLoginRequest) {
        return "";
    }
}




