package com.mall.controller;

import com.mall.common.exception.BizIllegalException;
import com.mall.domain.dto.PayApplyDTO;
import com.mall.domain.dto.PayOrderFormDTO;
import com.mall.enums.PayType;
import com.mall.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付相关接口。
 * <p>
 * 采用「先生成支付单、再按支付单支付」的两段式流程，支付单生成时做幂等处理，
 * 避免重复点击产生多笔支付记录。
 */
@Api(tags = "支付相关接口")
@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final IPayOrderService payOrderService;

    /**
     * 生成（或复用）支付单。
     *
     * @param applyDTO 支付申请信息
     * @return 支付单 id
     */
    @ApiOperation("生成支付单")
    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO applyDTO) {
        if (!PayType.BALANCE.equalsValue(applyDTO.getPayType())) {
            // 目前只支持余额支付
            throw new BizIllegalException("抱歉，目前只支持余额支付");
        }
        return payOrderService.applyPayOrder(applyDTO);
    }

    /**
     * 使用用户余额支付指定支付单。
     *
     * @param id               支付单 id
     * @param payOrderFormDTO  支付表单（含支付密码）
     */
    @ApiOperation("尝试基于用户余额支付")
    @ApiImplicitParam(value = "支付单id", name = "id")
    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO) {
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }
}
