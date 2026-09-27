package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwz.forum.common.annotation.RewardScore;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.dto.CommentCreateRequest;
import com.zwz.forum.entity.Comment;
import com.zwz.forum.entity.Post;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.CommentMapper;
import com.zwz.forum.mapper.PostMapper;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.ICommentService;
import com.zwz.forum.service.IMessageService;
import com.zwz.forum.utils.SecurityUtils;
import com.zwz.forum.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final IMessageService messageService;

    // 1. 发表评论
    @Transactional(rollbackFor = Exception.class)
    @RewardScore(2) // 评论+2分
    public void createComment(CommentCreateRequest req) {
        Post post = postMapper.selectById(req.postId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        String finalContent = req.content();
        Long rootId = 0L; // 默认为根评论

        // 如果是回复评论
        if (req.parentId() != null && req.parentId() != 0) {
            Comment parent = commentMapper.selectById(req.parentId());
            if (parent == null) {
                throw new BusinessException("回复的评论不存在了");
            }

            // A. 计算 rootId
            if (parent.getRootId() == 0) {
                // 父评论本身就是根 -> 那我就是它的儿子，rootId = 父ID
                rootId = parent.getId();
            } else {
                // 父评论也是子评论 -> 那我们同属一个根，rootId 继承
                rootId = parent.getRootId();
            }

            // B. 处理内容 "@User"
            User parentUser = userMapper.selectById(parent.getUserId());
            if (parentUser != null) {
                finalContent = "回复 @" + parentUser.getNickname() + " : " + finalContent;
            }
        }

        Comment comment = new Comment();
        comment.setPostId(req.postId());
        comment.setContent(finalContent);
        comment.setUserId(SecurityUtils.getUserId());
        comment.setParentId(req.parentId());
        comment.setRootId(rootId); // 设置计算好的 rootId

        this.save(comment);

        // 帖子总回复数 +1
        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("reply_count = reply_count + 1").eq(Post::getId, req.postId());
        postMapper.update(null, updateWrapper);

        Long currentUserId = SecurityUtils.getUserId();

        // A. 场景：给“帖主”发通知 (前提：帖主不是我自己,并且是一级评论)
        if (!post.getUserId().equals(currentUserId) && comment.getRootId().equals(0L)) {
            // 构造消息内容，格式约定： "POST:帖子ID:消息摘要"
            // 这样前端收到后，可以解析出 ID 进行跳转
            String summary = req.content().length() > 20 ? req.content().substring(0, 20) + "..." : req.content();
            String msgContent = "POST:" + post.getId() + ":" + summary;

            messageService.createSystemNotification(currentUserId, post.getUserId(), "COMMENT", msgContent);
        }

        // B. 场景：给“层主/被回复的人”发通知 (楼中楼)
        if (req.parentId() != null && req.parentId() != 0) {
            Comment parent = commentMapper.selectById(req.parentId());

            // 只有当父评论存在，且作者不是我自己时，才发通知
            if (parent != null && !parent.getUserId().equals(currentUserId)) {
                    String summary = req.content().length() > 20 ? req.content().substring(0, 20) + "..." : req.content();
                    String msgContent = "POST:" + post.getId() + ":回复了你的评论: " + summary;

                    messageService.createSystemNotification(currentUserId, parent.getUserId(), "COMMENT", msgContent);

            }
        }
    }

    // 2. 删除评论(区分根评论和子评论)
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long currentUserId = SecurityUtils.getUserId();
        Comment target = this.getById(commentId);
        if (target == null) {
            throw new BusinessException("评论不存在");
        }

        User currentUser = userMapper.selectById(currentUserId);
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isAuthor = target.getUserId().equals(currentUserId);

        if (!isAuthor && !isAdmin) {
            throw new BusinessException(403, "没有权限删除该评论");
        }

        // 准备要删除的ID列表
        List<Long> deleteIds = new ArrayList<>();
        deleteIds.add(commentId);

        // 如果是根评论 (rootId=0)，需要连带删除它下面的所有子评论
        if (target.getRootId() == 0) {
            List<Comment> children = this.list(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getRootId, commentId));
            if (!children.isEmpty()) {
                deleteIds.addAll(children.stream().map(Comment::getId).collect(Collectors.toList()));
            }
        }

        // 批量删除
        this.removeBatchByIds(deleteIds);

        // 更新帖子回复数
        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("reply_count = CASE WHEN reply_count >= " + deleteIds.size() +
                        " THEN reply_count - " + deleteIds.size() + " ELSE 0 END")
                .eq(Post::getId, target.getPostId());
        postMapper.update(null, updateWrapper);
    }

    // 3. 获取某帖子的评论列表
    public List<CommentVO> getCommentsByPostId(Long postId) {
        // A. 只查根评论
        List<Comment> rootComments = this.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getRootId, 0)
                .orderByDesc(Comment::getCreateTime)); // 根评论一般按时间倒序或热度

        if (rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        return convertToVOList(rootComments, true);
    }

    // 4. 获取某个根评论下的子评论列表 (查 rootId = ?)
    public List<CommentVO> getSubComments(Long rootId) {
        // A. 查子评论 (一般按时间正序，楼层感)
        List<Comment> subComments = this.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getRootId, rootId)
                .orderByAsc(Comment::getCreateTime));

        return convertToVOList(subComments, false);
    }

    // --- 辅助方法：统一转换 VO ---
    private List<CommentVO> convertToVOList(List<Comment> comments, boolean countChildren) {
        if (comments.isEmpty()) return new ArrayList<>();

        // 1. 批量查作者信息
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 2. 如果是一级评论，需要统计每条评论有多少个回复 (childCount)
        //    MVP做法：循环查 count (性能一般，但写起来最快。优化做法是 GROUP BY 查询)
        return comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);

            // 填充作者
            User author = userMap.get(c.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }

            // 填充 childCount
            if (countChildren) {
                Long count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getRootId, c.getId()));
                vo.setChildCount(count);
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
