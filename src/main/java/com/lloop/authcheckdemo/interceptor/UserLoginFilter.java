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
        log.info("拦截请求路径: {}", request.getRequestURI());
        // 1. 获取token
        String token = request.getHeader(jwtUtils.header);
        if (StringUtils.isEmpty(token)) {
            log.warn("请求头中缺少 token 信息");
            return false;
        }

        // 去掉 Bearer 前缀
        token = token.substring(7);

        // 2. 判断token是否有效
        ThrowUtils.throwIf(StringUtils.isEmpty(token), ErrorCode.NULL_ERROR, "请登录后操作!");
        ThrowUtils.throwIf(jwtUtils.isTokenExpired(token), ErrorCode.LOGIN_EXPIRED, "登录已过期!");
        ThrowUtils.throwIf(jwtUtils.checkBlacklist(token), ErrorCode.PARAMS_ERROR, "用户已被禁止登录!");

        // 3. token有效 => 记录登录用户信息
        UserTokenInfo userTokenInfo = jwtUtils.getUserTokenInfo(token);
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
        log.info("请求路径: {}, 状态: {}" , request.getRequestURI() , response.getStatus());
        UserHolder.removeUser();
    }


}
