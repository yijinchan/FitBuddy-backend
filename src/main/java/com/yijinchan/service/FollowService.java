package com.yijinchan.service;

import com.yijinchan.model.domain.Follow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.User;

import java.util.List;

/**
* @author Zhang Bridge
* @description 针对表【follow】的数据库操作Service
* @createDate 2024-01-27 16:33:20
*/
public interface FollowService extends IService<Follow> {
    void followUser(Long followUserId, Long userId);

    List<User> listUserFollowedMe(Long userId);
}
