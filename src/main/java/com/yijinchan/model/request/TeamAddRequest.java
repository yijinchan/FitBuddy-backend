package com.yijinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: TeamAddRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 13:45
 */
@Data
@ApiModel(value = "队伍添加请求参数")
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1006159196178009294L;
    /**
     * 名称的私有变量，存储名称信息
     */
    @ApiModelProperty(value = "队伍名称")
    private String name;
    /**
     * 描述的私有变量，存储描述信息
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 最大数量的私有变量，存储最大数量信息
     */
    @ApiModelProperty(value = "最大人数")
    private Integer maxNum;
    /**
     * 到期时间的私有变量，存储到期时间信息
     */
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;
    /**
     * 状态的私有变量，存储状态信息
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty(value = "队伍状态，0 - 公开，1 - 私有，2 - 加密")
    private Integer status;
    /**
     * 密码的私有变量，存储密码信息
     */
    @ApiModelProperty(value = "密码")
    private String password;

}
