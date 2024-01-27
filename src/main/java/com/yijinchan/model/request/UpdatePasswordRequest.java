package com.yijinchan.model.request;

import lombok.Data;

/**
 * ClassName: UpdatePasswordRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/27 20:51
 */
@Data
public class UpdatePasswordRequest {
    private String phone;
    private String code;
    private String password;
    private String confirmPassword;
}
