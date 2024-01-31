package com.yijinchan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.Chat;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.ChatRequest;
import com.yijinchan.model.vo.MessageVO;

import java.util.Date;
import java.util.List;

/**
* @author yijinchang
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2024-01-31 18:29:35
*/
public interface ChatService extends IService<Chat> {

    List<MessageVO> getPrivateChat(ChatRequest chatRequest, int privateChat, User loginUser);

    List<MessageVO> getCache(String redisKey, String id);

    void saveCache(String redisKey, String id, List<MessageVO> messageVos);

    MessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);

    void deleteKey(String key, String id);
}
