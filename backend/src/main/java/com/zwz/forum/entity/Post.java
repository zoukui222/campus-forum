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
 * 帖子表
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Getter
@Setter
@TableName("forum_post")
@Schema(name = "Post", description = "帖子表")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "帖子ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "板块ID")
    @TableField("board_id")
    private Integer boardId;

    @Schema(description = "作者ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "内容(Markdown源码)")
    @TableField("content")
    private String content;

    @Schema(description = "浏览量")
    @TableField("view_count")
    private Integer viewCount;

    @Schema(description = "回复数")
    @TableField("reply_count")
    private Integer replyCount;

    @Schema(description = "是否置顶")
    @TableField("is_top")
    private Boolean isTop;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Boolean deleted;
}
