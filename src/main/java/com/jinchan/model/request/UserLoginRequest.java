package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "用户登录请求参数")
public class UserLoginRequest implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = -1890165808668317793L;
    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    private String userAccount;
    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码")
    private String userPassword;

}
