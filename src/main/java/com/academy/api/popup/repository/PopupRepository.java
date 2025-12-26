package com.academy.api.popup.repository;

import com.academy.api.popup.domain.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팝업 Repository.
 */
@Repository
public interface PopupRepository extends JpaRepository<Popup, Long>, PopupRepositoryCustom {

    /**
     * 공개 상태별 팝업 조회.
     */
    Page<Popup> findByIsPublishedOrderBySortOrderAscCreatedAtDesc(Boolean isPublished, Pageable pageable);

    /**
     * 노출중인 팝업 조회 (기본 쿼리).
     * 
     * @param now 현재 시각
     * @param pageable 페이징 정보
     * @return 노출중인 팝업 목록
     */
    @Query("SELECT p FROM Popup p WHERE p.isPublished = true " +
           "AND (p.exposureType = 'ALWAYS' OR (p.exposureStartAt <= :now AND p.exposureEndAt > :now)) " +
           "ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Popup> findActivePopups(@Param("now") LocalDateTime now);

    /**
     * 팝업 타입별 조회.
     */
    Page<Popup> findByTypeOrderByCreatedAtDesc(Popup.PopupType type, Pageable pageable);

    /**
     * 정렬순서별 팝업 조회.
     */
    List<Popup> findAllByOrderBySortOrderAscCreatedAtDesc();
}