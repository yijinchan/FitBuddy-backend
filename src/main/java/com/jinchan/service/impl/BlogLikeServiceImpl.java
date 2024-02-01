package com.jinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jinchan.model.domain.BlogLike;
import com.jinchan.service.BlogLikeService;
import com.jinchan.mapper.BlogLikeMapper;
import org.springframework.stereotype.Service;

/**
* @author jinchan
* @description 针对表【blog_like】的数据库操作Service实现
* @createDate 2024-01-23 22:55:03
*/
@Service
public class BlogLikeServiceImpl extends ServiceImpl<BlogLikeMapper, BlogLike>
    implements BlogLikeService{

}




