package com.lloop.authcheckdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.constant.UserConstant;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.model.dto.UserDTO;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.mapper.UserMapper;
import com.lloop.authcheckdemo.utils.RedisIdWorker;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

    /**
     * 用户注册
     * @param account, userPassword, checkPassword
     * @return
     */
    @Override
    public String userRegister(String account, String userPassword, String checkPassword) {
        ThrowUtils.throwIf(userMapper.selectByAccount(account) != null, ErrorCode.PARAMS_ERROR, "用户名已存在");
        isValidPassword(userPassword, checkPassword);

        User user = new User();
        user.setAccount(account);
        user.setPassword(userPassword);
        userMapper.insert(user);
        

        String cookieId = redisIdWorker.nextId(UserConstant.USER_LOGIN_PREFIX);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_PREFIX + cookieId, String.valueOf(user.getId()));
        stringRedisTemplate.expire(cookieId, 30, TimeUnit.MINUTES);
        return cookieId;
    }

    @Override
    public String userLogin(String account, String userPassword) {
        User user = userMapper.selectByAccount(account);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.PARAMS_ERROR, "请输入正确的用户名和密码");
        ThrowUtils.throwIf(!user.getPassword().equals(userPassword), ErrorCode.PARAMS_ERROR, "请输入正确的用户名和密码");

        String cookieId = redisIdWorker.nextId(UserConstant.USER_LOGIN_PREFIX);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_PREFIX + cookieId, String.valueOf(user.getId()));
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

}




