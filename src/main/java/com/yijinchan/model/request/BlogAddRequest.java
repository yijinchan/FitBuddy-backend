package com.yijinchan.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * ClassName: BlogAddRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/23 17:30
 */
@Data
public class BlogAddRequest implements Serializable {
    private static final long serialVersionUID = 8975136896057535409L;

    private MultipartFile[] images;
    private String title;
    private String content;
}
