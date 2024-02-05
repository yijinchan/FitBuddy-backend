package com.jinchan.mapper;

import com.jinchan.model.domain.Friends;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【friends(好友申请管理表)】的数据库操作Mapper
* @createDate 2024-01-31 21:53:01
* @Entity com.jinchan.model.domain.Friends
*/
@Mapper
public interface FriendsMapper extends BaseMapper<Friends> {

}




