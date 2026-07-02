package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationSchedule;
import com.academy.api.explanation.domain.Explanation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 정보와 회차/설명회 정보를 함께 담는 DTO.
 * D-1 리마인더 등에서 예약과 관련 정보를 함께 조회할 때 사용.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationWithSchedule {
    
    /** 예약 정보 */
    private ExplanationReservation reservation;
    
    /** 회차 정보 */
    private ExplanationSchedule schedule;
    
    /** 설명회 정보 */
    private Explanation explanation;
    
    /**
     * 예약과 회차 정보만 있는 경우의 생성자.
     */
    public ReservationWithSchedule(ExplanationReservation reservation, ExplanationSchedule schedule) {
        this.reservation = reservation;
        this.schedule = schedule;
        this.explanation = null; // Explanation은 별도로 설정해야 함
    }
}