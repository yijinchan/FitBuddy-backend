package com.jinchan.mapper;

import com.jinchan.model.domain.BlogComments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【blog_comments】的数据库操作Mapper
* @createDate 2024-01-25 22:02:59
* @Entity generator.domain.BlogComments
*/
@Mapper
public interface BlogCommentsMapper extends BaseMapper<BlogComments> {

}




