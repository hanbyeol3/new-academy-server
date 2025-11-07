package com.academy.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * 데이터베이스 설정.
 * 
 * 모든 환경에서 MySQL만 사용합니다.
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    /**
     * MySQL 데이터소스 (모든 환경에서 사용).
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.h2.Driver", matchIfMissing = true)
    public DataSource dataSource() {
        log.info("[DatabaseConfig] MySQL 데이터베이스 연결을 시도합니다.");
        
        HikariConfig mysqlConfig = new HikariConfig();
        mysqlConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        mysqlConfig.setJdbcUrl("jdbc:mysql://localhost:3306/academy?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=utf8&allowPublicKeyRetrieval=true");
        mysqlConfig.setUsername("root");
        mysqlConfig.setPassword("root-pass-123!");
        mysqlConfig.setMaximumPoolSize(20);
        mysqlConfig.setMinimumIdle(5);
        mysqlConfig.setIdleTimeout(300000);
        mysqlConfig.setConnectionTimeout(5000);
        mysqlConfig.setValidationTimeout(3000);
        mysqlConfig.setInitializationFailTimeout(1);
        
        HikariDataSource mysqlDataSource = new HikariDataSource(mysqlConfig);
        
        try {
            // 연결 테스트
            mysqlDataSource.getConnection().close();
            log.info("[DatabaseConfig] ✅ MySQL 데이터베이스 연결 성공!");
        } catch (Exception e) {
            log.error("[DatabaseConfig] ❌ MySQL 데이터베이스 연결 실패: {}", e.getMessage());
            throw new RuntimeException("MySQL 데이터베이스 연결에 실패했습니다. 데이터베이스 서버를 확인해주세요.", e);
        }
        
        return mysqlDataSource;
    }

    /**
     * 테스트용 H2 데이터소스 (테스트 프로파일에서만 사용).
     */
    @Bean
    @Primary
    @Profile("test")
    public DataSource testDataSource() {
        log.info("[DatabaseConfig] 테스트 프로파일: H2 인메모리 데이터베이스를 사용합니다.");
        
        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:mem:academy;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(5);
        h2Config.setMinimumIdle(2);
        h2Config.setIdleTimeout(300000);
        h2Config.setConnectionTimeout(20000);
        
        return new HikariDataSource(h2Config);
    }
}