package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.model.domain.Follow;
import com.yijinchan.model.domain.User;
import com.yijinchan.service.FollowService;
import com.yijinchan.mapper.FollowMapper;
import com.yijinchan.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Zhang Bridge
* @description 针对表【follow】的数据库操作Service实现
* @createDate 2024-01-27 16:33:20
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
    implements FollowService{

    @Resource
    @Lazy
    private UserService userService;

    @Override
    public void followUser(Long followUserId, Long userId) {
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowUserId, followUserId).eq(Follow::getUserId, userId);
        long count = this.count(followLambdaQueryWrapper);
        if (count == 0) {
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            this.save(follow);
        } else {
            this.remove(followLambdaQueryWrapper);
        }
    }
    @Override
    public List<User> listUserFollowedMe(Long userId) {
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowUserId,userId);
        List<Follow> list = this.list(followLambdaQueryWrapper);
        return list.stream().map((follow -> userService.getById(follow.getUserId()))).collect(Collectors.toList());
    }
}




