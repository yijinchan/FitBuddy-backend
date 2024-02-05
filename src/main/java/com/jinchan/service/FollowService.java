package com.jinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.Follow;
import com.jinchan.model.domain.User;
import com.jinchan.model.vo.UserVO;

import java.util.List;

/**
* @author jinchan
* @description 针对表【follow】的数据库操作Service
* @createDate 2024-01-27 16:33:20
*/
public interface FollowService extends IService<Follow> {

    /**
     * 跟随用户
     *
     * @param followUserId 跟随用户id
     * @param userId       用户id
     */
    void followUser(Long followUserId, Long userId);

    /**
     * 列出粉丝
     *
     * @param userId 用户id
     * @return {@link List}<{@link UserVO}>
     */
    List<UserVO> listFans(Long userId);

    /**
     * 列出我关注
     *
     * @param userId 用户id
     * @return {@link List}<{@link UserVO}>
     */
    List<UserVO> listMyFollow(Long userId);

    /**
     * 分页我跟随
     *
     * @param userId      用户id
     * @param currentPage 当前页码
     * @return {@link Page}<{@link UserVO}>
     */
    Page<UserVO> pageMyFollow(Long userId, String currentPage);

    /**
     * 分页风扇
     *
     * @param userId      用户id
     * @param currentPage 当前页码
     * @return {@link Page}<{@link UserVO}>
     */
    Page<UserVO> pageFans(Long userId, String currentPage);

    /**
     * 获取用户关注信息
     *
     * @param user   用户
     * @param userId 用户id
     * @return {@link UserVO}
     */
    UserVO getUserFollowInfo(User user, long userId);
}
