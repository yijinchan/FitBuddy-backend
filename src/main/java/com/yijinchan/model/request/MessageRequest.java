package com.yijinchan.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageRequest implements Serializable {

    private Long toId;
    private Long teamId;
    private String text;
    private Integer chatType;
    private boolean isAdmin;
}
