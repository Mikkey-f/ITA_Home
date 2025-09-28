package com.ita.home.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:53
 */
@Data
public class FileUploadRequest {

    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    @NotBlank(message = "文件分类不能为空")
    private String category;

    /**
     * 可选：自定义文件名
     */
    private String customFileName;
}
