package com.mall.controller;

import com.mall.domain.dto.LoginFormDTO;
import com.mall.domain.vo.UserLoginVO;
import com.mall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

/**
 * 用户相关接口。
 * <p>
 * 提供登录、余额扣减等能力；除登录外，其余接口均需携带 JWT，由 LoginInterceptor 统一校验。
 */
@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;

    /**
     * 用户登录。
     *
     * @param loginFormDTO 登录表单（用户名、密码）
     * @return 登录结果，包含用户 id、用户名、余额与 access-token
     */
    @ApiOperation("用户登录接口")
    @PostMapping("login")
    public UserLoginVO login(@RequestBody @Validated LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    /**
     * 扣减当前登录用户的余额。
     *
     * @param pw     支付密码
     * @param amount 扣减金额（单位：分），必须为正数
     */
    @ApiOperation("扣减余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pw", value = "支付密码"),
            @ApiImplicitParam(name = "amount", value = "支付金额")
    })
    @PutMapping("/money/deduct")
    public void deductMoney(@RequestParam("pw") String pw,
                            @RequestParam("amount") @Min(value = 1, message = "扣减金额必须大于0") Integer amount) {
        userService.deductMoney(pw, amount);
    }
}

