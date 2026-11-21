package com.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城核心服务启动类。
 * <p>
 * 单体应用入口，包含用户、商品、购物车、订单、支付五个业务模块；
 * {@code @MapperScan} 显式声明 Mapper 扫描路径，避免多模块下自动扫描不到。
 */
@MapperScan("com.mall.mapper")
@SpringBootApplication
public class MallApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }
}