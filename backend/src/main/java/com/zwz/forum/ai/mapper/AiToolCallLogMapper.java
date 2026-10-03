package com.zwz.forum.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zwz.forum.ai.entity.AiToolCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI 工具调用审计 Mapper
 */
@Mapper
public interface AiToolCallLogMapper extends BaseMapper<AiToolCallLog> {

    @Select("select * from ai_tool_call_log where chat_log_id = #{chatLogId} order by id")
    List<AiToolCallLog> selectByChatLogId(Long chatLogId);

    /** 各工具被调用次数与失败次数：看「模型到底爱用哪个工具」 */
    @Select("select tool_name as toolName, count(*) as total, " +
            "sum(case when success = 0 then 1 else 0 end) as failed " +
            "from ai_tool_call_log group by tool_name order by total desc")
    List<Map<String, Object>> selectToolUsageStats();
}
