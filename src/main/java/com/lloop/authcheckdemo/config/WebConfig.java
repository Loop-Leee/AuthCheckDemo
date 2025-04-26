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
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/index",
                        "/",
                        "/user/login",
                        "/user/register",
                        "/user/refreshToken/**",
                        "/v3/api-docs",
                        "/auth/login",       // 允许登录页面
                        "/auth/register",    // 允许注册页面
                        "/user/homepage",     // 新增：放行主页路径
                        "/user/infopage",     // 新增: 放行个人信息页面
                        // 放行静态资源（CSS/JS/图片等）
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );
    }

}