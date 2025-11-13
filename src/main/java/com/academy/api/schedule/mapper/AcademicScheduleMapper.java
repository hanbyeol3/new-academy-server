package com.academy.api.schedule.mapper;

import com.academy.api.schedule.domain.AcademicSchedule;
import com.academy.api.schedule.dto.RequestAcademicScheduleCreate;
import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 학사일정 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class AcademicScheduleMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     */
    public AcademicSchedule toEntity(RequestAcademicScheduleCreate request) {
        return AcademicSchedule.builder()
                .title(request.getTitle())
                .description(null) // DTO에 description 필드가 없으므로 null
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(null) // DTO에 createdBy 필드가 없으므로 null
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환.
     */
    public ResponseAcademicScheduleListItem toResponse(AcademicSchedule entity) {
        return ResponseAcademicScheduleListItem.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 리스트를 응답 DTO 리스트로 변환.
     */
    public List<ResponseAcademicScheduleListItem> toResponseList(List<AcademicSchedule> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}