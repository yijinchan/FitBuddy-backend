package com.jinchan.controller;

import com.jinchan.common.BaseResponse;
import com.jinchan.common.ErrorCode;
import com.jinchan.common.ResultUtils;
import com.jinchan.exception.BusinessException;
import com.jinchan.model.domain.User;
import com.jinchan.model.vo.UserVO;
import com.jinchan.service.FollowService;
import com.jinchan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ClassName: FollowController
 * Package: com.jinchan.controller
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/27 16:37
 */
@RestController
@RequestMapping("/follow")
@Api(tags = "关注管理模块")
public class FollowController {
    @Resource
    private FollowService followService;
    @Resource
    private UserService userService;
    @PostMapping("/{id}")
    @ApiOperation(value = "关注用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "关注用户id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> followUser(@PathVariable Long id, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        followService.followUser(id,loginUser.getId());
        return ResultUtils.success("ok");
    }

    @GetMapping("/fans")
    @ApiOperation(value = "获取粉丝")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<UserVO>> listFans(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<UserVO> userList = followService.listFans(loginUser.getId());
        return ResultUtils.success(userList);
    }

    /**
     * 获取我关注的用户
     * @param request request请求
     * @return  我关注的用户列表
     */
    @GetMapping("/my")
    @ApiOperation(value = "获取我关注的用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<UserVO>> listMyFollow(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<UserVO> userVOList = followService.listMyFollow(loginUser.getId());
        return ResultUtils.success(userVOList);
    }
}
