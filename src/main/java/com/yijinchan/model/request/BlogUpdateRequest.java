package com.yijinchan.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * ClassName: BlogUpdateRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/27 15:33
 */
@Data
public class BlogUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1731681201891093103L;
    private Long id;
    private String imgStr;
    private MultipartFile[] images;
    private String title;
    private String content;
}
