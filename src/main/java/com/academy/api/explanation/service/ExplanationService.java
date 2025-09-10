package com.academy.api.explanation.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.explanation.model.*;
import org.springframework.data.domain.Pageable;

/**
 * 설명회 서비스 인터페이스.
 */
public interface ExplanationService {

    // ===== Public API =====
    
    /**
     * 설명회 이벤트 목록 조회.
     */
    ResponseList<ResponseExplanationEventListItem> getEvents(ExplanationEventSearchCriteria criteria, Pageable pageable);

    /**
     * 설명회 이벤트 상세 조회.
     */
    ResponseData<ResponseExplanationEventDetail> getEvent(Long eventId);

    /**
     * 비회원 예약 조회.
     */
    ResponseData<ResponseReservation> searchGuestReservation(Long eventId, GuestReservationSearchRequest request);

    // ===== Reservation API =====
    
    /**
     * 설명회 예약 신청.
     */
    ResponseData<Long> createReservation(Long eventId, RequestReservationCreate request, Long currentMemberId);

    /**
     * 설명회 예약 취소.
     */
    Response cancelReservation(Long eventId, Long reservationId, Long currentMemberId);

    /**
     * 내 예약 조회 (회원용).
     */
    ResponseData<ResponseReservation> getMyReservation(Long eventId, Long currentMemberId);

    // ===== Admin API =====
    
    /**
     * 설명회 이벤트 생성.
     */
    ResponseData<Long> createEvent(RequestExplanationEventCreate request);

    /**
     * 설명회 이벤트 수정.
     */
    Response updateEvent(Long eventId, RequestExplanationEventUpdate request);

    /**
     * 설명회 상태 변경.
     */
    Response updateEventStatus(Long eventId, RequestStatusUpdate request);

    /**
     * 설명회 예약 목록 조회 (관리자용).
     */
    ResponseList<ResponseReservationAdminItem> getEventReservations(Long eventId, Pageable pageable);

    /**
     * 설명회 예약 강제 취소 (관리자용).
     */
    Response forceCancel(Long eventId, Long reservationId);
}