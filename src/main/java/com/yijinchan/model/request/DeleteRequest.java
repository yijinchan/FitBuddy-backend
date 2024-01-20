package com.yijinchan.model.request;

import lombok.Data;
import org.apache.ibatis.annotations.Delete;

import java.io.Serializable;

/**
 * ClassName: DeleteRequest
 * Package: com.yijinchan.model.request
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/19 22:47
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -3499858766784170500L;

    private long id;
}
