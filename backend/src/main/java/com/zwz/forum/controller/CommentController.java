package com.zwz.forum.controller;

import com.zwz.forum.common.Result;
import com.zwz.forum.dto.CommentCreateRequest;
import com.zwz.forum.service.ICommentService;
import com.zwz.forum.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid CommentCreateRequest request) {
        commentService.createComment(request);
        return Result.success("评论成功");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取帖子的一级评论")
    @GetMapping("/list/{postId}")
    public Result<List<CommentVO>> list(@PathVariable Long postId) {
        List<CommentVO> list = commentService.getCommentsByPostId(postId);
        return Result.success(list);
    }

    @Operation(summary = "获取子评论(楼中楼)")
    @GetMapping("/sub-list/{rootId}")
    public Result<List<CommentVO>> subList(@PathVariable Long rootId) {
        List<CommentVO> list = commentService.getSubComments(rootId);
        return Result.success(list);
    }
}