package com.academy.api.explanation.scheduler;

import com.academy.api.explanation.dto.ReservationWithSchedule;
import com.academy.api.explanation.repository.ExplanationReservationRepository;
import com.academy.api.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 설명회 리마인더 스케줄러.
 * 
 * D-1 알림 SMS를 발송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExplanationReminderScheduler {
    
    private final ExplanationReservationRepository reservationRepository;
    private final SmsService smsService;
    
    @Value("${academy.admin.phone:010-0000-0000}")
    private String adminPhoneNumber;
    
    @Value("${academy.scheduler.explanation-reminder.enabled:true}")
    private boolean reminderEnabled;
    
    /**
     * D-1 리마인더 SMS 발송.
     * 매일 오후 2시에 실행되며, 내일 예정된 설명회 예약자들에게 알림 SMS를 발송합니다.
     */
    @Scheduled(cron = "0 0 14 * * *", zone = "Asia/Seoul") // 매일 오후 2시
    public void sendDayBeforeReminder() {
        if (!reminderEnabled) {
            log.debug("[ExplanationReminderScheduler] D-1 리마인더 비활성화 상태");
            return;
        }
        
        log.info("[ExplanationReminderScheduler] D-1 리마인더 시작");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        try {
            // 내일 예정된 예약들 조회
            List<ReservationWithSchedule> reservations = reservationRepository.findReservationsForReminder(tomorrow);
            
            if (reservations.isEmpty()) {
                log.info("[ExplanationReminderScheduler] 내일 예정된 설명회 예약 없음");
                return;
            }
            
            log.info("[ExplanationReminderScheduler] D-1 리마인더 발송 대상: {}건", reservations.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 각 예약자에게 SMS 발송
            for (ReservationWithSchedule reservationWithSchedule : reservations) {
                try {
                    sendReminderSms(reservationWithSchedule);
                    successCount++;
                } catch (Exception e) {
                    log.error("[ExplanationReminderScheduler] SMS 발송 실패. reservationId={}, error={}", 
                            reservationWithSchedule.getReservation().getId(), e.getMessage());
                    failCount++;
                }
            }
            
            log.info("[ExplanationReminderScheduler] D-1 리마인더 완료. 성공={}, 실패={}", 
                    successCount, failCount);
            
            // 관리자에게 발송 결과 알림
            sendAdminNotification(reservations, successCount);
            
        } catch (Exception e) {
            log.error("[ExplanationReminderScheduler] D-1 리마인더 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 개별 예약에 대한 리마인더 SMS 발송.
     */
    private void sendReminderSms(ReservationWithSchedule reservationWithSchedule) {
        var reservation = reservationWithSchedule.getReservation();
        var schedule = reservationWithSchedule.getSchedule();
        var explanation = reservationWithSchedule.getExplanation();
        
        // 날짜/시간 포맷팅
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String scheduleDate = schedule.getStartAt().format(dateFormatter);
        String scheduleTime = schedule.getStartAt().format(timeFormatter);
        
        // SMS 템플릿 변수 설정
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", reservation.getApplicantName());
        variables.put("title", explanation.getTitle());
        variables.put("attendeeCount", String.valueOf(reservation.getAttendeeCount()));
        variables.put("scheduleDate", scheduleDate + " " + scheduleTime);
        variables.put("location", schedule.getLocation() != null ? schedule.getLocation() : "본원");
        
        // DB 템플릿 USER_EXPLANATION_REMINDER 사용
        smsService.sendMessageByPurposeCode(
            "USER_EXPLANATION_REMINDER",
            reservation.getApplicantPhone(),
            reservation.getApplicantName(),
            variables
        );
        
        log.debug("[ExplanationReminderScheduler] 리마인더 SMS 발송 완료. phone={}, name={}, schedule={}", 
                reservation.getApplicantPhone(), reservation.getApplicantName(), 
                scheduleDate + " " + scheduleTime);
    }
    
    /**
     * 관리자에게 발송 결과 알림.
     * DB 템플릿의 {title}, {totalTeamCount}, {totalAttendeeCount}를 사용합니다.
     */
    private void sendAdminNotification(List<ReservationWithSchedule> reservations, int successCount) {
        try {
            // 첫 번째 설명회 정보 가져오기
            String explanationTitle = "";
            String scheduleDate = "";
            int totalAttendeeCount = 0;
            
            if (!reservations.isEmpty()) {
                ReservationWithSchedule firstReservation = reservations.get(0);
                if (firstReservation.getExplanation() != null) {
                    explanationTitle = firstReservation.getExplanation().getTitle();
                }
                if (firstReservation.getSchedule() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
                    scheduleDate = firstReservation.getSchedule().getStartAt().format(formatter);
                }
                
                // 총 참석 인원 계산
                for (ReservationWithSchedule res : reservations) {
                    totalAttendeeCount += res.getReservation().getAttendeeCount();
                }
            }
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("title", explanationTitle);
            variables.put("totalTeamCount", String.valueOf(reservations.size()));  // 팀 수 = 예약 건수
            variables.put("totalAttendeeCount", String.valueOf(totalAttendeeCount));  // 총 참석 인원
            variables.put("scheduleDate", scheduleDate);
            
            // DB 템플릿 ADMIN_NOTIFICATION 사용
            // 템플릿 내용:
            // [대치숨마투스학원]
            // ■ {title}
            // ■ 일시: {scheduleDate}
            // ■ 신청인원: {totalAttendeeCount}명 ({totalTeamCount}팀)
            // 설명회 리마인더 문자 전송이 완료되었습니다.
            smsService.sendMessageByPurposeCode(
                "ADMIN_NOTIFICATION",
                adminPhoneNumber,
                "관리자",
                variables
            );
            
            log.info("[ExplanationReminderScheduler] 관리자 알림 SMS 발송 완료. 설명회={}, 팀수={}, 총인원={}", 
                    explanationTitle, reservations.size(), totalAttendeeCount);
            
        } catch (Exception e) {
            log.error("[ExplanationReminderScheduler] 관리자 알림 SMS 발송 실패", e);
        }
    }
}