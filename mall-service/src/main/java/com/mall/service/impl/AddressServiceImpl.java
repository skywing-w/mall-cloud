package com.mall.service.impl;

import com.mall.domain.po.Address;
import com.mall.mapper.AddressMapper;
import com.mall.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 收货地址 服务实现类
 * </p>
 * <p>
 * 地址的增删改查均可由 MyBatis-Plus 通用方法完成，无需额外实现；
 * 归属校验放在 Controller 层，避免在通用 CRUD 中混入鉴权逻辑。
 * </p>
 *
 * @author wwty
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

}
