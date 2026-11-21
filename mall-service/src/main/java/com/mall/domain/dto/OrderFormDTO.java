package com.mall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 交易下单表单。
 */
@Data
@ApiModel(description = "交易下单表单实体")
public class OrderFormDTO {
    @ApiModelProperty("收货地址id")
    private Long addressId;
    @ApiModelProperty("支付类型")
    private Integer paymentType;
    /** 下单商品明细，不能为空，否则无法计算总价与生成订单详情 */
    @NotEmpty(message = "下单商品不能为空")
    @ApiModelProperty(value = "下单商品列表", required = true)
    private List<OrderDetailDTO> details;
}
