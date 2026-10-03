package com.zwz.forum.ai.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwz.forum.ai.core.AgentContext;
import com.zwz.forum.ai.mapper.AiQueryMapper;
import com.zwz.forum.ai.tool.AiTool;
import com.zwz.forum.ai.tool.ToolPermission;
import com.zwz.forum.entity.Post;
import com.zwz.forum.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具：发布新帖
 *
 * 会产生副作用，但有两点约束让它足够安全：
 * 1) 作者身份显式取 AgentContext.userId，绝不用 SecurityUtils —— 后者依赖请求线程的
 *    ThreadLocal，在 Agent 线程里会抛 401；
 * 2) 只能发帖，不能删帖、不能改别人的帖子，且发出来的内容是可见内容而非不可逆操作。
 */
@Component
@RequiredArgsConstructor
public class CreatePostTool implements AiTool {

    private final PostMapper postMapper;
    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "create_post";
    }

    @Override
    public String description() {
        return "在指定板块发布一篇新帖子。当用户明确要求「帮我发个帖」「把刚才说的发到论坛上」时使用，"
                + "需要提供标题、正文和板块 ID（板块 ID 可用 list_boards 查询）。"
                + "正文支持 Markdown 语法。发布前建议先确认板块 ID 是否有效。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>();

        Map<String, Object> title = new LinkedHashMap<>();
        title.put("type", "string");
        title.put("description", "帖子标题，最多 100 字");
        properties.put("title", title);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "string");
        content.put("description", "帖子正文，支持 Markdown");
        properties.put("content", content);

        Map<String, Object> boardId = new LinkedHashMap<>();
        boardId.put("type", "integer");
        boardId.put("description", "目标板块 ID。" + boardOptions());
        properties.put("boardId", boardId);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("title", "content", "boardId"));
        return schema;
    }

    private String boardOptions() {
        try {
            List<Map<String, Object>> boards = aiQueryMapper.selectBoards();
            if (boards == null || boards.isEmpty()) {
                return "当前暂无板块";
            }
            return "可选值：" + boards.stream()
                    .map(b -> b.get("id") + "=" + b.get("name"))
                    .collect(Collectors.joining("，"));
        } catch (Exception e) {
            return "板块列表暂不可用";
        }
    }

    @Override
    public ToolPermission permission() {
        return ToolPermission.ANY;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        if (!args.hasNonNull("title") || !args.hasNonNull("content") || !args.hasNonNull("boardId")) {
            return "参数缺失：title、content、boardId 均为必填。可先用 list_boards 查询板块 ID。";
        }
        if (context == null || context.getUserId() == null) {
            return "拒绝执行：无法确定当前登录用户，不能发帖。";
        }

        String title = args.get("title").asText().trim();
        String content = args.get("content").asText();
        int boardId = args.get("boardId").asInt();

        if (title.isEmpty()) {
            return "参数错误：标题不能为空";
        }
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }
        if (content.trim().isEmpty()) {
            return "参数错误：正文不能为空";
        }

        // 校验板块存在，避免模型编一个不存在的 boardId
        boolean boardExists = aiQueryMapper.selectBoards().stream()
                .anyMatch(b -> String.valueOf(boardId).equals(String.valueOf(b.get("id"))));
        if (!boardExists) {
            return "参数错误：板块 ID=" + boardId + " 不存在。" + boardOptions();
        }

        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(context.getUserId());
        post.setTitle(title);
        post.setContent(content);
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setIsTop(false);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setDeleted(false);
        postMapper.insert(post);

        return "发帖成功：id=" + post.getId() + "，标题《" + title + "》，板块 ID=" + boardId
                + "，作者 " + context.displayName() + "。用户可在帖子列表中看到。";
    }
}
