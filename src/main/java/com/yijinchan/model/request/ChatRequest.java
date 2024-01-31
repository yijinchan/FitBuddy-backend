package com.yijinchan.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {
    /**
     * 队伍聊天室id
     */
    private Long teamId;

    /**
     * 接收消息id
     */
    private Long toId;
}
