package com.zwz.forum.ai.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwz.forum.ai.core.AgentContext;
import com.zwz.forum.ai.mapper.AiQueryMapper;
import com.zwz.forum.ai.tool.AiTool;
import com.zwz.forum.ai.tool.ToolPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具：检索帖子
 * 只读，登录用户即可使用。
 */
@Component
@RequiredArgsConstructor
public class PostSearchTool implements AiTool {

    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "search_posts";
    }

    @Override
    public String description() {
        return "按关键词或板块检索论坛帖子，返回帖子 ID、标题、所属板块、作者、浏览量与回复数。"
                + "当用户问「有没有关于X的帖子」「技术交流板块里有什么」或需要帖子列表时使用。"
                + "拿到 id 后可再用 get_post_detail 查看正文与评论。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>();

        Map<String, Object> keyword = new LinkedHashMap<>();
        keyword.put("type", "string");
        keyword.put("description", "搜索关键词，会模糊匹配标题与正文。例如 '算法'、'二手'、'考试'");
        properties.put("keyword", keyword);

        Map<String, Object> boardId = new LinkedHashMap<>();
        boardId.put("type", "integer");
        boardId.put("description", "板块 ID。" + boardOptions());
        properties.put("boardId", boardId);

        Map<String, Object> limit = new LinkedHashMap<>();
        limit.put("type", "integer");
        limit.put("description", "返回条数，范围 1-10，默认 5");
        properties.put("limit", limit);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.emptyList());
        return schema;
    }

    /** 板块 ID 会随运营变动，从库里实时读出拼进工具说明，避免模型拿着过期的映射猜 */
    private String boardOptions() {
        try {
            List<Map<String, Object>> boards = aiQueryMapper.selectBoards();
            if (boards == null || boards.isEmpty()) {
                return "当前暂无板块";
            }
            String options = boards.stream()
                    .map(b -> b.get("id") + "=" + b.get("name"))
                    .collect(Collectors.joining("，"));
            return "可选值：" + options;
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
        String keyword = args.hasNonNull("keyword") ? args.get("keyword").asText().trim() : null;
        Integer boardId = args.hasNonNull("boardId") ? args.get("boardId").asInt() : null;

        int limit = args.hasNonNull("limit") ? args.get("limit").asInt() : 5;
        limit = Math.max(1, Math.min(limit, 10));
        if (keyword != null && keyword.isEmpty()) {
            keyword = null;
        }

        List<Map<String, Object>> posts = aiQueryMapper.searchPosts(keyword, boardId, limit);

        if (posts == null || posts.isEmpty()) {
            // 空结果也要给出信息量，否则模型只能干巴巴说「没找到」
            List<Map<String, Object>> fallback = aiQueryMapper.searchPosts(null, null, 3);
            StringBuilder miss = new StringBuilder();
            miss.append("没有检索到匹配");
            if (keyword != null) {
                miss.append("关键词 '").append(keyword).append("'");
            }
            if (boardId != null) {
                miss.append("板块 ID=").append(boardId);
            }
            miss.append("的帖子。");
            if (fallback != null && !fallback.isEmpty()) {
                miss.append("站内帖子较少，当前浏览量较高的是：\n");
                miss.append(format(fallback));
            }
            return miss.toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("检索到 ").append(posts.size()).append(" 篇帖子：\n");
        builder.append(format(posts));
        return builder.toString();
    }

    /** 用紧凑文本而非 JSON 回灌给模型：同样的信息量省下大量 token */
    private String format(List<Map<String, Object>> posts) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> post : posts) {
            builder.append("- id=").append(post.get("id"))
                    .append(" 《").append(post.get("title")).append("》");
            if (post.get("boardName") != null) {
                builder.append(" 板块:").append(post.get("boardName"));
            }
            if (post.get("nickname") != null) {
                builder.append(" 作者:").append(post.get("nickname"));
            }
            builder.append(" 浏览:").append(post.get("viewCount"));
            builder.append(" 回复:").append(post.get("replyCount"));
            Object top = post.get("isTop");
            if (top != null && ("1".equals(String.valueOf(top)) || Boolean.TRUE.equals(top))) {
                builder.append(" [已置顶]");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
