package com.academy.api.explanation.mapper;

import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.model.ResponseExplanationEventDetail;
import com.academy.api.explanation.model.ResponseExplanationEventListItem;
import com.academy.api.explanation.model.ResponseReservation;
import com.academy.api.explanation.model.ResponseReservationAdminItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 설명회 도메인 Mapper.
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
public class ExplanationMapper {

    /**
     * ExplanationEvent 엔티티를 목록용 응답 DTO로 변환.
     */
    public ResponseExplanationEventListItem toListItem(ExplanationEvent event) {
        return ResponseExplanationEventListItem.from(event);
    }

    /**
     * ExplanationEvent 엔티티 목록을 목록용 응답 DTO 목록으로 변환.
     */
    public List<ResponseExplanationEventListItem> toListItems(List<ExplanationEvent> events) {
        return events.stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * ExplanationEvent 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponseExplanationEventDetail toDetail(ExplanationEvent event) {
        return ResponseExplanationEventDetail.from(event);
    }
}