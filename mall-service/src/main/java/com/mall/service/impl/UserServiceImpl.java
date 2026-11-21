package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BadRequestException;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.exception.ForbiddenException;
import com.mall.common.utils.UserContext;
import com.mall.config.JwtProperties;
import com.mall.domain.dto.LoginFormDTO;
import com.mall.domain.po.User;
import com.mall.domain.vo.UserLoginVO;
import com.mall.enums.UserStatus;
import com.mall.mapper.UserMapper;
import com.mall.service.IUserService;
import com.mall.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author wwty
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    /**
     * 用户登录：校验用户状态与密码，成功后签发 access-token。
     *
     * @param loginDTO 登录表单
     * @return 登录结果（用户 id、用户名、余额、token）
     * @throws IllegalArgumentException 用户不存在
     * @throws ForbiddenException       用户已被冻结
     * @throws BadRequestException      密码错误
     */
    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {
        // 1.数据校验
        var username = loginDTO.getUsername();
        var password = loginDTO.getPassword();
        // 2.根据用户名或手机号查询
        var user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 5.生成TOKEN
        var token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        // 6.封装VO返回
        var vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        log.info("用户登录成功，userId:{}", user.getId());
        return vo;
    }

    /**
     * 扣减当前登录用户余额。
     * <p>
     * 扣减在数据库层通过带条件的 UPDATE 完成（余额充足才更新），返回影响行数为 0 即视为余额不足，
     * 这样可避免「先查后扣」在并发下的超扣问题。
     *
     * @param pw       支付密码（明文，与库中 BCrypt 密文比对）
     * @param totalFee 扣减金额（单位：分）
     * @throws BizIllegalException 支付密码错误或余额不足
     */
    @Override
    public void deductMoney(String pw, Integer totalFee) {
        var userId = UserContext.getUser();
        log.info("开始扣款，userId:{}, amount:{}", userId, totalFee);
        // 1.校验密码
        var user = getById(userId);
        if (user == null || !passwordEncoder.matches(pw, user.getPassword())) {
            // 密码错误
            throw new BizIllegalException("用户密码错误");
        }

        // 2.尝试扣款
        int i = baseMapper.updateMoney(userId, totalFee);
        if (i <= 0) {
            throw new BizIllegalException("扣款失败，可能是余额不足！");
        }
        log.info("扣款成功，userId:{}, amount:{}", userId, totalFee);
    }
}
