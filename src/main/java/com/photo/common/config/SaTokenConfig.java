package com.photo.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限拦截器配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 公开接口放行
            SaRouter.match("/auth/**").stop();
            SaRouter.match("/categories/**").stop();
            SaRouter.match("/photos").stop();
            SaRouter.match("/photos/search").stop();
            SaRouter.match("/photos/{id}").stop();
            // Knife4j 文档放行
            SaRouter.match("/doc.html/**").stop();
            SaRouter.match("/webjars/**").stop();
            SaRouter.match("/v3/api-docs/**").stop();

            // 需登录接口
            SaRouter.match("/**").check(r -> StpUtil.checkLogin());

            // 管理员接口
            SaRouter.match("/admin/**").check(r -> StpUtil.checkRole("admin"));

            // 摄影师接口
            SaRouter.match("/studio/**").check(r -> StpUtil.checkRoleOr("photographer", "admin"));
        })).addPathPatterns("/**");
    }
}
