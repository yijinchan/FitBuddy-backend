package com.jinchan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import com.jinchan.mapper.BlogCommentsMapper;
import com.jinchan.model.domain.*;
import com.jinchan.model.enums.MessageTypeEnum;
import com.jinchan.model.request.AddCommentRequest;
import com.jinchan.model.vo.BlogCommentsVO;
import com.jinchan.model.vo.BlogVO;
import com.jinchan.model.vo.UserVO;
import com.jinchan.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.jinchan.constant.RedisConstants.MESSAGE_LIKE_NUM_KEY;
import static com.jinchan.constant.SystemConstants.QiNiuUrl;

/**
 * @author Zhang Bridge
 * @description 针对表【blog_comments】的数据库操作Service实现
 * @createDate 2024-01-25 22:02:59
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
        implements BlogCommentsService {

    @Resource
    UserService userService;
    @Resource
    private CommentLikeService commentLikeService;
    @Resource
    BlogService blogService;
    @Resource
    private MessageService messageService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
            Message message = new Message();
            message.setType(MessageTypeEnum.BLOG_COMMENT_LIKE.getValue());
            message.setFromId(userId);
            message.setToId(blogComments.getUserId());
            message.setData(String.valueOf(blogComments.getId()));
            messageService.save(message);
        } else {
            commentLikeService.remove(commentLikeLambdaQueryWrapper);
            BlogComments blogComments = this.getById(commentId);
            this.update().eq("id", commentId)
                    .set("liked_num", blogComments.getLikedNum() - 1)
                    .update();
            String likeNumKey = MESSAGE_LIKE_NUM_KEY + blogComments.getUserId();
            Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
            if (Boolean.TRUE.equals(hasKey)) {
                stringRedisTemplate.opsForValue().increment(likeNumKey);
            } else {
                stringRedisTemplate.opsForValue().set(likeNumKey, "1");
            }
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        BlogComments blogComments = this.getById(id);
        if (isAdmin) {
            this.removeById(id);
            Integer commentsNum = blogService.getById(blogComments.getBlogId()).getCommentsNum();
            blogService.update().eq("id", blogComments.getBlogId()).set("comments_num", commentsNum - 1).update();
            return;
        }
        if (blogComments == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!blogComments.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        this.removeById(id);
        Integer commentsNum = blogService.getById(blogComments.getBlogId()).getCommentsNum();
        blogService.update().eq("id", blogComments.getBlogId()).set("comments_num", commentsNum - 1).update();
    }

    @Override
    public List<BlogCommentsVO> listMyComments(Long id) {
        LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogCommentsLambdaQueryWrapper.eq(BlogComments::getUserId, id);
        List<BlogComments> blogCommentsList = this.list(blogCommentsLambdaQueryWrapper);
        return blogCommentsList.stream().map((item) -> {
            BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
            BeanUtils.copyProperties(item, blogCommentsVO);
            User user = userService.getById(item.getUserId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            blogCommentsVO.setCommentUser(userVO);

            Long blogId = blogCommentsVO.getBlogId();
            Blog blog = blogService.getById(blogId);
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            String images = blogVO.getImages();
            if (images == null) {
                blogVO.setCoverImage(null);
            } else {
                String[] imgStr = images.split(",");
                blogVO.setCoverImage(QiNiuUrl + imgStr[0]);
            }
            Long authorId = blogVO.getUserId();
            User author = userService.getById(authorId);
            UserVO authorVO = new UserVO();
            BeanUtils.copyProperties(author, authorVO);
            blogVO.setAuthor(authorVO);

            blogCommentsVO.setBlog(blogVO);
            LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, id).eq(CommentLike::getCommentId, item.getId());
            long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
            blogCommentsVO.setIsLiked(count > 0);
            return blogCommentsVO;
        }).collect(Collectors.toList());
    }
}




