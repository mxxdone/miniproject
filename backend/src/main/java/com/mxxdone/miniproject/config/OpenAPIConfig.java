package com.mxxdone.miniproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 정의 (JWT 방식)
        String jwt = "JWT";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // Cookie 스키마 정의
        String cookieAuth = "cookieAuth";
        SecurityScheme cookieScheme = new SecurityScheme()
                .name("refreshToken")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE);

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(jwt, securityScheme)
                        .addSecuritySchemes(cookieAuth, cookieScheme))
                .addSecurityItem(new SecurityRequirement().addList(jwt))
                .addSecurityItem(new SecurityRequirement().addList(cookieAuth))
                .info(new Info()
                        .title("MiniProject API")
                        .description("미니프로젝트 API 명세서")
                        .version("1.0.0"));
    }
}
