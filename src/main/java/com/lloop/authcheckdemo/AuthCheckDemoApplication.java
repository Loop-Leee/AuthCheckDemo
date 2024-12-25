package com.lloop.authcheckdemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.Environment;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@MapperScan("com.lloop.authcheckdemo.mapper")
public class AuthCheckDemoApplication {

    public static void main(String[] args) throws UnknownHostException {

        // 启动 SpringBoot 应用
        ConfigurableApplicationContext application = SpringApplication.run(AuthCheckDemoApplication.class, args);
        log.info("""
                        \n
                        $$\\       $$\\        $$$$$$\\   $$$$$$\\  $$$$$$$\\ \s
                        $$ |      $$ |      $$ /  $$ |$$ /  $$ |$$ |  $$ |
                        $$ |      $$ |      $$ |  $$ |$$ |  $$ |$$$$$$$  |
                        $$ |      $$ |      $$ |  $$ |$$ |  $$ |$$  ____/\s
                        $$$$$$$$\\ $$$$$$$$\\  $$$$$$  | $$$$$$  |$$ |     \s
                        \\________|\\________| \\______/  \\______/ \\__|     \s
               \s"""
        );

        // 获取 Environment 实例
        ConfigurableEnvironment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path", "");
        String apiPath = env.getProperty("server.servlet.context-path", "");

        log.info("\n----------------------------------------------------------\n\t" +
                "Application  is running! Access URLs:\n\t" +
                "Swagger访问地址: \thttp://localhost:" + port + apiPath + "/swagger-ui/index.html\n\t" +
                "Local访问网址: \t\thttp://localhost:" + port + path + "\n\t" +
                "External访问网址: \thttp://" + ip + ":" + port + path + "\n\t" +
                "----------------------------------------------------------");

    }


}
