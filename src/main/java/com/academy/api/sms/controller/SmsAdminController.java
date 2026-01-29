package com.academy.api.sms.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.sms.dto.RequestSmsMessage;
import com.academy.api.sms.dto.ResponseSmsMessage;
import com.academy.api.sms.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SMS 관리자 API 컨트롤러.
 *
 * SMS 발송과 관련된 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "SMS (Admin)", description = "SMS 발송 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/sms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SmsAdminController {

    private final SmsService smsService;
    private final com.academy.api.config.SolapiConfig solapiConfig;

    /**
     * SMS 메시지 발송.
     *
     * @param request SMS 발송 요청 데이터
     * @return 발송 결과
     */
    @Operation(
        summary = "SMS 메시지 발송",
        description = """
                SMS 메시지를 발송합니다.
                
                필수 입력 사항:
                - 수신자 전화번호 (010-XXXX-XXXX 형식)
                - 메시지 내용 (최대 2000자)
                
                선택 입력 사항:
                - 발신자 번호 (기본값: 시스템 설정값)
                - 메시지 타입 (SMS, LMS, MMS)
                - 메시지 제목 (LMS의 경우)
                
                주의사항:
                - SMS: 90자 이하 권장
                - LMS: 2000자 이하, 제목 필수
                - 발송 비용이 차감됩니다
                
                예시:
                - 일반 SMS: 90자 이하 단문 메시지
                - LMS: 긴 메시지 또는 중요 공지
                """
    )
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<ResponseSmsMessage> sendMessage(
            @Parameter(description = "SMS 발송 요청 데이터")
            @RequestBody @Valid RequestSmsMessage request) {

        log.info("[SmsAdminController] SMS 발송 요청. 수신자={}, 타입={}", 
                request.getTo(), request.getType());
        
        return smsService.sendMessage(request);
    }

    /**
     * 상담 신청 확인 SMS 발송.
     *
     * @param phoneNumber 수신자 전화번호
     * @param name 신청자 이름
     * @return 발송 결과
     */
    @Operation(
        summary = "상담 신청 확인 SMS 발송",
        description = """
                상담 신청자에게 접수 확인 SMS를 발송합니다.
                
                발송 내용:
                - 상담 신청 접수 확인
                - 연락 예정 안내
                - 문의 전화번호
                
                사용 시점:
                - 상담 신청이 접수된 직후
                - 관리자가 수동으로 발송하는 경우
                """
    )
    @PostMapping("/inquiry-confirmation")
    public Response sendInquiryConfirmation(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "신청자 이름", example = "김학생")
            @RequestParam String name) {

        log.info("[SmsAdminController] 상담 신청 확인 SMS 발송 요청. 수신자={}, 이름={}", 
                phoneNumber, name);
        
        return smsService.sendInquiryConfirmation(phoneNumber, name);
    }

    /**
     * 설명회 예약 확인 SMS 발송.
     *
     * @param phoneNumber 수신자 전화번호
     * @param name 예약자 이름
     * @param scheduleDate 설명회 일정
     * @return 발송 결과
     */
    @Operation(
        summary = "설명회 예약 확인 SMS 발송",
        description = """
                설명회 예약자에게 확인 SMS를 발송합니다.
                
                발송 내용:
                - 설명회 예약 완료 확인
                - 설명회 일정 정보
                - 추가 안내 예정 알림
                
                사용 시점:
                - 설명회 예약이 완료된 직후
                - 일정 변경 시 재발송
                """
    )
    @PostMapping("/explanation-confirmation")
    public Response sendExplanationConfirmation(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "예약자 이름", example = "김학생")
            @RequestParam String name,
            @Parameter(description = "설명회 일정", example = "2024-01-15 14:00")
            @RequestParam String scheduleDate) {

        log.info("[SmsAdminController] 설명회 예약 확인 SMS 발송 요청. 수신자={}, 이름={}, 일정={}", 
                phoneNumber, name, scheduleDate);
        
        return smsService.sendExplanationConfirmation(phoneNumber, name, scheduleDate);
    }

    /**
     * QnA 답변 알림 SMS 발송.
     *
     * @param phoneNumber 수신자 전화번호
     * @param questionTitle 질문 제목
     * @return 발송 결과
     */
    @Operation(
        summary = "QnA 답변 알림 SMS 발송",
        description = """
                QnA 답변 등록 시 질문자에게 알림 SMS를 발송합니다.
                
                발송 내용:
                - 답변 등록 알림
                - 질문 제목 (20자 제한)
                - 홈페이지 확인 안내
                
                사용 시점:
                - QnA 답변이 등록된 직후
                - 답변 수정 시 재발송
                """
    )
    @PostMapping("/qna-answer-notification")
    public Response sendQnaAnswerNotification(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "질문 제목", example = "입학 관련 문의")
            @RequestParam String questionTitle) {

        log.info("[SmsAdminController] QnA 답변 알림 SMS 발송 요청. 수신자={}, 질문제목={}", 
                phoneNumber, questionTitle);
        
        return smsService.sendQnaAnswerNotification(phoneNumber, questionTitle);
    }
    
    /**
     * 🔍 임시 디버그: SOLAPI 설정 확인.
     */
    @GetMapping("/debug/config")
    public ResponseData<String> debugConfig() {
        log.info("🔍 [DEBUG] SOLAPI 설정 확인");
        String senderNumber = solapiConfig.getSenderNumber();
        log.info("🔍 [DEBUG] senderNumber: {}", senderNumber);
        return ResponseData.ok("senderNumber: " + senderNumber);
    }

    /**
     * 관리자 알림 SMS 발송.
     *
     * @param message 알림 메시지
     * @return 발송 결과
     */
    @Operation(
        summary = "관리자 알림 SMS 발송",
        description = """
                시스템에서 관리자에게 알림 SMS를 발송합니다.
                
                발송 내용:
                - 시스템 알림 메시지
                - "[아카데미 시스템]" 접두사 자동 추가
                
                사용 예시:
                - 새로운 상담 신청 접수
                - 설명회 예약 접수
                - 시스템 오류 알림
                - 중요한 업무 알림
                
                주의사항:
                - 관리자 전화번호는 시스템 설정에서 관리
                - 스팸 방지를 위해 적절한 빈도로 사용
                """
    )
    @PostMapping("/admin-notification")
    public Response sendAdminNotification(
            @Parameter(description = "알림 메시지", example = "새로운 상담 신청이 접수되었습니다.")
            @RequestParam String message) {

        log.info("[SmsAdminController] 관리자 알림 SMS 발송 요청. 메시지 길이={}", message.length());
        
        return smsService.sendAdminNotification(message);
    }
}