package com.yijinchan.service.impl;

import java.util.ArrayList;
import java.util.Date;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yijinchan.mapper.BlogMapper;
import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.BlogAddRequest;
import com.yijinchan.service.BlogService;
import com.yijinchan.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
* @author jinchan
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2024-01-23 22:22:47
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Override
    public Boolean addBlog(BlogAddRequest blogAddRequest, User loginUser) {
        // 创建一个Blog对象
        Blog blog = new Blog();
        // 创建一个存储图片名称的ArrayList
        ArrayList<String> imageNameList = new ArrayList<>();
        // 获取上传的图片
        MultipartFile[] images = blogAddRequest.getImages();
        // 如果图片不为空
        if (images != null) {
            // 遍历每一张图片
            for (MultipartFile image : images) {
                // 上传图片并获取文件名
                String filename = FileUtils.uploadFile(image);
                // 将文件名添加到imageNameList中
                imageNameList.add(filename);
            }
            // 将imageNameList中的元素用逗号拼接成字符串
            String imageStr = StringUtils.join(imageNameList, ",");
            // 将拼接后的字符串设置到blog的images属性中
            blog.setImages(imageStr);
        }
        // 设置blog的userId为loginUser的id
        blog.setUserId(loginUser.getId());
        // 设置blog的title为blogAddRequest的title
        blog.setTitle(blogAddRequest.getTitle());
        // 设置blog的内容为blogAddRequest的内容
        blog.setContent(blogAddRequest.getContent());
        // 保存blog到数据库并返回保存结果
        return this.save(blog);
    }

}



