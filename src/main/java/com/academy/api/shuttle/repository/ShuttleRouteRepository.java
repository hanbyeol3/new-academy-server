package com.academy.api.shuttle.repository;

import com.academy.api.shuttle.domain.ShuttleRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 셔틀 노선 Repository.
 */
@Repository
public interface ShuttleRouteRepository extends JpaRepository<ShuttleRoute, Long> {

    /**
     * 모든 노선 조회 (정렬 순서 기준).
     */
    @Query("SELECT r FROM ShuttleRoute r ORDER BY r.sortOrder ASC, r.routeId ASC")
    Page<ShuttleRoute> findAllRoutes(Pageable pageable);

    /**
     * 공개된 노선만 조회.
     */
    @Query("SELECT r FROM ShuttleRoute r WHERE r.isPublished = true ORDER BY r.sortOrder ASC, r.routeId ASC")
    Page<ShuttleRoute> findPublishedRoutes(Pageable pageable);

    /**
     * 노선 ID로 정류장 포함 조회.
     */
    @Query("SELECT r FROM ShuttleRoute r LEFT JOIN FETCH r.stops s WHERE r.routeId = :routeId ORDER BY s.sortOrder ASC")
    Optional<ShuttleRoute> findByIdWithStops(@Param("routeId") Long routeId);

    /**
     * 공개된 노선 ID로 정류장 포함 조회.
     */
    @Query("SELECT r FROM ShuttleRoute r LEFT JOIN FETCH r.stops s WHERE r.routeId = :routeId AND r.isPublished = true ORDER BY s.sortOrder ASC")
    Optional<ShuttleRoute> findPublishedByIdWithStops(@Param("routeId") Long routeId);

    /**
     * 노선명으로 검색.
     */
    @Query("SELECT r FROM ShuttleRoute r WHERE r.routeName LIKE %:routeName% ORDER BY r.sortOrder ASC, r.routeId ASC")
    Page<ShuttleRoute> findByRouteNameContaining(@Param("routeName") String routeName, Pageable pageable);

    /**
     * 공개 상태로 필터링.
     */
    Page<ShuttleRoute> findByIsPublished(Boolean isPublished, Pageable pageable);

    /**
     * 노선명과 공개 상태로 검색.
     */
    @Query("SELECT r FROM ShuttleRoute r WHERE r.routeName LIKE %:routeName% AND r.isPublished = :isPublished ORDER BY r.sortOrder ASC, r.routeId ASC")
    Page<ShuttleRoute> findByRouteNameContainingAndIsPublished(@Param("routeName") String routeName, @Param("isPublished") Boolean isPublished, Pageable pageable);

    /**
     * 정렬 순서가 겹치는지 확인.
     */
    boolean existsBySortOrder(Integer sortOrder);

    /**
     * 특정 ID를 제외하고 정렬 순서가 겹치는지 확인.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ShuttleRoute r WHERE r.sortOrder = :sortOrder AND r.routeId <> :excludeId")
    boolean existsBySortOrderAndRouteIdNot(@Param("sortOrder") Integer sortOrder, @Param("excludeId") Long excludeId);
}