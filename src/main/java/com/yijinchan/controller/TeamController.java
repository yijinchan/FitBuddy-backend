package com.yijinchan.controller;

import cn.hutool.core.bean.BeanUtil;
import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.Team;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.TeamAddRequest;
import com.yijinchan.model.request.TeamUpdateRequest;
import com.yijinchan.service.TeamService;
import com.yijinchan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @PostMapping
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtil.copyProperties(teamAddRequest, team);
        return ResultUtils.success(teamService.addTeam(team, loginUser));
    }

    @DeleteMapping("/{id}")
    public BaseResponse deleteTeam(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (result) {
            return ResultUtils.success("删除成功");
        } else {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
    }

    @PutMapping
    public BaseResponse<Long> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtil.copyProperties(teamUpdateRequest, team);
        boolean updateById = teamService.updateById(team);
        if (updateById) {
            return ResultUtils.success(team.getId());
        } else {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
    }

    @GetMapping("/{id}")
    public BaseResponse<Team> getById(@PathVariable Long id) {
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(teamService.getById(id));
    }
    @GetMapping("/list")
    public BaseResponse listTeam() {
        return ResultUtils.success(teamService.list(null));
    }
}
