package com.jinchan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.Friends;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.FriendAddRequest;
import com.jinchan.model.vo.FriendsRecordVO;

import java.util.List;
import java.util.Set;

/**
* @author jinchan
* @description 针对表【friends(好友申请管理表)】的数据库操作Service
* @createDate 2024-01-31 21:53:01
*/
public interface FriendsService extends IService<Friends> {

    /**
     * 好友申请
     *
     * @param loginUser        登录用户
     * @param friendAddRequest 好友添加请求
     * @return boolean
     */
    boolean addFriendRecords(User loginUser, FriendAddRequest friendAddRequest);

    /**
     * 查询出所有申请、同意记录
     *
     * @param loginUser 登录用户
     * @return {@link List}<{@link FriendsRecordVO}>
     */
    List<FriendsRecordVO> obtainFriendApplicationRecords(User loginUser);

    /**
     * 同意申请
     *
     * @param loginUser 登录用户
     * @param fromId    从…起id
     * @return boolean
     */
    boolean agreeToApply(User loginUser, Long fromId);

    /**
     * 撤销好友申请
     *
     * @param id        申请记录id
     * @param loginUser 登录用户
     * @return boolean
     */
    boolean canceledApply(Long id, User loginUser);

    /**
     * 获取我申请的记录
     *
     * @param loginUser 登录用户
     * @return {@link List}<{@link FriendsRecordVO}>
     */
    List<FriendsRecordVO> getMyRecords(User loginUser);

    /**
     * 获取未读记录条数
     *
     * @param loginUser 登录用户
     * @return int
     */
    int getRecordCount(User loginUser);

    /**
     * 读取纪录
     *
     * @param loginUser 登录用户
     * @param ids       ids
     * @return boolean
     */
    boolean toRead(User loginUser, Set<Long> ids);
}
