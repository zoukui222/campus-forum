package com.zwz.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwz.forum.dto.CommentCreateRequest;
import com.zwz.forum.entity.Comment;
import com.zwz.forum.vo.CommentVO;

import java.util.List;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
public interface ICommentService extends IService<Comment> {
    void createComment(CommentCreateRequest req);
    void deleteComment(Long commentId);
    List<CommentVO> getCommentsByPostId(Long postId);
    List<CommentVO> getSubComments(Long rootId);
}
