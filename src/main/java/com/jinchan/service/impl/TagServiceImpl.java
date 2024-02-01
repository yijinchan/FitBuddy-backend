package com.jinchan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jinchan.model.domain.Tag;
import com.jinchan.mapper.TagMapper;
import com.jinchan.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author jinchan
* @description 针对表【tag】的数据库操作Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}




