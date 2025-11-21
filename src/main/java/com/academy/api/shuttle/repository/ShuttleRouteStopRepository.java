package com.academy.api.shuttle.repository;

import com.academy.api.shuttle.domain.ShuttleRouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 셔틀 노선 정류장 Repository.
 */
@Repository
public interface ShuttleRouteStopRepository extends JpaRepository<ShuttleRouteStop, Long> {

    /**
     * 특정 노선의 모든 정류장 조회 (순서대로).
     */
    @Query("SELECT s FROM ShuttleRouteStop s WHERE s.route.routeId = :routeId ORDER BY s.sortOrder ASC")
    List<ShuttleRouteStop> findByRouteIdOrderBySortOrder(@Param("routeId") Long routeId);

    /**
     * 특정 노선의 정류장 개수 조회.
     */
    @Query("SELECT COUNT(s) FROM ShuttleRouteStop s WHERE s.route.routeId = :routeId")
    long countByRouteId(@Param("routeId") Long routeId);

    /**
     * 특정 노선의 모든 정류장 삭제 (풀 교체용).
     */
    @Modifying
    @Query("DELETE FROM ShuttleRouteStop s WHERE s.route.routeId = :routeId")
    void deleteByRouteId(@Param("routeId") Long routeId);

    /**
     * 특정 노선에서 정렬 순서가 겹치는지 확인.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ShuttleRouteStop s WHERE s.route.routeId = :routeId AND s.sortOrder = :sortOrder")
    boolean existsByRouteIdAndSortOrder(@Param("routeId") Long routeId, @Param("sortOrder") Integer sortOrder);

    /**
     * 특정 노선에서 최대 정렬 순서 조회.
     */
    @Query("SELECT COALESCE(MAX(s.sortOrder), 0) FROM ShuttleRouteStop s WHERE s.route.routeId = :routeId")
    Integer findMaxSortOrderByRouteId(@Param("routeId") Long routeId);
}