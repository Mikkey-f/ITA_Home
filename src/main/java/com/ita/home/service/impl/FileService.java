package com.ita.home.service.impl;

import com.ita.home.config.FileConfig;
import com.ita.home.enums.FileCategoryEnum;
import com.ita.home.model.vo.FileListVo;
import com.ita.home.model.vo.FileUploadVo;
import com.ita.home.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:56
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileConfig fileConfig;

    /**
     * 上传文件（覆盖策略）
     */
    public FileUploadVo uploadFile(MultipartFile file, String category, String customFileName) {
        // 1. 验证文件
        validateFile(file);

        // 2. 验证分类
        FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);

        // 3. 构建目标路径
        String targetDirectory = buildTargetDirectory(fileCategory);
        FileUtil.ensureDirectoryExists(targetDirectory);

        // 4. 生成最终文件名
        String finalFileName = generateFinalFileName(file.getOriginalFilename(), customFileName);

        // 5. 检查是否存在同名文件
        boolean fileExists = FileUtil.fileExists(targetDirectory, finalFileName);
        if (fileExists) {
            log.info("检测到同名文件 {}/{}，将进行覆盖", category, finalFileName);
        }

        // 6. 保存文件（覆盖模式）
        String savedFilePath = saveFileWithOverwrite(file, targetDirectory, finalFileName);

        // 7. 构建响应
        return buildUploadResponse(file, category, finalFileName, savedFilePath, fileExists);
    }

    /**
     * 获取所有文件列表
     */
    public FileListVo getAllFiles() {
        Map<String, List<String>> fileMap = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();
        int totalCount = 0;

        for (FileCategoryEnum category : FileCategoryEnum.values()) {
            List<String> files = getFilesInCategory(category);
            fileMap.put(category.getDisplayName(), files);
            categoryCount.put(category.getDisplayName(), files.size());
            totalCount += files.size();
        }

        FileListVo response = new FileListVo();
        response.setFileMap(fileMap);
        response.setCategoryCount(categoryCount);
        response.setTotalCount(totalCount);

        return response;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String category, String fileName) {
        try {
            FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);
            String directoryPath = buildTargetDirectory(fileCategory);

            boolean deleted = FileUtil.deleteFileIfExists(directoryPath, fileName);
            log.info("删除文件: {}/{} - {}", category, fileName, deleted ? "成功" : "失败");
            return deleted;
        } catch (Exception e) {
            log.error("删除文件失败: {}/{}", category, fileName, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public Map<String, Object> getFileInfo(String category, String fileName) {
        try {
            FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);
            String filePath = buildFilePath(fileCategory, fileName);
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                return null;
            }

            Map<String, Object> info = new HashMap<>();
            info.put("fileName", fileName);
            info.put("category", category);
            info.put("fileSize", file.length());
            info.put("formattedSize", FileUtil.formatFileSize(file.length()));
            info.put("lastModified", file.lastModified());
            info.put("extension", FileUtil.getFileExtension(fileName));

            return info;
        } catch (Exception e) {
            log.error("获取文件信息失败: {}/{}", category, fileName, e);
            return null;
        }
    }

    /**
     * 读取文件内容（文本文件）
     * 思路：将文件读取为字符串返回
     */
    public String getFileContent(String category, String fileName) {
        // 1. 验证文件安全性
        validateFileAccess(category, fileName);

        FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);
        // 2. 构建文件路径
        String filePath = buildFilePath(fileCategory, fileName);

        // 3. 读取文件内容
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("文件不存在");
            }

            // 使用UTF-8编码读取，确保中文正常显示
            return Files.readString(path, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件资源（二进制文件）
     * 思路：返回Resource对象，Spring自动处理流传输
     */
    public Resource getFileResource(String category, String fileName) {
        // 1. 验证文件安全性
        validateFileAccess(category, fileName);

        FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);
        // 2. 构建文件路径
        String filePath = buildFilePath(fileCategory, fileName);

        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("文件不存在或不可读");
            }

            return resource;

        } catch (Exception e) {
            log.error("获取文件资源失败: {}", filePath, e);
            throw new RuntimeException("文件资源获取失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件访问权限
     * 思路：确保文件路径安全，防止路径遍历攻击
     */
    private void validateFileAccess(String category, String fileName) {
        // 1. 验证分类是否合法
        FileCategoryEnum.fromDisplayName(category);

        // 2. 验证文件名安全性
        if (!FileUtil.isSecureFileName(fileName)) {
            throw new RuntimeException("文件名不安全");
        }
        FileCategoryEnum fileCategory = FileCategoryEnum.fromDisplayName(category);

        // 3. 确保文件在允许的目录内
        String basePath = fileConfig.getBasePath();
        String fullPath = buildFilePath(fileCategory, fileName);
        Path normalizedPath = Paths.get(fullPath).normalize();
        Path basePart = Paths.get(basePath).normalize();

        if (!normalizedPath.startsWith(basePart)) {
            throw new RuntimeException("文件路径不合法");
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > fileConfig.getMaxSize()) {
            throw new RuntimeException("文件大小不能超过 " + FileUtil.formatFileSize(fileConfig.getMaxSize()));
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new RuntimeException("文件名不能为空");
        }

        if (!FileUtil.isSecureFileName(originalFilename)) {
            throw new RuntimeException("文件名包含非法字符或格式不正确");
        }

        String extension = FileUtil.getFileExtension(originalFilename);
        if (!fileConfig.getAllowedTypes().contains(extension)) {
            throw new RuntimeException("不支持的文件类型，仅支持: " + String.join(", ", fileConfig.getAllowedTypes()));
        }
    }

    /**
     * 构建目标目录路径
     */
    private String buildTargetDirectory(FileCategoryEnum category) {
        return Paths.get(fileConfig.getBasePath(), category.getDisplayName()).toString();
    }

    /**
     * 生成最终文件名
     */
    private String generateFinalFileName(String originalName, String customFileName) {
        // 生成基础文件名
        String baseFileName = FileUtil.generateFinalFileName(originalName, customFileName);

        // 清理文件名，确保安全
        return FileUtil.sanitizeFileName(baseFileName);
    }

    /**
     * 保存文件（覆盖模式）
     */
    private String saveFileWithOverwrite(MultipartFile file, String targetDirectory, String fileName) {
        try {
            Path targetPath = Paths.get(targetDirectory, fileName);

            // 使用 REPLACE_EXISTING 选项实现覆盖
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件保存成功: {} (大小: {})", targetPath.toString(), FileUtil.formatFileSize(file.getSize()));
            return targetPath.toString();
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 构建上传响应
     */
    private FileUploadVo buildUploadResponse(MultipartFile file, String category,
                                                   String savedFileName, String savedFilePath, boolean wasOverwritten) {
        FileUploadVo response = new FileUploadVo();
        response.setOriginalName(file.getOriginalFilename());
        response.setSavedName(savedFileName);
        response.setFileSize(file.getSize());
        response.setCategory(category);
        response.setFilePath(category + "/" + savedFileName);

        // 添加一个字段表示是否进行了覆盖
        response.setOverwritten(wasOverwritten);

        return response;
    }

    /**
     * 获取指定分类下的所有文件
     */
    private List<String> getFilesInCategory(FileCategoryEnum category) {
        String directoryPath = buildTargetDirectory(category);
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }

        File[] files = directory.listFiles((dir, name) -> {
            String extension = FileUtil.getFileExtension(name);
            return fileConfig.getAllowedTypes().contains(extension);
        });

        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .filter(File::isFile)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())) // 按修改时间倒序
                .map(file -> category.getDisplayName() + "/" + file.getName())
                .collect(Collectors.toList());
    }

    /**
     * 构建文件完整路径
     */
    private String buildFilePath(FileCategoryEnum category, String fileName) {
        return Paths.get(fileConfig.getBasePath(), category.getDisplayName(), fileName).toString();
    }
}
