package com.lloop.authcheckdemo.interceptor;

import com.lloop.authcheckdemo.model.dto.UserDTO;
import com.lloop.authcheckdemo.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * @Author lloop
 * @Create 2024/12/24 22:01
 */
@Component
public class UserHolderHandler implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取请求头中的 token
        String token = request.getHeader("Authorization");
        // 1.1 如果没有token则还未登陆，无需存储用户信息
        if(StringUtils.isEmpty(token)){
            return true;
        }
        // 2. 如果有token，则判断token是否合法
        UserDTO user = getUserByToken(token);
        // 2.1 如果token无法解析为user，则无需存储用户信息
        if(ObjectUtils.isEmpty(user)){
            return true;
        }
        // 2.2 如果token合法，则存储用户信息
        UserHolder.saveUser(user);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户信息，避免内存泄露
        UserHolder.removeUser();
    }


    // TODO 从Redis中获取用户信息
    private UserDTO getUserByToken(String token) {
        return null;
    }

}
