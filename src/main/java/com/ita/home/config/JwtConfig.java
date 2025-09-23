package com.ita.home.config;

import com.ita.home.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 * 
 * 配置JWT身份验证过滤器，注册到Spring容器中
 * 
 * @author ITA Team
 * @since 2025-09-22
 */
@Configuration
public class JwtConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public JwtConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 注册JWT过滤器
     * 设置过滤器的优先级和URL匹配模式
     * 优先级设置为最高，确保在其他过滤器之前执行JWT验证
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        // 设置过滤器
        registration.setFilter(jwtAuthenticationFilter);
        
        // 设置过滤器名称
        registration.setName("jwtAuthenticationFilter");
        
        // 设置URL匹配模式（对所有API路径生效）
        registration.addUrlPatterns("/api/*");
        
        // 设置过滤器执行顺序（数字越小优先级越高）
        registration.setOrder(1);
        
        // 启用过滤器
        registration.setEnabled(true);
        
        return registration;
    }
}