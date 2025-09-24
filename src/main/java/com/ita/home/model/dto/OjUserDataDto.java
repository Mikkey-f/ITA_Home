package com.ita.home.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OJ用户数据DTO
 * 用于映射外部OJHunt API返回的数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OJ用户数据")
public class OjUserDataDto {
    
    /** 是否有错误 */
    @Schema(description = "是否有错误", example = "false")
    private Boolean error;
    
    /** 用户数据 */
    @Schema(description = "用户数据")
    private UserData data;
    
    /**
     * 用户数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "用户解题数据")
    public static class UserData {
        
        /** 解决题目数量 */
        @Schema(description = "解决题目数量", example = "97")
        private Integer solved;
        
        /** 提交次数 */
        @Schema(description = "提交次数", example = "363")
        private Integer submissions;
        
        /** 已解决题目列表 */
        @Schema(description = "已解决题目列表")
        private List<String> solvedList;
    }
}