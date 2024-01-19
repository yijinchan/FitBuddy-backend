package com.yijinchan.service;

import com.yijinchan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.User;
import org.springframework.transaction.annotation.Transactional;

/**
* @author jinchan
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-01-18 20:53:25
*/
public interface TeamService extends IService<Team> {
    @Transactional(rollbackFor = Exception.class)
    long addTeam(Team team, User loginUser);

}
