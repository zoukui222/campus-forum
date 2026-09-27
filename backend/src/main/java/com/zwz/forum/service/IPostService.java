package com.zwz.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zwz.forum.dto.PostCreateRequest;
import com.zwz.forum.dto.PostUpdateRequest;
import com.zwz.forum.entity.Post;
import com.zwz.forum.vo.PostVO;

/**
 * <p>
 * 帖子表 服务类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
public interface IPostService extends IService<Post> {
    // 发布帖子
    void createPost(PostCreateRequest req);
    // 分页查询帖子列表
    Page<PostVO> getPostList(Page<Post> page, Integer boardId,Long userId,String keyword);
    // 获取帖子详情
    PostVO getPostDetail(Long id);
    // 删除帖子
    void deletePost(Long postId);
    // 编辑帖子
    void updatePost(PostUpdateRequest req);
    // 管理员：切换置顶状态
    void toggleTop(Long postId);
    // 管理员：直接删除 (不需要校验作者)
    void deletePostByAdmin(Long postId);
}
