package com.zwz.forum.aspect;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zwz.forum.common.annotation.RewardScore;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @Author: ljx
 * @Date: 2025/11/22 20:51
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ScoreAspect {
    private final UserMapper userMapper;

    /**
     * 定义切点：被 @RewardScore 注解标记的方法
     * result 是目标方法的返回值（虽然这里我们不怎么用它，但可以用来判断是否执行成功）
     */
    @AfterReturning(pointcut = "@annotation(rewardScore)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, RewardScore rewardScore, Object result) {
        try {
            // 1. 获取要加的分数值
            int scoreToAdd = rewardScore.value();

            // 2. 获取当前操作的用户 ID
            // 因为 AOP 是在同一个线程里执行的，SecurityContextHolder 依然有效
            Long userId = SecurityUtils.getUserId();

            if (userId != null) {
                // 3. 执行数据库更新： update sys_user set score = score + ? where id = ?
                // 使用 Mybatis-Plus 的原子更新，线程安全
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.setSql("score = score + " + scoreToAdd)
                        .eq(User::getId, userId);

                userMapper.update(null, updateWrapper);

                log.info("用户 {} 触发积分奖励，增加 {} 分", userId, scoreToAdd);
            }

        } catch (Exception e) {
            // AOP 里的异常最好捕获住，不要影响主业务（比如发帖成功了，但加分失败，不应该让发帖回滚）
            log.error("积分奖励执行失败", e);
        }
    }
}
