package com.mall.controller;


import cn.hutool.core.util.StrUtil;
import com.mall.common.domain.PageDTO;
import com.mall.domain.dto.ItemDTO;
import com.mall.domain.po.Item;
import com.mall.domain.query.ItemPageQuery;
import com.mall.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品搜索接口。
 * <p>
 * 基于 MyBatis-Plus 条件构造器实现关键字、品牌、分类、价格区间的组合筛选，仅返回已上架商品。
 */
@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    /** 商品状态：1 表示已上架，搜索只返回上架商品 */
    private static final int STATUS_ON_SALE = 1;

    private final IItemService itemService;

    /**
     * 搜索商品。
     *
     * @param query 搜索与分页条件
     * @return 商品分页结果
     */
    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        // 分页查询：各条件均为「有值才拼接」，key 为商品名称模糊匹配
        var result = itemService.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .eq(Item::getStatus, STATUS_ON_SALE)
                // 价格区间：最低价、最高价各自有值才拼接，支持只传单边
                .ge(query.getMinPrice() != null, Item::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, Item::getPrice, query.getMaxPrice())
                .page(query.toMpPage("update_time", false));
        // 封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }
}
