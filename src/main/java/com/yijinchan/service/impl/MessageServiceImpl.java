package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Message;
import com.yijinchan.service.MessageService;
import com.yijinchan.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author yijinchang
* @description 针对表【message】的数据库操作Service实现
* @createDate 2024-01-31 18:33:51
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

}




