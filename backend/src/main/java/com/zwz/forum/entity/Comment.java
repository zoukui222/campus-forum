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
 * 评论表
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Getter
@Setter
@TableName("forum_comment")
@Schema(name = "Comment", description = "评论表")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属帖子")
    @TableField("post_id")
    private Long postId;

    @Schema(description = "根评论ID (0代表是一级评论)")
    private Long rootId;

    @Schema(description = "评论人")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "评论内容")
    @TableField("content")
    private String content;

    @Schema(description = "父评论ID(用于记录回复谁)")
    @TableField("parent_id")
    private Long parentId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("deleted")
    private Boolean deleted;
}
