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
import java.util.Map;

/**
 * 工具：置顶 / 取消置顶帖子
 *
 * 版主与管理员专属的管理类操作（权限等级 MODERATOR）。
 * 选它作为 AI 的第二个写工具，是因为「影响力大但完全可逆」——置顶错了再取消即可，
 * 不像删帖那样不可逆。给 Agent 开放写能力时，可逆性应当排在前面考虑。
 */
@Component
@RequiredArgsConstructor
public class TopPostTool implements AiTool {

    private final PostMapper postMapper;
    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "top_post";
    }

    @Override
    public String description() {
        return "将某篇帖子置顶或取消置顶，仅版主与管理员可用。"
                + "当用户（版主/管理员）要求「把这篇帖子置顶」「取消置顶」时使用，需要帖子 ID。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>();

        Map<String, Object> postId = new LinkedHashMap<>();
        postId.put("type", "integer");
        postId.put("description", "帖子 ID");
        properties.put("postId", postId);

        Map<String, Object> top = new LinkedHashMap<>();
        top.put("type", "boolean");
        top.put("description", "true=置顶，false=取消置顶，默认 true");
        properties.put("top", top);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("postId"));
        return schema;
    }

    @Override
    public ToolPermission permission() {
        return ToolPermission.MODERATOR;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        if (!args.hasNonNull("postId")) {
            return "参数缺失：需要提供 postId";
        }
        // 防御性校验：即便声明层已按权限裁剪，执行前再拦一道
        if (context == null || !context.isModerator()) {
            return "拒绝执行：置顶操作需要版主或管理员权限。";
        }

        long postId = args.get("postId").asLong();
        boolean top = !args.hasNonNull("top") || args.get("top").asBoolean();

        Map<String, Object> existing = aiQueryMapper.selectPostDetail(postId);
        if (existing == null) {
            return "不存在 ID=" + postId + " 的帖子，请先用 search_posts 确认。";
        }

        Post update = new Post();
        update.setId(postId);
        update.setIsTop(top);
        update.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(update);

        return "操作成功：帖子 id=" + postId + "《" + existing.get("title") + "》已"
                + (top ? "置顶" : "取消置顶") + "，操作人 " + context.displayName()
                + "（" + context.getRole() + "）。";
    }
}
