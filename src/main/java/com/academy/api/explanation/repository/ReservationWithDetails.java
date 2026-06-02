package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.Explanation;
import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 정보와 관련 설명회/회차 정보를 함께 담는 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationWithDetails {
    
    private ExplanationReservation reservation;
    private ExplanationSchedule schedule;
    private Explanation explanation;
}