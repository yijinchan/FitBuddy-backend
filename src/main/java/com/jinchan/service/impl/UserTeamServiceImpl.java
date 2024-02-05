package com.jinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jinchan.mapper.UserTeamMapper;
import com.jinchan.model.domain.UserTeam;
import com.jinchan.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author jinchan
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-01-19 16:46:47
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

}




