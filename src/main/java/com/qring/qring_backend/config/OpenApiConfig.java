package com.qring.qring_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** springdoc-openapi 설정 — Swagger UI에 JWT Bearer 인증 스킴을 등록한다. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /** API 메타데이터 + JWT Bearer 보안 스킴 등록. Swagger UI의 Authorize 버튼에서 토큰 입력 가능. */
    @Bean
    public OpenAPI qringOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Qring Backend API")
                .version("v1")
                .description("큐링 백엔드 API 문서. 인증이 필요한 엔드포인트는 우측 상단의 Authorize 버튼으로 JWT 액세스 토큰을 입력 후 호출하세요."))
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
