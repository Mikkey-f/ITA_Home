package com.ita.home.service.impl;

import com.ita.home.exception.BaseException;
import com.ita.home.model.entity.Member;
import com.ita.home.service.MemberService;
import com.ita.home.mapper.MemberMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    // 支持的图片类型
    private static final List<String> VALID_PICTURE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/jpg"
    );

    // 从配置文件读取图片保存目录
    @Value("${member.picture.save-dir:static/}")
    private String pictureSaveDir;

    // 从配置文件读取图片访问路径前缀
    @Value("${member.picture.access-path:/static/}")
    private String pictureAccessPath;

    @Override
    public boolean addMember(String name, String content, String picturePath) {
        try {
            Member member = new Member();
            member.setName(name);
            member.setContent(content);
            member.setPicture(picturePath);
            int result = memberMapper.insert(member);
            return result > 0;
        } catch (Exception e) {
            log.error("添加成员失败", e);
            return false;
        }
    }

    @Override
    public boolean updateMember(Long id, String name, String content, String picturePath) {
        try {
            // 查询原有成员信息
            Member existingMember = memberMapper.selectById(id);
            if (existingMember == null) {
                throw new BaseException("成员不存在");
            }

            Member member = new Member();
            member.setId(id);

            // 只设置非空字段
            if (name != null) {
                member.setName(name);
            }
            if (content != null) {
                member.setContent(content);
            }
            if (picturePath != null) {
                member.setPicture(picturePath);
                // 如果传入了新图片，删除原有的图片文件
                deleteOldPicture(existingMember.getPicture());
            }

            int result = memberMapper.updateById(member);
            return result > 0;
        } catch (Exception e) {
            throw new BaseException("更新失败");
        }
    }


    @Override
    public Member getMember(Long id) {
        try {
            return memberMapper.selectById(id);
        } catch (Exception e) {
            log.error("获取成员失败", e);
            return null;
        }
    }

    @Override
    public boolean deleteMember(Long id) {
        try {
            int result = memberMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除成员失败", e);
            return false;
        }
    }

    @Override
    public boolean isValidPictureType(String contentType) {
        return VALID_PICTURE_TYPES.contains(contentType);
    }

    @Override
    public String savePicture(MultipartFile picture) {
        try {
            // 创建保存目录（相对于项目根目录）
            File uploadDir = new File(pictureSaveDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = picture.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件到静态资源目录
            Path filePath = Paths.get(pictureSaveDir, uniqueFilename);
            Files.write(filePath, picture.getBytes());

            // 返回可访问的路径
            return pictureAccessPath + uniqueFilename;
        } catch (IOException e) {
            log.error("保存图片失败", e);
            return null;
        }
    }

    /**
     * 删除旧的图片文件
     * @param oldPicturePath 旧图片路径
     */
    private void deleteOldPicture(String oldPicturePath) {
        if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
            try {
                // 从访问路径转换为实际文件路径
                // 假设访问路径格式为 /members/xxx.jpg，需要转换为实际的文件路径
                String fileName = oldPicturePath.substring(oldPicturePath.lastIndexOf("/") + 1);
                Path oldFilePath = Paths.get(pictureSaveDir, fileName);

                File oldFile = oldFilePath.toFile();
                if (oldFile.exists()) {
                    oldFile.delete();
                    log.info("成功删除旧图片文件: {}", oldFilePath);
                }
            } catch (Exception e) {
                throw new BaseException("删除图片失败");
            }
        }
    }

}
