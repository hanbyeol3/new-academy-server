package com.academy.api.sms.service;

import com.academy.api.sms.domain.MessageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 메시지 로그 서비스 인터페이스.
 * 
 * SMS, LMS, 카카오톡 등 모든 메시징 채널의 로그 관리를 담당합니다.
 * 
 * 주요 기능:
 * - 메시지 발송 로그 저장 및 조회
 * - 발송 상태 업데이트
 * - 통계 데이터 생성
 * - 실패 메시지 재발송 지원
 * - 배치 발송 로그 관리
 */
public interface MessageLogService {

    /**
     * 메시지 로그 저장.
     * 
     * @param messageLog 저장할 메시지 로그
     * @return 저장된 메시지 로그
     */
    MessageLog saveMessageLog(MessageLog messageLog);

    /**
     * 메시지 로그 ID로 조회.
     * 
     * @param id 메시지 로그 ID
     * @return 메시지 로그 (Optional)
     */
    Optional<MessageLog> getMessageLogById(Long id);

    /**
     * 전화번호별 발송 이력 조회.
     * 
     * @param toPhone 수신번호
     * @param pageable 페이징 정보
     * @return 메시지 로그 목록
     */
    Page<MessageLog> getMessageLogsByPhone(String toPhone, Pageable pageable);

    /**
     * 목적 코드별 발송 이력 조회.
     * 
     * @param purposeCode 목적 코드
     * @param pageable 페이징 정보
     * @return 메시지 로그 목록
     */
    Page<MessageLog> getMessageLogsByPurpose(String purposeCode, Pageable pageable);

    /**
     * 배치 ID로 발송 이력 조회.
     * 
     * @param batchId 배치 ID
     * @return 메시지 로그 목록 (배치 순서대로 정렬)
     */
    List<MessageLog> getMessageLogsByBatch(String batchId);

    /**
     * 업체 메시지 ID로 조회.
     * 
     * @param providerMessageId 업체 메시지 ID
     * @return 메시지 로그 (Optional)
     */
    Optional<MessageLog> getMessageLogByProviderMessageId(String providerMessageId);

    /**
     * 참조 정보로 메시지 로그 조회.
     * 
     * @param refType 참조 타입 (예: "QNA", "NOTICE")
     * @param refId 참조 ID
     * @return 메시지 로그 목록
     */
    List<MessageLog> getMessageLogsByReference(String refType, Long refId);

    /**
     * 발송 상태별 조회.
     * 
     * @param status 발송 상태
     * @param pageable 페이징 정보
     * @return 메시지 로그 목록
     */
    Page<MessageLog> getMessageLogsByStatus(MessageLog.Status status, Pageable pageable);

    /**
     * 기간별 발송 이력 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 메시지 로그 목록
     */
    Page<MessageLog> getMessageLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 예약 발송 대상 메시지 조회.
     * 
     * @param now 현재 시각
     * @return 발송 대상 메시지 목록
     */
    List<MessageLog> getScheduledMessages(LocalDateTime now);

    /**
     * 발송 실패한 메시지 조회 (재발송 대상).
     * 
     * @param sinceDate 조회 시작 날짜
     * @param retryablePurposes 재발송 가능한 목적 코드 목록
     * @return 실패 메시지 목록
     */
    List<MessageLog> getFailedMessagesForRetry(LocalDateTime sinceDate, List<String> retryablePurposes);

    /**
     * 메시지 발송 성공 처리.
     * 
     * @param id 메시지 로그 ID
     * @param sentAt 발송 시각
     */
    void markMessageAsSent(Long id, LocalDateTime sentAt);

    /**
     * 메시지 발송 실패 처리.
     * 
     * @param id 메시지 로그 ID
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    void markMessageAsFailed(Long id, String errorCode, String errorMessage);

    /**
     * 메시지 발송 실패 처리 (상세 정보 포함).
     * 
     * @param id 메시지 로그 ID
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     * @param responseJson 실패 응답 JSON
     * @param characterCount 메시지 문자 수
     * @param byteCount 메시지 바이트 수
     */
    void markMessageAsFailedWithDetails(Long id, String errorCode, String errorMessage, 
                                       String responseJson, Integer characterCount, Integer byteCount);

    /**
     * 업체 응답 정보 업데이트.
     * 
     * @param id 메시지 로그 ID
     * @param providerMessageId 업체 메시지 ID
     * @param cost 발송 비용
     * @param responseJson 응답 JSON
     */
    void updateProviderInfo(Long id, String providerMessageId, Integer cost, String responseJson);

    /**
     * 목적 코드별 발송 통계 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 데이터 (목적코드, 상태, 건수, 총비용)
     */
    List<Object[]> getStatisticsByPurpose(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 채널별 발송 통계 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 데이터 (채널, 상태, 건수, 총비용)
     */
    List<Object[]> getStatisticsByChannel(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 일별 발송 통계 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 데이터 (날짜, 총건수, 성공건수, 실패건수, 총비용)
     */
    List<Object[]> getDailyStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 수신자 타입별 발송 수 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 통계 데이터 (수신자타입, 건수)
     */
    List<Object[]> getCountByTargetType(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 목적 코드별 최근 성공 발송 조회.
     * 
     * @param purposeCode 목적 코드
     * @param toPhone 수신번호
     * @return 최근 성공 발송 로그 (Optional)
     */
    Optional<MessageLog> getLastSuccessfulMessage(String purposeCode, String toPhone);
}