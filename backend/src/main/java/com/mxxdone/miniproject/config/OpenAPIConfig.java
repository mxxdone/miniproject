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

        // 보안 요구사항 정의 (모든 API에 이 보안 스키마 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(jwt, securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("MiniProject API")
                        .description("미니프로젝트 API 명세서")
                        .version("1.0.0"));
    }
}
