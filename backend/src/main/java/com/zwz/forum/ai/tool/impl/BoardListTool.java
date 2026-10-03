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
 * 工具：板块列表
 * 只读，登录用户即可使用。板块是论坛内容的顶层分类，模型在「按板块检索」前需要先知道有哪些板块。
 */
@Component
@RequiredArgsConstructor
public class BoardListTool implements AiTool {

    private final AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "list_boards";
    }

    @Override
    public String description() {
        return "列出论坛所有板块及其 ID 与简介。"
                + "当用户问「论坛有哪些板块」「板块是怎么分的」，或需要先确定板块 ID 再检索帖子时使用，无需参数。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", new LinkedHashMap<String, Object>());
        schema.put("required", Collections.emptyList());
        return schema;
    }

    @Override
    public ToolPermission permission() {
        return ToolPermission.ANY;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        List<Map<String, Object>> boards = aiQueryMapper.selectBoards();
        if (boards == null || boards.isEmpty()) {
            return "当前论坛还没有任何板块。";
        }
        StringBuilder builder = new StringBuilder("论坛共有 ").append(boards.size()).append(" 个板块：\n");
        for (Map<String, Object> board : boards) {
            builder.append("- id=").append(board.get("id"))
                    .append(" ").append(board.get("name"));
            Object description = board.get("description");
            if (description != null && !String.valueOf(description).isEmpty()) {
                builder.append("（").append(description).append("）");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
