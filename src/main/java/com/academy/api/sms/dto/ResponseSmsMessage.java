package com.academy.api.sms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * SMS 메시지 발송 결과 응답.
 */
@Getter
@Builder
@Schema(description = "SMS 메시지 발송 결과")
public class ResponseSmsMessage {

    @Schema(description = "메시지 ID", example = "M4V20180307110044DTYYJBBYLPQZIB1")
    private String messageId;

    @Schema(description = "수신자 전화번호", example = "01076665012")
    private String to;

    @Schema(description = "발신자 전화번호", example = "01053725012")
    private String from;

    @Schema(description = "메시지 내용", example = "[아카데미] 안녕하세요.")
    private String text;

    @Schema(description = "메시지 타입", example = "SMS")
    private String type;

    @Schema(description = "발송 상태", example = "PENDING", 
            allowableValues = {"PENDING", "SENDING", "SENT", "FAILED"})
    private String status;

    @Schema(description = "발송 비용 (원)", example = "20")
    private Integer cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "발송 요청 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime sentAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "발송 완료 시각", example = "2024-01-01 10:00:05")
    private LocalDateTime deliveredAt;

    @Schema(description = "에러 코드", example = "")
    private String errorCode;

    @Schema(description = "에러 메시지", example = "")
    private String errorMessage;
}