package com.academy.api.schedule.service;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.holiday.domain.Holiday;
import com.academy.api.holiday.repository.HolidayRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
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
    private final HolidayRepository holidayRepository;
    private final MemberRepository memberRepository;

    /**
     * 월별 학사일정 조회 (공휴일 포함).
     */
    @Override
    public ResponseList<ResponseAcademicScheduleListItem> getMonthlySchedules(RequestAcademicScheduleSearch searchRequest) {
        log.info("[AcademicScheduleService] 월별 일정 조회 시작. year={}, month={}", 
                searchRequest.getYear(), searchRequest.getMonth());

        try {
            // 월의 첫날과 마지막날 계산
            LocalDate monthStart = LocalDate.of(searchRequest.getYear(), searchRequest.getMonth(), 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            // 1. 학사일정 조회
            List<AcademicSchedule> schedules = scheduleRepository.findSchedulesInMonth(monthStart, monthEnd);
            
            // 2. 공휴일 조회
            List<Holiday> holidays = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(monthStart, monthEnd);
            
            // 3. 통합 리스트 생성
            List<ResponseAcademicScheduleListItem> items = new ArrayList<>();
            
            // 학사일정 추가 (회원 이름 포함)
            for (AcademicSchedule schedule : schedules) {
                String createdByName = getMemberName(schedule.getCreatedBy());
                String updatedByName = getMemberName(schedule.getUpdatedBy());
                ResponseAcademicScheduleListItem item = ResponseAcademicScheduleListItem.fromWithNames(
                    schedule, createdByName, updatedByName
                );
                items.add(item);
            }
            
            // 공휴일 추가
            for (Holiday holiday : holidays) {
                ResponseAcademicScheduleListItem item = ResponseAcademicScheduleListItem.fromHoliday(holiday);
                items.add(item);
            }
            
            // 4. 날짜순 정렬
            items.sort(Comparator.comparing(ResponseAcademicScheduleListItem::getStartAt));

            log.debug("[AcademicScheduleService] 월별 일정 조회 완료. 학사일정={}건, 공휴일={}건, 총={}건", 
                    schedules.size(), holidays.size(), items.size());
            
            return ResponseList.ok(items, (long) items.size(), 0, items.size());

        } catch (Exception e) {
            log.error("[AcademicScheduleService] 월별 일정 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(List.of(), 0L, 0, 0);
        }
    }

    /**
     * 관리자용 월별 학사일정 조회 (비공개 포함, 공휴일 포함).
     */
    @Override
    public ResponseList<ResponseAcademicScheduleListItem> getMonthlySchedulesForAdmin(RequestAcademicScheduleSearch searchRequest) {
        log.info("[AcademicScheduleService] 관리자용 월별 일정 조회 시작. year={}, month={}", 
                searchRequest.getYear(), searchRequest.getMonth());

        try {
            // 월의 첫날과 마지막날 계산
            LocalDate monthStart = LocalDate.of(searchRequest.getYear(), searchRequest.getMonth(), 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            // 1. 모든 학사일정 조회 (비공개 포함)
            List<AcademicSchedule> schedules = scheduleRepository.findAllSchedulesInMonth(monthStart, monthEnd);
            
            // 2. 공휴일 조회
            List<Holiday> holidays = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(monthStart, monthEnd);
            
            // 3. 통합 리스트 생성
            List<ResponseAcademicScheduleListItem> items = new ArrayList<>();
            
            // 학사일정 추가 (회원 이름 포함)
            for (AcademicSchedule schedule : schedules) {
                String createdByName = getMemberName(schedule.getCreatedBy());
                String updatedByName = getMemberName(schedule.getUpdatedBy());
                ResponseAcademicScheduleListItem item = ResponseAcademicScheduleListItem.fromWithNames(
                    schedule, createdByName, updatedByName
                );
                items.add(item);
            }
            
            // 공휴일 추가
            for (Holiday holiday : holidays) {
                ResponseAcademicScheduleListItem item = ResponseAcademicScheduleListItem.fromHoliday(holiday);
                items.add(item);
            }
            
            // 4. 날짜순 정렬
            items.sort(Comparator.comparing(ResponseAcademicScheduleListItem::getStartAt));

            log.debug("[AcademicScheduleService] 관리자용 월별 일정 조회 완료. 학사일정={}건, 공휴일={}건, 총={}건", 
                    schedules.size(), holidays.size(), items.size());
            
            return ResponseList.ok(items, (long) items.size(), 0, items.size());

        } catch (Exception e) {
            log.error("[AcademicScheduleService] 관리자용 월별 일정 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(List.of(), 0L, 0, 0);
        }
    }

    /**
     * 관리자용 학사일정 목록 조회 (연도별 필터링 지원, 공휴일 포함).
     */
    @Override
    public ResponseList<ResponseAcademicScheduleListItem> getScheduleList(Integer year, Pageable pageable) {
        log.info("[AcademicScheduleService] 일정 목록 조회 시작. year={}, page={}, size={}", 
                year, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // 연도 유효성 검증
            if (year != null && (year < 1900 || year > 2100)) {
                log.warn("[AcademicScheduleService] 유효하지 않은 연도: {}", year);
                return ResponseList.ok(List.of(), 0L, 0, 0);
            }

            List<ResponseAcademicScheduleListItem> allItems = new ArrayList<>();
            
            // 1. 학사일정 조회
            Page<AcademicSchedule> schedulePage;
            
            if (year != null) {
                // 연도별 조회
                schedulePage = scheduleRepository.findSchedulesByYear(year, pageable);
                log.debug("[AcademicScheduleService] {}년도 일정 조회 완료. 총 {}개", year, schedulePage.getTotalElements());
            } else {
                // 전체 조회
                schedulePage = scheduleRepository.findAllSchedules(pageable);
                log.debug("[AcademicScheduleService] 전체 일정 조회 완료. 총 {}개", schedulePage.getTotalElements());
            }
            
            // 학사일정 변환
            for (AcademicSchedule schedule : schedulePage.getContent()) {
                String createdByName = getMemberName(schedule.getCreatedBy());
                String updatedByName = getMemberName(schedule.getUpdatedBy());
                allItems.add(ResponseAcademicScheduleListItem.fromWithNames(schedule, createdByName, updatedByName));
            }
            
            // 2. 공휴일 조회 및 추가 (연도별 필터링일 경우에만)
            int holidayCount = 0;
            if (year != null) {
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                List<Holiday> holidays = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(yearStart, yearEnd);
                holidayCount = holidays.size();
                
                log.debug("[AcademicScheduleService] {}년도 공휴일 조회 완료. 총 {}개", year, holidayCount);
                
                // 공휴일 변환 및 추가
                for (Holiday holiday : holidays) {
                    allItems.add(ResponseAcademicScheduleListItem.fromHoliday(holiday));
                }
            }
            
            // 3. 날짜순 정렬
            allItems.sort(Comparator.comparing(ResponseAcademicScheduleListItem::getStartAt));
            
            // 페이징 처리 (수동)
            int totalElements = allItems.size();
            int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), totalElements);
            int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);
            List<ResponseAcademicScheduleListItem> pagedItems = allItems.subList(fromIndex, toIndex);

            log.debug("[AcademicScheduleService] 전체 일정 목록 조회 완료. 학사일정={}, 공휴일={}, 총={}", 
                    schedulePage.getTotalElements(), 
                    holidayCount,
                    totalElements);
            
            return ResponseList.ok(
                    pagedItems,
                    (long) totalElements,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
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

            // 시간 겹침 검증 제거 - 학교 환경에서는 여러 일정이 동시에 진행될 수 있음
            // 예: 개강일, 신입생 오리엔테이션, 정기수업, 도서관 운영 등이 같은 날 동시 진행 가능

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

                        // 시간 겹침 검증 제거 - 학교 환경에서는 여러 일정이 동시에 진행될 수 있음
                        // 수정 시에도 다른 일정과 시간이 겹치는 것을 허용

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
                                request.getIsPublished(),
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
     * 학사일정 공개 상태 변경.
     *
     * @param id 대상 학사일정 ID
     * @param isPublished 공개 여부 (true: 공개, false: 비공개)
     * @return 변경 결과
     */
    @Override
    @Transactional
    public Response updatePublishedStatus(Long id, Boolean isPublished) {
        log.info("[AcademicScheduleService] 공개 상태 변경 시작. id={}, isPublished={}", id, isPublished);
        
        return scheduleRepository.findById(id)
                .map(schedule -> {
                    try {
                        schedule.updatePublishedStatus(isPublished);
                        
                        log.debug("[AcademicScheduleService] 공개 상태 변경 완료. id={}, 새상태={}", id, isPublished);
                        
                        String statusMessage = Boolean.TRUE.equals(isPublished) 
                            ? "학사일정이 공개로 변경되었습니다" 
                            : "학사일정이 비공개로 변경되었습니다";
                            
                        return Response.ok("0000", statusMessage);
                        
                    } catch (Exception e) {
                        log.error("[AcademicScheduleService] 공개 상태 변경 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "공개 상태 변경 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[AcademicScheduleService] 공개 상태를 변경할 일정을 찾을 수 없음. id={}", id);
                    return Response.error("S404", "학사일정을 찾을 수 없습니다");
                });
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