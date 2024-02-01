package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天请求
 * @Author jinchan
 * @date 2024/1/27 15:33
 */
@Data
@ApiModel(value = "聊天请求")
public class ChatRequest implements Serializable {
    /**
     * 队伍聊天室id
     */
    @ApiModelProperty(value = "队伍聊天室id")
    private Long teamId;

    /**
     * 接收消息id
     */
    @ApiModelProperty(value = "接收消息id")
    private Long toId;
}
