package com.jinchan.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.Message;
import com.jinchan.model.vo.BlogVO;
import com.jinchan.model.vo.MessageVO;

import java.util.List;

/**
* @author jinchan
* @description 针对表【message】的数据库操作Service
* @createDate 2024-01-31 18:33:51
*/
public interface MessageService extends IService<Message> {
    long getMessageNum(Long userId);
    long getLikeNum(Long userId);
    List<MessageVO> getLike(Long userId);
    List<BlogVO> getUserBlog(Long userId);
    Boolean hasNewMessage(Long userId);
}
