package com.mall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连通性测试接口。
 * <p>
 * 用于验证服务是否正常启动、网关转发是否可达，并顺带演示按来源 IP 计数。
 */
@RestController
@RequestMapping("hi")
public class HelloController {

    /** 模拟业务耗时，便于观察网关超时与熔断配置是否生效（单位：毫秒） */
    private static final long MOCK_COST_MILLIS = 300L;

    /**
     * 按来源 IP 累加访问次数。
     * <p>
     * 使用 ConcurrentHashMap 而非 HashMap：该 Map 是单例 Bean 的成员变量，
     * 会被多个请求线程并发读写，HashMap 在并发扩容时可能出现数据丢失甚至死循环。
     */
    private final Map<String, AtomicInteger> countMap = new ConcurrentHashMap<>();

    @GetMapping
    public String hello(HttpServletRequest request) throws InterruptedException {
        Thread.sleep(MOCK_COST_MILLIS);
        var ip = request.getRemoteAddr();
        // computeIfAbsent 保证「判断 + 插入」的原子性，避免并发下重复创建计数器
        var ai = countMap.computeIfAbsent(ip, key -> new AtomicInteger(0));
        return String.format("<h5>欢迎访问商城, 这是您第%d次访问<h5>", ai.incrementAndGet());
    }
}
