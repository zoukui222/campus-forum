package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwz.forum.common.annotation.RewardScore;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.dto.PostCreateRequest;
import com.zwz.forum.dto.PostUpdateRequest;
import com.zwz.forum.entity.Board;
import com.zwz.forum.entity.Post;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.BoardMapper;
import com.zwz.forum.mapper.PostMapper;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.IPostService;
import com.zwz.forum.utils.SecurityUtils;
import com.zwz.forum.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 帖子表 服务实现类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    // 1. 发布帖子
    @Transactional
    @RewardScore(10) // 发帖成功 +10 分
    public void createPost(PostCreateRequest req) {
        // 校验板块是否存在
        Board board = boardMapper.selectById(req.boardId());
        if (board == null) {
            throw new BusinessException("板块不存在");
        }

        // 构建帖子对象
        Post post = new Post();
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setBoardId(req.boardId());
        post.setUserId(SecurityUtils.getUserId()); // 关键：从 Token 获取当前用户ID
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setIsTop(false); // 默认不置顶

        this.save(post);
    }

    // 2. 分页查询帖子列表 (带作者信息)
    public Page<PostVO> getPostList(Page<Post> page, Integer boardId, Long userId, String keyword) {
        // A. 查询帖子基本数据
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        // 如果传了 boardId 就按板块筛选
        wrapper.eq(boardId != null, Post::getBoardId, boardId);
        // 如果传了userId 就按照userId筛选
        wrapper.eq(userId != null && userId != 0, Post::getUserId, userId);
        // 如果传了keyword 还要按照keyward模糊搜索
        wrapper.like(StringUtils.hasText(keyword), Post::getTitle, keyword);
        wrapper.orderByDesc(Post::getIsTop) // 置顶的排前面
                .orderByDesc(Post::getCreateTime); // 剩下的按时间倒序

        Page<Post> postPage = this.page(page, wrapper);

        // 如果没有数据，直接返回空
        if (postPage.getRecords().isEmpty()) {
            return new Page<>();
        }

        // B. 批量查询关联数据 (User 和 Board) - 避免循环查库
        // 收集所有 userId 和 boardId
        Set<Long> userIds = postPage.getRecords().stream().map(Post::getUserId).collect(Collectors.toSet());
        Set<Integer> boardIds = postPage.getRecords().stream().map(Post::getBoardId).collect(Collectors.toSet());

        // 查 User Map
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        // 查 Board Map
        Map<Integer, Board> boardMap = boardMapper.selectBatchIds(boardIds).stream()
                .collect(Collectors.toMap(Board::getId, board -> board));

        // C. 组装 VO
        List<PostVO> voList = postPage.getRecords().stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo); // 复制基本属性

            // 填充作者信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setAuthorName(user.getNickname());
                vo.setAuthorAvatar(user.getAvatar());
            }

            // 填充板块名称
            Board board = boardMap.get(post.getBoardId());
            if (board != null) {
                vo.setBoardName(board.getName());
            }

            // 列表页内容太长可以截取 (可选)
            if (vo.getContent().length() > 200) {
                vo.setContent(vo.getContent().substring(0, 200) + "...");
            }
            return vo;
        }).collect(Collectors.toList());

        // D. 重新封装 Page 对象返回
        Page<PostVO> resultPage = new Page<>();
        BeanUtils.copyProperties(postPage, resultPage, "records");
        resultPage.setRecords(voList);

        return resultPage;
    }

    // 3. 获取帖子详情
    @Transactional
    public PostVO getPostDetail(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 浏览量 +1 (简单做法，高并发建议用 Redis)
        post.setViewCount(post.getViewCount() + 1);
        this.updateById(post);

        // 转换 VO
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);

        // 填充作者
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setAuthorName(user.getNickname());
            vo.setAuthorAvatar(user.getAvatar());
        }

        // 填充板块
        Board board = boardMapper.selectById(post.getBoardId());
        if (board != null) {
            vo.setBoardName(board.getName());
        }

        return vo;
    }

    // 删除帖子
    @Override
    @Transactional
    public void deletePost(Long postId) {
        Long currentUserId = SecurityUtils.getUserId();
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 查出当前用户的信息 (需要看他的角色和管理的板块)
        User currentUser = userMapper.selectById(currentUserId);

        // 权限判定逻辑：
        // 1. 是作者本人
        boolean isAuthor = post.getUserId().equals(currentUserId);
        // 2. 是超级管理员
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        // 3. 是该板块的板主
        boolean isBoardModerator = "MODERATOR".equals(currentUser.getRole())
                && post.getBoardId().equals(currentUser.getManagedBoardId());

        if (!isAuthor && !isAdmin && !isBoardModerator) {
            throw new BusinessException(403, "无权删除该帖子");
        }

        this.removeById(postId);

        // TODO 删除这个帖子的所有评论
    }

    // 编辑帖子
    @Override
    @Transactional
    public void updatePost(PostUpdateRequest req) {
        Long currentUserId = SecurityUtils.getUserId();
        Post post = this.getById(req.getId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权修改该帖子");
        }

        // 只有标题和内容可以改，板块不能改
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());

        this.updateById(post);
    }

    // 管理员：切换置顶状态
    @Override
    @Transactional
    public void toggleTop(Long postId) {
        Post post = this.getById(postId);
        if (post == null) throw new BusinessException("帖子不存在");

        // 取反当前状态
        post.setIsTop(!post.getIsTop());
        this.updateById(post);
    }

    // 管理员：直接删除 (不需要校验作者)
    @Override
    @Transactional
    public void deletePostByAdmin(Long postId) {
        this.removeById(postId);
        // TODO 这里也可以加上级联删除评论的逻辑，和之前的 deletePost 类似
    }
}
