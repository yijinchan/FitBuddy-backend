package com.yijinchan.constant;

/**
 * ClassName: RedisConstants
 * Package: com.yijinchan.constant
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/18 19:52
 */
public interface RedisConstants {
    String RECOMMEND_KEY = "FitBuddy:user:recommend";
    String REGISTER_CODE_KEY = "FitBuddy:register:";
    Long REGISTER_CODE_TTL = 15L;
}
