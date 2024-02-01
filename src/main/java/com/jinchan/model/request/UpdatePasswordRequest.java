package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ClassName: UpdatePasswordRequest
 * Package: com.jinchan.model.request
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/27 20:51
 */
@Data
@ApiModel(value = "密码更新请求")
public class UpdatePasswordRequest {
    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;
    /**
     * 验证码
     */
    @ApiModelProperty(value = "验证码")
    private String code;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码")
    private String confirmPassword;
}
