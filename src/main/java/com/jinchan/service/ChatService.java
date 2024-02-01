package com.jinchan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.Chat;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.ChatRequest;
import com.jinchan.model.vo.ChatMessageVO;

import java.util.Date;
import java.util.List;

/**
* @author jinchan
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2024-01-31 18:29:35
*/
public interface ChatService extends IService<Chat> {

    List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int privateChat, User loginUser);

    List<ChatMessageVO> getCache(String redisKey, String id);

    void saveCache(String redisKey, String id, List<ChatMessageVO> ChatMessageVOs);

    ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);

    void deleteKey(String key, String id);

    List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int teamChat, User loginUser);

    List<ChatMessageVO> getHallChat(int hallChat, User loginUser);
}
