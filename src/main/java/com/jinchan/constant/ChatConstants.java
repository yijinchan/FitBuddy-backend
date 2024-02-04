package com.jinchan.constant;

/**
 * 聊天常量类
 *@author jinchan
 *@data 2024/2/5
 */
public final class ChatConstants {
    private ChatConstants() {
    }

    /**
     * 私聊
     */
    public static final int PRIVATE_CHAT = 1;

    /**
     * 队伍群聊
     */

    public static final int TEAM_CHAT = 2;
    /**
     * 大厅聊天
     */
    public static final int HALL_CHAT = 3;

    /**
     * 缓存聊天大厅
     */
    public static final String CACHE_CHAT_HALL = "fitbuddy:chat:chat_records:chat_hall";

    /**
     * 缓存私人聊天
     */
    public static final String CACHE_CHAT_PRIVATE = "fitbuddy:chat:chat_records:chat_private:";

    /**
     * 缓存聊天团队
     */
    public static final String CACHE_CHAT_TEAM = "fitbuddy:chat:chat_records:chat_team:";
}