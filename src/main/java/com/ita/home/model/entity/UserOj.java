package com.ita.home.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户OJ平台账号实体类
 * 对应数据库中的user_oj表
 */
@TableName("user_oj")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户OJ平台账号信息")
public class UserOj {
    
    /** 主键ID - 自动递增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;
    
    /** 用户ID - 关联user表 */
    @NonNull
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    /** 洛谷平台用户名 */
    @Schema(description = "洛谷平台用户名", example = "luogu_user123")
    private String luoguUsername;
    
    /** LeetCode中国站用户名 */
    @Schema(description = "LeetCode中国站用户名", example = "leetcode_user123")
    private String leetcodeCnUsername;
    
    /** 牛客网用户ID */
    @Schema(description = "牛客网用户ID", example = "nowcoder123")
    private String nowcoderUserId;
    
    /** Codeforces用户名 */
    @Schema(description = "Codeforces用户名", example = "cf_user123")
    private String codeforceUsername;
    
    /** 创建时间 */
    @Schema(description = "创建时间", example = "2025-09-24T15:30:00")
    private LocalDateTime createTime;
    
    /** 修改时间 */
    @Schema(description = "修改时间", example = "2025-09-24T15:30:00")
    private LocalDateTime updateTime;
    
    /**
     * 便捷构造方法 - 创建用户OJ账号
     */
    public UserOj(@NonNull Long userId) {
        this.userId = userId;
    }

    /**
     * 自定义
     */
    @Override
    public String toString() {
        return "UserOj{" +
                "id=" + id +
                ", userId=" + userId +
                ", luoguUsername='" + luoguUsername + '\'' +
                ", leetcodeCnUsername='" + leetcodeCnUsername + '\'' +
                ", nowcoderUserId='" + nowcoderUserId + '\'' +
                ", codeforceUsername='" + codeforceUsername + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}