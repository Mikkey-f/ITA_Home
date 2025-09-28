package com.ita.home.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileListVo {
    /**
     * 文件映射：分类 -> 文件路径列表
     */
    private Map<String, List<String>> fileMap;

    /**
     * 总文件数
     */
    private int totalCount;

    /**
     * 各分类文件统计
     */
    private Map<String, Integer> categoryCount;
}
