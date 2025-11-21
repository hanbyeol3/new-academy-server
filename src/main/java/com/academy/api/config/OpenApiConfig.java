package com.academy.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Academy API Server",
                version = "1.0.0",
                description = """
                        Spring Boot 3.x + QueryDSL + Security 기반 Academy API 서버입니다.
                        
                        ## 🚀 빠른 인증 방법
                        1. **로그인**: `/api/auth/sign-in` 실행
                           ```json
                           {
                             "username": "testadmin", 
                             "password": "password123!"
                           }
                           ```
                        
                        2. **토큰 설정**: 응답에서 `accessToken` 복사
                        
                        3. **Authorize**: 우상단 🔒 버튼 클릭 → Bearer 필드에 토큰 붙여넣기 (Bearer 접두사 없이)
                        
                        4. **완료**: 모든 관리자 API 사용 가능!
                        
                        ## 📋 테스트 계정
                        - 관리자: `testadmin` / `password123!`
                        - 확인용: http://localhost:8080/auth-test.html
                        """
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "로컬 개발 서버"),
                @Server(url = "http://localhost:8081", description = "로컬 개발 서버 (대체 포트)")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer 토큰을 입력하세요 (Bearer 접두사 없이)"
)
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 커스터마이저.
     * 
     * - 태그를 ABC 순으로 정렬
     * - 전역 Security Requirement 설정
     * - 스웨거 UI에서 아코디언을 기본 접힌 상태로 설정
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // 태그를 ABC 순으로 정렬
            if (openApi.getTags() != null) {
                openApi.getTags().sort((tag1, tag2) -> 
                    tag1.getName().compareToIgnoreCase(tag2.getName()));
            }
            
            // 전역 Security Requirement 설정 (Admin API용)
            SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
            
            // 모든 경로에 대해 Security Requirement 추가 (admin API는 자동으로 적용)
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem -> {
                    pathItem.readOperations().forEach(operation -> {
                        // Admin API 또는 auth 보호 API에만 적용
                        if (operation.getTags() != null && operation.getTags().stream()
                                .anyMatch(tag -> tag.contains("(Admin)") || tag.contains("Auth API"))) {
                            operation.addSecurityItem(securityRequirement);
                        }
                    });
                });
            }
        };
    }
}