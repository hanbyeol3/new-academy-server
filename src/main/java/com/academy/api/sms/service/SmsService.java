package com.academy.api.sms.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
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

    /**
     * 설명회 예약 확인 SMS 발송.
     * 
     * @param phoneNumber 수신자 전화번호
     * @param name 예약자 이름
     * @param scheduleDate 설명회 일정
     * @return 발송 결과
     */
    Response sendExplanationConfirmation(String phoneNumber, String name, String scheduleDate);

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
}