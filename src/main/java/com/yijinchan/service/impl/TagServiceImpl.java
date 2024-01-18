package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.model.domain.Tag;
import com.yijinchan.mapper.TagMapper;
import com.yijinchan.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author jinchan
* @description 针对表【tag】的数据库操作Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}




