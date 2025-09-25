package com.ita.home.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "成员信息")
public class Member {

    /** 成员ID - 主键，自动递增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "成员ID", example = "1")
    private Long id;

    /** 成员姓名 - 唯一，不能重复 */
    @lombok.NonNull
    @Schema(description = "成员姓名", example = "曾彤飞")
    private String name;

    /** 个人简介 - 必填 */
    @NonNull
    @Schema(description = "对自己的简单介绍", example = "喜欢学java，热爱开发")
    private String content;

    /** 竞赛图片存储路径 - 可选 */
    @Schema(description = "自拍", example = "/images/competitions/a1b2c3d4-e5f6-7890-1234-567890abcdef.jpg")
    private String picture;
}
