package com.yijinchan.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: UserVO
 * Package: com.yijinchan.model.vo
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 17:55
 */
@Data
@ApiModel(value = "用户返回")
public class UserVO implements Serializable {

    private static final long serialVersionUID = -2172218520043068828L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String username;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String avatarUrl;

    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private Integer gender;

    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 标签列表 json
     */
    @ApiModelProperty(value = "标签列表 json")
    private String tags;

    /**
     * 状态 0 - 正常
     */
    @ApiModelProperty(value = "状态 0 - 正常")
    private Integer userStatus;

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
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    @ApiModelProperty(value = "用户角色")
    private Integer userRole;

}

