package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: ljx
 * @Date: 2025/11/27 15:32
 */
public record BoardRequest(
        @NotBlank(message = "板块名称不能为空") String name,
        String description,
        @NotNull(message = "排序值不能为空") Integer sort,
        String icon
) {}