package com.zwz.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: ljx
 * @Date: 2025/11/30 14:49
 */
@Data
public class ModeratorAppointRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // 如果 boardId 为空，表示撤销板主职务，降为普通用户
    private Integer boardId;
}
