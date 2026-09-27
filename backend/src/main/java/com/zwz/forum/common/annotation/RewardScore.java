package com.zwz.forum.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: ljx
 * @Date: 2025/11/22 20:50
 * 在需要加分的方法上使用，例如: @RewardScore(10)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RewardScore {
    // 默认加的分数
    int value() default 5;
}
