package com.mall.interceptor;

import com.mall.common.utils.UserContext;
import com.mall.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录校验拦截器。
 * <p>
 * 从请求头 {@code authorization} 中取出 JWT 并解析出用户 id，写入 {@link UserContext} 供后续业务读取；
 * 请求结束后必须清理 ThreadLocal，否则线程复用会把用户信息串到下一个请求（脏读）。
 */
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    /** 存放 access-token 的请求头名称 */
    private static final String TOKEN_HEADER = "authorization";

    private final JwtTool jwtTool;

    /**
     * 请求进入前校验 token。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  目标处理器
     * @return 校验通过返回 true，继续执行后续流程
     * @throws UnauthorizedException token 缺失或无效
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的 token
        var token = request.getHeader(TOKEN_HEADER);
        // 2.校验token
        var userId = jwtTool.parseToken(token);
        // 3.存入上下文
        UserContext.setUser(userId);
        // 4.放行
        return true;
    }

    /**
     * 请求结束后清理用户上下文，防止线程复用导致的用户信息串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        UserContext.removeUser();
    }
}
