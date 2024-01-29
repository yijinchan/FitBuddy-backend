package com.yijinchan.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.Team;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.domain.UserTeam;
import com.yijinchan.model.request.*;
import com.yijinchan.model.vo.TeamVO;
import com.yijinchan.service.TeamService;
import com.yijinchan.service.UserService;
import com.yijinchan.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;

/**
 * ClassName: TeamController
 * Package: com.yijinchan.controller
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 13:35
 */
@RestController
@RequestMapping("/team")
@Slf4j
@Api(tags = "队伍管理模块")
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    @ApiOperation(value = "添加队伍")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamAddRequest", value = "队伍添加请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtil.copyProperties(teamAddRequest, team);
        return ResultUtils.success(teamService.addTeam(team, loginUser));
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新队伍")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamUpdateRequest", value = "队伍更新请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "队伍id")})
    public BaseResponse<TeamVO> getTeamById(@PathVariable Long id,HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(teamService.getTeam(id,loginUser.getId()));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取队伍列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQueryRequest", value = "队伍查询请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Page<TeamVO>> listTeams(long currentPage, TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        Page<TeamVO> teamVOPage = teamService.listTeams(currentPage, teamQueryRequest, userService.isAdmin(loginUser));
        List<TeamVO> teamList = teamVOPage.getRecords();
        teamList.forEach((team) -> {
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, team.getId());
            long hasJoinNum = userTeamService.count(userTeamLambdaQueryWrapper);
            team.setHasJoinNum(hasJoinNum);
        });
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVOPage);
        return getUserJoinedList(loginUser, finalPage);
    }


    @PostMapping("/join")
    @ApiOperation(value = "加入队伍")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamJoinRequest", value = "加入队伍请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    @ApiOperation(value = "退出队伍")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamQuitRequest", value = "退出队伍请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除队伍")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamDeleteRequest", value = "队伍删除请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (teamDeleteRequest == null || teamDeleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = teamDeleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean result = teamService.deleteTeam(id, loginUser,isAdmin);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQuery 获取队伍请求参数
     * @param request   HTTP请求对象
     * @return 队伍用户信息列表
     */
    @GetMapping("/list/my/create")
    @ApiOperation(value = "获取我创建的队伍")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "HTTP请求对象")
    })
    public BaseResponse<Page<TeamVO>> listMyCreateTeams(long currentPage, TeamQueryRequest teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        Page<TeamVO> teamVOPage = teamService.listTeams(currentPage, teamQuery, true);
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVOPage);
        return getUserJoinedList(loginUser, finalPage);
    }

    /**
     * 获取我加入的队伍
     *
     * @param currentPage 当前页面数
     * @param teamQuery 队伍查询请求参数
     * @param request HTTP请求对象
     * @return 队伍用户信息列表
     */
    @GetMapping("/list/my/join")
    @ApiOperation(value = "获取我加入的队伍")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "HTTP请求对象")
    })
    public BaseResponse<Page<TeamVO>> listMyJoinTeams(long currentPage, TeamQueryRequest teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);

        // 将队伍根据队伍ID进行分组
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));

        // 获取队伍ID列表
        List<Long> idList = new ArrayList<>(listMap.keySet());
        if (idList.isEmpty()){
            return ResultUtils.success(new Page<TeamVO>());
        }
        // 设置队伍查询参数的队伍ID列表
        teamQuery.setIdList(idList);

        // 获取队伍信息列表
        Page<TeamVO> teamVOPage = teamService.listMyJoin(currentPage, teamQuery);

        // 获取加入了的队伍信息列表
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVOPage);

        // 返回队伍用户信息列表
        return getUserJoinedList(loginUser, finalPage);
    }


    /**
     * 获取用户已加入的队伍列表
     *
     * @param loginUser 当前用户
     * @param teamPage  teamPage 队伍列表
     * @return BaseResponse<List < TeamVO>> 返回队伍列表及状态信息
     */
    private BaseResponse<Page<TeamVO>> getUserJoinedList(User loginUser, Page<TeamVO> teamPage) {
        try {
            // 将队伍列表转换为队伍id列表
            List<TeamVO> teamList = teamPage.getRecords();
            List<Long> teamIdList = teamList.stream().map(TeamVO::getId).collect(Collectors.toList());
            // 判断当前用户已加入的队伍
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId()).in(UserTeam::getTeamId, teamIdList);
            // 获取用户已加入的队伍
            List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
            // 将队伍id列表转换为集合
            Set<Long> joinedTeamIdList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            // 遍历队伍列表，设置队伍是否已加入的标志
            teamList.forEach(team -> {
                team.setHasJoin(joinedTeamIdList.contains(team.getId()));
            });
            teamPage.setRecords(teamList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回队伍列表及状态信息
        return ResultUtils.success(teamPage);
    }

    /**
     * 获取具有加入人数的团队
     * @param teamVOPage 分页对象，包含团队信息
     * @return 分页对象，包含具有加入人数的团队信息
     */
    private Page<TeamVO> getTeamHasJoinNum(Page<TeamVO> teamVOPage) {
        List<TeamVO> teamList = teamVOPage.getRecords();
        teamList.forEach((team) -> {
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, team.getId());
            long hasJoinNum = userTeamService.count(userTeamLambdaQueryWrapper);
            team.setHasJoinNum(hasJoinNum);
        });
        teamVOPage.setRecords(teamList);
        return teamVOPage;
    }


}
