package com.jinchan.utils;

import java.util.Random;

/**
 * ClassName: 验证码生成工具
 * Package: com.jinchan.utils
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/22 14:00
 */
public class ValidateCodeUtils {
    /**
     * 生成验证代码
     *
     * @param length 长度
     * @return
     */
    public static Integer generateValidateCode(int length){
        Integer code =null;
        if(length == 4){
            code = new Random().nextInt(9999);//生成随机数，最大为9999
            if(code < 1000){
                code = code + 1000;//保证随机数为4位数字
            }
        }else if(length == 6){
            code = new Random().nextInt(999999);//生成随机数，最大为999999
            if(code < 100000){
                code = code + 100000;//保证随机数为6位数字
            }
        }else{
            throw new RuntimeException("只能生成4位或6位数字验证码");
        }
        return code;
    }

}
