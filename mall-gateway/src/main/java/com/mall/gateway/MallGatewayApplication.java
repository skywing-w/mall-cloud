package com.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城网关启动类。
 * <p>
 * 所有外部请求的统一入口，负责路由转发、跨域处理与服务发现；下游服务不直接对外暴露端口。
 */
@SpringBootApplication
public class MallGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallGatewayApplication.class, args);
    }
}
