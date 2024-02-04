package com.jinchan.constant;

/**
 * 好友常量类
 * @author jinchan
 * @date 2024/2/1
 */
public final class FriendConstants {
    private FriendConstants() {
    }

    /**
     * 默认状态 未处理
     */
    public static final int DEFAULT_STATUS = 0;
    /**
     * 已同意
     */
    public static final int AGREE_STATUS = 1;
    /**
     * 已过期
     */
    public static final int EXPIRED_STATUS = 2;

    /**
     * 撤销
     */
    public static final int REVOKE_STATUS = 3;
    /**
     * 未读
     */
    public static final int NOT_READ = 0;

    /**
     * 已读
     */
    public static final int READ = 1;
    /**
     * 最大备注长度
     */
    public static final int MAXIMUM_REMARK_LENGTH = 120;
    /**
     * 最长申请时间
     */
    public static final int MAXIMUM_APPLY_TIME = 3;
}
