package com.zwz.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwz.forum.common.Result;
import com.zwz.forum.dto.MessageSendRequest;
import com.zwz.forum.service.IMessageService;
import com.zwz.forum.vo.ConversationVO;
import com.zwz.forum.vo.MessageVO;
import com.zwz.forum.vo.UnreadCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 消息通知表 前端控制器
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;
    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public Result<String> send(@RequestBody @Valid MessageSendRequest request) {
        messageService.sendPrivateMessage(request.toUserId(), request.content());
        return Result.success("发送成功");
    }

    @Operation(summary = "获取我的消息列表")
    @GetMapping("/list")
    public Result<Page<MessageVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type // 传 CHAT 查私信，传 COMMENT 查回复，不传查所有
    ) {
        Page<MessageVO> page = messageService.getMyMessages(new Page<>(pageNum, pageSize), type);
        return Result.success(page);
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getUnreadCount());
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/read/{id}")
    public Result<String> read(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success("标记成功");
    }

    @Operation(summary = "获取私信会话列表(左侧栏)")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations() {
        return Result.success(messageService.getConversationList());
    }

    @Operation(summary = "获取与某人的聊天记录(右侧栏)")
    @GetMapping("/history/{partnerId}")
    public Result<List<MessageVO>> getHistory(@PathVariable Long partnerId) {
        // 获取记录的同时，建议把这些消息标记为已读
        return Result.success(messageService.getChatHistory(partnerId));
    }

    @Operation(summary = "批量标记已读")
    @PostMapping("/batch-read")
    public Result<String> batchRead(@RequestBody List<Long> ids) {
        messageService.batchMarkAsRead(ids);
        return Result.success("批量标记成功");
    }

    @Operation(summary = "全部标记已读SYSTEM和COMMENT消息")
    @PostMapping("/read-all")
    public Result<String> readAll() {
        messageService.markAllAsRead();
        return Result.success("全部已读");
    }

    @Operation(summary = "获取各类型未读消息数")
    @GetMapping("/unread-details")
    public Result<UnreadCountVO> getUnreadDetails() {
        return Result.success(messageService.getUnreadCountByType());
    }
}
