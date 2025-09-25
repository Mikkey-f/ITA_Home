package com.ita.home.model.req;

import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 竞赛信息保存请求实体
 * 用于保存竞赛信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "竞赛信息保存请求")
public class CompetitionSaveRequest {

    @Schema(description = "竞赛编号", example = "1", required = true)
    private Long id;

    /** 竞赛标题 - 选填*/
    @Schema(description = "标题", example = "蓝桥杯省二", required = true, minLength = 4, maxLength = 20)
    private String title;

    /** 竞赛内容 - 选填*/
    @Schema(description = "内容", example = "这是司志俊同学在2024年蓝桥杯四川赛区取得的优异成绩", required = true, minLength = 6, maxLength = 200)
    private String content;

    /** 竞赛图片 - 选填*/
    @Schema(description = "图片", example = "获奖证书", required = true, maxLength = 200)
    private MultipartFile picture;

    /**
     * 重写toString方法 - 不显示图片信息，保证安全
     */
    @Override
    public String toString() {
        return "SaveRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }
}
