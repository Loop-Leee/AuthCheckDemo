package com.lloop.authcheckdemo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.constant.UserConstant;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.model.dto.UserDTO;
import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.model.dto.UserTokenInfo;
import com.lloop.authcheckdemo.model.request.UserLoginRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.mapper.UserMapper;
import com.lloop.authcheckdemo.utils.JwtUtils;
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
    private JwtUtils jwtUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     * @param account, userPassword, checkPassword
     * @return
     */
    @Override
    public UserToken userRegister(String account, String userPassword, String checkPassword) {
        ThrowUtils.throwIf(userMapper.selectByAccount(account) != null, ErrorCode.PARAMS_ERROR, "用户名已存在");
        isValidPassword(userPassword, checkPassword);

        // 1. 创建用户
        User user = new User();
        user.setAccount(account);
        user.setPassword(userPassword);
        userMapper.insert(user);

        // 2. 注册后自动登录
        return userLogin(account, userPassword);
    }

    @Override
    public UserToken userLogin(String account, String userPassword) {
        // 1. 校验账号和密码
        User user = userMapper.selectByAccount(account);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.PARAMS_ERROR, "请输入正确的用户名和密码");
        ThrowUtils.throwIf(!user.getPassword().equals(userPassword), ErrorCode.PARAMS_ERROR, "请输入正确的用户名和密码");

        // 2. 获取并返回登录凭证
        return jwtUtils.createTokens(BeanUtil.copyProperties(user, UserTokenInfo.class));
    }

    @Override
    public UserToken refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public UserToken logOut(String token) {
        return null;
    }

    private void isValidPassword(String userPassword, String checkPassword){
        ThrowUtils.throwIf(StringUtils.isEmpty(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,12}$";
        boolean matches = userPassword.matches(pattern);
        ThrowUtils.throwIf(!matches, ErrorCode.PARAMS_ERROR, "密码必须包含数字、大小写字母、特殊字符，长度在6-12位");
    }

}




