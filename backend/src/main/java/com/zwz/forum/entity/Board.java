package com.zwz.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 板块表
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Getter
@Setter
@TableName("forum_board")
@Schema(name = "Board", description = "板块表")
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "板块ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "板块名称")
    @TableField("name")
    private String name;

    @Schema(description = "板块描述")
    @TableField("description")
    private String description;

    @Schema(description = "排序优先级")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "图标")
    @TableField("icon")
    private String icon;

    @TableField("create_time")
    private LocalDateTime createTime;
}
