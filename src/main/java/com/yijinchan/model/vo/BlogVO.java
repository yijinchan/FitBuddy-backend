package com.yijinchan.model.vo;

import com.yijinchan.model.domain.User;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: BlogVO
 * Package: com.yijinchan.model.vo
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/25 21:39
 */
@Data
public class BlogVO implements Serializable {

    private static final long serialVersionUID = 1508195411026290362L;

    private Boolean isLike;

    private String coverImage;

    private User author;
}
