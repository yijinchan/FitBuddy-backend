package com.jinchan.exception;

import com.jinchan.common.ErrorCode;

/**
 * 业务异常
 * @author jinchan
 * @data 2024/2/1
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -7999891598058747554L;
    /**
     * 错误码
     */
    private final int code;
    /**
     * 错误描述
     */
    private final String description;

    /**
     * 业务异常
     * @param message
     * @param code
     * @param description
     */
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    /**
     * 业务异常
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
    /**
     * 业务异常
     * @param errorCode
     * @param description
     */
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
