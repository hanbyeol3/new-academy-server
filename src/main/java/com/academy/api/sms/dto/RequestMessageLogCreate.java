package com.academy.api.sms.dto;

import com.academy.api.sms.domain.MessageLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 메시지 로그 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메시지 로그 생성 요청")
public class RequestMessageLogCreate {

    @NotNull(message = "발송 채널을 선택해주세요")
    @Schema(description = "발송 채널", example = "SMS", 
            allowableValues = {"SMS", "LMS", "KAKAO_AT", "KAKAO_FT"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MessageLog.Channel channel;

    @Schema(description = "발송 방식", example = "IMMEDIATE", 
            allowableValues = {"IMMEDIATE", "BATCH", "SCHEDULED"},
            defaultValue = "IMMEDIATE")
    private MessageLog.SendType sendType = MessageLog.SendType.IMMEDIATE;

    @NotBlank(message = "수신번호를 입력해주세요")
    @Size(max = 20, message = "수신번호는 20자 이하여야 합니다")
    @Schema(description = "수신번호", example = "010-1234-5678", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String toPhone;

    @Size(max = 50, message = "수신자명은 50자 이하여야 합니다")
    @Schema(description = "수신자명", example = "홍길동")
    private String toName;

    @NotNull(message = "수신자 타입을 선택해주세요")
    @Schema(description = "수신자 타입", example = "USER", 
            allowableValues = {"ADMIN", "USER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MessageLog.TargetType toType;

    @Size(max = 20, message = "발신번호는 20자 이하여야 합니다")
    @Schema(description = "발신번호", example = "02-123-4567")
    private String fromPhone;

    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    @Schema(description = "제목 (LMS 전용)", example = "중요한 공지사항")
    private String subject;

    @NotBlank(message = "메시지 본문을 입력해주세요")
    @Schema(description = "메시지 본문", example = "안녕하세요. 중요한 공지사항입니다.", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Size(max = 50, message = "템플릿 코드는 50자 이하여야 합니다")
    @Schema(description = "카카오톡 템플릿 코드", example = "TEMPLATE_001")
    private String templateCode;

    @NotNull(message = "발송 상태를 선택해주세요")
    @Schema(description = "발송 상태", example = "PENDING", 
            allowableValues = {"SUCCESS", "FAILED", "PENDING"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MessageLog.Status status = MessageLog.Status.PENDING;

    @NotBlank(message = "발송 업체를 입력해주세요")
    @Size(max = 20, message = "발송 업체는 20자 이하여야 합니다")
    @Schema(description = "발송 업체", example = "SOLAPI", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String provider;

    @Size(max = 100, message = "업체 메시지 ID는 100자 이하여야 합니다")
    @Schema(description = "업체 메시지 ID", example = "M4V20240101000001")
    private String providerMessageId;

    @Schema(description = "발송 비용 (원)", example = "20")
    private Integer cost;

    @Schema(description = "글자 수", example = "15")
    private Integer characterCount;

    @Schema(description = "EUC-KR 바이트 수", example = "30")
    private Integer byteCount;

    @Size(max = 100, message = "에러 코드는 100자 이하여야 합니다")
    @Schema(description = "에러 코드", example = "E001")
    private String errorCode;

    @Size(max = 500, message = "에러 메시지는 500자 이하여야 합니다")
    @Schema(description = "에러 메시지", example = "발송에 실패했습니다")
    private String errorMessage;

    @NotBlank(message = "목적 코드를 입력해주세요")
    @Size(max = 50, message = "목적 코드는 50자 이하여야 합니다")
    @Schema(description = "발송 목적 코드", example = "QNA_ANSWER", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String purposeCode;

    @Size(max = 50, message = "참조 타입은 50자 이하여야 합니다")
    @Schema(description = "참조 타입 (연관 엔티티)", example = "QNA")
    private String refType;

    @Schema(description = "참조 ID (연관 엔티티 ID)", example = "123")
    private Long refId;

    @Size(max = 50, message = "배치 ID는 50자 이하여야 합니다")
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

    @Schema(description = "시스템 자동 발송 여부", example = "false", defaultValue = "false")
    private Boolean createdBySystem = false;
}