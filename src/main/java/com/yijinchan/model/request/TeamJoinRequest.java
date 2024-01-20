package com.yijinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "加入队伍请求参数")
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 4050574328988680436L;

    /**
     * id
     */
    @ApiModelProperty(value = "队伍id")
    private Long teamId;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
}
