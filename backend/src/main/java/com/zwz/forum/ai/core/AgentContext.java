package com.zwz.forum.ai.core;

/**
 * Agent 执行上下文
 *
 * 为什么不直接调 SecurityUtils.getUserId()：
 * SecurityUtils 底层是 SecurityContextHolder，默认使用 MODE_THREADLOCAL 策略，
 * 只在「处理当前 HTTP 请求的那条线程」上有效。Agent 为了做 SSE 流式输出必须换线程执行，
 * 一旦换线程，SecurityContextHolder 里就是空的 —— 而且本项目的 SecurityUtils 在拿不到时
 * 会直接抛 BusinessException(401)，也就是说工具会集体报「获取登录用户信息失败」。
 *
 * 更棘手的是：它抛异常，但异常发生在 Agent 线程里，不会回传给用户，
 * 表现就是「AI 说它没权限/查不到」，排查起来比静默失败更绕。
 *
 * 所以这里在 Controller 的请求线程里一次性把身份快照出来，显式随参数传下去，
 * 不依赖任何 ThreadLocal，天然线程安全。
 */
public class AgentContext {

    private final Long userId;
    private final String username;
    private final String nickname;
    /** USER / MODERATOR / ADMIN */
    private final String role;

    public AgentContext(Long userId, String username, String nickname, String role) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    /** 管理员或版主：可执行管理类工具 */
    public boolean isModerator() {
        return "ADMIN".equals(role) || "MODERATOR".equals(role);
    }

    /** 仅超级管理员 */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }

    public String displayName() {
        return nickname != null && !nickname.isEmpty() ? nickname : username;
    }

    @Override
    public String toString() {
        return "AgentContext{userId=" + userId + ", role=" + role + "}";
    }
}
