package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yijinchan.mapper.UserTeamMapper;
import com.yijinchan.model.domain.UserTeam;
import com.yijinchan.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author Zhang Bridge
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-01-19 16:46:47
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




