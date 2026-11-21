package com.mall.controller;


import com.mall.common.exception.BadRequestException;
import com.mall.common.utils.BeanUtils;
import com.mall.common.utils.CollUtils;
import com.mall.common.utils.UserContext;
import com.mall.domain.dto.AddressDTO;
import com.mall.service.IAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址管理接口。
 * <p>
 * 地址属用户私有数据，所有查询都会校验归属，避免通过猜测 id 越权读取他人地址。
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Api(tags = "收货地址管理接口")
public class AddressController {

    private final IAddressService addressService;

    /**
     * 根据 id 查询地址，并校验该地址是否属于当前登录用户。
     *
     * @param id 地址 id
     * @return 地址信息
     * @throws BadRequestException 地址不存在或不属于当前用户时抛出
     */
    @ApiOperation("根据id查询地址")
    @GetMapping("{addressId}")
    public AddressDTO findAddressById(@ApiParam("地址id") @PathVariable("addressId") Long id) {
        // 1.根据id查询
        var address = addressService.getById(id);
        if (address == null) {
            // 地址不存在时直接返回业务异常，避免后续 address.getUserId() 触发空指针
            throw new BadRequestException("地址不存在");
        }
        // 2.判断当前用户，防止越权读取他人地址
        var userId = UserContext.getUser();
        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("地址不属于当前登录用户");
        }
        return BeanUtils.copyBean(address, AddressDTO.class);
    }

    /**
     * 查询当前登录用户的收货地址列表。
     *
     * @return 地址列表，为空时返回不可变空列表
     */
    @ApiOperation("查询当前用户地址列表")
    @GetMapping
    public List<AddressDTO> findMyAddresses() {
        // 1.查询列表
        var list = addressService.query().eq("user_id", UserContext.getUser()).list();
        // 2.判空
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        // 3.转vo
        return BeanUtils.copyList(list, AddressDTO.class);
    }
}
