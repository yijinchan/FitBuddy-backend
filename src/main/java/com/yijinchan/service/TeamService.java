package com.yijinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yijinchan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.TeamQueryRequest;
import com.yijinchan.model.request.TeamJoinRequest;
import com.yijinchan.model.request.TeamQuitRequest;
import com.yijinchan.model.request.TeamUpdateRequest;
import com.yijinchan.model.vo.TeamVO;
import org.springframework.transaction.annotation.Transactional;

/**
* @author jinchan
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-01-18 20:53:25
*/
public interface TeamService extends IService<Team> {
    @Transactional(rollbackFor = Exception.class)
    long addTeam(Team team, User loginUser);

    Page<TeamVO> listTeams(long currentPage, TeamQueryRequest teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
    @Transactional(rollbackFor = Exception.class)
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser, boolean isAdmin);

    TeamVO getTeam(Long teamId,Long userId);

    Page<TeamVO> listMyJoin(long currentPage, TeamQueryRequest teamQuery);
}
