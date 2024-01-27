package com.yijinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yijinchan.model.domain.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.BlogAddRequest;
import com.yijinchan.model.request.BlogUpdateRequest;
import com.yijinchan.model.vo.BlogVO;

/**
* @author jinchan
* @description 针对表【blog】的数据库操作Service
* @createDate 2024-01-23 22:22:47
*/
public interface BlogService extends IService<Blog> {

    Boolean addBlog(BlogAddRequest blogAddRequest, User loginUser);

    Page<BlogVO> listMyBlogs(long currentPage, Long id);

    void likeBlog(long blogId, Long userId);

    Page<BlogVO> pageBlog(long currentPage, Long id);

    BlogVO getBlogById(long blogId, Long userId);

    void deleteBlog(Long blogId, Long userId, boolean isAdmin);

    void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId);
}
