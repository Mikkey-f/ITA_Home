package com.ita.home.controller;

import com.ita.home.annotation.RequireAuth;
import com.ita.home.model.vo.FileListVo;
import com.ita.home.model.vo.FileUploadVo;
import com.ita.home.result.Result;
import com.ita.home.service.impl.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
}
