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

    String USER_UPDATE_PHONE_KEY = "FitBuddy:user:update:phone:";

    Long USER_UPDATE_PHONE_TTL = 15L;

    String USER_UPDATE_EMAIL_KEY = "FitBuddy:user:update:email:";

    Long USER_UPDATE_EMAIl_TTL = 15L;

    String USER_FORGET_PASSWORD_KEY = "super:user:forget:";
    Long USER_FORGET_PASSWORD_TTL = 15L;
}
