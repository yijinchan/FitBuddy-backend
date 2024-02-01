package com.jinchan.model.vo;

import com.jinchan.model.domain.Blog;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * ClassName: BlogVO
 * Package: com.jinchan.model.vo
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/25 21:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "博文返回")
public class BlogVO extends Blog implements Serializable {

    private static final long serialVersionUID = 1508195411026290362L;

    @ApiModelProperty(value = "是否点赞")
    private Boolean isLike;

    @ApiModelProperty(value = "封面图片")
    private String coverImage;

    @ApiModelProperty(value = "作者")
    private UserVO author;
}
