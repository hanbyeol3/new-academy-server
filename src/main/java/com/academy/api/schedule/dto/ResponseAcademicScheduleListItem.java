package com.academy.api.schedule.dto;

import com.academy.api.holiday.domain.Holiday;
import com.academy.api.schedule.domain.AcademicSchedule;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 학사일정 목록 항목 응답 DTO.
 * 
 * 월별 목록 조회에서 사용되는 간소화된 일정 정보입니다.
 * 학사일정과 공휴일을 통합하여 표시합니다.
 */
@Getter
@Setter
@Builder
@Schema(description = "학사일정 목록 항목 응답")
public class ResponseAcademicScheduleListItem {

    @Schema(description = "일정 ID (공휴일의 경우 null)", example = "1")
    private Long id;

    @Schema(description = "일정 제목", example = "2025학년도 1학기 개강일")
    private String title;

    @Schema(description = "상세 설명", example = "신입생과 재학생 모두 해당됩니다")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "시작 일시", example = "2025-03-01 00:00:00")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "종료 일시", example = "2025-03-01 23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "종일 이벤트 여부", example = "true")
    private Boolean isAllDay;

    @Schema(description = "반복 여부", example = "false")
    private Boolean isRepeat;
    
    @Schema(description = "주말 제외 여부", example = "false")
    private Boolean excludeWeekends;

    @Schema(description = "주간 반복 요일 비트마스크", example = "31")
    private Integer weekdayMask;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;
    
    @Schema(description = "이벤트 타입 (ACADEMIC: 학사일정, HOLIDAY: 공휴일)", example = "ACADEMIC")
    private CalendarEventType eventType;
    
    @Schema(description = "공휴일 여부", example = "false")
    private Boolean isHoliday;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponseAcademicScheduleListItem from(AcademicSchedule schedule) {
        return ResponseAcademicScheduleListItem.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .isAllDay(schedule.getIsAllDay())
                .isRepeat(schedule.getIsRepeat())
                .excludeWeekends(schedule.getExcludeWeekends())
                .weekdayMask(schedule.getWeekdayMask())
                .isPublished(schedule.getIsPublished())
                .eventType(CalendarEventType.ACADEMIC)  // 학사일정 타입
                .isHoliday(false)
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(schedule.getCreatedAt())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseAcademicScheduleListItem fromWithNames(AcademicSchedule schedule, 
                                                                String createdByName, 
                                                                String updatedByName) {
        return ResponseAcademicScheduleListItem.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .isAllDay(schedule.getIsAllDay())
                .isRepeat(schedule.getIsRepeat())
                .excludeWeekends(schedule.getExcludeWeekends())
                .weekdayMask(schedule.getWeekdayMask())
                .isPublished(schedule.getIsPublished())
                .eventType(CalendarEventType.ACADEMIC)  // 학사일정 타입
                .isHoliday(false)
                .createdByName(createdByName)
                .createdAt(schedule.getCreatedAt())
                .updatedByName(updatedByName)
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseAcademicScheduleListItem> fromList(List<AcademicSchedule> schedules) {
        return schedules.stream()
                .map(ResponseAcademicScheduleListItem::from)
                .toList();
    }
    
    /**
     * 공휴일 Entity에서 DTO로 변환.
     */
    public static ResponseAcademicScheduleListItem fromHoliday(Holiday holiday) {
        // 공휴일은 종일 이벤트로 처리 (00:00:00 ~ 23:59:59)
        LocalDateTime startDateTime = holiday.getHolidayDate().atTime(LocalTime.MIN);
        LocalDateTime endDateTime = holiday.getHolidayDate().atTime(23, 59, 59);
        
        return ResponseAcademicScheduleListItem.builder()
                .id(null)  // 공휴일은 ID를 null로 설정
                .title(holiday.getName())
                .description(null)
                .startAt(startDateTime)
                .endAt(endDateTime)
                .isAllDay(true)
                .isRepeat(false)
                .excludeWeekends(false)
                .weekdayMask(null)
                .isPublished(true)  // 공휴일은 항상 공개
                .eventType(CalendarEventType.HOLIDAY)
                .isHoliday(true)
                .createdByName("시스템")
                .createdAt(holiday.getCreatedAt())
                .updatedByName("시스템")
                .updatedAt(holiday.getUpdatedAt())
                .build();
    }
}