package com.jinchan.constant;

/**
 * 聊天常量类
 *@author jinchan
 *@data 2024/2/5
 */
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
    /**
     * 聊天大厅缓存
     */
    String CACHE_CHAT_HALL = "FitBuddy:chat:chat_records:chat_hall";
    /**
     * 私聊缓存
     */
    String CACHE_CHAT_PRIVATE = "FitBuddy:chat:chat_records:chat_private:";
    /**
     * 队伍聊天缓存
     */
    String CACHE_CHAT_TEAM = "FitBuddy:chat:chat_records:chat_team:";

}