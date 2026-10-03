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

/**
 * 工具：论坛数据统计
 * 只读，登录用户即可使用。用一个工具覆盖「有多少帖子」「哪个板块最热」「谁最活跃」这类问题，
 * 而不是给每类统计各开一个工具 —— 工具数量越多，模型选错的概率越高。
 */
@Component
@RequiredArgsConstructor
public class ForumStatsTool implements AiTool {

    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "get_forum_stats";
    }

    @Override
    public String description() {
        return "获取论坛整体统计：帖子总数、累计浏览量、回复与评论数、注册用户数、各板块帖子分布、发帖最活跃的用户。"
                + "当用户问「论坛有多少帖子」「哪个板块最火」「谁最活跃」时使用，无需参数。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.emptyList());
        return schema;
    }

    @Override
    public ToolPermission permission() {
        return ToolPermission.ANY;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        StringBuilder builder = new StringBuilder();

        Map<String, Object> stats = aiQueryMapper.selectForumStats();
        if (stats != null) {
            builder.append("【论坛概览】\n");
            builder.append("帖子总数：").append(stats.get("postCount")).append("\n");
            builder.append("累计浏览量：").append(stats.get("totalViews")).append("\n");
            builder.append("累计回复数：").append(stats.get("totalReplies")).append("\n");
            builder.append("评论总数：").append(stats.get("commentCount")).append("\n");
            builder.append("注册用户：").append(stats.get("userCount")).append("\n");
            builder.append("板块数量：").append(stats.get("boardCount")).append("\n");
        }

        List<Map<String, Object>> boardStats = aiQueryMapper.selectBoardStats();
        if (boardStats != null && !boardStats.isEmpty()) {
            builder.append("\n【各板块帖子分布】\n");
            for (Map<String, Object> row : boardStats) {
                builder.append("- ").append(row.get("name")).append("：").append(row.get("value")).append(" 篇\n");
            }
        }

        List<Map<String, Object>> activeUsers = aiQueryMapper.selectActiveUsers(5);
        if (activeUsers != null && !activeUsers.isEmpty()) {
            builder.append("\n【发帖最活跃的用户】\n");
            for (Map<String, Object> row : activeUsers) {
                builder.append("- ").append(row.get("nickname"))
                        .append("：").append(row.get("postCount")).append(" 帖")
                        .append("，积分 ").append(row.get("score")).append("\n");
            }
        }

        if (context != null && context.isAuthenticated()) {
            builder.append("\n（当前提问用户：").append(context.displayName())
                    .append("，角色：").append(context.getRole()).append("）");
        }
        return builder.toString();
    }
}
