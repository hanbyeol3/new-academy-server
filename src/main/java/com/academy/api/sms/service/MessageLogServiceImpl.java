package com.academy.api.sms.service;

import com.academy.api.sms.domain.MessageLog;
import com.academy.api.sms.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 메시지 로그 서비스 구현체.
 * 
 * - 모든 메시징 채널 로그 관리 (SMS, LMS, 카카오톡)
 * - 발송 상태 추적 및 업데이트
 * - 통계 데이터 생성 및 분석
 * - 배치 발송 및 예약 발송 지원
 * - 실패 메시지 재발송 지원
 * 
 * 로깅 레벨 원칙:
 *  - info: 메시지 저장, 상태 변경 등 주요 비즈니스 로직
 *  - debug: 조회 결과 요약, 처리 단계별 상세 정보
 *  - warn: 존재하지 않는 리소스, 예상 가능한 예외 상황
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageLogServiceImpl implements MessageLogService {

    private final MessageLogRepository messageLogRepository;

    @Override
    @Transactional
    public MessageLog saveMessageLog(MessageLog messageLog) {
        log.info("[MessageLogService] 메시지 로그 저장 시작. channel={}, toPhone={}, purposeCode={}", 
                messageLog.getChannel(), messageLog.getToPhone(), messageLog.getPurposeCode());
        
        try {
            MessageLog savedLog = messageLogRepository.save(messageLog);
            log.debug("[MessageLogService] 메시지 로그 저장 완료. id={}", savedLog.getId());
            return savedLog;
        } catch (Exception e) {
            log.error("[MessageLogService] 메시지 로그 저장 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<MessageLog> getMessageLogById(Long id) {
        log.debug("[MessageLogService] 메시지 로그 조회. id={}", id);
        
        Optional<MessageLog> messageLog = messageLogRepository.findById(id);
        if (messageLog.isEmpty()) {
            log.warn("[MessageLogService] 메시지 로그를 찾을 수 없음. id={}", id);
        }
        
        return messageLog;
    }

    @Override
    public Page<MessageLog> getMessageLogsByPhone(String toPhone, Pageable pageable) {
        log.debug("[MessageLogService] 전화번호별 메시지 로그 조회. toPhone={}, page={}, size={}", 
                toPhone, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageLog> logPage = messageLogRepository.findByToPhoneOrderByCreatedAtDesc(toPhone, pageable);
        log.debug("[MessageLogService] 전화번호별 조회 완료. toPhone={}, 총 {}건, 현재 페이지 {}건", 
                toPhone, logPage.getTotalElements(), logPage.getNumberOfElements());
        
        return logPage;
    }

    @Override
    public Page<MessageLog> getMessageLogsByPurpose(String purposeCode, Pageable pageable) {
        log.debug("[MessageLogService] 목적 코드별 메시지 로그 조회. purposeCode={}, page={}, size={}", 
                purposeCode, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageLog> logPage = messageLogRepository.findByPurposeCodeOrderByCreatedAtDesc(purposeCode, pageable);
        log.debug("[MessageLogService] 목적 코드별 조회 완료. purposeCode={}, 총 {}건, 현재 페이지 {}건", 
                purposeCode, logPage.getTotalElements(), logPage.getNumberOfElements());
        
        return logPage;
    }

    @Override
    public List<MessageLog> getMessageLogsByBatch(String batchId) {
        log.debug("[MessageLogService] 배치별 메시지 로그 조회. batchId={}", batchId);
        
        List<MessageLog> logs = messageLogRepository.findByBatchIdOrderByBatchSeqAsc(batchId);
        log.debug("[MessageLogService] 배치별 조회 완료. batchId={}, 총 {}건", batchId, logs.size());
        
        return logs;
    }

    @Override
    public Optional<MessageLog> getMessageLogByProviderMessageId(String providerMessageId) {
        log.debug("[MessageLogService] 업체 메시지 ID로 조회. providerMessageId={}", providerMessageId);
        
        Optional<MessageLog> messageLog = messageLogRepository.findByProviderMessageId(providerMessageId);
        if (messageLog.isEmpty()) {
            log.warn("[MessageLogService] 업체 메시지 ID로 메시지 로그를 찾을 수 없음. providerMessageId={}", providerMessageId);
        }
        
        return messageLog;
    }

    @Override
    public List<MessageLog> getMessageLogsByReference(String refType, Long refId) {
        log.debug("[MessageLogService] 참조 정보로 메시지 로그 조회. refType={}, refId={}", refType, refId);
        
        List<MessageLog> logs = messageLogRepository.findByRefTypeAndRefIdOrderByCreatedAtDesc(refType, refId);
        log.debug("[MessageLogService] 참조 정보별 조회 완료. refType={}, refId={}, 총 {}건", refType, refId, logs.size());
        
        return logs;
    }

    @Override
    public Page<MessageLog> getMessageLogsByStatus(MessageLog.Status status, Pageable pageable) {
        log.debug("[MessageLogService] 상태별 메시지 로그 조회. status={}, page={}, size={}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageLog> logPage = messageLogRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        log.debug("[MessageLogService] 상태별 조회 완료. status={}, 총 {}건, 현재 페이지 {}건", 
                status, logPage.getTotalElements(), logPage.getNumberOfElements());
        
        return logPage;
    }

    @Override
    public Page<MessageLog> getMessageLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("[MessageLogService] 기간별 메시지 로그 조회. startDate={}, endDate={}, page={}, size={}", 
                startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageLog> logPage = messageLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        log.debug("[MessageLogService] 기간별 조회 완료. 기간={} ~ {}, 총 {}건, 현재 페이지 {}건", 
                startDate, endDate, logPage.getTotalElements(), logPage.getNumberOfElements());
        
        return logPage;
    }

    @Override
    public List<MessageLog> getScheduledMessages(LocalDateTime now) {
        log.debug("[MessageLogService] 예약 발송 대상 조회. now={}", now);
        
        List<MessageLog> scheduledMessages = messageLogRepository.findScheduledMessages(now);
        log.info("[MessageLogService] 예약 발송 대상 조회 완료. 발송 대상 {}건", scheduledMessages.size());
        
        return scheduledMessages;
    }

    @Override
    public List<MessageLog> getFailedMessagesForRetry(LocalDateTime sinceDate, List<String> retryablePurposes) {
        log.debug("[MessageLogService] 재발송 대상 실패 메시지 조회. sinceDate={}, retryablePurposes={}", 
                sinceDate, retryablePurposes);
        
        List<MessageLog> failedMessages = messageLogRepository.findFailedMessagesForRetry(sinceDate, retryablePurposes);
        log.info("[MessageLogService] 재발송 대상 조회 완료. 대상 {}건", failedMessages.size());
        
        return failedMessages;
    }

    @Override
    @Transactional
    public void markMessageAsSent(Long id, LocalDateTime sentAt) {
        log.info("[MessageLogService] 메시지 발송 성공 처리. id={}, sentAt={}", id, sentAt);
        
        Optional<MessageLog> optionalLog = messageLogRepository.findById(id);
        if (optionalLog.isEmpty()) {
            log.warn("[MessageLogService] 발송 성공 처리할 메시지 로그를 찾을 수 없음. id={}", id);
            return;
        }
        
        MessageLog messageLog = optionalLog.get();
        messageLog.markAsSent(sentAt);
        messageLogRepository.save(messageLog);
        
        log.debug("[MessageLogService] 메시지 발송 성공 처리 완료. id={}", id);
    }

    @Override
    @Transactional
    public void markMessageAsFailed(Long id, String errorCode, String errorMessage) {
        log.info("[MessageLogService] 메시지 발송 실패 처리. id={}, errorCode={}, errorMessage={}", 
                id, errorCode, errorMessage);
        
        Optional<MessageLog> optionalLog = messageLogRepository.findById(id);
        if (optionalLog.isEmpty()) {
            log.warn("[MessageLogService] 발송 실패 처리할 메시지 로그를 찾을 수 없음. id={}", id);
            return;
        }
        
        MessageLog messageLog = optionalLog.get();
        messageLog.markAsFailed(errorCode, errorMessage);
        messageLogRepository.save(messageLog);
        
        log.debug("[MessageLogService] 메시지 발송 실패 처리 완료. id={}", id);
    }

    @Override
    @Transactional
    public void updateProviderInfo(Long id, String providerMessageId, Integer cost, String responseJson) {
        log.info("[MessageLogService] 업체 응답 정보 업데이트. id={}, providerMessageId={}, cost={}", 
                id, providerMessageId, cost);
        
        Optional<MessageLog> optionalLog = messageLogRepository.findById(id);
        if (optionalLog.isEmpty()) {
            log.warn("[MessageLogService] 업체 정보 업데이트할 메시지 로그를 찾을 수 없음. id={}", id);
            return;
        }
        
        MessageLog messageLog = optionalLog.get();
        messageLog.updateProviderInfo(providerMessageId, cost, responseJson);
        messageLogRepository.save(messageLog);
        
        log.debug("[MessageLogService] 업체 응답 정보 업데이트 완료. id={}", id);
    }

    @Override
    public List<Object[]> getStatisticsByPurpose(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[MessageLogService] 목적 코드별 통계 조회. startDate={}, endDate={}", startDate, endDate);
        
        List<Object[]> statistics = messageLogRepository.getStatisticsByPurposeAndStatus(startDate, endDate);
        log.debug("[MessageLogService] 목적 코드별 통계 조회 완료. 통계 {}건", statistics.size());
        
        return statistics;
    }

    @Override
    public List<Object[]> getStatisticsByChannel(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[MessageLogService] 채널별 통계 조회. startDate={}, endDate={}", startDate, endDate);
        
        List<Object[]> statistics = messageLogRepository.getStatisticsByChannel(startDate, endDate);
        log.debug("[MessageLogService] 채널별 통계 조회 완료. 통계 {}건", statistics.size());
        
        return statistics;
    }

    @Override
    public List<Object[]> getDailyStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[MessageLogService] 일별 통계 조회. startDate={}, endDate={}", startDate, endDate);
        
        List<Object[]> statistics = messageLogRepository.getDailyStatistics(startDate, endDate);
        log.debug("[MessageLogService] 일별 통계 조회 완료. 통계 {}건", statistics.size());
        
        return statistics;
    }

    @Override
    public List<Object[]> getCountByTargetType(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[MessageLogService] 수신자 타입별 발송 수 조회. startDate={}, endDate={}", startDate, endDate);
        
        List<Object[]> statistics = messageLogRepository.getCountByTargetType(startDate, endDate);
        log.debug("[MessageLogService] 수신자 타입별 조회 완료. 통계 {}건", statistics.size());
        
        return statistics;
    }

    @Override
    public Optional<MessageLog> getLastSuccessfulMessage(String purposeCode, String toPhone) {
        log.debug("[MessageLogService] 최근 성공 발송 조회. purposeCode={}, toPhone={}", purposeCode, toPhone);
        
        Optional<MessageLog> lastSuccess = messageLogRepository
                .findFirstByPurposeCodeAndToPhoneAndStatusOrderByCreatedAtDesc(purposeCode, toPhone, MessageLog.Status.SUCCESS);
        
        if (lastSuccess.isPresent()) {
            log.debug("[MessageLogService] 최근 성공 발송 조회 완료. id={}, createdAt={}", 
                    lastSuccess.get().getId(), lastSuccess.get().getCreatedAt());
        } else {
            log.debug("[MessageLogService] 해당 조건의 성공 발송 이력 없음. purposeCode={}, toPhone={}", purposeCode, toPhone);
        }
        
        return lastSuccess;
    }
}