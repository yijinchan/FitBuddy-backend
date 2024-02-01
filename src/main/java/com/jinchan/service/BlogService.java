package com.jinchan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinchan.model.domain.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.BlogAddRequest;
import com.jinchan.model.request.BlogUpdateRequest;
import com.jinchan.model.vo.BlogVO;

/**
* @author jinchan
* @description 针对表【blog】的数据库操作Service
* @createDate 2024-01-23 22:22:47
*/
public interface BlogService extends IService<Blog> {

    Long addBlog(BlogAddRequest blogAddRequest, User loginUser);

    Page<BlogVO> listMyBlogs(long currentPage, Long id);

    void likeBlog(long blogId, Long userId);

    Page<BlogVO> pageBlog(long currentPage,String title, Long id);

    BlogVO getBlogById(long blogId, Long userId);

    void deleteBlog(Long blogId, Long userId, boolean isAdmin);

    void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId,boolean isAdmin);
}
