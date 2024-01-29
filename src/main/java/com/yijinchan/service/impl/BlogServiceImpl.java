package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.mapper.BlogMapper;
import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.BlogLike;
import com.yijinchan.model.domain.Follow;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.BlogAddRequest;
import com.yijinchan.model.request.BlogUpdateRequest;
import com.yijinchan.model.vo.BlogVO;
import com.yijinchan.model.vo.UserVO;
import com.yijinchan.service.BlogLikeService;
import com.yijinchan.service.BlogService;
import com.yijinchan.service.FollowService;
import com.yijinchan.service.UserService;
import com.yijinchan.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yijinchan.constant.SystemConstants.PAGE_SIZE;
import static com.yijinchan.constant.SystemConstants.QiNiuUrl;

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

    @Resource
    private FollowService followService;

    @Override
    public Boolean addBlog(BlogAddRequest blogAddRequest, User loginUser) {
        Blog blog = new Blog();
        ArrayList<String> imageNameList = new ArrayList<>();
        try {
            MultipartFile[] images = blogAddRequest.getImages();
            if (images != null) {
                for (MultipartFile image : images) {
                    String filename = FileUtils.uploadFile(image);
                    imageNameList.add(filename);
                }
                String imageStr = StringUtils.join(imageNameList, ",");
                blog.setImages(imageStr);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        blog.setUserId(loginUser.getId());
        blog.setTitle(blogAddRequest.getTitle());
        blog.setContent(blogAddRequest.getContent());
        return this.save(blog);
    }


    @Override
    public Page<BlogVO> listMyBlogs(long currentPage, Long id) {
        if (currentPage <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.eq(Blog::getUserId, id);
        Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
        Page<BlogVO> blogVOPage = new Page<>();
        BeanUtils.copyProperties(blogPage, blogVOPage);
        //todo 设置博文首页图片，返回图片完整url
        List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            return blogVO;
        }).collect(Collectors.toList());
        for (BlogVO blogVO : blogVOList) {
            String images = blogVO.getImages();
            if (images == null) {
                continue;
            }
            String[] imgStrs = images.split(",");
            blogVO.setCoverImage(QiNiuUrl + imgStrs[0]);
        }
        blogVOPage.setRecords(blogVOList);
        return blogVOPage;
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
     * @param userId      用户ID
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
        List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
            // 创建一个新的博文视图对象
            BlogVO blogVO = new BlogVO();
            // 将博文对象的属性复制到博文视图对象中
            BeanUtils.copyProperties(blog, blogVO);
            return blogVO;
        }).collect(Collectors.toList());
        for (BlogVO blogVO : blogVOList) {
            String images = blogVO.getImages();
            if (images == null) {
                continue;
            }
            String[] imgStrs = images.split(",");
            blogVO.setCoverImage(QiNiuUrl + imgStrs[0]);
        }
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
        UserVO authorVO = new UserVO();
        BeanUtils.copyProperties(author, authorVO);
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowUserId, authorVO.getId()).eq(Follow::getUserId, userId);
        long count = followService.count(followLambdaQueryWrapper);
        authorVO.setIsFollow(count > 0);
        blogVO.setAuthor(authorVO);
        String images = blogVO.getImages();
        if (images == null) {
            return blogVO;
        }
        String[] imgStrs = images.split(",");
        ArrayList<String> imgStrList = new ArrayList<>();
        for (String imgStr : imgStrs) {
            imgStrList.add(QiNiuUrl + imgStr);
        }
        String imgStr = StringUtils.join(imgStrList, ",");
        blogVO.setImages(imgStr);
        return blogVO;
    }
    @Override
    public void deleteBlog(Long blogId, Long userId, boolean isAdmin) {
        if (isAdmin) {
            this.removeById(blogId);
            return;
        }
        Blog blog = this.getById(blogId);
        if (!userId.equals(blog.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        this.removeById(blogId);
    }

    /**
     * 更新博客
     *
     * @param blogUpdateRequest 博客更新请求对象
     * @param userId 用户ID
     */
    @Override
    public void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId, boolean isAdmin) {
        if (blogUpdateRequest.getId() == null) { // 检查博客ID是否为空
            throw new BusinessException(ErrorCode.PARAMS_ERROR); // 抛出业务异常，参数错误
        }
        Long createUserId = this.getById(blogUpdateRequest.getId()).getUserId(); // 获取创建该博客的用户ID
        if (!createUserId.equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限");
        }
        String title = blogUpdateRequest.getTitle(); // 获取更新请求中的博客标题
        String content = blogUpdateRequest.getContent(); // 获取更新请求中的博客内容
        if (StringUtils.isAnyBlank(title, content)) { // 检查标题和内容是否为空或者换行符
            throw new BusinessException(ErrorCode.PARAMS_ERROR); // 抛出业务异常，参数错误
        }
        Blog blog = new Blog(); // 创建博客对象
        blog.setId(blogUpdateRequest.getId()); // 设置博客对象的ID
        ArrayList<String> imageNameList = new ArrayList<>(); // 创建图片名称列表
        if (StringUtils.isNotBlank(blogUpdateRequest.getImgStr())) { // 检查更新请求中的图片字符串是否为空或者空白
            String imgStr = blogUpdateRequest.getImgStr(); // 获取更新请求中的图片字符串
            String[] imgs = imgStr.split(","); // 将图片字符串按逗号分隔为字符串数组
            for (String img : imgs) { // 遍历图片字符串数组
                imageNameList.add(img.substring(25)); // 将截取自索引25后的图片名称添加至图片名称列表
            }
        }
        if (blogUpdateRequest.getImages() != null) { // 检查更新请求中的图片是否为空
            MultipartFile[] images = blogUpdateRequest.getImages(); // 获取更新请求中的多文件对象
            for (MultipartFile image : images) { // 遍历图片数组
                String filename = FileUtils.uploadFile(image); // 上传图片并获取文件名
                imageNameList.add(filename); // 将文件名添加至图片名称列表
            }
        }
        if (imageNameList.size() > 0) { // 检查图片名称列表是否为空
            String imageStr = StringUtils.join(imageNameList, ","); // 将图片名称列表以逗号拼接成字符串
            blog.setImages(imageStr); // 设置博客对象的图片字段
        }
        blog.setTitle(blogUpdateRequest.getTitle()); // 设置博客对象的标题字段
        blog.setContent(blogUpdateRequest.getContent()); // 设置博客对象的内容字段
        this.updateById(blog); // 更新博客对象的信息
    }

}



