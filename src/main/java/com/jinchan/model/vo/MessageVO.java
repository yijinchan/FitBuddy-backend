package com.jinchan.model.vo;

import com.jinchan.model.domain.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息视图对象
 * @author jinchan
 * @date 2024/2/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "消息返回")
public class MessageVO extends Message {

    private static final long serialVersionUID = 439544534900405741L;
    /**
     * 从用户
     */
    @ApiModelProperty(value = "发送用户id")
    private UserVO fromUser;
    /**
     * 博客
     */
    @ApiModelProperty(value = "博客")
    private BlogVO blog;
    /**
     * 评论
     */
    @ApiModelProperty(value = "评论")
    private BlogCommentsVO comment;
}
