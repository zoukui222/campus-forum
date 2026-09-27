package com.zwz.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zwz.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息通知表 Mapper 接口
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
