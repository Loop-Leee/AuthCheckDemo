package com.lloop.authcheckdemo.interceptor;

import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.common.UserHolder;
import com.lloop.authcheckdemo.model.dto.UserTokenInfo;
import com.lloop.authcheckdemo.utils.JwtUtils;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @Author lloop
 * @Create 2024/12/26 14:58
 */
@Slf4j
@Component
public class UserLoginFilter implements HandlerInterceptor {

    @Resource
    JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 获取token
        String token = request.getHeader(jwtUtils.header);

        // 2. 判断token是否有效
        ThrowUtils.throwIf(StringUtils.isEmpty(token), ErrorCode.NULL_ERROR, "请登录后操作!");
        ThrowUtils.throwIf(jwtUtils.isTokenExpired(token), ErrorCode.PARAMS_ERROR, "登录已过期!");
        ThrowUtils.throwIf(jwtUtils.checkBlacklist(token), ErrorCode.PARAMS_ERROR, "用户已被禁止登录!");

        // 3. token有效 => 记录登录用户信息
        UserTokenInfo userTokenInfo = jwtUtils.getUserInfoToken(token);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userTokenInfo), ErrorCode.NULL_ERROR, "对不起,身份认证出现错误,请重新登录...");
        UserHolder.saveUser(userTokenInfo);

        return true;
    }

    /**
     * 移除用户信息，防止内存溢出
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }


}
