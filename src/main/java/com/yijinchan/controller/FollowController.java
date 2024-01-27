package com.yijinchan.controller;

import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.User;
import com.yijinchan.service.FollowService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;

/**
 * ClassName: FollowController
 * Package: com.yijinchan.controller
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/27 16:37
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private FollowService followService;

    @PostMapping("/{id}")
    public BaseResponse<String> followUser(@PathVariable Long id, HttpServletRequest request){
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        followService.followUser(id,loginUser.getId());
        return ResultUtils.success("ok");
    }
}
