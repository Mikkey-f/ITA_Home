package com.ita.home.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OJ平台枚举类
 * 定义支持的在线判题平台
 */
@Getter
@AllArgsConstructor
public enum OjPlatformEnum {
    
    /** LeetCode中国站 */
    LEETCODE_CN(1, "leetcode_cn", "LeetCode中国站"),
    
    /** 洛谷 */
    LUOGU(2, "luogu", "洛谷"),
    
    /** Codeforces */
    CODEFORCES(3, "codeforces", "Codeforces"),
    
    /** 牛客网 */
    NOWCODER(4, "nowcoder", "牛客网");
    
    /** 平台ID */
    private final Integer platformId;
    
    /** 平台代码（用于API调用） */
    private final String platformCode;
    
    /** 平台名称 */
    private final String platformName;
    
    /**
     * 根据平台ID获取枚举
     * @param platformId 平台ID
     * @return OJ平台枚举
     */
    public static OjPlatformEnum getByPlatformId(Integer platformId) {
        if (platformId == null) {
            return null;
        }
        for (OjPlatformEnum platform : values()) {
            if (platform.getPlatformId().equals(platformId)) {
                return platform;
            }
        }
        return null;
    }
    
    /**
     * 根据平台代码获取枚举
     * @param platformCode 平台代码
     * @return OJ平台枚举
     */
    public static OjPlatformEnum getByPlatformCode(String platformCode) {
        if (platformCode == null || platformCode.trim().isEmpty()) {
            return null;
        }
        for (OjPlatformEnum platform : values()) {
            if (platform.getPlatformCode().equals(platformCode)) {
                return platform;
            }
        }
        return null;
    }
}