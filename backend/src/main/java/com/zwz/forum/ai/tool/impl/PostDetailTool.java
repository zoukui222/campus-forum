package com.zwz.forum.ai.tool.impl;

import cn.hutool.http.HtmlUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.zwz.forum.ai.core.AgentContext;
import com.zwz.forum.ai.mapper.AiQueryMapper;
import com.zwz.forum.ai.tool.AiTool;
import com.zwz.forum.ai.tool.ToolPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具：读取帖子正文与评论
 * 只读，登录用户即可使用。
 */
@Component
@RequiredArgsConstructor
public class PostDetailTool implements AiTool {

    /** 正文回灌给模型的字符上限：长帖可能上万字，必须截断 */
    private static final int CONTENT_LIMIT = 1500;
    private static final int MAX_COMMENTS = 10;

    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "get_post_detail";
    }

    @Override
    public String description() {
        return "根据帖子 ID 读取正文内容、作者与评论列表。"
                + "当用户要求「总结这个帖子」「这篇讲了什么」「大家评论怎么说」时使用，"
                + "ID 通常来自 search_posts 的结果。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>();

        Map<String, Object> postId = new LinkedHashMap<>();
        postId.put("type", "integer");
        postId.put("description", "帖子 ID");
        properties.put("postId", postId);

        Map<String, Object> commentLimit = new LinkedHashMap<>();
        commentLimit.put("type", "integer");
        commentLimit.put("description", "返回多少条评论，默认 5，最多 10");
        properties.put("commentLimit", commentLimit);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.singletonList("postId"));
        return schema;
    }

    @Override
    public ToolPermission permission() {
        return ToolPermission.ANY;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        if (!args.hasNonNull("postId")) {
            return "参数缺失：需要提供 postId";
        }
        long postId = args.get("postId").asLong();

        Map<String, Object> post = aiQueryMapper.selectPostDetail(postId);
        if (post == null) {
            return "不存在 ID=" + postId + " 的帖子，请先用 search_posts 确认 ID 是否正确。";
        }

        int commentLimit = args.hasNonNull("commentLimit") ? args.get("commentLimit").asInt() : 5;
        commentLimit = Math.max(1, Math.min(commentLimit, MAX_COMMENTS));

        String plain = HtmlUtil.cleanHtmlTag(String.valueOf(post.getOrDefault("content", "")))
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        StringBuilder builder = new StringBuilder();
        builder.append("标题：").append(post.get("title")).append("\n");
        builder.append("板块：").append(post.getOrDefault("boardName", "未知")).append("\n");
        builder.append("作者：").append(post.getOrDefault("nickname", "未知"))
                .append("（").append(post.getOrDefault("authorRole", "USER")).append("）\n");
        builder.append("浏览：").append(post.get("viewCount"))
                .append(" 回复：").append(post.get("replyCount")).append("\n");
        builder.append("发布时间：").append(post.get("createTime")).append("\n");
        builder.append("正文：\n");
        builder.append(plain.length() <= CONTENT_LIMIT
                ? plain
                : plain.substring(0, CONTENT_LIMIT) + "\n…（正文过长，此处已截断）");

        List<Map<String, Object>> comments = aiQueryMapper.selectComments(postId, commentLimit);
        if (comments == null || comments.isEmpty()) {
            builder.append("\n\n评论：暂无评论");
        } else {
            builder.append("\n\n评论（共返回 ").append(comments.size()).append(" 条）：\n");
            for (Map<String, Object> comment : comments) {
                builder.append("- ").append(comment.getOrDefault("nickname", "匿名"))
                        .append("：").append(abbreviate(String.valueOf(comment.get("content")), 120)).append("\n");
            }
        }
        return builder.toString();
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= max ? flat : flat.substring(0, max) + "…";
    }
}
