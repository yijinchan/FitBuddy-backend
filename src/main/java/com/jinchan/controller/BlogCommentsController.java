package com.jinchan.controller;

import com.jinchan.common.BaseResponse;
import com.jinchan.common.ErrorCode;
import com.jinchan.common.ResultUtils;
import com.jinchan.exception.BusinessException;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.AddCommentRequest;
import com.jinchan.model.vo.BlogCommentsVO;
import com.jinchan.service.BlogCommentsService;
import com.jinchan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ClassName: BlogCommentsController
 * Package: com.jinchan.controller
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/26 22:44
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "博客评论管理模块")
public class BlogCommentsController {
    /**
     * 博客评论服务
     */
    @Resource
    private BlogCommentsService blogCommentsService;
    /**
     * 用户服务
     */
    @Resource
    private UserService userService;

    /**
     * @param addCommentRequest 博文评论添加请求
     * @param request           request
     * @return BaseResponse
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "addCommentRequest", value = "博文评论添加请求"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> addComment(@RequestBody AddCommentRequest addCommentRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
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
     * @param blogId  博文id
     * @param request
     * @return
     */
    @GetMapping
    @ApiOperation(value = "根据id获取博文评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "blogId", value = "博文id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<BlogCommentsVO>> listBlogComments(long blogId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
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
        User loginUser = userService.getLoginUser(request);
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
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        BlogCommentsVO commentsVO = blogCommentsService.getComment(id, loginUser.getId());
        return ResultUtils.success(commentsVO);
    }

    /**
     * 根据id删除评论
     * @param id 评论id
     * @param request request
     * @return BaseResponse
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "博文评论id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> deleteBlogComment(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean isAdmin = userService.isAdmin(loginUser);
        blogCommentsService.deleteComment(id, loginUser.getId(), isAdmin);
        return ResultUtils.success("ok");
    }

    /**
     * 获取我的评论
     * @param request request
     * @return BaseResponse
     */
    @GetMapping("/list/my")
    @ApiOperation(value = "获取我的评论")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<BlogCommentsVO>> listMyBlogComments(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<BlogCommentsVO> commentsVOList = blogCommentsService.listMyComments(loginUser.getId());
        return ResultUtils.success(commentsVOList);
    }
}