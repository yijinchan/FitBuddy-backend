package com.yijinchan.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WebSocketVO implements Serializable {

    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatarUrl;
}
