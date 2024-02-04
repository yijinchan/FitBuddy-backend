package com.jinchan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinchan.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author jinchan
* @description 针对表【user】的数据库操作Mapper
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<User> getRandomUser();
}




