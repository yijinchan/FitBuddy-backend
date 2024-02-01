package com.jinchan.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: DeleteRequest
 * Package: com.jinchan.model.request
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/19 22:47
 */
@Data
@ApiModel(value = "删除请求参数")
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -3499858766784170500L;
    @ApiModelProperty(value = "id")
    private long id;
}
