package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.utils.BeanUtils;
import com.mall.domain.dto.ItemDTO;
import com.mall.domain.dto.OrderDetailDTO;
import com.mall.domain.po.Item;
import com.mall.mapper.ItemMapper;
import com.mall.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author wwty
 */
@Slf4j
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    /**
     * 批量扣减库存。
     * <p>
     * 每件商品使用带「库存充足」条件的 UPDATE 扣减，返回影响行数为 0 即说明库存不足，
     * 由方法上的事务保证已扣减的商品不会留下脏数据。
     *
     * @param items 商品 id 与扣减数量
     * @throws BizIllegalException 库存不足
     */
    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        if (items == null || items.isEmpty()) {
            // 空列表直接返回，避免无意义的循环与事务开销
            return;
        }
        for (OrderDetailDTO item : items) {
            var i = baseMapper.updateStock(item);
            if (i <= 0) {
                log.warn("库存扣减失败，itemId:{}，num:{}", item.getItemId(), item.getNum());
                throw new BizIllegalException("库存不足！");
            }
        }
    }

    /**
     * 根据 id 批量查询商品并转换为 DTO。
     *
     * @param ids 商品 id 集合
     * @return 商品列表，为空时返回不可变空列表
     */
    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
}
