package com.academy.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * 애플리케이션 시작 시 시간대 정보를 로깅하는 컴포넌트
 */
@Slf4j
@Component
@Order(1) // DataInitializer보다 먼저 실행
public class TimeZoneInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("==================== 시간대 정보 ====================");
        log.info("JVM default zone: {}", ZoneId.systemDefault());
        log.info("JVM TimeZone: {}", TimeZone.getDefault().getID());
        log.info("LocalDateTime.now(): {}", LocalDateTime.now());
        log.info("KST now: {}", LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        log.info("====================================================");
    }
}