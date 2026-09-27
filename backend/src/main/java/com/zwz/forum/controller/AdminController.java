package com.zwz.forum.controller;

import com.zwz.forum.common.Result;
import com.zwz.forum.common.annotation.RequireAdmin;
import com.zwz.forum.dto.BoardRequest;
import com.zwz.forum.dto.ModeratorAppointRequest;
import com.zwz.forum.service.IUserService;
import com.zwz.forum.service.impl.BoardServiceImpl;
import com.zwz.forum.service.impl.PostServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ljx
 * @Date: 2025/11/27 15:10
 */
@Tag(name = "管理员后台")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final PostServiceImpl postService;
    private final BoardServiceImpl boardService;
    private final IUserService userService;

    // 内容审核可以抽象为对不正当的帖子进行删除
    @RequireAdmin
    @Operation(summary = "删除任意帖子")
    @DeleteMapping("/post/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        postService.deletePostByAdmin(id);
        return Result.success("删除成功");
    }

    @RequireAdmin
    @Operation(summary = "置顶/取消置顶帖子")
    @PostMapping("/post/top/{id}")
    public Result<String> toggleTop(@PathVariable Long id) {
        postService.toggleTop(id);
        return Result.success("操作成功");
    }

    @RequireAdmin
    @Operation(summary = "添加新板块")
    @PostMapping("/board/add")
    public Result<String> addBoard(@RequestBody @Valid BoardRequest req) {
        boardService.createBoard(req);
        return Result.success("板块创建成功");
    }

    @RequireAdmin
    @Operation(summary = "删除板块")
    @DeleteMapping("/board/{id}")
    public Result<String> deleteBoard(@PathVariable Integer id) {
        boardService.deleteBoard(id);
        return Result.success("板块删除成功");
    }

    @RequireAdmin(superAdminOnly = true) // 版主任免属敏感操作，仅超级管理员可调用
    @Operation(summary = "任命/撤销板主")
    @PostMapping("/appoint")
    public Result<String> appointModerator(@RequestBody @Valid ModeratorAppointRequest req) {
        // 在 ServiceImpl 里可以强转或者直接在 Interface 里定义这个方法
        // 这里假设你在 IUserService 加了 appointModerator，或者直接注入 UserServiceImpl
        userService.appointModerator(req.getUserId(), req.getBoardId());

        String msg = (req.getBoardId() != null) ? "任命成功" : "撤销成功";
        return Result.success(msg);
    }
}
