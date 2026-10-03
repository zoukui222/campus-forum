package com.zwz.forum.ai.tool;

import com.zwz.forum.ai.core.AgentContext;

/**
 * 工具所需权限等级
 */
public enum ToolPermission {

    /** 登录用户即可使用（只读查询） */
    ANY {
        @Override
        public boolean allowed(AgentContext context) {
            return context != null && context.isAuthenticated();
        }
    },

    /** 版主与管理员可用（管理类写操作） */
    MODERATOR {
        @Override
        public boolean allowed(AgentContext context) {
            return context != null && context.isModerator();
        }
    };

    public abstract boolean allowed(AgentContext context);
}
