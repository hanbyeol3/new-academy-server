package com.academy.api.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정.
 * 
 * JWT 기반 Stateless 인증을 위한 보안 설정을 담당합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 보안 필터 체인 설정.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화 (Stateless API)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 정책: Stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // HTTP 기본 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // Form 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable)
            
            // 요청 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 관련 API는 모두 허용
                .requestMatchers("/api/auth/**").permitAll()
                
                // Actuator health 체크 허용
                .requestMatchers("/actuator/health").permitAll()
                
                // Swagger UI 허용 (커스텀 JS/CSS 포함)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui-custom.js", "/swagger-ui-custom.css", "/swagger-helper.js").permitAll()
                
                // 인증 테스트 페이지 허용
                .requestMatchers("/auth-test.html").permitAll()
                
                // H2 콘솔 허용 (개발용)
                .requestMatchers("/h2-console/**").permitAll()
                
                // QnA 공개 API 허용 (비회원도 사용 가능)
                .requestMatchers("/api/qna-simple/**").permitAll()
                
                // 공지사항 공개 API 허용 (비회원도 사용 가능)
                .requestMatchers("/api/notices").permitAll()
                .requestMatchers("/api/notices/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // 설명회 공개 API 허용 (비회원도 사용 가능)
                .requestMatchers("/api/explanations").permitAll()
                .requestMatchers("/api/explanations/**").permitAll()
                
                // 갤러리 공개 API 허용 (비회원도 사용 가능)
                .requestMatchers("/api/gallery").permitAll()
                .requestMatchers("/api/gallery/**").permitAll()
                
                // 학사일정 공개 API 허용 (비회원도 사용 가능)
                .requestMatchers("/api/academic-schedules").permitAll()
                .requestMatchers("/api/academic-schedules/**").permitAll()
                
                // 관리자 API는 ADMIN 권한 필요
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 그 외 모든 API는 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 예외 처리
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            
            // H2 콘솔을 위한 프레임 옵션 설정
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }

    /**
     * CORS 설정.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 (프론트엔드 로컬 개발 서버)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        
        // 자격 증명 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        
        // 사전 요청(preflight) 결과 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * BCrypt 비밀번호 인코더.
     * 
     * @return BCryptPasswordEncoder (strength 12)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}