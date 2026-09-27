package com.zwz.forum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: ljx
 * @Date: 2025/11/27 15:28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {

    /**
     * 是否仅允许超级管理员（ADMIN）访问。
     * 默认 false 表示 ADMIN 与 MODERATOR 均可访问；角色任免等敏感操作应设为 true。
     */
    boolean superAdminOnly() default false;
}
