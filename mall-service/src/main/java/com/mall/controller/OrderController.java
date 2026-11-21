package com.mall.controller;

import com.mall.common.utils.BeanUtils;
import com.mall.domain.dto.OrderFormDTO;
import com.mall.domain.vo.OrderVO;
import com.mall.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 订单管理接口。
 * <p>
 * 创建订单会在同一事务内完成：写订单、写订单详情、清购物车、扣库存。
 */
@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    /**
     * 根据 id 查询订单。
     *
     * @param orderId 订单 id
     * @return 订单信息，不存在时返回 null
     */
    @ApiOperation("根据id查询订单")
    @GetMapping("{id}")
    public OrderVO queryOrderById(@PathVariable("id") Long orderId) {
        return BeanUtils.copyBean(orderService.getById(orderId), OrderVO.class);
    }

    /**
     * 创建订单。
     *
     * @param orderFormDTO 下单表单（收货地址、支付方式、商品明细）
     * @return 新订单 id
     */
    @ApiOperation("创建订单")
    @PostMapping
    public Long createOrder(@RequestBody @Valid OrderFormDTO orderFormDTO) {
        return orderService.createOrder(orderFormDTO);
    }

    /**
     * 标记订单为已支付，供支付回调/余额支付成功后调用。
     *
     * @param orderId 订单 id
     */
    @ApiOperation("标记订单已支付")
    @ApiImplicitParam(name = "orderId", value = "订单id", paramType = "path")
    @PutMapping("/{orderId}")
    public void markOrderPaySuccess(@PathVariable("orderId") Long orderId) {
        orderService.markOrderPaySuccess(orderId);
    }
}
