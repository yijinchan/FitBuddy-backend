package com.jinchan.mapper;

import com.jinchan.model.domain.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-01-18 20:53:25
* @Entity com.jinchan.model.domain.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




