package com.lloop.authcheckdemo.config;

import com.lloop.authcheckdemo.interceptor.UserLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author lloop
 * @Create 2024/12/24 22:45
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserLoginFilter userLoginFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginFilter)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register");
    }

}
