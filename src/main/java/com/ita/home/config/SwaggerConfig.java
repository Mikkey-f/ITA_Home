package com.ita.home.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API文档配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ITA Home 用户管理系统 API")
                        .version("1.0.0")
                        .description("基于Spring Boot + MyBatis Plus的用户注册登录系统接口文档")
                        .contact(new Contact()
                                .name("ITA Team")
                                .email("support@ita.com")
                                .url("https://ita.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}