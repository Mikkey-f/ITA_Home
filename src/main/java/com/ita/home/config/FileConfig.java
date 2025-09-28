package com.ita.home.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileConfig {
    /**
     * 文件存储基础路径
     */
    private String basePath = "uploads";

    /**
     * 最大文件大小（字节）
     */
    private Long maxSize = 10 * 1024 * 1024L; // 10MB

    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes = Arrays.asList("md", "pdf");

    /**
     * 是否创建日期子目录
     */
    private boolean createDateFolder = false;
}
