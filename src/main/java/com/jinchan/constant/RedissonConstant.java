package com.jinchan.constant;

/**
 * Redisson常量
 * @Author jinchan
 * @Create 2024/2/1
 */
public interface RedissonConstant {
    /**
     * 申请锁
     */
    String APPLY_LOCK="FitBuddy:apply:";

    String DISBAND_EXPIRED_TEAM_LOCK = "FitBuddy:disbandTeam:lock";
    String USER_RECOMMEND_LOCK = "FitBuddy:user:recommend:lock";
}
