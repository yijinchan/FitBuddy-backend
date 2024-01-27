package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.model.domain.CommentLike;
import com.yijinchan.service.CommentLikeService;
import com.yijinchan.mapper.CommentLikeMapper;
import org.springframework.stereotype.Service;

/**
* @author Zhang Bridge
* @description 针对表【comment_like】的数据库操作Service实现
* @createDate 2024-01-26 23:00:28
*/
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike>
    implements CommentLikeService{

}




