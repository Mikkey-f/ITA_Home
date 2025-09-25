package com.ita.home.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 竞赛图片资源映射
        registry.addResourceHandler("/competitions/**")
                .addResourceLocations("file:static/competitions/");

        // 成员图片资源映射
        registry.addResourceHandler("/members/**")
                .addResourceLocations("file:static/members/");
    }
}