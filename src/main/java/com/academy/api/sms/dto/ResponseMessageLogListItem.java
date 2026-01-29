package com.academy.api.sms.dto;

import com.academy.api.sms.domain.MessageLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 로그 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "메시지 로그 목록 항목 응답")
public class ResponseMessageLogListItem {

    @Schema(description = "메시지 로그 ID", example = "1")
    private Long id;

    @Schema(description = "발송 채널", example = "SMS")
    private MessageLog.Channel channel;

    @Schema(description = "발송 방식", example = "IMMEDIATE")
    private MessageLog.SendType sendType;

    @Schema(description = "수신번호", example = "010-1234-5678")
    private String toPhone;

    @Schema(description = "수신자명", example = "홍길동")
    private String toName;

    @Schema(description = "수신자 타입", example = "USER")
    private MessageLog.TargetType toType;

    @Schema(description = "제목 (LMS인 경우)", example = "중요한 공지사항")
    private String subject;

    @Schema(description = "메시지 본문 (요약)", example = "안녕하세요. 중요한 공지사항입니다...")
    private String messagePreview;

    @Schema(description = "발송 상태", example = "SUCCESS")
    private MessageLog.Status status;

    @Schema(description = "발송 업체", example = "SOLAPI")
    private String provider;

    @Schema(description = "업체 메시지 ID", example = "M4V20240101000001")
    private String providerMessageId;

    @Schema(description = "발송 비용 (원)", example = "20")
    private Integer cost;

    @Schema(description = "글자 수", example = "15")
    private Integer characterCount;

    @Schema(description = "에러 코드", example = "E001")
    private String errorCode;

    @Schema(description = "에러 메시지", example = "발송에 실패했습니다")
    private String errorMessage;

    @Schema(description = "발송 목적 코드", example = "QNA_ANSWER")
    private String purposeCode;

    @Schema(description = "참조 타입", example = "QNA")
    private String refType;

    @Schema(description = "참조 ID", example = "123")
    private Long refId;

    @Schema(description = "배치 ID", example = "BATCH_20240101001")
    private String batchId;

    @Schema(description = "배치 내 순서", example = "1")
    private Integer batchSeq;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 발송 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "실제 발송 시각", example = "2024-01-01 10:01:00")
    private LocalDateTime sentAt;

    @Schema(description = "생성자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "생성자 이름", example = "관리자")
    private String createdByName;

    @Schema(description = "시스템 자동 발송 여부", example = "false")
    private Boolean createdBySystem;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    /**
     * 엔티티에서 목록용 DTO로 변환.
     */
    public static ResponseMessageLogListItem from(MessageLog entity) {
        return ResponseMessageLogListItem.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .sendType(entity.getSendType())
                .toPhone(entity.getToPhone())
                .toName(entity.getToName())
                .toType(entity.getToType())
                .subject(entity.getSubject())
                .messagePreview(createMessagePreview(entity.getMessage()))
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .providerMessageId(entity.getProviderMessageId())
                .cost(entity.getCost())
                .characterCount(entity.getCharacterCount())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .purposeCode(entity.getPurposeCode())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .batchId(entity.getBatchId())
                .batchSeq(entity.getBatchSeq())
                .scheduledAt(entity.getScheduledAt())
                .sentAt(entity.getSentAt())
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdBySystem(entity.getCreatedBySystem())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 엔티티에서 목록용 DTO로 변환 (생성자 이름 포함).
     */
    public static ResponseMessageLogListItem fromWithCreatorName(MessageLog entity, String createdByName) {
        return ResponseMessageLogListItem.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .sendType(entity.getSendType())
                .toPhone(entity.getToPhone())
                .toName(entity.getToName())
                .toType(entity.getToType())
                .subject(entity.getSubject())
                .messagePreview(createMessagePreview(entity.getMessage()))
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .providerMessageId(entity.getProviderMessageId())
                .cost(entity.getCost())
                .characterCount(entity.getCharacterCount())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .purposeCode(entity.getPurposeCode())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .batchId(entity.getBatchId())
                .batchSeq(entity.getBatchSeq())
                .scheduledAt(entity.getScheduledAt())
                .sentAt(entity.getSentAt())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdBySystem(entity.getCreatedBySystem())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 엔티티 목록을 목록용 DTO 목록으로 변환.
     */
    public static List<ResponseMessageLogListItem> fromList(List<MessageLog> entities) {
        return entities.stream()
                .map(ResponseMessageLogListItem::from)
                .toList();
    }

    /**
     * 메시지 본문 미리보기 생성 (50자 제한).
     */
    private static String createMessagePreview(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        if (message.length() <= 50) {
            return message;
        }
        
        return message.substring(0, 50) + "...";
    }
}