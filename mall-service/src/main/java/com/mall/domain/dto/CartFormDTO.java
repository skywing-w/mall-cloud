package com.mall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 新增购物车商品表单。
 */
@Data
@ApiModel(description = "新增购物车商品表单实体")
public class CartFormDTO {
    /** 商品 id，加入购物车的必填项，缺失时无法定位商品 */
    @NotNull(message = "商品id不能为空")
    @ApiModelProperty(value = "商品id", required = true)
    private Long itemId;
    @ApiModelProperty("商品标题")
    private String name;
    @ApiModelProperty("商品动态属性键值集")
    private String spec;
    @ApiModelProperty("价格,单位：分")
    private Integer price;
    @ApiModelProperty("商品图片")
    private String image;
}
