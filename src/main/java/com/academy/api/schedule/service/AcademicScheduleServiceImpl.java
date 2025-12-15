package com.academy.api.schedule.service;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.schedule.domain.AcademicSchedule;
import com.academy.api.schedule.dto.*;
import com.academy.api.schedule.repository.AcademicScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 학사일정 서비스 구현체.
 * 
 * - 학사일정 CRUD 비즈니스 로직 처리
 * - 종일 이벤트 정규화 (half-open interval 방식)
 * - 반복 일정 월별 조회 최적화
 * - end_at NULL 처리 (종료시간 미정 일정)
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicScheduleServiceImpl implements AcademicScheduleService {

    private final AcademicScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    /**
     * 월별 공개 학사일정 조회.
     */
    @Override
    public ResponseList<ResponseAcademicScheduleListItem> getMonthlySchedules(RequestAcademicScheduleSearch searchRequest) {
        log.info("[AcademicScheduleService] 월별 일정 조회 시작. year={}, month={}", 
                searchRequest.getYear(), searchRequest.getMonth());

        try {
            // 월의 첫날과 마지막날 계산
            LocalDate monthStart = LocalDate.of(searchRequest.getYear(), searchRequest.getMonth(), 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            List<AcademicSchedule> schedules = scheduleRepository.findSchedulesInMonth(monthStart, monthEnd);

            // 회원 이름을 포함한 DTO 변환
            List<ResponseAcademicScheduleListItem> items = schedules.stream()
                    .map(schedule -> {
                        String createdByName = getMemberName(schedule.getCreatedBy());
                        String updatedByName = getMemberName(schedule.getUpdatedBy());
                        return ResponseAcademicScheduleListItem.fromWithNames(schedule, createdByName, updatedByName);
                    })
                    .toList();

            log.debug("[AcademicScheduleService] 월별 일정 조회 완료. 조회된 항목 수={}", items.size());
            
            return ResponseList.ok(items, (long) items.size(), 0, items.size());

        } catch (Exception e) {
            log.error("[AcademicScheduleService] 월별 일정 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(List.of(), 0L, 0, 0);
        }
    }

    /**
     * 관리자용 전체 학사일정 목록 조회.
     */
    @Override
    public ResponseList<ResponseAcademicScheduleListItem> getScheduleList(Pageable pageable) {
        log.info("[AcademicScheduleService] 전체 일정 목록 조회 시작. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<AcademicSchedule> schedulePage = scheduleRepository.findAllSchedules(pageable);
            
            List<ResponseAcademicScheduleListItem> items = schedulePage.getContent()
                    .stream()
                    .map(schedule -> {
                        String createdByName = getMemberName(schedule.getCreatedBy());
                        String updatedByName = getMemberName(schedule.getUpdatedBy());
                        return ResponseAcademicScheduleListItem.fromWithNames(schedule, createdByName, updatedByName);
                    })
                    .toList();

            log.debug("[AcademicScheduleService] 전체 일정 목록 조회 완료. 조회된 항목 수={}, 전체 항목 수={}", 
                    schedulePage.getNumberOfElements(), schedulePage.getTotalElements());
            
            return ResponseList.ok(
                    items,
                    schedulePage.getTotalElements(),
                    schedulePage.getNumber(),
                    schedulePage.getSize()
            );

        } catch (Exception e) {
            log.error("[AcademicScheduleService] 전체 일정 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(
                    List.of(),
                    0L,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
    }

    /**
     * 학사일정 상세 조회.
     */
    @Override
    public ResponseData<ResponseAcademicSchedule> getSchedule(Long id) {
        log.info("[AcademicScheduleService] 일정 상세 조회 시작. id={}", id);

        return scheduleRepository.findById(id)
                .map(schedule -> {
                    String createdByName = getMemberName(schedule.getCreatedBy());
                    String updatedByName = getMemberName(schedule.getUpdatedBy());
                    
                    ResponseAcademicSchedule response = ResponseAcademicSchedule.fromWithNames(schedule, createdByName, updatedByName);
                    log.debug("[AcademicScheduleService] 일정 상세 조회 완료. id={}, title={}", id, schedule.getTitle());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[AcademicScheduleService] 일정을 찾을 수 없음. id={}", id);
                    return ResponseData.error("S404", "학사일정을 찾을 수 없습니다");
                });
    }

    /**
     * 학사일정 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createSchedule(RequestAcademicScheduleCreate request) {
        log.info("[AcademicScheduleService] 일정 생성 시작. title={}, isAllDay={}", 
                request.getTitle(), request.getIsAllDay());

        try {
            // 종일 이벤트 정규화
            LocalDateTime startAt = request.getStartAt();
            LocalDateTime endAt = request.getEndAt();

            if (Boolean.TRUE.equals(request.getIsAllDay())) {
                // Half-open interval 방식 적용
                startAt = normalizeAllDayStart(startAt);
                endAt = normalizeAllDayEnd(startAt, endAt);
                log.debug("[AcademicScheduleService] 종일 이벤트 정규화. startAt={}, endAt={}", startAt, endAt);
            }

            // 시간 겹침 검증
            long overlappingCount = scheduleRepository.countOverlappingSchedules(startAt, endAt);
            if (overlappingCount > 0) {
                log.warn("[AcademicScheduleService] 동일 시간대 일정 존재. overlappingCount={}", overlappingCount);
                return ResponseData.error("S409", "동일한 시간대에 다른 일정이 존재합니다");
            }

            // 엔티티 생성 및 저장
            AcademicSchedule schedule = AcademicSchedule.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startAt(startAt)
                    .endAt(endAt)
                    .isAllDay(request.getIsAllDay())
                    .isRepeat(request.getIsRepeat())
                    .weekdayMask(request.getWeekdayMask())
                    .excludeWeekends(request.getExcludeWeekends())
                    .createdBy(SecurityUtils.getCurrentUserId())
                    .build();

            AcademicSchedule savedSchedule = scheduleRepository.save(schedule);
            
            log.debug("[AcademicScheduleService] 일정 생성 완료. id={}, title={}", 
                    savedSchedule.getId(), savedSchedule.getTitle());
            
            return ResponseData.ok("0000", "학사일정이 생성되었습니다", savedSchedule.getId());

        } catch (Exception e) {
            log.error("[AcademicScheduleService] 일정 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "일정 생성 중 오류가 발생했습니다");
        }
    }

    /**
     * 학사일정 수정.
     */
    @Override
    @Transactional
    public Response updateSchedule(Long id, RequestAcademicScheduleUpdate request) {
        log.info("[AcademicScheduleService] 일정 수정 시작. id={}, title={}, isAllDay={}", 
                id, request.getTitle(), request.getIsAllDay());

        return scheduleRepository.findById(id)
                .map(schedule -> {
                    try {
                        // 종일 이벤트 정규화
                        LocalDateTime startAt = request.getStartAt();
                        LocalDateTime endAt = request.getEndAt();

                        if (Boolean.TRUE.equals(request.getIsAllDay())) {
                            startAt = normalizeAllDayStart(startAt);
                            endAt = normalizeAllDayEnd(startAt, endAt);
                            log.debug("[AcademicScheduleService] 종일 이벤트 정규화. startAt={}, endAt={}", startAt, endAt);
                        }

                        // 시간 겹침 검증 (자기 자신 제외)
                        long overlappingCount = scheduleRepository.countOverlappingSchedulesExcluding(startAt, endAt, id);
                        if (overlappingCount > 0) {
                            log.warn("[AcademicScheduleService] 동일 시간대 다른 일정 존재. overlappingCount={}", overlappingCount);
                            return Response.error("S409", "동일한 시간대에 다른 일정이 존재합니다");
                        }

                        // 엔티티 업데이트
                        schedule.update(
                                request.getTitle(),
                                request.getDescription(),
                                startAt,
                                endAt,
                                request.getIsAllDay(),
                                request.getIsRepeat(),
                                request.getWeekdayMask(),
                                request.getExcludeWeekends(),
                                SecurityUtils.getCurrentUserId()
                        );

                        scheduleRepository.save(schedule);
                        
                        log.debug("[AcademicScheduleService] 일정 수정 완료. id={}, title={}", id, schedule.getTitle());
                        
                        return Response.ok("0000", "학사일정이 수정되었습니다");

                    } catch (Exception e) {
                        log.error("[AcademicScheduleService] 일정 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "일정 수정 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[AcademicScheduleService] 수정할 일정을 찾을 수 없음. id={}", id);
                    return Response.error("S404", "학사일정을 찾을 수 없습니다");
                });
    }

    /**
     * 학사일정 삭제.
     */
    @Override
    @Transactional
    public Response deleteSchedule(Long id) {
        log.info("[AcademicScheduleService] 일정 삭제 시작. id={}", id);

        return scheduleRepository.findById(id)
                .map(schedule -> {
                    try {
                        scheduleRepository.delete(schedule);
                        
                        log.debug("[AcademicScheduleService] 일정 삭제 완료. id={}, title={}", id, schedule.getTitle());
                        
                        return Response.ok("0000", "학사일정이 삭제되었습니다");

                    } catch (Exception e) {
                        log.error("[AcademicScheduleService] 일정 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "일정 삭제 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[AcademicScheduleService] 삭제할 일정을 찾을 수 없음. id={}", id);
                    return Response.error("S404", "학사일정을 찾을 수 없습니다");
                });
    }

    /**
     * 학사일정 공개/비공개 상태 변경.
     */
    @Override
    @Transactional
    public Response togglePublicStatus(Long id) {
        log.info("[AcademicScheduleService] 일정 공개 상태 전환 시작. id={}", id);
        
        // isPublic 필드가 제거되어 이 기능은 사용할 수 없음
        log.warn("[AcademicScheduleService] isPublic 필드가 없어 공개 상태 전환 불가. id={}", id);
        return Response.error("E501", "공개 상태 전환 기능을 사용할 수 없습니다");
    }

    /**
     * 종일 이벤트 시작시간 정규화 (00:00:00으로 고정).
     */
    private LocalDateTime normalizeAllDayStart(LocalDateTime startAt) {
        if (startAt == null) return null;
        return startAt.toLocalDate().atStartOfDay();
    }

    /**
     * 종일 이벤트 종료시간 정규화 (half-open interval 방식).
     * 
     * 사용자 피드백에 따른 half-open interval 적용:
     * - 단일일: start 00:00:00, end 다음날 00:00:00
     * - 기간: start 시작일 00:00:00, end 종료일+1 00:00:00
     */
    private LocalDateTime normalizeAllDayEnd(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null) return null;
        
        if (endAt == null) {
            // 종료시간 미정인 경우 시작일 다음날 00:00:00으로 설정 (단일일 처리)
            return startAt.toLocalDate().plusDays(1).atStartOfDay();
        }
        
        // 종료일 다음날 00:00:00으로 설정 (half-open interval)
        return endAt.toLocalDate().plusDays(1).atStartOfDay();
    }

    /**
     * 회원 이름 조회 도우미 메서드.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return "Unknown";
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }
}