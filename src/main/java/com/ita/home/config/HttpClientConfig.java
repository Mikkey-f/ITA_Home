package com.ita.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP客户端配置类
 * 配置RestTemplate用于调用外部API
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置RestTemplate Bean
     * 用于调用OJHunt API等外部服务
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(10000);
        
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(30000);
        
        return new RestTemplate(factory);
    }
}