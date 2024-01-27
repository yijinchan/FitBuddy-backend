package com.yijinchan.controller;

import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.AddCommentRequest;
import com.yijinchan.model.vo.BlogCommentsVO;
import com.yijinchan.service.BlogCommentsService;
import com.yijinchan.service.BlogLikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;

/**
 * ClassName: BlogCommentsController
 * Package: com.yijinchan.controller
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/26 22:44
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "博文评论管理模块")
public class BlogCommentsController {
    /**
     * 博文评论服务
     */
    @Resource
    private BlogCommentsService blogCommentsService;

    /**
     *
     * @param addCommentRequest 博文评论添加请求
     * @param request request
     * @return BaseResponse
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "addCommentRequest", value = "博文评论添加请求"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> addComment(@RequestBody AddCommentRequest addCommentRequest, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (addCommentRequest.getBlogId() == null || StringUtils.isBlank(addCommentRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        blogCommentsService.addComment(addCommentRequest, loginUser.getId());
        return ResultUtils.success("添加成功");
    }

    /**
     *
     * @param blogId 博文id
     * @param request
     * @return
     */
    @GetMapping
    @ApiOperation(value = "根据id获取博文评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "blogId", value = "博文id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<BlogCommentsVO>> listBlogComments(long blogId, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<BlogCommentsVO> blogCommentsVOList = blogCommentsService.listComments(blogId, loginUser.getId());
        return ResultUtils.success(blogCommentsVOList);
    }


    @PutMapping("/like/{id}")
    @ApiOperation(value = "点赞博文评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "博文评论id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> likeComment(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        blogCommentsService.likeComment(id, loginUser.getId());
        return ResultUtils.success("ok");
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "博文评论id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<BlogCommentsVO> getCommentById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        BlogCommentsVO commentsVO = blogCommentsService.getComment(id, loginUser.getId());
        return ResultUtils.success(commentsVO);
    }
}