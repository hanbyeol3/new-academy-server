package com.academy.api.sms.dto;

import com.academy.api.sms.domain.MessageLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 로그 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "메시지 로그 응답")
public class ResponseMessageLog {

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

    @Schema(description = "발신번호", example = "02-123-4567")
    private String fromPhone;

    @Schema(description = "제목", example = "중요한 공지사항")
    private String subject;

    @Schema(description = "메시지 본문", example = "안녕하세요. 중요한 공지사항입니다.")
    private String message;

    @Schema(description = "카카오톡 템플릿 코드", example = "TEMPLATE_001")
    private String templateCode;

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

    @Schema(description = "EUC-KR 바이트 수", example = "30")
    private Integer byteCount;

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

    @Schema(description = "요청 JSON", example = "{\"to\":\"010-1234-5678\",\"text\":\"메시지\"}")
    private String requestJson;

    @Schema(description = "응답 JSON", example = "{\"groupId\":\"G4V...\",\"status\":\"SENDING\"}")
    private String responseJson;

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
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseMessageLog from(MessageLog entity) {
        return ResponseMessageLog.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .sendType(entity.getSendType())
                .toPhone(entity.getToPhone())
                .toName(entity.getToName())
                .toType(entity.getToType())
                .fromPhone(entity.getFromPhone())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .templateCode(entity.getTemplateCode())
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .providerMessageId(entity.getProviderMessageId())
                .cost(entity.getCost())
                .characterCount(entity.getCharacterCount())
                .byteCount(entity.getByteCount())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .purposeCode(entity.getPurposeCode())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .batchId(entity.getBatchId())
                .batchSeq(entity.getBatchSeq())
                .scheduledAt(entity.getScheduledAt())
                .requestJson(entity.getRequestJson())
                .responseJson(entity.getResponseJson())
                .sentAt(entity.getSentAt())
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdBySystem(entity.getCreatedBySystem())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 엔티티에서 DTO로 변환 (생성자 이름 포함).
     */
    public static ResponseMessageLog fromWithCreatorName(MessageLog entity, String createdByName) {
        return ResponseMessageLog.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .sendType(entity.getSendType())
                .toPhone(entity.getToPhone())
                .toName(entity.getToName())
                .toType(entity.getToType())
                .fromPhone(entity.getFromPhone())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .templateCode(entity.getTemplateCode())
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .providerMessageId(entity.getProviderMessageId())
                .cost(entity.getCost())
                .characterCount(entity.getCharacterCount())
                .byteCount(entity.getByteCount())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .purposeCode(entity.getPurposeCode())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .batchId(entity.getBatchId())
                .batchSeq(entity.getBatchSeq())
                .scheduledAt(entity.getScheduledAt())
                .requestJson(entity.getRequestJson())
                .responseJson(entity.getResponseJson())
                .sentAt(entity.getSentAt())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdBySystem(entity.getCreatedBySystem())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 엔티티 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseMessageLog> fromList(List<MessageLog> entities) {
        return entities.stream()
                .map(ResponseMessageLog::from)
                .toList();
    }
}