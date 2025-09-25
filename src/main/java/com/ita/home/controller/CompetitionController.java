package com.ita.home.controller;

import com.ita.home.model.entity.Competition;
import com.ita.home.model.req.CompetitionSaveRequest;
import com.ita.home.result.Result;
import com.ita.home.service.CompetitionService;
import com.ita.home.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/competition")
@Tag(name = "竞赛信息管理", description = "竞赛信息保存等接口")
@Slf4j
public class CompetitionController {

    @Resource
    private CompetitionService competitionService;

    /**
     * 添加竞赛信息
     * POST /api/competition/add
     */
    @Operation(summary = "新增竞赛", description = "新增竞赛的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/add")
    public Result<String> addCompetition(@RequestBody CompetitionSaveRequest  request) {
        String title = request.getTitle();
        String content = request.getContent();
        MultipartFile picture = request.getPicture();

        // 参数校验
        ThrowUtils.throwIf(title == null || title.length() < 4 || title.length() > 20, "标题长度必须在4到20个字符之间");
        ThrowUtils.throwIf(content == null || content.length() < 6 || content.length() > 200, "内容长度必须在6到200个字符之间");
        ThrowUtils.throwIf(picture == null, "请上传有效的图片文件(jpg/jpeg/png/gif)");

        // 验证图片类型是否有效
        ThrowUtils.throwIf(!competitionService.isValidPictureType(picture.getContentType()), "请上传有效的图片文件(jpg/jpeg/png/gif)");

        // 保存图片并获取访问路径
        String picturePath = competitionService.savePicture(picture);

        // 添加竞赛信息
        boolean result = competitionService.addCompetition(title, content, picturePath);
        ThrowUtils.throwIf(!result, "添加竞赛信息失败");

        return Result.success("竞赛信息保存成功");
    }

    /**
     * 更新竞赛信息
     * POST /api/competition/update
     */
    @Operation(summary = "更新竞赛信息", description = "更新竞赛的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/update")
    public Result<String> updateCompetition(@RequestBody CompetitionSaveRequest  request) {
        Long id = request.getId();
        String title = request.getTitle();
        String content = request.getContent();
        MultipartFile picture = request.getPicture();
        //参数校验
        ThrowUtils.throwIf(id == null || id < 0, "请选择要更新的竞赛信息");
        if (title != null){
            ThrowUtils.throwIf(title.length() < 4 || title.length() > 20, "标题长度必须在4到20个字符之间");
        }
        if (content != null){
            ThrowUtils.throwIf(content.length() < 6 || content.length() > 200, "内容长度必须在6到200个字符之间");
        }
        if (picture != null){
            ThrowUtils.throwIf(!competitionService.isValidPictureType(picture.getContentType()), "请上传有效的图片文件(jpg/jpeg/png/gif)");
        }

        boolean result = competitionService.updateCompetition(id, title, content, picture);
        ThrowUtils.throwIf(!result, "更新竞赛信息失败");
        return Result.success("竞赛信息更新成功");
    }

    /**
     * 获取竞赛信息
     * POST /api/competition/get
     */
    @Operation(summary = "获取竞赛信息", description = "获取竞赛的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/get")
    public Result<Competition> getCompetition(Long id) {
        Competition competition = competitionService.getCompetition(id);
        return Result.success(competition);
    }

    /**
     * 删除竞赛信息
     * POST /api/competition/delete
     */
    @Operation(summary = "删除竞赛信息", description = "删除竞赛的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/delete")
    public Result<String> deleteCompetition(Long id) {
        boolean result = competitionService.deleteCompetition(id);
        ThrowUtils.throwIf(!result, "删除竞赛信息失败");
        return Result.success("竞赛信息删除成功");
    }
}
