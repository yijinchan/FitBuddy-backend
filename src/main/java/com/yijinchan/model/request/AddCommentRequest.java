package com.yijinchan.model.request;

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
public class AddCommentRequest implements Serializable {
    private static final long serialVersionUID = 5175395873589440448L;

    private Long blogId;

    private String content;
}
