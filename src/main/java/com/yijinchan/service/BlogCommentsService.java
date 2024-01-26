package com.yijinchan.service;

import com.yijinchan.model.domain.BlogComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.request.AddCommentRequest;
import com.yijinchan.model.vo.BlogCommentsVO;

import java.util.List;

/**
* @author Zhang Bridge
* @description 针对表【blog_comments】的数据库操作Service
* @createDate 2024-01-25 22:02:59
*/
public interface BlogCommentsService extends IService<BlogComments> {
    void addComment(AddCommentRequest addCommentRequest, Long userId);

    List<BlogCommentsVO> listComments(long blogId);
}
