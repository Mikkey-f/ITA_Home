package com.ita.home.model.req;

import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 成员信息保存请求实体
 * 用于保存成员信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "成员信息保存请求")
public class MemberSaveRequest {

    @Schema(description = "成员编号", example = "1", required = true)
    private Long id;

    /** 成员姓名 - 选填，唯一*/
    @Schema(description = "姓名", example = "曾彤飞", required = true, minLength = 2, maxLength = 20)
    private String name;

    /** 个人简介 - 选填 */
    @Schema(description = "个人简介", example = "喜欢学java，热爱开发", required = true, minLength = 6, maxLength = 200)
    private String content;

    /** 自拍图片 - 可选 */
    @Schema(description = "自拍", example = "个人照片", maxLength = 200)
    private MultipartFile picture;

    /**
     * 重写toString方法 - 不显示图片信息，保证安全
     */
    @Override
    public String toString() {
        return "MemberSaveRequest{" +
                "name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }
}
