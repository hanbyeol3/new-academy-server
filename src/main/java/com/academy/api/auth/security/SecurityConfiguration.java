package com.academy.api.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				// =====================
				// 🔥 1. CORS + CSRF
				// =====================
				.cors(cors -> cors.configurationSource(corsConfigSource()))
				.csrf(AbstractHttpConfigurer::disable)

				// =====================
				// 🔥 2. Stateless JWT 환경
				// =====================
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)

				// =====================
				// 🔥 3. URL 권한 설정
				// =====================
				.authorizeHttpRequests(auth -> auth

						// ✅ 인증 불필요 (로그인/회원가입/토큰)
						.requestMatchers("/api/auth/**").permitAll()

						// ✅ 헬스체크/문서/콘솔
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui-custom.js", "/swagger-helper.js").permitAll()
						.requestMatchers("/h2-console/**").permitAll()

						// ✅ 공개 API
						.requestMatchers("/api/qna-simple/**").permitAll()
						.requestMatchers("/api/notices/**").permitAll()
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers("/api/explanations/**").permitAll()
						.requestMatchers("/api/gallery/**").permitAll()
						.requestMatchers("/api/academic-schedules/**").permitAll()
						.requestMatchers("/api/facility/**").permitAll()
						.requestMatchers("/api/shuttle-routes/**").permitAll()
						.requestMatchers("/api/teachers/**").permitAll()
						.requestMatchers("/api/popups/active").permitAll()
						.requestMatchers("/api/inquiries/**").permitAll()

						// ✅ 관리자 API (ADMIN 권한 필요)
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						// ✅ 그 외는 모두 인증 필요
						.anyRequest().authenticated()
				)

				// =====================
				// 🔥 4. JWT 인증 필터
				// =====================
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				// =====================
				// 🔥 5. 예외 처리
				// =====================
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
						.accessDeniedHandler(jwtAccessDeniedHandler)
				)

				// =====================
				// 🔥 6. H2 콘솔 옵션
				// =====================
				.headers(headers ->
						headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
				);

		return http.build();
	}

	// =====================
	// 🔥 CORS 설정 (여기 한 군데만!)
	// =====================
	@Bean
	public CorsConfigurationSource corsConfigSource() {
		CorsConfiguration config = new CorsConfiguration();

		// ⚠️ 여기 Origin에 네 프론트 포트를 전부 추가
		config.setAllowedOriginPatterns(List.of(
				"http://localhost:3000",   // web
				"http://localhost:3001",   // 혹시 다른 dev
				"http://localhost:3002",   // admin (지금 이거 중요)
				"http://localhost:5173",
				"http://127.0.0.1:3000",
				"http://127.0.0.1:3001",
				"http://127.0.0.1:3002",
				"http://127.0.0.1:5173"
		));

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// 전체 경로에 CORS 적용
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	// =====================
	// 🔒 비밀번호 인코더
	// =====================
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
}
