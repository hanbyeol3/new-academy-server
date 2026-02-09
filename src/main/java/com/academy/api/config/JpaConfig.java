package com.academy.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * JPA 설정.
 *
 * Spring Boot의 자동 설정(auto-configuration)을 사용합니다.
 * JPA/Hibernate 설정은 application-{profile}.yml에서 관리합니다.
 *
 * - local 프로파일: MySQL Dialect, ddl-auto=none
 * - test 프로파일: H2 Dialect, ddl-auto=create-drop
 */
@Slf4j
@Configuration
public class JpaConfig {
    // Spring Boot auto-configuration이 application-{profile}.yml 기반으로
    // EntityManagerFactory, TransactionManager를 자동 생성합니다.
}
