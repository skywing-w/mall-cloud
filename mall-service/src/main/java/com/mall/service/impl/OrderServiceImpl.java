package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BadRequestException;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.utils.UserContext;
import com.mall.domain.dto.ItemDTO;
import com.mall.domain.dto.OrderDetailDTO;
import com.mall.domain.dto.OrderFormDTO;
import com.mall.domain.po.Order;
import com.mall.domain.po.OrderDetail;
import com.mall.mapper.OrderMapper;
import com.mall.service.ICartService;
import com.mall.service.IItemService;
import com.mall.service.IOrderDetailService;
import com.mall.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author wwty
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    /** 订单状态：1 待支付 */
    private static final int STATUS_UNPAID = 1;

    /** 订单状态：2 已支付 */
    private static final int STATUS_PAID = 2;

    private final IItemService itemService;
    private final IOrderDetailService detailService;
    private final ICartService cartService;

    /**
     * 创建订单。
     * <p>
     * 全流程在同一事务内完成：写订单 → 写订单详情 → 清购物车 → 扣库存。
     * 库存扣减放在最后，任一步失败都会整体回滚，避免出现「扣了钱没减库存」的脏数据。
     *
     * @param orderFormDTO 下单表单
     * @return 新订单 id
     * @throws BadRequestException 商品不存在
     * @throws com.mall.common.exception.BizIllegalException 库存不足
     */
    @Override
    @Transactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        var order = new Order();
        // 1.1.查询商品
        var detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        var itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        var itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        var items = itemService.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        var total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(STATUS_UNPAID);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        cartService.removeByItemIds(itemIds);

        // 4.扣减库存：把原始异常作为 cause 保留，便于排查是库存不足还是数据库异常
        try {
            itemService.deductStock(detailDTOS);
        } catch (Exception e) {
            log.error("扣减库存失败，orderId:{}, itemIds:{}", order.getId(), itemIds, e);
            throw new BizIllegalException("库存不足！", e);
        }
        log.info("订单创建成功，orderId:{}, userId:{}, totalFee:{}", order.getId(), order.getUserId(), total);
        return order.getId();
    }

    /**
     * 标记订单为已支付。
     *
     * @param orderId 订单 id
     */
    @Override
    public void markOrderPaySuccess(Long orderId) {
        var order = new Order();
        order.setId(orderId);
        order.setStatus(STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    /**
     * 构建订单详情列表，下单时的商品快照（名称、规格、单价、图片）会固化到详情表，
     * 避免商品后续改价影响历史订单展示。
     *
     * @param orderId 订单 id
     * @param items   商品列表
     * @param numMap  商品 id 与购买数量
     * @return 订单详情列表
     */
    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        var details = new ArrayList<OrderDetail>(items.size());
        for (ItemDTO item : items) {
            var detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
