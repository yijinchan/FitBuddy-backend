package com.yijinchan.constant;

public interface ChatConstants {
    /**
     * 私聊
     */
    int PRIVATE_CHAT = 1;
    /**
     * 队伍群聊
     */
    int TEAM_CHAT = 2;
    /**
     * 大厅聊天
     */
    int HALL_CHAT = 3;

    String CACHE_CHAT_HALL = "FitBuddy:chat:chat_records:chat_hall";

    String CACHE_CHAT_PRIVATE = "FitBuddy:chat:chat_records:chat_private:";

    String CACHE_CHAT_TEAM = "FitBuddy:chat:chat_records:chat_team:";

}