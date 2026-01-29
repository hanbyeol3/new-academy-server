package com.academy.api.sms.template;

/**
 * SMS 메시지 템플릿 열거형.
 * 
 * 다양한 상황에 맞는 SMS 메시지 템플릿을 정의합니다.
 * 템플릿 변수는 {변수명} 형태로 표시됩니다.
 */
public enum SmsTemplate {
    
    // ===== 상담 관련 템플릿 =====
    INQUIRY_CONFIRMATION(
        "상담 신청 접수 확인",
        "[아카데미] {name}님의 상담 신청이 접수되었습니다. 빠른 시일 내에 연락드리겠습니다. 문의: {contactNumber}",
        SmsType.SMS
    ),
    
    INQUIRY_ADMIN_NOTIFICATION(
        "관리자 상담 신청 알림",
        "[아카데미 시스템] 새로운 상담 신청이 접수되었습니다. 신청자: {name}, 연락처: {phoneNumber}, 내용: {content}",
        SmsType.LMS
    ),
    
    // ===== 설명회 관련 템플릿 =====
    EXPLANATION_CONFIRMATION(
        "설명회 예약 확인",
        "[아카데미] {name}님의 설명회 예약이 완료되었습니다.\n일정: {scheduleDate}\n장소: {location}\n준비물은 별도 안내드리겠습니다. 문의: {contactNumber}",
        SmsType.LMS
    ),
    
    EXPLANATION_REMINDER(
        "설명회 리마인더",
        "[아카데미] {name}님, 내일 {scheduleTime} 설명회 일정을 잊지 마세요! 장소: {location} 문의: {contactNumber}",
        SmsType.LMS
    ),
    
    EXPLANATION_CANCELLATION(
        "설명회 취소 안내",
        "[아카데미] {name}님, {scheduleDate} 설명회가 {reason}으로 인해 취소되었습니다. 새로운 일정은 별도 안내드리겠습니다. 문의: {contactNumber}",
        SmsType.LMS
    ),
    
    // ===== QnA 관련 템플릿 =====
    QNA_ANSWER_NOTIFICATION(
        "QnA 답변 알림",
        "[아카데미] 질문 '{questionTitle}'에 대한 답변이 등록되었습니다. 홈페이지에서 확인해주세요. 문의: {contactNumber}",
        SmsType.SMS
    ),
    
    QNA_ADMIN_NOTIFICATION(
        "관리자 QnA 접수 알림",
        "[아카데미 시스템] 새로운 질문이 등록되었습니다. 제목: {questionTitle}, 작성자: {authorName}",
        SmsType.SMS
    ),
    
    // ===== 공지사항 관련 템플릿 =====
    NOTICE_IMPORTANT_ALERT(
        "중요 공지사항 알림",
        "[아카데미 중요] {title}\n\n{content}\n\n자세한 내용은 홈페이지를 확인해주세요.",
        SmsType.LMS
    ),
    
    // ===== 학사일정 관련 템플릿 =====
    SCHEDULE_REMINDER(
        "학사일정 리마인더",
        "[아카데미] {eventName}이 {daysLeft}일 남았습니다. 날짜: {eventDate} 잊지 마세요!",
        SmsType.SMS
    ),
    
    // ===== 시스템 관련 템플릿 =====
    SYSTEM_MAINTENANCE(
        "시스템 점검 안내",
        "[아카데미] 시스템 점검 안내\n일시: {maintenanceDate}\n예상 소요시간: {duration}\n점검 중 서비스 이용이 제한됩니다.",
        SmsType.LMS
    ),
    
    ADMIN_GENERAL_NOTIFICATION(
        "관리자 일반 알림",
        "[아카데미 시스템] {message}",
        SmsType.SMS
    ),
    
    // ===== 인증 관련 템플릿 =====
    VERIFICATION_CODE(
        "인증번호 발송",
        "[아카데미] 인증번호: {verificationCode}\n3분 내에 입력해주세요. 타인에게 노출하지 마세요.",
        SmsType.SMS
    ),
    
    PASSWORD_RESET(
        "비밀번호 재설정",
        "[아카데미] 비밀번호 재설정 요청이 접수되었습니다. 홈페이지에서 새 비밀번호를 설정해주세요. 문의: {contactNumber}",
        SmsType.SMS
    );
    
    private final String description;
    private final String template;
    private final SmsType defaultType;
    
    SmsTemplate(String description, String template, SmsType defaultType) {
        this.description = description;
        this.template = template;
        this.defaultType = defaultType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public SmsType getDefaultType() {
        return defaultType;
    }
    
    /**
     * SMS 타입 열거형.
     */
    public enum SmsType {
        SMS("SMS", 90),      // 단문 (90자 이하)
        LMS("LMS", 2000),    // 장문 (2000자 이하)
        MMS("MMS", 2000);    // 멀티미디어 (이미지 포함)
        
        private final String code;
        private final int maxLength;
        
        SmsType(String code, int maxLength) {
            this.code = code;
            this.maxLength = maxLength;
        }
        
        public String getCode() {
            return code;
        }
        
        public int getMaxLength() {
            return maxLength;
        }
    }
}