package com.academy.api.explanation.service;

import com.academy.api.domain.member.Member;
import com.academy.api.domain.member.MemberRepository;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.domain.ExplanationEventStatus;
import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationReservationStatus;
import com.academy.api.explanation.model.*;
import com.academy.api.explanation.repository.ExplanationEventQueryRepository;
import com.academy.api.explanation.repository.ExplanationEventRepository;
import com.academy.api.explanation.repository.ExplanationReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 설명회 서비스 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExplanationServiceImpl implements ExplanationService {

    private final ExplanationEventRepository eventRepository;
    private final ExplanationEventQueryRepository eventQueryRepository;
    private final ExplanationReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    // ===== Public API =====

    @Override
    public ResponseList<ResponseExplanationEventListItem> getEvents(ExplanationEventSearchCriteria criteria, Pageable pageable) {
        log.info("[ExplanationService] 설명회 목록 조회 시작. 조건={}, 페이지={}", criteria, pageable);
        
        Page<ResponseExplanationEventListItem> page = eventQueryRepository.search(criteria, pageable);
        ResponseList<ResponseExplanationEventListItem> result = ResponseList.from(page);
        
        log.debug("[ExplanationService] 설명회 목록 조회 완료. 전체={}건, 페이지={}", result.getTotal(), result.getPage());
        
        return result;
    }

    @Override
    public ResponseData<ResponseExplanationEventDetail> getEvent(Long eventId) {
        log.info("[ExplanationService] 설명회 상세 조회 시작. ID={}", eventId);
        
        return eventRepository.findById(eventId)
                .filter(ExplanationEvent::getPublished)
                .map(event -> {
                    ResponseExplanationEventDetail detail = ResponseExplanationEventDetail.from(event);
                    log.debug("[ExplanationService] 설명회 상세 조회 완료. ID={}, 제목={}", eventId, event.getTitle());
                    return ResponseData.ok(detail);
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 설명회 미존재 또는 비공개. ID={}", eventId);
                    return ResponseData.error("EVENT_NOT_FOUND", "설명회를 찾을 수 없습니다.");
                });
    }

    @Override
    public ResponseData<ResponseReservation> searchGuestReservation(Long eventId, GuestReservationSearchRequest request) {
        log.info("[ExplanationService] 비회원 예약 조회 시작. eventId={}, name={}, phone={}", 
                eventId, request.getName(), maskPhoneForLog(request.getPhone()));
        
        return reservationRepository.findByEventIdAndGuestNameAndGuestPhone(eventId, request.getName(), request.getPhone())
                .map(reservation -> {
                    log.debug("[ExplanationService] 비회원 예약 조회 완료. reservationId={}", reservation.getId());
                    return ResponseData.ok(ResponseReservation.from(reservation));
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 비회원 예약 미존재. eventId={}, name={}", eventId, request.getName());
                    return ResponseData.error("RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다.");
                });
    }

    // ===== Reservation API =====

    @Override
    @Transactional
    public ResponseData<Long> createReservation(Long eventId, RequestReservationCreate request, Long currentMemberId) {
        log.info("[ExplanationService] 예약 신청 시작. eventId={}, isMember={}, memberId={}", 
                eventId, request.isMemberReservation(), currentMemberId);
        
        try {
            // 1. 이벤트 조회 (락 적용)
            ExplanationEvent event = eventRepository.findByIdWithLock(eventId)
                    .orElse(null);
            
            if (event == null) {
                log.warn("[ExplanationService] 설명회 미존재. eventId={}", eventId);
                return ResponseData.error("EVENT_NOT_FOUND", "설명회를 찾을 수 없습니다.");
            }
            
            // 2. 기본 검증
            if (!event.getPublished()) {
                log.warn("[ExplanationService] 비공개 설명회. eventId={}", eventId);
                return ResponseData.error("EVENT_NOT_FOUND", "설명회를 찾을 수 없습니다.");
            }
            
            // 3. 신청 기간 검증
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(event.getApplyStartAt()) || now.isAfter(event.getApplyEndAt())) {
                log.warn("[ExplanationService] 신청 기간 외. eventId={}, 현재시간={}, 신청기간={}-{}", 
                        eventId, now, event.getApplyStartAt(), event.getApplyEndAt());
                return ResponseData.error("OUT_OF_APPLY_PERIOD", "신청 기간이 아닙니다.");
            }
            
            // 4. 상태 검증
            if (event.getStatus() != ExplanationEventStatus.RESERVABLE) {
                log.warn("[ExplanationService] 예약 불가 상태. eventId={}, status={}", eventId, event.getStatus());
                return ResponseData.error("EVENT_CLOSED", "예약이 마감되었습니다.");
            }
            
            // 5. 정원 검증
            if (!event.canAcceptReservation()) {
                log.warn("[ExplanationService] 정원 초과. eventId={}, capacity={}, reserved={}", 
                        eventId, event.getCapacity(), event.getReservedCount());
                return ResponseData.error("CAPACITY_FULL", "예약 정원이 초과되었습니다.");
            }
            
            // 6. 중복 예약 검증 및 예약 생성
            ExplanationReservation reservation;
            
            if (request.isMemberReservation()) {
                // 회원 예약
                if (currentMemberId == null) {
                    log.warn("[ExplanationService] 비로그인 상태에서 회원 예약 시도. eventId={}", eventId);
                    return ResponseData.error("AUTH_REQUIRED", "로그인이 필요합니다.");
                }
                
                // 기존 예약 확인
                if (reservationRepository.findByEventIdAndMemberIdAndStatus(eventId, currentMemberId, ExplanationReservationStatus.CONFIRMED).isPresent()) {
                    log.warn("[ExplanationService] 회원 중복 예약 시도. eventId={}, memberId={}", eventId, currentMemberId);
                    return ResponseData.error("DUPLICATE_RESERVATION", "이미 예약하셨습니다.");
                }
                
                reservation = ExplanationReservation.builder()
                        .eventId(eventId)
                        .memberId(currentMemberId)
                        .build();
                        
                log.debug("[ExplanationService] 회원 예약 생성. eventId={}, memberId={}", eventId, currentMemberId);
                
            } else {
                // 비회원 예약
                if (!request.isGuestReservation()) {
                    log.warn("[ExplanationService] 비회원 예약 정보 부족. eventId={}", eventId);
                    return ResponseData.error("INVALID_GUEST_INFO", "비회원 예약 정보를 입력해주세요.");
                }
                
                String guestName = request.getGuest().getName();
                String guestPhone = request.getGuest().getPhone();
                
                // 기존 예약 확인
                if (reservationRepository.findByEventIdAndGuestNameAndGuestPhoneAndStatus(eventId, guestName, guestPhone, ExplanationReservationStatus.CONFIRMED).isPresent()) {
                    log.warn("[ExplanationService] 비회원 중복 예약 시도. eventId={}, name={}", eventId, guestName);
                    return ResponseData.error("DUPLICATE_RESERVATION", "이미 예약하셨습니다.");
                }
                
                reservation = ExplanationReservation.builder()
                        .eventId(eventId)
                        .guestName(guestName)
                        .guestPhone(guestPhone)
                        .build();
                        
                log.debug("[ExplanationService] 비회원 예약 생성. eventId={}, name={}", eventId, guestName);
            }
            
            // 7. 예약 저장 및 예약자 수 증가
            ExplanationReservation savedReservation = reservationRepository.save(reservation);
            event.incrementReservedCount();
            
            log.info("[ExplanationService] 예약 신청 완료. reservationId={}, eventId={}, 예약자수={}", 
                    savedReservation.getId(), eventId, event.getReservedCount());
            
            return ResponseData.ok("0000", "예약이 완료되었습니다.", savedReservation.getId());
            
        } catch (Exception e) {
            log.error("[ExplanationService] 예약 신청 중 오류 발생. eventId={}", eventId, e);
            return ResponseData.error("RESERVATION_FAILED", "예약 신청에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public Response cancelReservation(Long eventId, Long reservationId, Long currentMemberId) {
        log.info("[ExplanationService] 예약 취소 시작. eventId={}, reservationId={}, memberId={}", 
                eventId, reservationId, currentMemberId);
        
        return reservationRepository.findById(reservationId)
                .filter(reservation -> reservation.getEventId().equals(eventId))
                .filter(reservation -> reservation.getStatus() == ExplanationReservationStatus.CONFIRMED)
                .map(reservation -> {
                    // 소유자 확인
                    if (reservation.isMemberReservation()) {
                        if (currentMemberId == null || !reservation.isOwner(currentMemberId)) {
                            log.warn("[ExplanationService] 회원 예약 취소 권한 없음. reservationId={}, memberId={}", reservationId, currentMemberId);
                            return Response.error("ACCESS_DENIED", "예약을 취소할 권한이 없습니다.");
                        }
                    }
                    
                    // 예약 취소
                    reservation.cancel();
                    
                    // 이벤트 예약자 수 감소
                    eventRepository.findById(eventId).ifPresent(ExplanationEvent::decrementReservedCount);
                    
                    log.info("[ExplanationService] 예약 취소 완료. reservationId={}", reservationId);
                    return Response.ok("0000", "예약이 취소되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 취소할 예약 미존재. eventId={}, reservationId={}", eventId, reservationId);
                    return Response.error("RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다.");
                });
    }

    @Override
    public ResponseData<ResponseReservation> getMyReservation(Long eventId, Long currentMemberId) {
        log.info("[ExplanationService] 내 예약 조회 시작. eventId={}, memberId={}", eventId, currentMemberId);
        
        if (currentMemberId == null) {
            log.warn("[ExplanationService] 비로그인 상태에서 내 예약 조회 시도");
            return ResponseData.error("AUTH_REQUIRED", "로그인이 필요합니다.");
        }
        
        return reservationRepository.findByEventIdAndMemberId(eventId, currentMemberId)
                .map(reservation -> {
                    log.debug("[ExplanationService] 내 예약 조회 완료. reservationId={}", reservation.getId());
                    return ResponseData.ok(ResponseReservation.from(reservation));
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 내 예약 미존재. eventId={}, memberId={}", eventId, currentMemberId);
                    return ResponseData.error("RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다.");
                });
    }

    // ===== Admin API =====

    @Override
    @Transactional
    public ResponseData<Long> createEvent(RequestExplanationEventCreate request) {
        log.info("[ExplanationService] 설명회 생성 시작. 제목={}, 구분={}", request.getTitle(), request.getDivision());
        
        try {
            ExplanationEvent event = ExplanationEvent.builder()
                    .division(request.getDivision())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .status(request.getStatus())
                    .startAt(request.getStartAt())
                    .endAt(request.getEndAt())
                    .applyStartAt(request.getApplyStartAt())
                    .applyEndAt(request.getApplyEndAt())
                    .capacity(request.getCapacity())
                    .location(request.getLocation())
                    .pinned(request.getPinned())
                    .published(request.getPublished())
                    .build();
                    
            ExplanationEvent savedEvent = eventRepository.save(event);
            
            log.info("[ExplanationService] 설명회 생성 완료. eventId={}, 제목={}", savedEvent.getId(), savedEvent.getTitle());
            
            return ResponseData.ok("0000", "설명회가 생성되었습니다.", savedEvent.getId());
            
        } catch (Exception e) {
            log.error("[ExplanationService] 설명회 생성 중 오류 발생. 제목={}", request.getTitle(), e);
            return ResponseData.error("EVENT_CREATE_FAILED", "설명회 생성에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public Response updateEvent(Long eventId, RequestExplanationEventUpdate request) {
        log.info("[ExplanationService] 설명회 수정 시작. eventId={}, 제목={}", eventId, request.getTitle());
        
        return eventRepository.findById(eventId)
                .map(event -> {
                    event.update(
                        request.getDivision(),
                        request.getTitle(),
                        request.getContent(),
                        request.getStartAt(),
                        request.getEndAt(),
                        request.getApplyStartAt(),
                        request.getApplyEndAt(),
                        request.getCapacity(),
                        request.getLocation(),
                        request.getPinned(),
                        request.getPublished()
                    );
                    
                    log.info("[ExplanationService] 설명회 수정 완료. eventId={}", eventId);
                    return Response.ok("0000", "설명회가 수정되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 수정할 설명회 미존재. eventId={}", eventId);
                    return Response.error("EVENT_NOT_FOUND", "설명회를 찾을 수 없습니다.");
                });
    }

    @Override
    @Transactional
    public Response updateEventStatus(Long eventId, RequestStatusUpdate request) {
        log.info("[ExplanationService] 설명회 상태 변경 시작. eventId={}, status={}", eventId, request.getStatus());
        
        return eventRepository.findById(eventId)
                .map(event -> {
                    event.updateStatus(request.getStatus());
                    
                    log.info("[ExplanationService] 설명회 상태 변경 완료. eventId={}, status={}", eventId, request.getStatus());
                    return Response.ok("0000", "설명회 상태가 변경되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 상태 변경할 설명회 미존재. eventId={}", eventId);
                    return Response.error("EVENT_NOT_FOUND", "설명회를 찾을 수 없습니다.");
                });
    }

    @Override
    public ResponseList<ResponseReservationAdminItem> getEventReservations(Long eventId, Pageable pageable) {
        log.info("[ExplanationService] 설명회 예약 목록 조회 시작. eventId={}, 페이지={}", eventId, pageable);
        
        List<ExplanationReservation> reservations = reservationRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        
        List<ResponseReservationAdminItem> items = reservations.stream()
                .map(reservation -> {
                    String memberName = null;
                    if (reservation.isMemberReservation()) {
                        memberName = memberRepository.findById(reservation.getMemberId())
                                .map(Member::getMemberName)
                                .orElse("탈퇴한 회원");
                    }
                    return ResponseReservationAdminItem.from(reservation, memberName);
                })
                .collect(Collectors.toList());
        
        // 수동 페이지네이션 (간단 구현)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<ResponseReservationAdminItem> pageContent = items.subList(start, end);
        
        Page<ResponseReservationAdminItem> page = new PageImpl<>(pageContent, pageable, items.size());
        ResponseList<ResponseReservationAdminItem> result = ResponseList.from(page);
        
        log.debug("[ExplanationService] 설명회 예약 목록 조회 완료. eventId={}, 총예약={}건", eventId, items.size());
        
        return result;
    }

    @Override
    @Transactional
    public Response forceCancel(Long eventId, Long reservationId) {
        log.info("[ExplanationService] 예약 강제 취소 시작. eventId={}, reservationId={}", eventId, reservationId);
        
        return reservationRepository.findById(reservationId)
                .filter(reservation -> reservation.getEventId().equals(eventId))
                .filter(reservation -> reservation.getStatus() == ExplanationReservationStatus.CONFIRMED)
                .map(reservation -> {
                    // 예약 취소
                    reservation.cancel();
                    
                    // 이벤트 예약자 수 감소
                    eventRepository.findById(eventId).ifPresent(ExplanationEvent::decrementReservedCount);
                    
                    log.info("[ExplanationService] 예약 강제 취소 완료. reservationId={}", reservationId);
                    return Response.ok("0000", "예약이 취소되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ExplanationService] 강제 취소할 예약 미존재. eventId={}, reservationId={}", eventId, reservationId);
                    return Response.error("RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다.");
                });
    }

    /**
     * 로깅용 전화번호 마스킹.
     */
    private String maskPhoneForLog(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}