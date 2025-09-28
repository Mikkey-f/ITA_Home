package com.ita.home.utils;

import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:55
 */
public class FileUtil {
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * 获取文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    /**
     * 检查文件名是否安全（防止路径遍历攻击）
     */
    public static boolean isSecureFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        // 检查是否包含危险字符
        return !filename.contains("..") &&
                !filename.contains("/") &&
                !filename.contains("\\") &&
                !filename.contains(":") &&
                !filename.contains("<") &&
                !filename.contains(">") &&
                !filename.contains("|") &&
                !filename.contains("?") &&
                !filename.contains("*") &&
                !filename.contains("\"") &&
                filename.length() <= 255 &&
                !filename.isEmpty();
    }

    /**
     * 确保目录存在
     */
    public static void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建目录: " + directoryPath);
            }
        }
    }

    /**
     * 生成最终文件名（支持覆盖策略）
     * @param originalName 原始文件名
     * @param customFileName 自定义文件名（可选）
     * @return 最终使用的文件名
     */
    public static String generateFinalFileName(String originalName, String customFileName) {
        if (StringUtils.hasText(customFileName)) {
            // 使用自定义文件名，但保留原始文件的扩展名
            String extension = getFileExtension(originalName);
            // 如果自定义文件名已经包含扩展名，就使用它；否则添加原扩展名
            if (customFileName.contains(".")) {
                return customFileName;
            } else {
                return customFileName + "." + extension;
            }
        } else {
            // 使用原始文件名
            return originalName;
        }
    }

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String directoryPath, String fileName) {
        File file = new File(directoryPath, fileName);
        return file.exists() && file.isFile();
    }

    /**
     * 删除文件（如果存在）
     */
    public static boolean deleteFileIfExists(String directoryPath, String fileName) {
        File file = new File(directoryPath, fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return true; // 文件不存在也算删除成功
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
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

    /**
     * 验证文件名合法性并清理
     */
    public static String sanitizeFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "untitled";
        }

        // 替换或移除非法字符
        String sanitized = filename
                .replaceAll("[<>:\"|?*\\\\]", "_")  // 替换Windows非法字符为下划线
                .replaceAll("/", "_")               // 替换Unix路径分隔符
                .replaceAll("\\s+", "_")           // 替换多个空格为单个下划线
                .replaceAll("_{2,}", "_");         // 多个连续下划线替换为单个

        // 移除开头和结尾的下划线和点号
        sanitized = sanitized.replaceAll("^[._]+|[._]+$", "");

        // 如果清理后为空，给个默认名称
        if (sanitized.isEmpty()) {
            sanitized = "file_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        // 限制文件名长度
        if (sanitized.length() > 200) {
            String extension = getFileExtension(filename);
            String baseName = sanitized.substring(0, 200 - extension.length() - 1);
            sanitized = baseName + "." + extension;
        }

        return sanitized;
    }
}
