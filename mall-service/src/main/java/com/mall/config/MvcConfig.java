package com.mall.config;

import cn.hutool.core.collection.CollUtil;
import com.mall.interceptor.LoginInterceptor;
import com.mall.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置。
 * <p>
 * 注册登录拦截器，并按配置决定需要拦截与放行的路径；接口文档等基础设施路径固定放行，
 * 避免 swagger/knife4j 资源被登录校验拦截导致文档无法打开。
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    private final JwtTool jwtTool;
    private final AuthProperties authProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1.添加拦截器
        var loginInterceptor = new LoginInterceptor(jwtTool);
        var registration = registry.addInterceptor(loginInterceptor);
        // 2.配置拦截路径
        var includePaths = authProperties.getIncludePaths();
        if (CollUtil.isNotEmpty(includePaths)) {
            registration.addPathPatterns(includePaths);
        }
        // 3.配置放行路径
        var excludePaths = authProperties.getExcludePaths();
        if (CollUtil.isNotEmpty(excludePaths)) {
            registration.excludePathPatterns(excludePaths);
        }
        registration.excludePathPatterns(
                "/error",
                "/favicon.ico",
                "/v2/**",
                "/v3/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/doc.html"
                );

    }
}
