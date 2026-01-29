package com.academy.api.sms.repository;

import com.academy.api.sms.domain.MessagePurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 메시지 목적 Repository.
 */
public interface MessagePurposeRepository extends JpaRepository<MessagePurpose, String> {

    /**
     * 활성화된 목적 코드 조회.
     */
    List<MessagePurpose> findByIsActiveTrueOrderByCodeAsc();

    /**
     * 대상 타입별 목적 코드 조회.
     */
    List<MessagePurpose> findByTargetTypeAndIsActiveTrueOrderByCodeAsc(MessagePurpose.TargetType targetType);

    /**
     * 대상 타입별 목적 코드 조회 (BOTH 포함).
     */
    @Query("SELECT mp FROM MessagePurpose mp WHERE " +
           "(mp.targetType = :targetType OR mp.targetType = 'BOTH') " +
           "AND mp.isActive = true ORDER BY mp.code ASC")
    List<MessagePurpose> findByTargetTypeIncludingBoth(MessagePurpose.TargetType targetType);

    /**
     * 배치 발송 가능한 목적 코드 조회.
     */
    List<MessagePurpose> findByIsBatchAvailableTrueAndIsActiveTrueOrderByCodeAsc();

    /**
     * 기본 채널별 목적 코드 조회.
     */
    List<MessagePurpose> findByDefaultChannelAndIsActiveTrueOrderByCodeAsc(MessagePurpose.DefaultChannel defaultChannel);

    /**
     * 카카오톡 템플릿이 있는 목적 코드 조회.
     */
    @Query("SELECT mp FROM MessagePurpose mp WHERE " +
           "mp.kakaoTemplateCode IS NOT NULL " +
           "AND mp.isActive = true ORDER BY mp.code ASC")
    List<MessagePurpose> findWithKakaoTemplate();

    /**
     * 코드로 활성화된 목적 조회.
     */
    Optional<MessagePurpose> findByCodeAndIsActiveTrue(String code);

    /**
     * 이름으로 검색.
     */
    List<MessagePurpose> findByNameContainingAndIsActiveTrueOrderByCodeAsc(String name);

    /**
     * SMS 템플릿이 있는 목적 코드 조회.
     */
    @Query("SELECT mp FROM MessagePurpose mp WHERE " +
           "mp.smsTemplate IS NOT NULL " +
           "AND mp.isActive = true ORDER BY mp.code ASC")
    List<MessagePurpose> findWithSmsTemplate();

    /**
     * LMS 템플릿이 있는 목적 코드 조회.
     */
    @Query("SELECT mp FROM MessagePurpose mp WHERE " +
           "mp.lmsTemplate IS NOT NULL " +
           "AND mp.isActive = true ORDER BY mp.code ASC")
    List<MessagePurpose> findWithLmsTemplate();

    /**
     * fallback 채널이 설정된 목적 코드 조회.
     */
    List<MessagePurpose> findByFallbackChannelIsNotNullAndIsActiveTrueOrderByCodeAsc();
}