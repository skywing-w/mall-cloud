package com.mall.service.impl;

import com.mall.domain.po.OrderDetail;
import com.mall.mapper.OrderDetailMapper;
import com.mall.service.IOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 * <p>
 * 订单详情仅在下单时批量写入、查询时按订单 id 读取，通用 CRUD 已覆盖，无需扩展。
 * </p>
 *
 * @author wwty
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

}
