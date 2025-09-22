package com.ita.home.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 */
@Configuration
@MapperScan("com.ita.home.mapper") // 扫描mapper包
public class MyBatisPlusConfig {
    
    // MyBatis Plus 3.5+ 版本默认配置已经很完善
    // 如果需要自定义配置，可以在这里添加Bean
    
    // 例如：分页插件、乐观锁插件等
    // @Bean
    // public MybatisPlusInterceptor mybatisPlusInterceptor() {
    //     MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    //     interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    //     return interceptor;
    // }
}