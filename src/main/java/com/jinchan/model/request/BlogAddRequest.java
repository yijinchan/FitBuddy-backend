package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * ClassName: BlogAddRequest
 * Package: com.jinchan.model.request
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/23 17:30
 */
@Data
@ApiModel(value = "添加博文请求")
public class BlogAddRequest implements Serializable {
    private static final long serialVersionUID = 8975136896057535409L;
    /**
     * 图片
     */
    @ApiModelProperty(value = "图片")
    private MultipartFile[] images;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 团队id
     */
    @ApiModelProperty(value = "正文")
    private String content;
}
