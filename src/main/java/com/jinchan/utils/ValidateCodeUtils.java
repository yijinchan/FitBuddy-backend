package com.jinchan.utils;

import java.util.Random;

import static com.jinchan.constant.SystemConstants.MAXIMUM_VERIFICATION_CODE_NUM;
import static com.jinchan.constant.SystemConstants.MINIMUM_VERIFICATION_CODE_NUM;

/**
 * ClassName: 验证码生成工具
 * Package: com.jinchan.utils
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/22 14:00
 */
public final class ValidateCodeUtils {
    private ValidateCodeUtils() {
    }
    /**
     * 生成验证代码
     *
     * @param
     * @return
     */
    public static Integer generateValidateCode() {
        int code = new Random().nextInt(MAXIMUM_VERIFICATION_CODE_NUM); //生成随机数，最大为999999
        if (code < MINIMUM_VERIFICATION_CODE_NUM) {
            code = code + MINIMUM_VERIFICATION_CODE_NUM; //保证随机数为6位数字
        }
        return code;
    }

}
