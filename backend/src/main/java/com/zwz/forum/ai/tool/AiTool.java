package com.zwz.forum.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwz.forum.ai.core.AgentContext;

import java.util.Map;

/**
 * Agent 可调用工具的统一抽象。
 * 新增一个能力 = 新增一个实现本接口的 Spring Bean，注册表会自动发现，
 * 无需改动 Agent 主循环与协议拼装代码。
 */
public interface AiTool {

    /** 工具名：模型据此选择工具，只允许小写字母和下划线 */
    String name();

    /** 工具说明：要写清「什么场景该用」，而不是「这个接口做了什么」 */
    String description();

    /**
     * 参数 JSON Schema（type=object 的部分）
     * 例：properties={keyword={type=string, description=...}}，required=["keyword"]
     */
    Map<String, Object> parameters();

    /**
     * 工具所需的最高权限等级：
     * ANY      —— 登录用户即可（只读查询）
     * MODERATOR—— 版主与管理员（管理类操作）
     */
    ToolPermission permission();

    /**
     * 执行工具。
     * 约定：返回值是给模型看的字符串，不要向外抛异常 —— 失败也要以可读的失败原因返回，
     * 让模型有机会自己纠正参数或换一种方式回答。
     */
    String execute(JsonNode args, AgentContext context);
}
