package com.yijinchan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: TeamQuitRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 22:45
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -4869820851943218319L;

    private long teamId;
}
