package com.zwz.forum.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI Agent 专用只读查询
 *
 * 单独放一套查询而不复用 PostMapper：Agent 要的是「轻量摘要 + 关联板块名/作者昵称 + 限量」，
 * 与页面接口要的「全字段 + 分页」取向不同；分开写也避免为了 AI 需求去动页面接口的 SQL。
 * 所有查询都显式带 deleted = 0（本项目的逻辑删除是手写字段，不是 MP 的 @TableLogic）。
 */
@Mapper
public interface AiQueryMapper {

    /** 按关键词/板块检索帖子，返回轻量字段（不含正文） */
    @Select("<script>"
            + "select p.id as id, p.title as title, p.board_id as boardId, b.name as boardName, "
            + "       u.nickname as nickname, p.view_count as viewCount, p.reply_count as replyCount, "
            + "       p.is_top as isTop, p.create_time as createTime "
            + "from forum_post p "
            + "left join forum_board b on p.board_id = b.id "
            + "left join sys_user u on p.user_id = u.id "
            + "where p.deleted = 0 "
            + "<if test='keyword != null and keyword != \"\"'> "
            + "  and (p.title like concat('%', #{keyword}, '%') or p.content like concat('%', #{keyword}, '%')) "
            + "</if> "
            + "<if test='boardId != null'> and p.board_id = #{boardId} </if> "
            + "order by p.is_top desc, p.view_count desc "
            + "limit #{limit}"
            + "</script>")
    List<Map<String, Object>> searchPosts(@Param("keyword") String keyword,
                                          @Param("boardId") Integer boardId,
                                          @Param("limit") Integer limit);

    /** 单篇帖子详情（含正文） */
    @Select("select p.id as id, p.title as title, p.content as content, p.board_id as boardId, "
            + "       b.name as boardName, u.nickname as nickname, u.role as authorRole, "
            + "       p.view_count as viewCount, p.reply_count as replyCount, p.is_top as isTop, "
            + "       p.create_time as createTime "
            + "from forum_post p "
            + "left join forum_board b on p.board_id = b.id "
            + "left join sys_user u on p.user_id = u.id "
            + "where p.id = #{id} and p.deleted = 0")
    Map<String, Object> selectPostDetail(@Param("id") Long id);

    /** 某帖子的评论（按时间正序） */
    @Select("select c.id as id, c.content as content, c.user_id as userId, "
            + "       u.nickname as nickname, c.create_time as createTime "
            + "from forum_comment c "
            + "left join sys_user u on c.user_id = u.id "
            + "where c.post_id = #{postId} and c.deleted = 0 "
            + "order by c.create_time asc limit #{limit}")
    List<Map<String, Object>> selectComments(@Param("postId") Long postId, @Param("limit") Integer limit);

    /** 板块列表（供模型把自然语言里的板块名映射成 id） */
    @Select("select id as id, name as name, description as description from forum_board order by sort, id")
    List<Map<String, Object>> selectBoards();

    /** 站点汇总 */
    @Select("select "
            + " (select count(*) from forum_post where deleted = 0) as postCount, "
            + " (select ifnull(sum(view_count), 0) from forum_post where deleted = 0) as totalViews, "
            + " (select ifnull(sum(reply_count), 0) from forum_post where deleted = 0) as totalReplies, "
            + " (select count(*) from forum_comment where deleted = 0) as commentCount, "
            + " (select count(*) from sys_user where deleted = 0) as userCount, "
            + " (select count(*) from forum_board) as boardCount")
    Map<String, Object> selectForumStats();

    /** 各板块帖子数量分布 */
    @Select("select b.name as name, count(p.id) as value "
            + "from forum_board b left join forum_post p on p.board_id = b.id and p.deleted = 0 "
            + "group by b.id, b.name order by value desc, b.sort")
    List<Map<String, Object>> selectBoardStats();

    /** 活跃用户榜（按发帖数） */
    @Select("select u.nickname as nickname, count(p.id) as postCount, u.score as score "
            + "from sys_user u left join forum_post p on p.user_id = u.id and p.deleted = 0 "
            + "where u.deleted = 0 group by u.id, u.nickname, u.score "
            + "having count(p.id) > 0 order by postCount desc, u.score desc limit #{limit}")
    List<Map<String, Object>> selectActiveUsers(@Param("limit") Integer limit);
}
