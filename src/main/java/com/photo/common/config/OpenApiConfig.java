package com.photo.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("摄影作品平台 API")
                        .description("微信小程序摄影作品交易与授权平台后端接口文档")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("sa-token"))
                .schemaRequirement("sa-token", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("token")
                        .description("Sa-Token 认证，填入登录返回的 token"));
    }
}
