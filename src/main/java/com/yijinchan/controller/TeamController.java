package com.yijinchan.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.Team;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.domain.UserTeam;
import com.yijinchan.model.request.*;
import com.yijinchan.model.vo.TeamUserVO;
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
@CrossOrigin(origins = {"http://localhost:5173"})
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
    @ApiImplicitParams({@ApiImplicitParam(name = "id",value = "队伍id")})
    public BaseResponse<Team> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(teamService.getById(id));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取队伍列表")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "teamQuery", value = "队伍查询请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return ResultUtils.success(teamService.listTeams(teamQueryRequest, userService.isAdmin(loginUser)));
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
        boolean result = teamService.deleteTeam(id, loginUser);
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
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQueryRequest teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }


    /**
     * 获取我加入的队伍
     *
     * @param teamQuery 获取队伍请求参数
     * @param request   HTTP请求对象
     * @return 返回队伍用户信息列表
     */
    @GetMapping("/list/my/join")
    @ApiOperation(value = "获取我加入的队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "HTTP请求对象")})
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQueryRequest teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

}
