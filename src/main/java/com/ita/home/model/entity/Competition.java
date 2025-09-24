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

/**
 * 竞赛实体类
 * 对应数据库中的competition表
 */
@TableName("user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "竞赛信息")
public class Competition {
    /** 竞赛ID - 主键，自动递增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "竞赛ID", example = "1")
    private Long id;

    /** 竞赛标题 - 必填 */
    @NonNull
    @Schema(description = "竞赛标题", example = "2024年蓝桥杯省二")
    private String title;

    /** 竞赛内容 - 必填 */
    @NonNull
    @Schema(description = "竞赛内容", example = "这是司志俊同学在2024年蓝桥杯四川赛区取得的优异成绩")
    private String content;

    /** 竞赛图片存储路径 - 可选 */
    @Schema(description = "竞赛图片", example = "/images/competitions/a1b2c3d4-e5f6-7890-1234-567890abcdef.jpg")
    private String picture;
}
