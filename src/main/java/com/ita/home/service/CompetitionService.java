package com.ita.home.service;

import com.ita.home.model.entity.Competition;
import com.ita.home.model.req.CompetitionSaveRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CompetitionService {

    /**
     * 添加竞赛信息
     *
     * @param title   标题
     * @param content 内容
     * @param picturePath 图片访问路径
     * @return 添加成功返回true，失败返回false
     */
    Boolean addCompetition(String title, String content, String picturePath);

    /**
     * 保存图片到静态资源目录并返回访问路径
     * @param picture 图片文件
     * @return 图片访问路径
     */
    String savePicture(MultipartFile picture);

    /**
     * 验证图片文件类型是否有效
     *
     * @param contentType 文件MIME类型
     * @return boolean 是否为有效的图片类型
     */
    boolean isValidPictureType(String contentType);

    /**
     * 根据ID获取竞赛信息
     *
     * @param id 竞赛ID
     * @return Competition 竞赛信息
     */
    Competition getCompetition(Long id);

    /**
     * 更新竞赛信息
     *
     * @param title   竞赛标题
     * @param content 竞赛内容
     * @param picturePath 竞赛图片访问路径
     * @return 更新成功返回true，失败返回false
     */
    boolean updateCompetition(Long id, String title, String content, MultipartFile picturePath);

    /**
     * 删除图片文件
     *
     * @param picturePath 图片访问路径
     * @return 删除成功返回true，失败返回false
     */
    void deleteOldPicture(String picturePath);

    /**
     * 删除竞赛信息
     *
     * @param id 竞赛ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteCompetition(Long id);
}
