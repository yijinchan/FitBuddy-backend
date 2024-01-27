package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.BlogComments;
import com.yijinchan.model.domain.CommentLike;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.AddCommentRequest;
import com.yijinchan.model.vo.BlogCommentsVO;
import com.yijinchan.model.vo.UserVO;
import com.yijinchan.service.BlogCommentsService;
import com.yijinchan.mapper.BlogCommentsMapper;
import com.yijinchan.service.BlogService;
import com.yijinchan.service.CommentLikeService;
import com.yijinchan.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Zhang Bridge
* @description 针对表【blog_comments】的数据库操作Service实现
* @createDate 2024-01-25 22:02:59
*/
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
    implements BlogCommentsService{

    @Resource
    UserService userService;

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    BlogService blogService;
    @Override
    @Transactional
    public void addComment(AddCommentRequest addCommentRequest, Long userId) {
        BlogComments blogComments = new BlogComments();
        blogComments.setUserId(userId);
        blogComments.setBlogId(addCommentRequest.getBlogId());
        blogComments.setContent(addCommentRequest.getContent());
        blogComments.setLikedNum(0);
        blogComments.setStatus(0);
        this.save(blogComments);
        Blog blog = blogService.getById(addCommentRequest.getBlogId());
        blogService.update().eq("id", addCommentRequest.getBlogId())
                .set("comments_num", blog.getCommentsNum() + 1).update();
    }

    /**
     * 根据博客ID和用户ID获取评论列表
     *
     * @param blogId 博客ID
     * @param userId 用户ID
     * @return 评论列表
     */
    @Override
    public List<BlogCommentsVO> listComments(long blogId, long userId) {
        // 创建查询条件封装对象
        LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogCommentsLambdaQueryWrapper.eq(BlogComments::getBlogId, blogId);

        // 根据查询条件获取评论列表
        List<BlogComments> blogCommentsList = this.list(blogCommentsLambdaQueryWrapper);

        // 遍历评论列表，转换为BlogCommentsVO对象列表
        return blogCommentsList.stream().map((comment) -> {
            // 创建BlogCommentsVO对象
            BlogCommentsVO blogCommentsVO = new BlogCommentsVO();

            // 将comment对象的属性复制给blogCommentsVO对象
            BeanUtils.copyProperties(comment, blogCommentsVO);

            // 获取评论用户的UserVO对象
            User user = userService.getById(comment.getUserId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            blogCommentsVO.setCommentUser(userVO);

            // 创建CommentLike封装对象，查询评论是否被用户点赞
            LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, comment.getId()).eq(CommentLike::getUserId, userId);
            // 统计评论被用户点赞的数量
            long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
            blogCommentsVO.setIsLiked(count > 0);

            return blogCommentsVO;
        }).collect(Collectors.toList());
    }
    @Override
    public BlogCommentsVO getComment(long commentId, Long userId) {
        BlogComments comments = this.getById(commentId);
        BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
        BeanUtils.copyProperties(comments, blogCommentsVO);
        LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, userId).eq(CommentLike::getCommentId, commentId);
        long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
        blogCommentsVO.setIsLiked(count > 0);
        return blogCommentsVO;
    }

    @Override
    @Transactional
    public void likeComment(long commentId, Long userId) {
        LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId);
        long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
        if (count == 0) {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLikeService.save(commentLike);
            BlogComments blogComments = this.getById(commentId);
            this.update().eq("id", commentId)
                    .set("liked_num", blogComments.getLikedNum() + 1)
                    .update();
        } else {
            commentLikeService.remove(commentLikeLambdaQueryWrapper);
            BlogComments blogComments = this.getById(commentId);
            this.update().eq("id", commentId)
                    .set("liked_num", blogComments.getLikedNum() - 1)
                    .update();
        }
    }
}




