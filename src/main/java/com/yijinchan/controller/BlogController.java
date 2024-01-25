package com.yijinchan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.BlogAddRequest;
import com.yijinchan.model.vo.BlogVO;
import com.yijinchan.service.BlogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.yijinchan.constant.SystemConstants.PAGE_SIZE;
import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;

/**
 * ClassName: BlogController
 * Package: com.yijinchan.controller
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/23 17:27
 */
@RestController
@RequestMapping("blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/list")
    public BaseResponse<Page<BlogVO>> listBlogPage(long currentPage, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            return ResultUtils.success(blogService.pageBlog(currentPage, null));
        } else {
            return ResultUtils.success(blogService.pageBlog(currentPage, loginUser.getId()));
        }
    }

    @PostMapping("/add")
    public BaseResponse<String> addBlog(BlogAddRequest blogAddRequest, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (StringUtils.isAnyBlank(blogAddRequest.getTitle(), blogAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        blogService.addBlog(blogAddRequest, loginUser);
        return ResultUtils.success("添加成功");
    }

    @GetMapping("/list/my/blog")
    public BaseResponse<Page<Blog>> listMyBlogs(long currentPage, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Page<Blog> blogPage = blogService.listMyBlogs(currentPage, loginUser.getId());
        return ResultUtils.success(blogPage);
    }

    @PutMapping("/like/{id}")
    public BaseResponse<String> likeBlog(@PathVariable long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        blogService.likeBlog(id, loginUser.getId());
        return ResultUtils.success("成功");
    }

    @GetMapping("/{id}")
    public BaseResponse<BlogVO> getBlogById(@PathVariable long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(blogService.getBlogById(id, loginUser.getId()));
    }
}
