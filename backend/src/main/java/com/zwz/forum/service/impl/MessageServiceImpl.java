package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.entity.Message;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.MessageMapper;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.IMessageService;
import com.zwz.forum.utils.SecurityUtils;
import com.zwz.forum.vo.ConversationVO;
import com.zwz.forum.vo.MessageVO;
import com.zwz.forum.vo.UnreadCountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息通知表 服务实现类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    private final UserMapper userMapper;

    // 1. 发送私信
    @Override
    @Transactional
    public void sendPrivateMessage(Long toUserId, String content) {
        Long fromUserId = SecurityUtils.getUserId();

        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能给自己发私信");
        }

        // 校验接收者是否存在
        User toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException("用户不存在");
        }

        createSystemNotification(fromUserId, toUserId, "CHAT", content);
    }

    // 2. 创建通知 (这是一个通用内部方法，供 CommentService 调用)
    @Override
    @Transactional
    public void createSystemNotification(Long fromId, Long toId, String type, String content) {
        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setType(type);
        message.setContent(content);
        message.setIsRead(false);
        this.save(message);
    }

    // 3. 获取我的消息列表 (支持分页)
    @Override
    public Page<MessageVO> getMyMessages(Page<Message> page, String type) {
        Long currentUserId = SecurityUtils.getUserId();

        // 查询条件：发给我的，且类型匹配（如果没传 type 就不卡类型）
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToId, currentUserId);
        wrapper.eq(type != null, Message::getType, type);
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = this.page(page, wrapper);

        if (messagePage.getRecords().isEmpty()) {
            return new Page<>();
        }

        // 填充发送者信息
        Set<Long> senderIds = messagePage.getRecords().stream()
                .map(Message::getFromId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<MessageVO> voList = messagePage.getRecords().stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            User sender = userMap.get(msg.getFromId());
            if (sender != null) {
                vo.setFromNickname(sender.getNickname());
                vo.setFromAvatar(sender.getAvatar());
            } else {
                vo.setFromNickname("系统通知"); // fromId=0 的情况
            }
            return vo;
        }).collect(Collectors.toList());

        Page<MessageVO> resultPage = new Page<>();
        BeanUtils.copyProperties(messagePage, resultPage, "records");
        resultPage.setRecords(voList);

        return resultPage;
    }

    // 4. 标记已读
    @Override
    public void markAsRead(Long id) {
        // 只能标记发给自己的
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Message::getIsRead, true)
                .eq(Message::getId, id)
                .eq(Message::getToId, SecurityUtils.getUserId());
        this.update(null, wrapper);
    }

    // 5. 获取未读数量 (用于前端小红点)
    @Override
    public Long getUnreadCount() {
        return this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToId, SecurityUtils.getUserId())
                .eq(Message::getIsRead, false));
    }

    // 6. 获取会话列表 (左侧栏)
    @Override
    public List<ConversationVO> getConversationList() {
        Long currentUserId = SecurityUtils.getUserId();

        // A. 查出所有与我相关的私信 (发给我的 或 我发出的)
        // 注意：这里只查 type=CHAT (私信)，不查系统通知
        // 按时间倒序，保证遍历时先拿到最新消息
        List<Message> allMessages = this.list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromId, currentUserId).or().eq(Message::getToId, currentUserId))
                .eq(Message::getType, "CHAT")
                .orderByDesc(Message::getCreateTime));

        // B. 内存分组逻辑
        // Map<聊天对象ID, 会话VO>
        Map<Long, ConversationVO> map = new LinkedHashMap<>(); // 有序Map，保持最新

        for (Message msg : allMessages) {
            // 确定"对方"是谁
            Long partnerId = msg.getFromId().equals(currentUserId) ? msg.getToId() : msg.getFromId();

            ConversationVO vo = map.get(partnerId);
            if (vo == null) {
                // 第一次遇到这个对象，说明这条是最新消息
                vo = new ConversationVO();
                vo.setUserId(partnerId);
                vo.setLatestMessage(msg.getContent());
                vo.setLatestTime(msg.getCreateTime());
                vo.setUnreadCount(0);
                map.put(partnerId, vo);
            }

            // 统计未读数：如果是我收到的，且未读，就+1
            if (msg.getToId().equals(currentUserId) && !msg.getIsRead()) {
                vo.setUnreadCount(vo.getUnreadCount() + 1);
            }
        }

        // C. 填充用户信息 (批量查询，避免循环查库)
        if (!map.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(map.keySet());
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

            for (ConversationVO vo : map.values()) {
                User u = userMap.get(vo.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatar(u.getAvatar());
                } else {
                    vo.setNickname("未知用户"); // 防止用户注销
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    // 7. 获取与某人的详细聊天记录 (右侧栏)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MessageVO> getChatHistory(Long partnerId) {
        Long currentUserId = SecurityUtils.getUserId();

        // 查询条件：(我发给他的) OR (他发给我的)
        // 按时间正序排列 (老消息在上面，新消息在下面)
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w ->
                w.nested(i -> i.eq(Message::getFromId, currentUserId).eq(Message::getToId, partnerId))
                        .or()
                        .nested(i -> i.eq(Message::getFromId, partnerId).eq(Message::getToId, currentUserId))
        );
        wrapper.eq(Message::getType, "CHAT");
        wrapper.orderByAsc(Message::getCreateTime);

        List<Message> messages = this.list(wrapper);

        // 将查询出来的【对方发给我的】且【未读】的消息批量设置为已读

        List<Long> unreadIds = messages.stream()
                // 过滤条件：发件人是对方 AND 状态是未读
                .filter(msg -> msg.getFromId().equals(partnerId) && !msg.getIsRead())
                // 收集 ID
                .map(Message::getId)
                .collect(Collectors.toList());

        if (!unreadIds.isEmpty()) {
            // 执行批量更新
            LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Message::getIsRead, true) // 设置为已读
                    .in(Message::getId, unreadIds); // 批量 ID
            this.update(null, updateWrapper);
        }
        // ==================================================================

        // 转换 VO (这部分保持不变)
        User partner = userMapper.selectById(partnerId);
        User me = userMapper.selectById(currentUserId);

        // 如果用户注销了，给个默认信息防止报错
        if (partner == null) {
            partner = new User();
            partner.setNickname("未知用户");
        }
        if (me == null) {
            // 理论上不可能，因为我自己正在访问，但安全起见
            me = new User();
            me.setNickname("我");
        }

        final User finalPartner = partner;
        final User finalMe = me;
        return messages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            if (msg.getFromId().equals(partnerId)) {
                vo.setFromNickname(finalPartner.getNickname());
                vo.setFromAvatar(finalPartner.getAvatar());
            } else {
                vo.setFromNickname(finalMe.getNickname());
                vo.setFromAvatar(finalMe.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // 批量标记已读
    @Override
    public void batchMarkAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 核心逻辑：UPDATE sys_message SET is_read = 1 WHERE id IN (...) AND to_id = currentUserId
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Message::getIsRead, true)
                .in(Message::getId, ids)
                .eq(Message::getToId, SecurityUtils.getUserId()); // 只能标记发给自己的

        this.update(null, wrapper);
    }

    // 全部已读系统和回复消息
    @Override
    public void markAllAsRead() {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Message::getIsRead, true)
                .eq(Message::getToId, SecurityUtils.getUserId())
                .eq(Message::getIsRead, false) // 只更新未读的
                .in(Message::getType,Arrays.asList("COMMENT","SYSTEM"));

        this.update(null, wrapper);
    }

    @Override
    public UnreadCountVO getUnreadCountByType() {
        Long currentUserId = SecurityUtils.getUserId();

        // 使用 QueryWrapper 进行分组查询
        // SELECT type, count(*) as count FROM sys_message WHERE to_id = ? AND is_read = 0 GROUP BY type
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.select("type", "count(*) as count")
                .eq("to_id", currentUserId)
                .eq("is_read", 0)
                .groupBy("type");

        // map结果示例: [{type=CHAT, count=5}, {type=COMMENT, count=2}]
        List<Map<String, Object>> list = this.listMaps(query);

        // 初始化计数器
        long chatCount = 0;
        long commentCount = 0;
        long systemCount = 0;

        // 遍历结果集填空
        for (Map<String, Object> map : list) {
            String type = (String) map.get("type");
            // 注意：count(*) 返回的类型可能是 Long 或 Integer，取决于数据库驱动，转 String 再转 Long 最稳妥
            Long count = Long.parseLong(map.get("count").toString());

            if ("CHAT".equals(type)) {
                chatCount = count;
            } else if ("COMMENT".equals(type)) {
                commentCount = count;
            } else if ("SYSTEM".equals(type)) {
                systemCount = count;
            }
        }

        return UnreadCountVO.builder()
                .total(chatCount + commentCount + systemCount)
                .chat(chatCount)
                .comment(commentCount)
                .system(systemCount)
                .build();
    }
}
