package com.academy.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * JPA 설정.
 * 
 * 데이터소스에 따라 적절한 Hibernate Dialect를 자동으로 설정합니다.
 */
@Slf4j
@Configuration
public class JpaConfig {

    @Autowired
    private JpaProperties jpaProperties;
    
    @Autowired
    private HibernateProperties hibernateProperties;

    @Bean(name = "entityManagerFactory")
    @Primary
    @Profile("!local")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) throws SQLException {
        
        // 데이터소스 타입에 따라 Dialect 설정
        String dialectClass;
        String jdbcUrl = dataSource.getConnection().getMetaData().getURL();
        
        if (jdbcUrl.contains("mysql")) {
            dialectClass = "org.hibernate.dialect.MySQLDialect";
            log.info("[JpaConfig] MySQL Dialect를 사용합니다.");
        } else {
            dialectClass = "org.hibernate.dialect.H2Dialect"; 
            log.info("[JpaConfig] H2 Dialect를 사용합니다.");
        }
        
        // JPA Properties 설정
        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
        properties.put("hibernate.dialect", dialectClass);
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        
        // H2인 경우 추가 설정
        if (jdbcUrl.contains("h2")) {
            properties.put("hibernate.hbm2ddl.auto", "create-drop");
            properties.put("spring.sql.init.mode", "always");
            log.info("[JpaConfig] H2 전용 설정이 적용되었습니다.");
        }

        return builder
                .dataSource(dataSource)
                .packages("com.academy.api")
                .persistenceUnit("academy")
                .properties(properties)
                .build();
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    @Profile("local")
    public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource localDataSource) {
        
        log.info("[JpaConfig] Local 프로파일: H2 Dialect를 사용합니다.");
        
        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("spring.sql.init.mode", "never");

        return builder
                .dataSource(localDataSource)
                .packages("com.academy.api")
                .persistenceUnit("academy")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}