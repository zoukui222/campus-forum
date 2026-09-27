package com.zwz.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zwz.forum.dto.PasswordUpdateRequest;
import com.zwz.forum.dto.UserUpdateRequest;
import com.zwz.forum.entity.User;
import com.zwz.forum.vo.UserVO;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
public interface IUserService extends IService<User> {
    // 获取用户详情 (用于个人主页)
    UserVO getUserProfile(Long userId);

    // 修改个人信息
    void updateUserInfo(UserUpdateRequest req);

    // 任命/撤销板主
    void appointModerator(Long userId, Integer boardId);

    // 分页查询用户列表 (支持昵称/用户名搜索)
    Page<UserVO> getUserList(Page<User> page, String keyword);

    // 修改密码
    void updatePassword(PasswordUpdateRequest req);

    // 管理员修改用户信息
    void adminUpdateUserInfo(Long userId, String username, String nickname, String email, Integer score, String role,
            LocalDateTime createTime, String avatar);
}
