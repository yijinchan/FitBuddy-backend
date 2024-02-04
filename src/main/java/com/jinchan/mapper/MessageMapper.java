package com.jinchan.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinchan.model.domain.Message;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jinchan
* @description 针对表【message】的数据库操作Mapper
* @createDate 2024-01-31 18:33:51
* @Entity generator.domain.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




