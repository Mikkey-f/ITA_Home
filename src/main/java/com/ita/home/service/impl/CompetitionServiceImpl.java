package com.ita.home.service.impl;

import com.ita.home.exception.BaseException;
import com.ita.home.mapper.CompetitionMapper;
import com.ita.home.model.entity.Competition;
import com.ita.home.service.CompetitionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class CompetitionServiceImpl implements CompetitionService {

    @Resource
    private CompetitionMapper competitionMapper;

    @Override
    public Boolean addCompetition(String title, String content, String picture) {
        Competition competition = Competition.builder()
                .title(title)
                .content(content)
                .picture(picture)
                .build();

        int i = competitionMapper.insert(competition);
        return i > 0;
    }

    @Override
    public Competition getCompetition(Long id) {
        Competition competition = competitionMapper.selectById(id);
        return competition;
    }

    @Override
    public boolean updateCompetition(Long id, String title, String content, MultipartFile newPicture) {

        // 1. 查询原有竞赛信息
        Competition existingCompetition = competitionMapper.selectById(id);

        String picturePath = existingCompetition.getPicture();

        // 2. 判断是否需要更新图片
        if (newPicture != null && !newPicture.isEmpty()) {
            // 3. 删除原有图片
            deleteOldPicture(existingCompetition.getPicture());

            // 4. 保存新图片
            picturePath = savePicture(newPicture);
        }

        // 5. 更新竞赛信息
        Competition competition = Competition.builder()
                .id(id)
                .title(title)
                .content(content)
                .picture(picturePath)
                .build();

        int result = competitionMapper.updateById(competition);
        return result > 0;
    }

    @Override
    public void deleteOldPicture(String picturePath) {
        if (StringUtils.isNotEmpty(picturePath)) {
            try {
                // 从访问路径转换为实际文件路径
                String filePath = "static" + picturePath;
                File oldFile = new File(filePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            } catch (Exception e) {
                throw new BaseException("删除图片失败");
            }
        }
    }


    @Override
    public String savePicture(MultipartFile picture) {
        // 生成唯一文件名，避免文件名冲突
        String originalFilename = picture.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        // 定义图片存储路径
        String uploadDir = "static/images/competitions/";
        File uploadPath = new File(uploadDir);
        // 创建目录（如果不存在）
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        try {
            // 保存文件
            File destFile = new File(uploadDir + newFilename);
            picture.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 返回相对路径供前端访问
        return "/images/competitions/" + newFilename;
    }

    @Override
    public boolean isValidPictureType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif");
    }

    @Override
    public boolean deleteCompetition(Long id) {
        int i = competitionMapper.deleteById(id);
        return i > 0;
    }
}
