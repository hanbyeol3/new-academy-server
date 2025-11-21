package com.academy.api.facility.repository;

import com.academy.api.facility.domain.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 시설 Repository.
 * 
 * 시설 안내 정보의 데이터 접근을 담당합니다.
 */
@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    /**
     * 공개된 시설 목록 조회 (공개 API용).
     * 
     * @param pageable 페이징 정보
     * @return 공개된 시설 목록
     */
    @Query("SELECT f FROM Facility f WHERE f.isPublished = true ORDER BY f.createdAt DESC")
    Page<Facility> findPublishedFacilities(Pageable pageable);

    /**
     * 모든 시설 목록 조회 (관리자용).
     * 
     * @param pageable 페이징 정보
     * @return 모든 시설 목록
     */
    @Query("SELECT f FROM Facility f ORDER BY f.createdAt DESC")
    Page<Facility> findAllFacilities(Pageable pageable);

    /**
     * 제목으로 시설 검색 (관리자용).
     * 
     * @param title 검색할 제목 (부분 검색)
     * @param pageable 페이징 정보
     * @return 검색된 시설 목록
     */
    @Query("SELECT f FROM Facility f WHERE f.title LIKE %:title% ORDER BY f.createdAt DESC")
    Page<Facility> findByTitleContaining(@Param("title") String title, Pageable pageable);

    /**
     * 공개 상태로 시설 검색 (관리자용).
     * 
     * @param isPublished 공개 여부
     * @param pageable 페이징 정보
     * @return 해당 공개 상태의 시설 목록
     */
    Page<Facility> findByIsPublished(Boolean isPublished, Pageable pageable);

    /**
     * 제목과 공개 상태로 시설 검색 (관리자용).
     * 
     * @param title 검색할 제목
     * @param isPublished 공개 여부
     * @param pageable 페이징 정보
     * @return 검색된 시설 목록
     */
    @Query("SELECT f FROM Facility f WHERE f.title LIKE %:title% AND f.isPublished = :isPublished ORDER BY f.createdAt DESC")
    Page<Facility> findByTitleContainingAndIsPublished(
            @Param("title") String title, 
            @Param("isPublished") Boolean isPublished, 
            Pageable pageable);

    /**
     * 공개된 시설 중 ID로 조회.
     * 
     * @param id 시설 ID
     * @return 공개된 시설 (없으면 Optional.empty())
     */
    @Query("SELECT f FROM Facility f WHERE f.id = :id AND f.isPublished = true")
    java.util.Optional<Facility> findByIdAndPublished(@Param("id") Long id);

    /**
     * 공개된 시설 개수 조회.
     * 
     * @return 공개된 시설 개수
     */
    @Query("SELECT COUNT(f) FROM Facility f WHERE f.isPublished = true")
    long countPublishedFacilities();
}