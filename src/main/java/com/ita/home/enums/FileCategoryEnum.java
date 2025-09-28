package com.ita.home.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:32
 */
@Getter
@AllArgsConstructor
public enum FileCategoryEnum {
    FRONTEND("前端", "frontend"),
    JAVA_BACKEND("java后端", "java_backend"),
    CPP_BACKEND("cpp后端", "cpp_backend"),
    DEEP_LEARNING("深度学习", "deep_learning");

    private final String displayName;
    private final String folderName;

    /**
     * 根据显示名称查找枚举
     */
    public static FileCategoryEnum fromDisplayName(String displayName) {
        for (FileCategoryEnum category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("无效的文件分类: " + displayName);
    }

    /**
     * 获取所有支持的分类名称
     */
    public static String[] getAllDisplayNames() {
        return new String[]{"前端", "java后端", "cpp后端"};
    }
}
