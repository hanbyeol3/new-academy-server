package com.academy.api.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SMS 메시지 발송 요청.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "SMS 메시지 발송 요청")
public class RequestSmsMessage {

    @NotBlank(message = "수신자 전화번호를 입력해주세요")
    @Pattern(regexp = "^01[0-9]-?[0-9]{4}-?[0-9]{4}$", message = "올바른 전화번호 형식으로 입력해주세요")
    @Schema(description = "수신자 전화번호", 
            example = "01076665012",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String to;

    @NotBlank(message = "메시지 내용을 입력해주세요")
    @Size(max = 2000, message = "메시지는 2000자 이하여야 합니다")
    @Schema(description = "메시지 내용", 
            example = "[아카데미] 안녕하세요. 문의해 주셔서 감사합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String text;

    @Schema(description = "발신자 전화번호", 
            example = "01053725012")
    private String from;

    @Schema(description = "메시지 제목 (LMS용)", 
            example = "아카데미 공지사항")
    private String subject;

    @Schema(description = "메시지 타입", 
            example = "SMS",
            allowableValues = {"SMS", "LMS", "MMS"})
    private String type = "SMS";
}