package com.jinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.BlogComments;
import com.jinchan.model.request.AddCommentRequest;
import com.jinchan.model.vo.BlogCommentsVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author jinchan
* @description 针对表【blog_comments】的数据库操作Service
* @createDate 2024-01-25 22:02:59
*/
public interface BlogCommentsService extends IService<BlogComments> {
    void addComment(AddCommentRequest addCommentRequest, Long userId);

    List<BlogCommentsVO> listComments(long blogId,long userId);

    BlogCommentsVO getComment(long commentId, Long userId);

    @Transactional
    void likeComment(Long commentId, Long userId);

    void deleteComment(Long id, Long id1, boolean isAdmin);

    List<BlogCommentsVO> listMyComments(Long id);

    Page<BlogCommentsVO> pageMyComments(Long id, Long currentPage);
}
