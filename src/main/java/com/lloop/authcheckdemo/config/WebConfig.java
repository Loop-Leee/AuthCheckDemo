package com.lloop.authcheckdemo.config;

import com.lloop.authcheckdemo.interceptor.UserLoginFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @Author lloop
 * @Create 2024/12/24 22:45
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private UserLoginFilter userLoginFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginFilter)
                // 只拦截后端API接口，避免拦静态资源
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",       // 登录接口
                        "/api/user/register",    // 注册接口
                        "/api/user/refreshToken/**" // 刷新Token接口
                );
    }

}