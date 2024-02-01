package com.jinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.Team;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.*;
import com.jinchan.model.vo.TeamVO;
import com.jinchan.model.vo.UserVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    TeamVO getTeam(Long teamId, Long userId);

    Page<TeamVO> listMyJoin(long currentPage, TeamQueryRequest teamQuery);

    List<UserVO> getTeamMember(Long teamId, Long userId);

    List<TeamVO> listAllMyJoin(Long id);

    void changeCoverImage(TeamCoverUpdateRequest request, Long userId, boolean admin);
    void kickOut(Long teamId, Long userId, Long loginUserId,boolean admin);

    Page<TeamVO> listMyCreate(long currentPage, Long userId);
}
