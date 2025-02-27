package com.lloop.authcheckdemo.interceptor;

import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.common.UserHolder;
import com.lloop.authcheckdemo.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @Author lloop
 * @Create 2025/2/27 21:40
 */

@Aspect
@Component
public class IsAdminAspect {

    private final HttpServletRequest request;

    public IsAdminAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Before("@annotation(com.lloop.authcheckdemo.annotation.IsAdmin)")
    public void checkAdmin() throws IllegalAccessException {
        Integer role = UserHolder.getUser().getRole();
        ThrowUtils.throwIf(role == null || role != 1, ErrorCode.PARAMS_ERROR, "非管理员不得进行此类操作!");
    }

}
