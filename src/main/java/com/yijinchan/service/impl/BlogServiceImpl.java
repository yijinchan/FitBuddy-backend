package com.yijinchan.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yijinchan.common.ErrorCode;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.mapper.BlogMapper;
import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.BlogLike;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.BlogAddRequest;
import com.yijinchan.model.vo.BlogVO;
import com.yijinchan.service.BlogLikeService;
import com.yijinchan.service.BlogService;
import com.yijinchan.service.UserService;
import com.yijinchan.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import static com.yijinchan.constant.SystemConstants.PAGE_SIZE;

/**
* @author jinchan
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2024-01-23 22:22:47
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    BlogLikeService blogLikeService;

    @Resource
    UserService userService;
    @Override
    public Boolean addBlog(BlogAddRequest blogAddRequest, User loginUser) {
        // 创建一个Blog对象
        Blog blog = new Blog();
        // 创建一个存储图片名称的ArrayList
        ArrayList<String> imageNameList = new ArrayList<>();
        // 获取上传的图片
        MultipartFile[] images = blogAddRequest.getImages();
        // 如果图片不为空
        if (images != null) {
            // 遍历每一张图片
            for (MultipartFile image : images) {
                // 上传图片并获取文件名
                String filename = FileUtils.uploadFile(image);
                // 将文件名添加到imageNameList中
                imageNameList.add(filename);
            }
            // 将imageNameList中的元素用逗号拼接成字符串
            String imageStr = StringUtils.join(imageNameList, ",");
            // 将拼接后的字符串设置到blog的images属性中
            blog.setImages(imageStr);
        }
        // 设置blog的userId为loginUser的id
        blog.setUserId(loginUser.getId());
        // 设置blog的title为blogAddRequest的title
        blog.setTitle(blogAddRequest.getTitle());
        // 设置blog的内容为blogAddRequest的内容
        blog.setContent(blogAddRequest.getContent());
        // 保存blog到数据库并返回保存结果
        return this.save(blog);
    }


    @Override
    public Page<Blog> listMyBlogs(long currentPage, Long id) {
        if (currentPage <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.eq(Blog::getUserId, id);
        return this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
    }

    /**
     * 点赞博客
     *
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    /**
     * 点赞博客
     *
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @Override
    public void likeBlog(long blogId, Long userId) {
        //todo redis实现
        //todo 分布式锁
        // 获取博客对象
        Blog blog = this.getById(blogId);
        if (blog == null) {
            // 如果博文不存在，抛出参数错误异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "博文不存在");
        }
        // 创建LambdaQueryWrapper对象，查询条件为博客ID和用户ID
        LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blogId);
        blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
        // 查询已赞数
        long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
        if (isLike > 0) {
            // 如果已赞数大于0，说明已经点赞，移除点赞记录
            blogLikeService.remove(blogLikeLambdaQueryWrapper);
            // 更新博客的点赞数
            int newNum = blog.getLikedNum() - 1;
            this.update().eq("id", blogId).set("liked_num", newNum).update();
        } else {
            // 如果已赞数等于0，说明未点赞，创建新的点赞记录
            BlogLike blogLike = new BlogLike();
            blogLike.setBlogId(blogId);
            blogLike.setUserId(userId);
            // 保存点赞记录
            blogLikeService.save(blogLike);
            // 更新博客的点赞数
            int newNum = blog.getLikedNum() + 1;
            this.update().eq("id", blogId).set("liked_num", newNum).update();
        }
    }
    /**
     * 分页查询博文信息
     *
     * @param currentPage 当前页码
     * @param userId 用户ID
     * @return 分页后的博文视图对象列表
     */
    @Override
    public Page<BlogVO> pageBlog(long currentPage, Long userId) {
        // 调用page方法进行博文分页查询
        Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE));
        // 创建一个空的博文视图对象分页页
        Page<BlogVO> blogVOPage = new Page<>();
        // 将博文分页对象的属性复制到博文视图对象分页页中
        BeanUtils.copyProperties(blogPage, blogVOPage);
        // 设置博文首页图片，返回图片完整url
        if (userId == null) {
            return blogVOPage;
        }
        // 对博文分页对象的记录进行流式处理
        List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
            // 创建一个新的博文视图对象
            BlogVO blogVO = new BlogVO();
            // 将博文对象的属性复制到博文视图对象中
            BeanUtils.copyProperties(blog, blogVO);
            // 创建一个博文点赞的查询包装器
            LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 设置查询条件为博文ID和用户ID等于当前博文的ID和用户ID
            blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blog.getId());
            blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
            // 查询博文点赞的数量
            long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
            // 判断是否有点赞，如果有则将isLike设置为true
            blogVO.setIsLike(isLike > 0);
            // 返回博文视图对象
            return blogVO;
        }).collect(Collectors.toList());
        // 将博文视图对象列表设置为博文视图对象分页页的记录
        blogVOPage.setRecords(blogVOList);
        // 返回博文视图对象分页页
        return blogVOPage;
    }
    @Override
    public BlogVO getBlogById(long blogId, Long userId) {
        Blog blog = this.getById(blogId);
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
        blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blogId);
        long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
        blogVO.setIsLike(isLike > 0);
        User author = userService.getById(blog.getUserId());
        blogVO.setAuthor(author);
        return blogVO;
    }
}



