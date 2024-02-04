package com.jinchan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import com.jinchan.mapper.BlogMapper;
import com.jinchan.model.domain.*;
import com.jinchan.model.enums.MessageTypeEnum;
import com.jinchan.model.request.BlogAddRequest;
import com.jinchan.model.request.BlogUpdateRequest;
import com.jinchan.model.vo.BlogVO;
import com.jinchan.model.vo.UserVO;
import com.jinchan.service.*;
import com.jinchan.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jinchan.constant.RedisConstants.*;
import static com.jinchan.constant.RedissonConstant.*;
import static com.jinchan.constant.SystemConstants.PAGE_SIZE;
import static com.jinchan.constant.SystemConstants.PROTOCOL_LENGTH;

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
    @Resource
    private MessageService messageService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Value("${fitbuddy.qiniu.url:null}")
    private String QINIU_URL;
    @Override
    public Long addBlog(BlogAddRequest blogAddRequest, User loginUser) {
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
        boolean saved = this.save(blog);
        if (saved) {
            List<UserVO> userVOList = followService.listFans(loginUser.getId());
            if (!userVOList.isEmpty()) {
                for (UserVO userVO : userVOList) {
                    String key = BLOG_FEED_KEY + userVO.getId();
                    stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
                    String likeNumKey = MESSAGE_BLOG_NUM_KEY + userVO.getId();
                    Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
                    if (Boolean.TRUE.equals(hasKey)) {
                        stringRedisTemplate.opsForValue().increment(likeNumKey);
                    } else {
                        stringRedisTemplate.opsForValue().set(likeNumKey, "1");
                    }
                }
            }
        }
        return blog.getId();
    }


    @Override
    public Page<BlogVO> listMyBlogs(long currentPage, Long id) {
        if (currentPage <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.eq(Blog::getUserId, id);
        Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
        Page<BlogVO> blogVoPage = new Page<>();
        BeanUtils.copyProperties(blogPage, blogVoPage);
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
            String[] imgStr = images.split(",");
            blogVO.setCoverImage(QINIU_URL + imgStr[0]);
        }
        blogVoPage.setRecords(blogVOList);
        return blogVoPage;
    }

    /**
     * 点赞博客
     *
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @Override
    public void likeBlog(long blogId, Long userId) {
        RLock lock = redissonClient.getLock(BLOG_LIKE_LOCK + blogId + ":" + userId);
        try {
            if (lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.MILLISECONDS)) {
                Blog blog = this.getById(blogId);
                if (blog == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "博文不存在");
                }
                LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blogId);
                blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
                long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
                if (isLike > 0) {
                    blogLikeService.remove(blogLikeLambdaQueryWrapper);
                    doUnLikeBlog(blog, userId);
                } else {
                    doLikeBlog(blog, userId);
                }
            }
        } catch (Exception e) {
            log.error("LikeBlog error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    /**
     * 取消点赞博文
     *
     * @param blog   博文
     * @param userId 用户id
     */
    public void doUnLikeBlog(Blog blog, Long userId) {
        int newNum = blog.getLikedNum() - 1;
        this.update().eq("id", blog.getId()).set("liked_num", newNum).update();
        LambdaQueryWrapper<Message> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper
                .eq(Message::getType, MessageTypeEnum.BLOG_LIKE.getValue())
                .eq(Message::getFromId, userId)
                .eq(Message::getToId, blog.getUserId())
                .eq(Message::getData, String.valueOf(blog.getId()));
        messageService.remove(messageQueryWrapper);
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + blog.getUserId();
        String upNumStr = stringRedisTemplate.opsForValue().get(likeNumKey);
        if (!StrUtil.isNullOrUndefined(upNumStr) && Long.parseLong(upNumStr) != 0) {
            stringRedisTemplate.opsForValue().decrement(likeNumKey);
        }
    }

    /**
     * 点赞博文
     *
     * @param blog   博文
     * @param userId 用户id
     */
    public void doLikeBlog(Blog blog, Long userId) {
        BlogLike blogLike = new BlogLike();
        blogLike.setBlogId(blog.getId());
        blogLike.setUserId(userId);
        blogLikeService.save(blogLike);
        int newNum = blog.getLikedNum() + 1;
        this.update().eq("id", blog.getId()).set("liked_num", newNum).update();
        Message message = new Message();
        message.setType(MessageTypeEnum.BLOG_LIKE.getValue());
        message.setFromId(userId);
        message.setToId(blog.getUserId());
        message.setData(String.valueOf(blog.getId()));
        messageService.save(message);
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + blog.getUserId();
        Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasKey)) {
            stringRedisTemplate.opsForValue().increment(likeNumKey);
        } else {
            stringRedisTemplate.opsForValue().set(likeNumKey, "1");
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
    public Page<BlogVO> pageBlog(long currentPage, String title, Long userId) {
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.like(StringUtils.isNotBlank(title), Blog::getTitle, title);
        Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);

        Page<BlogVO> blogVoPage = new Page<>();
        BeanUtils.copyProperties(blogPage, blogVoPage);
        List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
            // 创建一个新的博文视图对象
            BlogVO blogVO = new BlogVO();
            // 将博文对象的属性复制到博文视图对象中
            BeanUtils.copyProperties(blog, blogVO);
            if (userId != null) {
                LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blog.getId()).eq(BlogLike::getUserId, userId);
                long count = blogLikeService.count(blogLikeLambdaQueryWrapper);
                blogVO.setIsLike(count > 0);
            }
            return blogVO;
        }).collect(Collectors.toList());
        for (BlogVO blogVO : blogVOList) {
            String images = blogVO.getImages();
            if (images == null) {
                continue;
            }
            String[] imgStrs = images.split(",");
            blogVO.setCoverImage(QINIU_URL + imgStrs[0]);
        }
        // 将博文视图对象列表设置为博文视图对象分页页的记录
        blogVoPage.setRecords(blogVOList);
        return blogVoPage;
    }

    @Override
    public BlogVO getBlogById(long blogId, Long userId) {
        Blog blog = this.getById(blogId);
        if (blog == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法找到该博文");
        }
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLikeLambdaQueryWrapper.eq(BlogLike::getUserId, userId);
        blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blogId);
        long isLike = blogLikeService.count(blogLikeLambdaQueryWrapper);
        blogVO.setIsLike(isLike > 0);
        User author = userService.getById(blog.getUserId());
        if (author == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法找到该博文作者");
        }
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
            imgStrList.add(QINIU_URL + imgStr);
        }
        String imgStr = StringUtils.join(imgStrList, ",");
        blogVO.setImages(imgStr);
        blogVO.setCoverImage(imgStrList.get(0));
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
     * @param userId            用户ID
     */
    @Override
    public void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId, boolean isAdmin) {
        if (blogUpdateRequest.getId() == null) { // 检查博客ID是否为空
            throw new BusinessException(ErrorCode.PARAMS_ERROR); // 抛出业务异常，参数错误
        }
        Long createUserId = this.getById(blogUpdateRequest.getId()).getUserId(); // 获取创建该博客的用户ID
        if (!createUserId.equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限");
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
                String fileName = img.substring(img.indexOf("/", PROTOCOL_LENGTH) + 1);
                imageNameList.add(fileName); // 将截取自索引25后的图片名称添加至图片名称列表
            }
        }
        if (blogUpdateRequest.getImages() != null) { // 检查更新请求中的图片是否为空
            MultipartFile[] images = blogUpdateRequest.getImages(); // 获取更新请求中的多文件对象
            for (MultipartFile image : images) { // 遍历图片数组
                String filename = FileUtils.uploadFile(image); // 上传图片并获取文件名
                imageNameList.add(filename); // 将文件名添加至图片名称列表
            }
        }
        if (!imageNameList.isEmpty()) { // 检查图片名称列表是否为空
            String imageStr = StringUtils.join(imageNameList, ","); // 将图片名称列表以逗号拼接成字符串
            blog.setImages(imageStr); // 设置博客对象的图片字段
        }
        blog.setTitle(blogUpdateRequest.getTitle()); // 设置博客对象的标题字段
        blog.setContent(blogUpdateRequest.getContent()); // 设置博客对象的内容字段
        this.updateById(blog); // 更新博客对象的信息
    }

}



