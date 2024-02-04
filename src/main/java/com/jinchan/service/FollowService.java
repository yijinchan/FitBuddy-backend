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
    void followUser(Long followUserId, Long userId);

    List<UserVO> listFans(Long userId);

    List<UserVO> listMyFollow(Long userId);

    Page<UserVO> pageFans(Long id, String currentPage);

    Page<UserVO> pageMyFollow(Long id, String currentPage);
    UserVO getUserFollowInfo(User user, long userId);
}
