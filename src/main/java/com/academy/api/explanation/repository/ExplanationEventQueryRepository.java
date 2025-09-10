package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.model.ExplanationEventSearchCriteria;
import com.academy.api.explanation.model.ResponseExplanationEventListItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 설명회 이벤트 검색 Repository.
 */
@Repository
@RequiredArgsConstructor
public class ExplanationEventQueryRepository {

    private final EntityManager entityManager;

    /**
     * 설명회 이벤트 목록 검색 (페이지네이션).
     */
    public Page<ResponseExplanationEventListItem> search(ExplanationEventSearchCriteria criteria, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM ExplanationEvent e WHERE 1=1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(e) FROM ExplanationEvent e WHERE 1=1");
        
        // 검색 조건 추가
        if (criteria.getPublishedOnly()) {
            jpql.append(" AND e.published = true");
            countJpql.append(" AND e.published = true");
        }
        
        if (criteria.getDivision() != null) {
            jpql.append(" AND e.division = :division");
            countJpql.append(" AND e.division = :division");
        }
        
        if (criteria.getStatus() != null) {
            jpql.append(" AND e.status = :status");
            countJpql.append(" AND e.status = :status");
        }
        
        if (criteria.getTitleLike() != null && !criteria.getTitleLike().trim().isEmpty()) {
            jpql.append(" AND LOWER(e.title) LIKE LOWER(:titleLike)");
            countJpql.append(" AND LOWER(e.title) LIKE LOWER(:titleLike)");
        }
        
        if (criteria.getStartFrom() != null) {
            jpql.append(" AND e.startAt >= :startFrom");
            countJpql.append(" AND e.startAt >= :startFrom");
        }
        
        if (criteria.getStartTo() != null) {
            jpql.append(" AND e.startAt <= :startTo");
            countJpql.append(" AND e.startAt <= :startTo");
        }
        
        // 정렬 추가
        jpql.append(" ORDER BY e.pinned DESC, e.startAt DESC");
        
        // 데이터 조회
        TypedQuery<ExplanationEvent> query = entityManager.createQuery(jpql.toString(), ExplanationEvent.class);
        setParameters(query, criteria);
        
        List<ExplanationEvent> events = query
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        
        // 전체 개수 조회
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        setParameters(countQuery, criteria);
        Long total = countQuery.getSingleResult();
        
        // DTO 변환
        List<ResponseExplanationEventListItem> content = events.stream()
                .map(ResponseExplanationEventListItem::from)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * 쿼리 파라미터 설정.
     */
    private void setParameters(TypedQuery<?> query, ExplanationEventSearchCriteria criteria) {
        if (criteria.getDivision() != null) {
            query.setParameter("division", criteria.getDivision());
        }
        
        if (criteria.getStatus() != null) {
            query.setParameter("status", criteria.getStatus());
        }
        
        if (criteria.getTitleLike() != null && !criteria.getTitleLike().trim().isEmpty()) {
            query.setParameter("titleLike", "%" + criteria.getTitleLike().trim() + "%");
        }
        
        if (criteria.getStartFrom() != null) {
            query.setParameter("startFrom", criteria.getStartFrom());
        }
        
        if (criteria.getStartTo() != null) {
            query.setParameter("startTo", criteria.getStartTo());
        }
    }
}