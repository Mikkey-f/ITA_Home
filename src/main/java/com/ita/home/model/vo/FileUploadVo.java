package com.ita.home.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVo {

    /**
     * 文件相对路径
     */
    private String filePath;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 保存的文件名
     */
    private String savedName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件分类
     */
    private String category;

    /**
     * 格式化的文件大小
     */
    public String getFormattedSize() {
        return formatFileSize(this.fileSize);
    }

    /**
     * 是否覆盖了已存在的文件
     */
    private boolean overwritten;

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
