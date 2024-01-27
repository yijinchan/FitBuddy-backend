package com.yijinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: AddCommentRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/26 22:50
 */
@Data
@ApiModel(value = "添加博文评论请求")
public class AddCommentRequest implements Serializable {
    private static final long serialVersionUID = 5175395873589440448L;

    @ApiModelProperty(value = "博文id")
    private Long blogId;

    @ApiModelProperty(value = "评论")
    private String content;
}
