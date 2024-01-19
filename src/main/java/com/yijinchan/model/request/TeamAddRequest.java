package com.yijinchan.model.request;

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
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1006159196178009294L;
    /**
     * 名称的私有变量，存储名称信息
     */
    private String name;
    /**
     * 描述的私有变量，存储描述信息
     */
    private String description;
    /**
     * 最大数量的私有变量，存储最大数量信息
     */
    private Integer maxNum;
    /**
     * 到期时间的私有变量，存储到期时间信息
     */
    private Date expireTime;
    /**
     * 状态的私有变量，存储状态信息
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
    /**
     * 密码的私有变量，存储密码信息
     */
    private String password;

}
