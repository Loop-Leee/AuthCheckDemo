package com.lloop.authcheckdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lloop.authcheckdemo.mapper")
public class AuthCheckDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthCheckDemoApplication.class, args);
    }


}
