package com.mall.controller;


import com.mall.domain.dto.CartFormDTO;
import com.mall.domain.po.Cart;
import com.mall.domain.vo.CartVO;
import com.mall.service.ICartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 购物车相关接口。
 * <p>
 * 所有接口都以当前登录用户为维度操作，用户身份由 LoginInterceptor 解析 JWT 后写入 UserContext。
 */
@Api(tags = "购物车相关接口")
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    /**
     * 添加商品到购物车。已存在则累加数量，不存在则新增条目。
     *
     * @param cartFormDTO 购物车表单
     */
    @ApiOperation("添加商品到购物车")
    @PostMapping
    public void addItem2Cart(@Valid @RequestBody CartFormDTO cartFormDTO) {
        cartService.addItem2Cart(cartFormDTO);
    }

    /**
     * 更新购物车条目（如修改数量）。
     *
     * @param cart 购物车条目，必须携带 id
     */
    @ApiOperation("更新购物车数据")
    @PutMapping
    public void updateCart(@RequestBody Cart cart) {
        cartService.updateById(cart);
    }

    /**
     * 删除指定的购物车条目。
     *
     * @param id 购物车条目 id
     */
    @ApiOperation("删除购物车中商品")
    @DeleteMapping("{id}")
    public void deleteCartItem(@PathVariable("id") Long id) {
        cartService.removeById(id);
    }

    /**
     * 查询当前用户的购物车列表，并回填商品最新价格、库存与状态。
     *
     * @return 购物车列表，为空时返回空列表
     */
    @ApiOperation("查询购物车列表")
    @GetMapping
    public List<CartVO> queryMyCarts() {
        return cartService.queryMyCarts();
    }

    /**
     * 按商品 id 批量删除当前用户购物车中的条目。
     *
     * @param ids 商品 id 集合
     */
    @ApiOperation("批量删除购物车中商品")
    @ApiImplicitParam(name = "ids", value = "购物车条目id集合")
    @DeleteMapping
    public void deleteCartItemByIds(@RequestParam("ids") List<Long> ids) {
        cartService.removeByItemIds(ids);
    }
}
