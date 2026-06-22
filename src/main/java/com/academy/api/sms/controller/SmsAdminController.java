package com.academy.api.sms.controller;

import com.academy.api.config.SolapiConfig;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.sms.dto.RequestSmsMessage;
import com.academy.api.sms.dto.ResponseSmsMessage;
import com.academy.api.sms.service.SmsService;
import com.academy.api.sms.service.SmsServiceImpl;
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
    private final SmsServiceImpl smsServiceImpl;
    private final SolapiConfig solapiConfig;

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
        
        // DB의 message_purposes 테이블 템플릿 사용
        return smsServiceImpl.sendExplanationConfirmationByPurposeCode(phoneNumber, name, scheduleDate);
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
        
        // DB의 message_purposes 테이블 템플릿 사용
        return smsServiceImpl.sendQnaAnswerNotificationByPurposeCode(phoneNumber, questionTitle);
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

    // =================== 🧪 목적 코드 기반 테스트 엔드포인트 ===================

    /**
     * 🧪 목적 코드 기반 SMS 발송 테스트.
     */
    @Operation(
        summary = "🧪 목적 코드 기반 SMS 발송 테스트",
        description = """
                새로 구현한 목적 코드 기반 메시징 시스템을 테스트합니다.
                
                테스트 가능한 목적 코드:
                - INQUIRY_CONFIRMATION: 상담 신청 확인
                - EXPLANATION_CONFIRMATION: 설명회 예약 확인  
                - QNA_ANSWER_NOTIFICATION: QnA 답변 알림
                - ADMIN_NOTIFICATION: 관리자 알림
                
                동작 방식:
                1. 데이터베이스에서 목적 코드에 맞는 템플릿 조회
                2. 변수 치환 및 채널 자동 선택 (SMS/LMS)
                3. 메시지 로그 저장 및 SOLAPI 발송
                4. 성공/실패 상태 자동 추적
                
                장점:
                - 템플릿 수정은 DB에서만 하면 됨
                - 코드 변경 없이 새 목적 코드 추가 가능
                - 모든 발송 이력이 자동으로 로그에 저장됨
                """
    )
    @PostMapping("/test/purpose-code")
    public ResponseData<ResponseSmsMessage> testPurposeCodeMessage(
            @Parameter(description = "목적 코드", example = "INQUIRY_CONFIRMATION")
            @RequestParam String purposeCode,
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String toPhone,
            @Parameter(description = "수신자명 (선택)", example = "김테스트")
            @RequestParam(required = false) String toName,
            @Parameter(description = "변수1 - name", example = "김학생")
            @RequestParam(required = false) String name,
            @Parameter(description = "변수2 - scheduleDate", example = "2024-01-15 14:00")
            @RequestParam(required = false) String scheduleDate,
            @Parameter(description = "변수3 - questionTitle", example = "입학 관련 문의")
            @RequestParam(required = false) String questionTitle,
            @Parameter(description = "변수4 - message (관리자 알림용)", example = "새로운 상담 신청이 접수되었습니다.")
            @RequestParam(required = false) String message) {

        log.info("[SmsAdminController] 🧪 목적 코드 기반 SMS 테스트. purposeCode={}, toPhone={}", 
                purposeCode, toPhone);

        try {
            // 변수 맵 생성
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            if (name != null) variables.put("name", name);
            if (scheduleDate != null) variables.put("scheduleDate", scheduleDate);
            if (questionTitle != null) variables.put("questionTitle", questionTitle);
            if (message != null) variables.put("message", message);

            return smsServiceImpl.sendMessageByPurposeCode(purposeCode, toPhone, toName, variables);
            
        } catch (Exception e) {
            log.error("[SmsAdminController] 🧪 목적 코드 기반 SMS 테스트 실패: {}", e.getMessage(), e);
            return ResponseData.error("TEST_ERROR", "테스트 실패: " + e.getMessage());
        }
    }

    /**
     * 🧪 상담 신청 확인 - 목적 코드 기반 테스트.
     */
    @Operation(
        summary = "🧪 상담 신청 확인 SMS (목적 코드 기반)",
        description = """
                목적 코드 INQUIRY_CONFIRMATION을 사용한 상담 신청 확인 SMS 발송 테스트
                
                기존 enum 템플릿 vs 새로운 DB 템플릿:
                - 기존: 하드코딩된 템플릿, 수정시 코드 변경 필요
                - 신규: DB 설정 템플릿, 실시간 템플릿 변경 가능
                
                테스트 확인사항:
                1. message_logs 테이블에 로그 저장 확인
                2. message_purposes 테이블에서 템플릿 조회 확인
                3. SMS/LMS 자동 선택 확인
                4. 변수 치환 정상 동작 확인
                """
    )
    @PostMapping("/test/inquiry-confirmation-db")
    public Response testInquiryConfirmationByPurposeCode(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "신청자 이름", example = "김학생")
            @RequestParam String name) {

        log.info("[SmsAdminController] 🧪 상담 신청 확인 SMS 테스트 (DB). 수신자={}, 이름={}", 
                phoneNumber, name);
        
        return smsServiceImpl.sendInquiryConfirmationByPurposeCode(phoneNumber, name);
    }

    /**
     * 🧪 설명회 예약 확인 - 목적 코드 기반 테스트.
     */
    @PostMapping("/test/explanation-confirmation-db")
    public Response testExplanationConfirmationByPurposeCode(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "예약자 이름", example = "김학생")
            @RequestParam String name,
            @Parameter(description = "설명회 일정", example = "2024-01-15 14:00")
            @RequestParam String scheduleDate) {

        log.info("[SmsAdminController] 🧪 설명회 예약 확인 SMS 테스트 (DB). 수신자={}, 이름={}, 일정={}", 
                phoneNumber, name, scheduleDate);
        
        return smsServiceImpl.sendExplanationConfirmationByPurposeCode(phoneNumber, name, scheduleDate);
    }

    /**
     * 🧪 QnA 답변 알림 - 목적 코드 기반 테스트.
     */
    @PostMapping("/test/qna-answer-notification-db")
    public Response testQnaAnswerNotificationByPurposeCode(
            @Parameter(description = "수신자 전화번호", example = "01076665012")
            @RequestParam String phoneNumber,
            @Parameter(description = "질문 제목", example = "입학 관련 문의")
            @RequestParam String questionTitle) {

        log.info("[SmsAdminController] 🧪 QnA 답변 알림 SMS 테스트 (DB). 수신자={}, 질문제목={}", 
                phoneNumber, questionTitle);
        
        return smsServiceImpl.sendQnaAnswerNotificationByPurposeCode(phoneNumber, questionTitle);
    }

    /**
     * 🧪 관리자 알림 - 목적 코드 기반 테스트.
     */
    @PostMapping("/test/admin-notification-db")
    public Response testAdminNotificationByPurposeCode(
            @Parameter(description = "알림 메시지", example = "새로운 상담 신청이 접수되었습니다.")
            @RequestParam String message) {

        log.info("[SmsAdminController] 🧪 관리자 알림 SMS 테스트 (DB). 메시지 길이={}", message.length());
        
        return smsServiceImpl.sendAdminNotificationByPurposeCode(message);
    }
}