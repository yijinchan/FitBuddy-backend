package com.yijinchan.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: BlogCommentsVO
 * Package: com.yijinchan.model.vo
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/26 22:51
 */
@Data
public class BlogCommentsVO implements Serializable {
    private static final long serialVersionUID = 4384362431081902087L;

    private UserVO commentUser;
}
