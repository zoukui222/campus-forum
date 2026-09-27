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
 * 消息通知表
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Getter
@Setter
@TableName("sys_message")
@Schema(name = "Message", description = "消息通知表")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "发送者ID(0代表系统)")
    @TableField("from_id")
    private Long fromId;

    @Schema(description = "接收者ID")
    @TableField("to_id")
    private Long toId;

    @Schema(description = "类型: COMMENT/SYSTEM")
    @TableField("type")
    private String type;

    @Schema(description = "消息内容或关联ID")
    @TableField("content")
    private String content;

    @Schema(description = "是否已读")
    @TableField("is_read")
    private Boolean isRead;

    @TableField("create_time")
    private LocalDateTime createTime;
}
