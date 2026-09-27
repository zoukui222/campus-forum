package com.zwz.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwz.forum.common.Result;
import com.zwz.forum.common.annotation.RequireAdmin;
import com.zwz.forum.dto.AdminUserUpdateRequest;
import com.zwz.forum.dto.PasswordUpdateRequest;
import com.zwz.forum.dto.UserUpdateRequest;
import com.zwz.forum.service.IUserService;
import com.zwz.forum.service.UploadService;
import com.zwz.forum.util.CheckUtil;
import com.zwz.forum.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final UploadService uploadService;

    @Operation(summary = "获取指定用户信息(个人主页)")
    @GetMapping("/{id}")
    public Result<UserVO> getUserProfile(@PathVariable Long id) {
        return Result.success(userService.getUserProfile(id));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/update")
    public Result<String> updateInfo(@RequestBody @Valid UserUpdateRequest req) {
        userService.updateUserInfo(req);
        return Result.success("修改成功");
    }

    @Operation(summary = "分页查询用户列表(用于管理或搜索)")
    @GetMapping("/list")
    @RequireAdmin
    public Result<Page<UserVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword // 搜索关键词
    ) {
        Page<UserVO> page = userService.getUserList(new Page<>(pageNum, pageSize), keyword);
        return Result.success(page);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<String> updatePassword(
            @RequestBody @Valid PasswordUpdateRequest req,
            HttpServletRequest request
    ) {
        // 执行修改密码逻辑
        userService.updatePassword(req);

        return Result.success("密码修改成功，请重新登录");
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        
        if (CheckUtil.isImage(file.getOriginalFilename())) {
            String url = uploadService.uploadImage(file);
            if (url != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("url", url);
                return Result.success(data);
            } else {
                return Result.error(500, "文件上传失败");
            }
        }
        return Result.error(400, "文件格式错误，请上传图片文件");
    }

    @Operation(summary = "管理员修改用户信息")
    @PutMapping("/admin/update")
    @RequireAdmin
    public Result<String> adminUpdateUserInfo(@RequestBody @Valid AdminUserUpdateRequest req) {
        userService.adminUpdateUserInfo(
                req.getUserId(),
                req.getUsername(),
                req.getNickname(),
                req.getEmail(),
                req.getScore(),
                req.getRole(),
                req.getCreateTime(),
                req.getAvatar()
        );
        return Result.success("用户信息修改成功");
    }

}
