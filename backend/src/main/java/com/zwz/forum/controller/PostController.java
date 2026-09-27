package com.zwz.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwz.forum.common.Result;
import com.zwz.forum.dto.PostCreateRequest;
import com.zwz.forum.dto.PostUpdateRequest;
import com.zwz.forum.service.IPostService;
import com.zwz.forum.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 帖子表 前端控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "帖子管理")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;

    @Operation(summary = "发布帖子")
    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid PostCreateRequest request) {
        postService.createPost(request);
        return Result.success("发布成功");
    }

    @Operation(summary = "获取帖子列表")
    @GetMapping("/list")
    public Result<Page<PostVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer boardId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword
    ) {
        Page<PostVO> result = postService.getPostList(new Page<>(pageNum, pageSize), boardId, userId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.success(postService.getPostDetail(id));
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "编辑帖子")
    @PutMapping("/update")
    public Result<String> update(@RequestBody @Valid PostUpdateRequest req) {
        postService.updatePost(req);
        return Result.success("修改成功");
    }
}
