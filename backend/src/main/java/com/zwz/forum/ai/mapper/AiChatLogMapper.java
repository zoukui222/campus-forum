package com.zwz.forum.ai.mapper;

import com.zwz.forum.ai.entity.AiChatLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 会话记录 Mapper
 */
@Mapper
public interface AiChatLogMapper extends BaseMapper<AiChatLog> {

    @Select("select * from ai_chat_log order by id desc limit #{limit}")
    List<AiChatLog> selectRecent(Integer limit);

    @Select("select ifnull(sum(prompt_tokens), 0) from ai_chat_log")
    Long sumPromptTokens();

    @Select("select ifnull(sum(completion_tokens), 0) from ai_chat_log")
    Long sumCompletionTokens();
}
