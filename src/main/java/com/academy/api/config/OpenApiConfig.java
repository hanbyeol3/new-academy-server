package com.academy.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
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
                             "username": "superadmin", 
                             "password": "password123!"
                           }
                           ```
                        
                        2. **토큰 설정**: 응답에서 `accessToken` 복사
                        
                        3. **Authorize**: 우상단 🔒 버튼 클릭 → Bearer 필드에 토큰 붙여넣기 (Bearer 접두사 없이)
                        
                        4. **완료**: 모든 관리자 API 사용 가능!
                        
                        ## 📋 테스트 계정
                        - 관리자: `superadmin` / `password123!`
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
}