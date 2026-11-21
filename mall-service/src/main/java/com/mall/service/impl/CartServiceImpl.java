package com.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.utils.BeanUtils;
import com.mall.common.utils.CollUtils;
import com.mall.common.utils.UserContext;
import com.mall.domain.dto.CartFormDTO;
import com.mall.domain.dto.ItemDTO;
import com.mall.domain.po.Cart;
import com.mall.domain.vo.CartVO;
import com.mall.mapper.CartMapper;
import com.mall.service.ICartService;
import com.mall.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author wwty
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    /** 单个用户购物车最大条目数，防止刷接口无限添加 */
    private static final int MAX_CART_ITEMS = 10;

    private final IItemService itemService;

    /**
     * 添加商品到购物车：已存在则数量加 1，不存在则新增条目，新增前校验条目上限。
     *
     * @param cartFormDTO 购物车表单
     * @throws BizIllegalException 购物车条目已达上限
     */
    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        // 1.获取登录用户
        var userId = UserContext.getUser();

        // 2.判断是否已经存在
        if (checkItemExists(cartFormDTO.getItemId(), userId)) {
            // 2.1.存在，则更新数量
            baseMapper.updateNum(cartFormDTO.getItemId(), userId);
            return;
        }
        // 2.2.不存在，判断是否超过购物车数量
        checkCartsFull(userId);

        // 3.新增购物车条目
        // 3.1.转换PO
        var cart = BeanUtils.copyBean(cartFormDTO, Cart.class);
        // 3.2.保存当前用户
        cart.setUserId(userId);
        // 3.3.保存到数据库
        save(cart);
    }

    /**
     * 查询当前用户的购物车列表，并回填商品最新价格、库存与上架状态。
     *
     * @return 购物车列表，为空时返回不可变空列表
     */
    @Override
    public List<CartVO> queryMyCarts() {
        // 1.查询我的购物车列表
        var carts = lambdaQuery().eq(Cart::getUserId, UserContext.getUser()).list();
        if (CollUtils.isEmpty(carts)) {
            return CollUtils.emptyList();
        }

        // 2.转换VO
        var vos = BeanUtils.copyList(carts, CartVO.class);

        // 3.处理VO中的商品信息
        handleCartItems(vos);

        // 4.返回
        return vos;
    }

    /**
     * 批量回填购物车中商品的最新信息。
     * <p>
     * 采用「一次批量查询 + 内存匹配」而非循环单查，避免 N+1 查询问题。
     *
     * @param vos 购物车视图对象列表
     */
    private void handleCartItems(List<CartVO> vos) {
        // 1.获取商品id
        var itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 2.查询商品
        var items = itemService.queryItemByIds(itemIds);
        if (CollUtils.isEmpty(items)) {
            return;
        }
        // 3.转为 id 到 item的map
        var itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
        // 4.写入vo
        for (CartVO v : vos) {
            var item = itemMap.get(v.getItemId());
            if (item == null) {
                // 商品已被删除，保留原购物车记录，前端可根据 status 提示失效
                continue;
            }
            v.setNewPrice(item.getPrice());
            v.setStatus(item.getStatus());
            v.setStock(item.getStock());
        }
    }

    /**
     * 按商品 id 批量删除当前用户购物车中的条目。
     *
     * @param itemIds 商品 id 集合
     */
    @Override
    public void removeByItemIds(Collection<Long> itemIds) {
        // 1.构建删除条件，userId和itemId
        var queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, UserContext.getUser())
                .in(Cart::getItemId, itemIds);
        // 2.删除
        remove(queryWrapper);
    }

    /**
     * 校验购物车条目是否已达上限。
     *
     * @param userId 用户 id
     * @throws BizIllegalException 已达上限
     */
    private void checkCartsFull(Long userId) {
        var count = lambdaQuery().eq(Cart::getUserId, userId).count();
        if (count >= MAX_CART_ITEMS) {
            throw new BizIllegalException(StrUtil.format("用户购物车商品不能超过{}", MAX_CART_ITEMS));
        }
    }

    /**
     * 判断购物车中是否已存在该商品。
     *
     * @param itemId 商品 id
     * @param userId 用户 id
     * @return 已存在返回 true
     */
    private boolean checkItemExists(Long itemId, Long userId) {
        var count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId)
                .count();
        return count > 0;
    }
}
