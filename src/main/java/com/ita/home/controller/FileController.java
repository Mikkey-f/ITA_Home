package com.ita.home.controller;

import com.ita.home.annotation.RequireAuth;
import com.ita.home.model.vo.FileListVo;
import com.ita.home.model.vo.FileUploadVo;
import com.ita.home.result.Result;
import com.ita.home.service.impl.FileService;
import com.ita.home.utils.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import static com.ita.home.utils.FileUtil.*;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 21:13
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、列表管理（支持覆盖）")
@Validated
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class FileController {
    private final FileService fileService;

    /**
     * 上传文件（覆盖模式）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "支持Markdown和PDF文件上传，同名文件将被覆盖")
    @RequireAuth
    public Result<FileUploadVo> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "文件分类", required = true, example = "前端")
            @RequestParam("category") @NotBlank(message = "文件分类不能为空") String category,

            @Parameter(description = "自定义文件名（可选，同名将覆盖）")
            @RequestParam(value = "customFileName", required = false) String customFileName) {

        log.info("上传文件请求: 文件名={}, 分类={}, 自定义名称={}",
                file.getOriginalFilename(), category, customFileName);

        FileUploadVo response = fileService.uploadFile(file, category, customFileName);

        return Result.success(response);
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "获取所有分类的文件列表，按修改时间倒序")
    public Result<FileListVo> getFileList() {
        log.info("获取文件列表请求");

        FileListVo response = fileService.getAllFiles();

        return Result.success(response);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "获取指定文件的详细信息")
    public Result<Map<String, Object>> getFileInfo(
            @Parameter(description = "文件分类", required = true)
            @RequestParam("category") @NotBlank(message = "文件分类不能为空") String category,

            @Parameter(description = "文件名", required = true)
            @RequestParam("fileName") @NotBlank(message = "文件名不能为空") String fileName) {

        log.info("获取文件信息请求: 分类={}, 文件名={}", category, fileName);

        Map<String, Object> fileInfo = fileService.getFileInfo(category, fileName);

        if (fileInfo != null) {
            return Result.success(fileInfo);
        } else {
            return Result.error("文件不存在");
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除指定分类下的文件")
    @RequireAuth
    public Result<String> deleteFile(
            @Parameter(description = "文件分类", required = true)
            @RequestParam("category") @NotBlank(message = "文件分类不能为空") String category,

            @Parameter(description = "文件名", required = true)
            @RequestParam("fileName") @NotBlank(message = "文件名不能为空") String fileName) {

        log.info("删除文件请求: 分类={}, 文件名={}", category, fileName);

        boolean deleted = fileService.deleteFile(category, fileName);

        if (deleted) {
            return Result.success("文件删除成功");
        } else {
            return Result.error("文件删除失败，文件可能不存在");
        }
    }

    /**
     * 文件预览接口 - 核心思路：根据文件类型返回不同的响应
     */
    @GetMapping("/preview")
    @Operation(summary = "预览文件", description = "预览分类下的文件")
    public ResponseEntity<?> previewFile(
            @Parameter(description = "文件分类", required = true)
            @RequestParam String category,

            @Parameter(description = "文件名", required = true)
            @RequestParam String fileName) {

        log.info("预览文件请求: {}/{}", category, fileName);

        // 1. 获取文件扩展名，决定处理策略
        String extension = FileUtil.getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "md" ->
                // Markdown文件返回文本内容，前端渲染
                    previewMarkdown(category, fileName);
            case "pdf" ->
                // PDF文件返回二进制流，浏览器直接显示
                    previewPdf(category, fileName);
            default -> ResponseEntity.badRequest()
                    .body(Result.error("不支持预览的文件类型"));
        };
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    @Operation(summary = "下载文件", description = "下载分类下的文件")
    @RequireAuth
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "文件分类", required = true)
            @RequestParam String category,

            @Parameter(description = "文件名", required = true)
            @RequestParam String fileName,

            HttpServletRequest request) {

        log.info("下载文件请求: {}/{}", category, fileName);

        try {
            // 1. 验证参数
            if (!StringUtils.hasText(category) || !StringUtils.hasText(fileName)) {
                log.warn("下载参数不完整: category={}, fileName={}", category, fileName);
                return ResponseEntity.badRequest()
                        .body(Result.error("文件分类和文件名不能为空"));
            }

            // 2. 记录下载请求信息
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            log.info("下载请求详情: IP={}, UserAgent={}", clientIp, userAgent);

            // 3. 安全检查
            try {
                performSecurityCheck(category, fileName, clientIp);
            } catch (SecurityException e) {
                log.warn("安全检查失败: {}/{}, IP={}, 原因={}", category, fileName, clientIp, e.getMessage());
                return ResponseEntity.status(403)
                        .body(Result.error("下载被拒绝: " + e.getMessage()));
            }

            // 4. 获取文件资源
            Resource resource;
            try {
                resource = fileService.getFileResource(category, fileName);
            } catch (Exception e) {
                log.error("获取文件资源失败: {}/{}", category, fileName, e);
                return ResponseEntity.notFound().build();
            }

            // 5. 获取文件信息
            Map<String, Object> fileInfo = fileService.getFileInfo(category, fileName);
            if (fileInfo == null) {
                log.warn("文件信息不存在: {}/{}", category, fileName);
                return ResponseEntity.notFound().build();
            }

            // 6. 确定文件类型和编码文件名
            String contentType = determineContentType(fileName);
            String encodedFileName = encodeFileName(fileName);
            Long fileSize = (Long) fileInfo.get("fileSize");

            // 7. 记录下载开始
            recordDownloadAttempt(category, fileName, clientIp, userAgent, true);

            // 8. 检查是否支持断点续传
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 支持断点续传
                return handleRangeDownload(resource, rangeHeader, fileName, contentType, encodedFileName, fileSize);
            } else {
                // 普通下载
                return handleNormalDownload(resource, fileName, contentType, encodedFileName, fileSize);
            }

        } catch (Exception e) {
            log.error("下载文件异常: {}/{}", category, fileName, e);

            // 记录失败
            try {
                String clientIp = getClientIp(request);
                String userAgent = request.getHeader("User-Agent");
                recordDownloadAttempt(category, fileName, clientIp, userAgent, false);
            } catch (Exception recordException) {
                log.warn("记录下载失败日志时出错", recordException);
            }

            return ResponseEntity.status(500)
                    .body(Result.error("文件下载失败: " + e.getMessage()));
        }
    }

    /**
     * 安全检查
     */
    private void performSecurityCheck(String category, String fileName, String clientIp) {
        // 1. 检查下载频率限制
        if (isDownloadRateLimited(clientIp)) {
            throw new SecurityException("下载频率过高，请稍后再试");
        }

        // 2. 检查文件大小限制
        Map<String, Object> fileInfo = fileService.getFileInfo(category, fileName);
        if (fileInfo != null) {
            Long fileSize = (Long) fileInfo.get("fileSize");
            long maxDownloadSize = 100 * 1024 * 1024L; // 100MB限制
            if (fileSize > maxDownloadSize) {
                throw new SecurityException("文件过大，超过下载限制");
            }
        }

        // 3. 检查IP黑名单（如果有）
        if (isIpBlacklisted(clientIp)) {
            throw new SecurityException("IP地址被禁止下载");
        }

        // 4. 文件安全验证已在FileService中处理
    }

    /**
     * Markdown预览 - 思路：返回原始文本，前端用JS库渲染
     */
    private ResponseEntity<Result<String>> previewMarkdown(String category, String fileName) {
        try {
            String content = fileService.getFileContent(category, fileName);
            return ResponseEntity.ok(Result.success(content));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error("文件读取失败: " + e.getMessage()));
        }
    }

    /**
     * PDF预览 - 思路：返回文件流，浏览器用内置PDF查看器显示
     */
    private ResponseEntity<Resource> previewPdf(String category, String fileName) {
        try {
            Resource resource = fileService.getFileResource(category, fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


}
