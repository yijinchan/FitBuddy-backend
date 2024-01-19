package com.yijinchan.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -1890165808668317793L;

    private String userAccount;

    private String userPassword;

}
