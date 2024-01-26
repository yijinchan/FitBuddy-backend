package com.yijinchan.controller;

import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.AddCommentRequest;
import com.yijinchan.model.vo.BlogCommentsVO;
import com.yijinchan.service.BlogCommentsService;
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
public class BlogCommentsController {
    @Resource
    private BlogCommentsService blogCommentsService;

    @PostMapping("/add")
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

    @GetMapping
    public BaseResponse<List<BlogCommentsVO>> listBlogComments(long blogId) {
        List<BlogCommentsVO> blogCommentsVOList = blogCommentsService.listComments(blogId);
        return ResultUtils.success(blogCommentsVOList);
    }
}