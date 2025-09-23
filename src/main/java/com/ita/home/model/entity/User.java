package com.ita.home.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的user表
 */
@TableName("user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户信息")
public class User {
    
    /** 用户ID - 主键，自动递增 */
    @TableId(type = IdType.AUTO) // 主键自增
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    /** 用户名 - 唯一，不能重复 */
    @NonNull
    @Schema(description = "用户名", example = "admin")
    private String name;
    
    /** 密码 - 使用加密存储 */
    @NonNull
    @JsonIgnore // 在JSON序列化时忽略此字段
    @Schema(description = "密码（加密存储）", hidden = true)
    private String password;

    /** 邮箱 - 唯一，不可重复 */
    @NonNull
    @Schema(description = "邮箱", hidden = true)
    private String email;

    /** 分组id - 1-3 */
    @Schema(description = "分组id1", hidden = true)
    private Integer groupId;
    
    /** 头像编号 - 1到9，对应9张不同的头像 */
    @Builder.Default
    @Schema(description = "头像编号", example = "1", minimum = "1", maximum = "9")
    private Integer avatar = 1;
    
    /** 创建时间 - 用户注册时间 */
    @Schema(description = "创建时间", example = "2025-09-22T15:30:00")
    private LocalDateTime createTime;
    
    /** 修改时间 - 用户信息最后修改时间 */
    @Schema(description = "修改时间", example = "2025-09-22T15:30:00")
    private LocalDateTime updateTime;

    /**
     * 便捷构造方法 - 创建用户时使用
     */
    public User(@NonNull String name, @NonNull String password) {
        this.name = name;
        this.password = password;
        this.avatar = 1; // 默认头像
    }

    /**
     * 便捷构造方法 - 创建用户时指定头像
     */
    public User(@NonNull String name, @NonNull String password, Integer avatar) {
        this.name = name;
        this.password = password;
        this.avatar = avatar != null ? avatar : 1;
    }

    /**
     * 自定义toString方法 - 不显示密码信息，保证安全
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar=" + avatar +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}