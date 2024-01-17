package com.FitBuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.FitBuddy.model.domain.Tag;
import com.FitBuddy.mapper.TagMapper;
import com.FitBuddy.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author jinchan
* @description 针对表【tag】的数据库操作Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}




