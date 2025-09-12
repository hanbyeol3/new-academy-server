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
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .title(request.getTitle())
                .published(request.getPublished())
                .color(request.getColor())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환.
     */
    public ResponseAcademicScheduleListItem toResponse(AcademicSchedule entity) {
        return ResponseAcademicScheduleListItem.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .title(entity.getTitle())
                .published(entity.getPublished())
                .color(entity.getColor())
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