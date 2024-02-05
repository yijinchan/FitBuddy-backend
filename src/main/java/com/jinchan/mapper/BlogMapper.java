package com.jinchan.mapper;

import com.jinchan.model.domain.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2024-01-23 22:22:47
* @Entity generator.domain.Blog
*/
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

}




