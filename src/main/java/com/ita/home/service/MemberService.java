package com.ita.home.service;

import com.ita.home.model.entity.Member;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    /**
     * 添加成员
     * @param name 成员姓名
     * @param content 个人简介
     * @param picturePath 图片路径
     * @return 是否添加成功
     */
    boolean addMember(String name, String content, String picturePath);

    /**
     * 更新成员信息
     * @param id 成员ID
     * @param name 成员姓名
     * @param content 个人简介
     * @param picturePath 图片路径
     * @return 是否更新成功
     */
    boolean updateMember(Long id, String name, String content, String picturePath);

    /**
     * 根据ID获取成员信息
     * @param id 成员ID
     * @return 成员信息
     */
    Member getMember(Long id);

    /**
     * 根据ID删除成员
     * @param id 成员ID
     * @return 是否删除成功
     */
    boolean deleteMember(Long id);

    /**
     * 验证图片类型是否有效
     * @param contentType 图片内容类型
     * @return 是否有效
     */
    boolean isValidPictureType(String contentType);

    /**
     * 保存图片并返回访问路径
     * @param picture 图片文件
     * @return 图片访问路径
     */
    String savePicture(MultipartFile picture);
}
