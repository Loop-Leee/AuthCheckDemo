package com.lloop.authcheckdemo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.common.UserHolder;
import com.lloop.authcheckdemo.mapper.UserMapper;
import com.lloop.authcheckdemo.model.domain.User;
import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.model.dto.UserTokenInfo;
import com.lloop.authcheckdemo.model.request.UserEditRequest;
import com.lloop.authcheckdemo.service.UserService;
import com.lloop.authcheckdemo.utils.JwtUtils;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Map;


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
        userPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt(8));
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
        ThrowUtils.throwIf(!BCrypt.checkpw(userPassword, user.getPassword()), ErrorCode.PARAMS_ERROR, "请输入正确的用户名和密码");

        UserTokenInfo userTokenInfo = BeanUtil.copyProperties(user, UserTokenInfo.class);
        userTokenInfo.setRole(user.getRole());
        // 2. 获取并返回登录凭证
        return jwtUtils.createTokens(userTokenInfo);
    }

    @Override
    public UserToken refreshToken(String refreshToken) {
        // 1. 检查refreshToken是否已过期或在黑名单
        ThrowUtils.throwIf(jwtUtils.isTokenExpired(refreshToken), ErrorCode.NOT_LOGIN, "登录已过期,请重新登录!");
        ThrowUtils.throwIf(jwtUtils.checkBlacklist(refreshToken), ErrorCode.PARAMS_ERROR, "用户已被禁止登录!");

        // 2. 刷新前先将旧的refreshToken和accessToken加入黑名单
        jwtUtils.addBlacklist(refreshToken, jwtUtils.getExpirationDate(refreshToken));
        String accessToken = jwtUtils.getAccessTokenByRefresh(refreshToken);
        if(StringUtils.isEmpty(accessToken)) {
            jwtUtils.addBlacklist(accessToken, jwtUtils.getExpirationDate(accessToken));
        }

        // 3. 为当前用户刷新token
        return jwtUtils.createTokens(jwtUtils.getUserTokenInfo(refreshToken));

    }

    @Override
    public void logOut(String token) {
        jwtUtils.addBlacklist(token, jwtUtils.getExpirationDate(token));
    }

    @Override
    public void editUser(UserEditRequest userEditRequest) {
        // 1. 校验账号是否已存在
        User accountExisted = userMapper.selectByAccount(userEditRequest.getAccount());
        ThrowUtils.throwIf(!ObjectUtils.isEmpty(accountExisted), ErrorCode.PARAMS_ERROR, "用户名已存在!");
        // 2. 获取用户信息并鉴权
        User user = userMapper.selectById(UserHolder.getUser().getId());
        ThrowUtils.throwIf(
                !user.getId().equals(UserHolder.getUser().getId()) && !user.getRole().equals(User.ADMIN),
                ErrorCode.PARAMS_ERROR, "非管理员不能修改他人的信息!");
        // 2.1 移除空属性
        Map<String, Object> attributes = BeanUtil.beanToMap(userEditRequest, false, true);
        attributes.entrySet().removeIf(entry -> StringUtils.isEmpty(String.valueOf(entry.getValue())));
        // 2.2 修改用户信息
        BeanUtil.copyProperties(attributes, user);
        // 2.3 密码更新则重新加密
        if(!StringUtils.isEmpty(userEditRequest.getPassword())){
            user.setPassword(BCrypt.hashpw(userEditRequest.getPassword(), BCrypt.gensalt(8)));
        }
        // 3. 更新用户信息
        userMapper.updateById(user);

    }

    private void isValidPassword(String userPassword, String checkPassword){
        ThrowUtils.throwIf(StringUtils.isEmpty(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~!@#$%^&*()<>?/,.])(?=\\S+$).{6,12}$";
        boolean matches = userPassword.matches(pattern);
        ThrowUtils.throwIf(!matches, ErrorCode.PARAMS_ERROR, "密码必须包含数字、大小写字母、特殊字符，长度在6-12位");
    }

}




