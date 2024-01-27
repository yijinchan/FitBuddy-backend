package com.yijinchan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yijinchan.model.domain.User;

import java.util.List;

/**
* @author jinchan
* @description 针对表【user】的数据库操作Mapper
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {
    List<User> getRandomUser();
}




