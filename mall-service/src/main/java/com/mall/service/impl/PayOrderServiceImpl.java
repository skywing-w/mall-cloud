package com.mall.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.utils.BeanUtils;
import com.mall.common.utils.UserContext;
import com.mall.domain.dto.PayApplyDTO;
import com.mall.domain.dto.PayOrderFormDTO;
import com.mall.domain.po.Order;
import com.mall.domain.po.PayOrder;
import com.mall.enums.PayStatus;
import com.mall.mapper.PayOrderMapper;
import com.mall.service.IOrderService;
import com.mall.service.IPayOrderService;
import com.mall.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 支付订单 服务实现类
 * </p>
 *
 * @author wwty
 * @since 2023-05-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    /** 支付单有效期：2 小时，超时后需重新申请 */
    private static final long PAY_ORDER_TIMEOUT_MINUTES = 120L;

    private final IUserService userService;

    private final IOrderService orderService;

    /**
     * 申请支付单（幂等）。
     * <p>
     * 同一业务订单重复申请时按以下规则处理：已支付/已关闭直接拒绝；
     * 支付渠道变更则重置原支付单；其余情况直接复用原支付单。
     *
     * @param applyDTO 支付申请信息
     * @return 支付单 id
     */
    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        // 1.幂等性校验
        var payOrder = checkIdempotent(applyDTO);
        // 2.返回结果
        return payOrder.getId().toString();
    }

    /**
     * 使用账户余额支付。
     *
     * @param payOrderFormDTO 支付表单（支付单 id、支付密码）
     * @throws BizIllegalException 支付单不存在、状态异常、余额不足或状态更新失败
     */
    @Override
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        // 1.查询支付单
        var po = getById(payOrderFormDTO.getId());
        if (po == null) {
            // 支付单不存在时必须提前拦截，否则下方 po.getStatus() 会抛空指针
            throw new BizIllegalException("支付单不存在");
        }
        // 2.判断状态
        if (!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())) {
            // 订单不是未支付，状态异常
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 3.尝试扣减余额
        userService.deductMoney(payOrderFormDTO.getPw(), po.getAmount());
        // 4.修改支付单状态
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 5.修改订单状态
        var order = new Order();
        order.setId(po.getBizOrderNo());
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        orderService.updateById(order);
        log.info("余额支付成功，payOrderId:{}, bizOrderNo:{}", po.getId(), po.getBizOrderNo());
    }

    /**
     * 将支付单置为支付成功。
     * <p>
     * 通过 status 的乐观锁条件（IN 未提交、待支付）保证并发下只有一次更新成功，
     * 防止重复回调或用户重复点击导致重复扣款。
     *
     * @param id          支付单 id
     * @param successTime 支付成功时间
     * @return 更新成功返回 true
     */
    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                // 支付状态的乐观锁判断
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }


    /**
     * 幂等校验：查询业务订单是否已有支付单，并按状态决定新建、重置还是复用。
     *
     * @param applyDTO 支付申请信息
     * @return 可用的支付单
     * @throws BizIllegalException 订单已支付或已关闭
     */
    private PayOrder checkIdempotent(PayApplyDTO applyDTO) {
        // 1.首先查询支付单
        var oldOrder = queryByBizOrderNo(applyDTO.getBizOrderNo());
        // 2.判断是否存在
        if (oldOrder == null) {
            // 不存在支付单，说明是第一次，写入新的支付单并返回
            var payOrder = buildPayOrder(applyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }
        // 3.旧单已经存在，判断是否支付成功
        if (PayStatus.TRADE_SUCCESS.equalsValue(oldOrder.getStatus())) {
            // 已经支付成功，抛出异常
            throw new BizIllegalException("订单已经支付！");
        }
        // 4.旧单已经存在，判断是否已经关闭
        if (PayStatus.TRADE_CLOSED.equalsValue(oldOrder.getStatus())) {
            // 已经关闭，抛出异常
            throw new BizIllegalException("订单已关闭");
        }
        // 5.旧单已经存在，判断支付渠道是否一致
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), applyDTO.getPayChannelCode())) {
            // 支付渠道不一致，需要重置数据，然后重新申请支付单
            var payOrder = buildPayOrder(applyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        // 6.旧单已经存在，且可能是未支付或未提交，且支付渠道一致，直接返回旧数据
        return oldOrder;
    }

    /**
     * 构建新支付单，并设置有效期与初始状态。
     *
     * @param payApplyDTO 支付申请信息
     * @return 待落库的支付单
     */
    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        // 1.数据转换
        var payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        // 2.初始化数据
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(PAY_ORDER_TIMEOUT_MINUTES));
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(UserContext.getUser());
        return payOrder;
    }
    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }
}
