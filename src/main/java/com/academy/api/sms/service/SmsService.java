package com.academy.api.sms.service;

import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.sms.dto.RequestSmsMessage;
import com.academy.api.sms.dto.ResponseSmsMessage;

/**
 * SMS 메시지 서비스 인터페이스.
 * 
 * SMS 발송과 관련된 비즈니스 로직을 정의합니다.
 * SOLAPI 클라이언트를 통해 실제 메시지 발송을 처리합니다.
 */
public interface SmsService {

    /**
     * SMS 메시지 발송.
     *
     * @param request SMS 발송 요청 데이터
     * @return 발송 결과
     */
    ResponseData<ResponseSmsMessage> sendMessage(RequestSmsMessage request);

    /**
     * 상담 신청 확인 SMS 발송.
     *
     * @param phoneNumber 수신자 전화번호
     * @param name 신청자 이름
     * @return 발송 결과
     */
    Response sendInquiryConfirmation(String phoneNumber, String name);

    // 중복 발송 방지를 위해 제거됨 - ExplanationServiceImpl에서 직접 sendMessageByPurposeCode 호출
    // Response sendExplanationConfirmation(String phoneNumber, String name, String scheduleDate);

    /**
     * QnA 답변 알림 SMS 발송.
     *
     * @param phoneNumber 수신자 전화번호
     * @param questionTitle 질문 제목
     * @return 발송 결과
     */
    Response sendQnaAnswerNotification(String phoneNumber, String questionTitle);

    /**
     * 관리자 알림 SMS 발송 (내부용).
     *
     * @param message 알림 메시지
     * @return 발송 결과
     */
    Response sendAdminNotification(String message);
    
    /**
     * 목적 코드 기반 SMS 메시지 발송.
     * DB에 저장된 템플릿을 사용하여 SMS를 발송합니다.
     * 
     * @param purposeCode 목적 코드
     * @param toPhone 수신자 전화번호
     * @param toName 수신자 이름
     * @param variables 템플릿 변수
     * @return 발송 결과
     */
    ResponseData<ResponseSmsMessage> sendMessageByPurposeCode(String purposeCode, String toPhone, 
                                                              String toName, java.util.Map<String, Object> variables);
}