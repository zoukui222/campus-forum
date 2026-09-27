package com.zwz.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zwz.forum.common.Result;
import com.zwz.forum.entity.Board;
import com.zwz.forum.service.IBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 板块表 前端控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "板块接口")
@RestController
@RequestMapping("/api/public/board")
@RequiredArgsConstructor
public class BoardController {

    private final IBoardService boardService;

    @Operation(summary = "获取板块列表(侧边栏)")
    @GetMapping("/list")
    public Result<List<Board>> list() {
        // 查询所有板块，并按 sort 字段排序 (1, 2, 3...)
        List<Board> list = boardService.list(new LambdaQueryWrapper<Board>()
                .orderByAsc(Board::getSort));

        return Result.success(list);
    }
}