package com.academy.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 데이터베이스 설정.
 * 
 * MySQL 연결에 실패할 경우 자동으로 H2 인메모리 데이터베이스로 fallback합니다.
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    /**
     * 운영 환경에서 MySQL 우선, 실패 시 H2 fallback 데이터소스.
     */
    @Bean
    @Primary
    @Profile("!local")
    public DataSource dataSource() {
        // 먼저 MySQL 연결 시도
        try {
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
            
            // 연결 테스트
            mysqlDataSource.getConnection().close();
            log.info("[DatabaseConfig] ✅ MySQL 데이터베이스 연결 성공!");
            return mysqlDataSource;
            
        } catch (Exception e) {
            log.warn("[DatabaseConfig] ❌ MySQL 데이터베이스 연결 실패: {}", e.getMessage());
            log.info("[DatabaseConfig] 🔄 H2 인메모리 데이터베이스로 fallback합니다.");
            
            // H2 인메모리 데이터베이스로 fallback
            HikariConfig h2Config = new HikariConfig();
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setJdbcUrl("jdbc:h2:mem:academy;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(10);
            h2Config.setMinimumIdle(1);
            h2Config.setIdleTimeout(300000);
            h2Config.setConnectionTimeout(20000);
            
            HikariDataSource h2DataSource = new HikariDataSource(h2Config);
            log.info("[DatabaseConfig] ✅ H2 인메모리 데이터베이스 연결 성공!");
            return h2DataSource;
        }
    }

    /**
     * local 프로파일에서는 H2를 직접 사용.
     */
    @Bean
    @Primary
    @Profile("local")
    public DataSource localDataSource() {
        log.info("[DatabaseConfig] Local 프로파일: H2 인메모리 데이터베이스를 사용합니다.");
        
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