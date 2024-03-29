package com.jinchan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinchan.model.domain.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2024-01-19 16:46:47
* @Entity generator.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




