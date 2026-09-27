package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.dto.PasswordUpdateRequest;
import com.zwz.forum.dto.UserUpdateRequest;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.IUserService;
import com.zwz.forum.utils.SecurityUtils;
import com.zwz.forum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    // 1. 获取用户详情 (用于个人主页)
    @Override
    public UserVO getUserProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 转换为 VO，自动脱敏密码等信息
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    // 2. 修改个人信息
    @Override
    @Transactional
    public void updateUserInfo(UserUpdateRequest req) {
        Long currentUserId = SecurityUtils.getUserId();
        User user = this.getById(currentUserId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        user.setNickname(req.getNickname());
        user.setAvatar(req.getAvatar());
        user.setEmail(req.getEmail());

        this.updateById(user);
    }

    @Override
    @Transactional
    public void appointModerator(Long userId, Integer boardId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 简单校验一下板块是否存在 (可选)
        // if (boardId != null && boardMapper.selectById(boardId) == null) throw ...

        if (boardId != null) {
            // 任命
            user.setRole("MODERATOR");
            user.setManagedBoardId(boardId);
        } else {
            // 撤销 (降级为普通用户)
            user.setRole("USER");
            user.setManagedBoardId(null);
        }

        this.updateById(user);
    }

    // 分页查询用户列表 (支持昵称/用户名搜索)
    @Override
    public Page<UserVO> getUserList(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 如果有搜索关键词，则模糊匹配 用户名 OR 昵称
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime); // 按注册时间倒序

        Page<User> userPage = this.page(page, wrapper);

        // 转换 Page<User> -> Page<UserVO>
        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(userPage, voPage, "records"); // 复制分页信息(total, pages等)，忽略记录列表

        List<UserVO> voList = userPage.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            // 这里已经包含了 role 和 managedBoardId，前端正好可以展示当前身份
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);

        return voPage;
    }

    // 修改密码
    @Override
    @Transactional
    public void updatePassword(PasswordUpdateRequest req) {
        Long userId = SecurityUtils.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 校验两次新密码是否一致
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 2. 校验旧密码是否正确
        // passwordEncoder.matches(明文, 数据库里的密文)
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 3. 校验新密码不能和旧密码相同 (可选，为了安全建议加上)
        if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 4. 加密并更新
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        this.updateById(user);
    }

    // 管理员修改用户信息
    @Override
    @Transactional
    public void adminUpdateUserInfo(Long userId, String username, String nickname, String email, Integer score,
            String role, LocalDateTime createTime, String avatar) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        if (username != null) {
            user.setUsername(username);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (score != null) {
            user.setScore(score);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (createTime != null) {
            user.setCreateTime(createTime);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        this.updateById(user);
    }
}
