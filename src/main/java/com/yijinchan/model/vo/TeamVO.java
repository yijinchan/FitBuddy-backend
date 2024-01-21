package com.yijinchan.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: TeamVO
 * Package: com.yijinchan.model.vo
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 17:54
 */
@Data
@ApiModel(value = "队伍用户返回")
public class TeamVO implements Serializable {
    private static final long serialVersionUID = 2625797734453117209L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 队伍名称
     */
    @ApiModelProperty(value = "队伍名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 最大人数
     */
    @ApiModelProperty(value = "最大人数")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty(value = "0 - 公开，1 - 私有，2 - 加密")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    @ApiModelProperty(value = "创建人用户信息")
    private UserVO createUser;

    /**
     * 已加入的用户数
     */
    @ApiModelProperty(value = "已加入的用户数")
    private Long hasJoinNum;

    /**
     * 是否已加入队伍
     */
    @ApiModelProperty(value = "是否已加入队伍")
    private boolean hasJoin = false;
}

