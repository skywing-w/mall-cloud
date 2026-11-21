package com.mall.controller;


import com.mall.common.domain.PageDTO;
import com.mall.common.domain.PageQuery;
import com.mall.common.utils.BeanUtils;
import com.mall.domain.dto.ItemDTO;
import com.mall.domain.dto.OrderDetailDTO;
import com.mall.domain.po.Item;
import com.mall.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 商品管理相关接口。
 * <p>
 * 覆盖商品的分页查询、批量查询、增删改、上下架以及库存扣减，供后台管理与下单链路调用。
 */
@Api(tags = "商品管理相关接口")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    /** 商品列表默认排序字段：按更新时间倒序，保证新商品优先展示 */
    private static final String DEFAULT_SORT_FIELD = "update_time";

    private final IItemService itemService;

    /**
     * 分页查询商品。
     *
     * @param query 分页与排序条件
     * @return 商品分页结果
     */
    @ApiOperation("分页查询商品")
    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        // 1.分页查询
        var result = itemService.page(query.toMpPage(DEFAULT_SORT_FIELD, false));
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }

    /**
     * 根据 id 批量查询商品。
     *
     * @param ids 商品 id 集合
     * @return 商品列表，不存在时返回空列表
     */
    @ApiOperation("根据id批量查询商品")
    @GetMapping
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids) {
        return itemService.queryItemByIds(ids);
    }

    /**
     * 根据 id 查询单个商品。
     *
     * @param id 商品 id
     * @return 商品信息，不存在时返回 null
     */
    @ApiOperation("根据id查询商品")
    @GetMapping("{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) {
        return BeanUtils.copyBean(itemService.getById(id), ItemDTO.class);
    }

    /**
     * 新增商品。
     *
     * @param item 商品信息
     */
    @ApiOperation("新增商品")
    @PostMapping
    public void saveItem(@RequestBody ItemDTO item) {
        // 新增
        itemService.save(BeanUtils.copyBean(item, Item.class));
    }

    /**
     * 更新商品上下架状态。
     *
     * @param id     商品 id
     * @param status 目标状态（1 上架，2 下架）
     */
    @ApiOperation("更新商品状态")
    @PutMapping("/status/{id}/{status}")
    public void updateItemStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        var item = new Item();
        item.setId(id);
        item.setStatus(status);
        itemService.updateById(item);
    }

    /**
     * 更新商品信息。
     * <p>
     * 状态字段由独立接口维护，这里强制置空，MyBatis-Plus 更新时会忽略 null 字段，
     * 从而避免通过该接口误改上下架状态。
     *
     * @param item 商品信息
     */
    @ApiOperation("更新商品")
    @PutMapping
    public void updateItem(@RequestBody ItemDTO item) {
        // 不允许修改商品状态，所以强制设置为null，更新时，就会忽略该字段
        item.setStatus(null);
        // 更新
        itemService.updateById(BeanUtils.copyBean(item, Item.class));
    }

    /**
     * 根据 id 删除商品。
     *
     * @param id 商品 id
     */
    @ApiOperation("根据id删除商品")
    @DeleteMapping("{id}")
    public void deleteItemById(@PathVariable("id") Long id) {
        itemService.removeById(id);
    }

    /**
     * 批量扣减商品库存，下单链路调用。
     *
     * @param items 商品 id 与扣减数量列表
     */
    @ApiOperation("批量扣减库存")
    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items) {
        itemService.deductStock(items);
    }
}
