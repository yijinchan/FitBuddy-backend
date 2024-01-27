package com.yijinchan.model.vo;

import com.yijinchan.model.domain.Blog;
import com.yijinchan.model.domain.BlogComments;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: BlogCommentsVO
 * Package: com.yijinchan.model.vo
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/26 22:51
 */
@Data
@ApiModel(value = "博文评论返回")
public class BlogCommentsVO extends BlogComments implements Serializable {
    private static final long serialVersionUID = 4384362431081902087L;

    @ApiModelProperty(value = "评论用户")
    private UserVO commentUser;

    @ApiModelProperty(value = "是否点赞")
    private Boolean isLiked;
}
