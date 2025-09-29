package com.ita.home.controller;

import com.ita.home.model.entity.Member;
import com.ita.home.model.req.MemberSaveRequest;
import com.ita.home.result.Result;
import com.ita.home.service.MemberService;
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
@RequestMapping("/api/member")
@Tag(name = "成员信息管理", description = "成员信息增删改查等接口")
@Slf4j
public class MemberController {

    @Resource
    private MemberService memberService;

    /**
     * 添加成员信息
     * POST /api/member/add
     */
    @Operation(summary = "新增成员", description = "新增成员的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/add")
    public Result<String> addMember( @RequestParam("name") String name,
                                     @RequestParam("content") String content,
                                     @RequestParam("picture") MultipartFile picture) {

        // 参数校验
        ThrowUtils.throwIf(name == null || name.length() < 2 || name.length() > 20, "姓名长度必须在2到20个字符之间");
        ThrowUtils.throwIf(content == null || content.length() < 6 || content.length() > 200, "个人简介长度必须在6到200个字符之间");
        ThrowUtils.throwIf(picture == null, "请上传有效的图片文件(jpg/jpeg/png/gif)");

//        // 验证图片类型是否有效
//        ThrowUtils.throwIf(!memberService.isValidPictureType(picture.getContentType()), "请上传有效的图片文件(jpg/jpeg/png/gif)");

        // 保存图片并获取访问路径
        String picturePath = memberService.savePicture(picture);

        // 添加成员信息
        boolean result = memberService.addMember(name, content, picturePath);
        ThrowUtils.throwIf(!result, "添加成员信息失败");

        return Result.success("成员信息保存成功");
    }

    /**
     * 更新成员信息
     * POST /api/member/update
     */
    @Operation(summary = "更新成员信息", description = "更新成员的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/update")
    public Result<String> updateMember(@RequestParam("id") Long id,
                                       @RequestParam("name") String name,
                                       @RequestParam("content") String content,
                                       @RequestParam("picture") MultipartFile picture) {

        // 参数校验
        ThrowUtils.throwIf(id == null || id < 0, "请选择要更新的成员信息");

        if (name != null) {
            ThrowUtils.throwIf(name.length() < 2 || name.length() > 20, "姓名长度必须在2到20个字符之间");
        }

        if (content != null) {
            ThrowUtils.throwIf(content.length() < 6 || content.length() > 200, "个人简介长度必须在6到200个字符之间");
        }

//        if (picture != null) {
//            ThrowUtils.throwIf(!memberService.isValidPictureType(picture.getContentType()), "请上传有效的图片文件(jpg/jpeg/png/gif)");
//        }

        // 保存图片并获取访问路径（仅在提供了新图片时）
        String picturePath = null;
        if (picture != null) {
            picturePath = memberService.savePicture(picture);
        }

        boolean result = memberService.updateMember(id, name, content, picturePath);
        ThrowUtils.throwIf(!result, "更新成员信息失败");
        return Result.success("成员信息更新成功");
    }


    /**
     * 获取成员信息
     * GET /api/member/get
     */
    @Operation(summary = "获取成员信息", description = "获取成员的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/get")
    public Result<Member> getMember(Long id) {
        Member member = memberService.getMember(id);
        return Result.success(member);
    }

    /**
     * 删除成员信息
     * POST /api/member/delete
     */
    @Operation(summary = "删除成员信息", description = "删除成员的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除信息成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/delete")
    public Result<String> deleteMember(Long id) {
        boolean result = memberService.deleteMember(id);
        ThrowUtils.throwIf(!result, "删除成员信息失败");
        return Result.success("成员信息删除成功");
    }
}
