package com.yijinchan.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageVO implements Serializable {

    private WebSocketVO formUser;
    private WebSocketVO toUser;
    private Long teamId;
    private String text;
    private Boolean isMy = false;
    private Integer chatType;
    private Boolean isAdmin = false;
    private String createTime;
}
