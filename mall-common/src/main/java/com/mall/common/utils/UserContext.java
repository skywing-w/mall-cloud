package com.mall.common.utils;

/**
 * 登录用户上下文。
 * <p>
 * 基于 {@link ThreadLocal} 保存当前请求的登录用户 id，由 LoginInterceptor 在请求进入时写入、
 * 请求结束时清理。仅在同一请求线程内有效，异步线程/线程池中取不到值。
 */
public class UserContext {

    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到ThreadLocal
     * @param userId 用户id
     */
    public static void setUser(Long userId) {
        tl.set(userId);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户id，未登录时为 null
     */
    public static Long getUser() {
        return tl.get();
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        tl.remove();
    }
}
