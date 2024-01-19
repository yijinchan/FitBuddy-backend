package com.yijinchan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: TeamJoinRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 17:48
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 4050574328988680436L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
