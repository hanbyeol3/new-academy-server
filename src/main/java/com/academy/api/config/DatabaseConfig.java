package com.academy.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 데이터베이스 설정.
 *
 * Spring Boot의 자동 설정(auto-configuration)을 사용합니다.
 * 데이터소스 설정은 application-{profile}.yml에서 관리합니다.
 *
 * - local 프로파일: application-local.yml (MySQL)
 * - test 프로파일: application-test.yml (H2 인메모리)
 */
@Slf4j
@Configuration
public class DatabaseConfig {
    // Spring Boot auto-configuration이 application-{profile}.yml 기반으로 DataSource를 생성합니다.
}
