package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: TeamQuitRequest
 * Package: com.jinchan.model.request
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/19 22:45
 */
@Data
@ApiModel(value = "退出队伍请求参数")
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -4869820851943218319L;
    @ApiModelProperty(value = "队伍id")
    private long teamId;
}
