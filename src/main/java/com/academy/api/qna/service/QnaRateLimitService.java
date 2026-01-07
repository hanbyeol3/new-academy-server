package com.academy.api.qna.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * QnA 비밀번호 검증 Rate Limiting 서비스.
 * 
 * IP 주소별로 비밀번호 검증 시도 횟수를 제한하여 무차별 대입 공격(Brute Force)을 방지합니다.
 * 
 * 제한 정책:
 * - 1시간 내 최대 5회 시도 허용
 * - 제한 초과 시 1시간 동안 차단
 * - 성공 시 시도 횟수 초기화
 * 
 * 주의사항:
 * - 현재는 메모리 기반 구현으로 서버 재시작 시 초기화됨
 * - 운영 환경에서는 Redis 등 영구 저장소 활용 권장
 */
@Slf4j
@Service
public class QnaRateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_HOURS = 1;
    
    // IP 주소별 시도 횟수 저장 (메모리 기반)
    private final ConcurrentHashMap<String, AttemptInfo> attemptMap = new ConcurrentHashMap<>();
    
    /**
     * 비밀번호 검증 시도 가능 여부 확인.
     * 
     * @param ipAddress 클라이언트 IP 주소
     * @return 시도 가능하면 true
     */
    public boolean isAttemptAllowed(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        
        AttemptInfo info = attemptMap.get(ipAddress);
        if (info == null) {
            return true;
        }
        
        // 차단 시간이 지났는지 확인
        if (info.isLockoutExpired()) {
            log.info("[QnaRateLimit] 차단 시간 만료로 접근 허용. IP={}", ipAddress);
            attemptMap.remove(ipAddress);
            return true;
        }
        
        // 최대 시도 횟수 초과 확인
        if (info.getAttemptCount() >= MAX_ATTEMPTS) {
            log.warn("[QnaRateLimit] 최대 시도 횟수 초과로 차단. IP={}, attempts={}", ipAddress, info.getAttemptCount());
            return false;
        }
        
        return true;
    }
    
    /**
     * 비밀번호 검증 실패 시 시도 횟수 증가.
     * 
     * @param ipAddress 클라이언트 IP 주소
     */
    public void recordFailedAttempt(String ipAddress) {
        if (ipAddress == null) {
            return;
        }
        
        AttemptInfo info = attemptMap.computeIfAbsent(ipAddress, k -> new AttemptInfo());
        info.incrementAttempt();
        
        log.info("[QnaRateLimit] 실패 시도 기록. IP={}, attempts={}/{}", 
                ipAddress, info.getAttemptCount(), MAX_ATTEMPTS);
        
        if (info.getAttemptCount() >= MAX_ATTEMPTS) {
            info.setLockoutTime(LocalDateTime.now().plusHours(LOCKOUT_HOURS));
            log.warn("[QnaRateLimit] IP 주소 차단 시작. IP={}, lockoutUntil={}", 
                    ipAddress, info.getLockoutTime());
        }
    }
    
    /**
     * 비밀번호 검증 성공 시 시도 횟수 초기화.
     * 
     * @param ipAddress 클라이언트 IP 주소
     */
    public void recordSuccessfulAttempt(String ipAddress) {
        if (ipAddress == null) {
            return;
        }
        
        AttemptInfo removed = attemptMap.remove(ipAddress);
        if (removed != null) {
            log.info("[QnaRateLimit] 성공으로 시도 횟수 초기화. IP={}", ipAddress);
        }
    }
    
    /**
     * 남은 시도 횟수 조회.
     * 
     * @param ipAddress 클라이언트 IP 주소
     * @return 남은 시도 횟수
     */
    public int getRemainingAttempts(String ipAddress) {
        if (ipAddress == null) {
            return MAX_ATTEMPTS;
        }
        
        AttemptInfo info = attemptMap.get(ipAddress);
        if (info == null || info.isLockoutExpired()) {
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - info.getAttemptCount());
    }
    
    /**
     * 차단 해제까지 남은 시간 조회 (분 단위).
     * 
     * @param ipAddress 클라이언트 IP 주소
     * @return 남은 시간 (분), 차단되지 않았으면 0
     */
    public long getLockoutMinutesRemaining(String ipAddress) {
        if (ipAddress == null) {
            return 0;
        }
        
        AttemptInfo info = attemptMap.get(ipAddress);
        if (info == null || info.getLockoutTime() == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (info.getLockoutTime().isBefore(now)) {
            return 0;
        }
        
        return java.time.Duration.between(now, info.getLockoutTime()).toMinutes();
    }
    
    /**
     * 시도 정보를 저장하는 내부 클래스.
     */
    private static class AttemptInfo {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private volatile LocalDateTime lockoutTime;
        
        public int incrementAttempt() {
            return attemptCount.incrementAndGet();
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
        
        public LocalDateTime getLockoutTime() {
            return lockoutTime;
        }
        
        public void setLockoutTime(LocalDateTime lockoutTime) {
            this.lockoutTime = lockoutTime;
        }
        
        public boolean isLockoutExpired() {
            return lockoutTime == null || LocalDateTime.now().isAfter(lockoutTime);
        }
    }
}