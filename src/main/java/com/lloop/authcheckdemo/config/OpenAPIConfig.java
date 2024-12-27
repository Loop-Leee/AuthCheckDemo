package com.lloop.authcheckdemo.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lloop
 * @Create 2024/12/25 13:20
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AuthCeckDemo 接口文档")
                        .description("SpringBoot3 集成 Swagger3 接口文档")
                        .version("v1"));
    }

}
