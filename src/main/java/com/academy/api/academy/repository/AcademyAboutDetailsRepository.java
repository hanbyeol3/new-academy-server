package com.academy.api.academy.repository;

import com.academy.api.academy.domain.AcademyAbout;
import com.academy.api.academy.domain.AcademyAboutDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학원 소개 상세 정보 Repository.
 * 
 * 다중 Row CRUD 테이블 특성:
 * - 정렬 순서 관리
 * - 연관관계 기반 조회
 * - 일괄 순서 업데이트 지원
 */
@Repository
public interface AcademyAboutDetailsRepository extends JpaRepository<AcademyAboutDetails, Long> {

    /**
     * 특정 학원 소개에 속한 상세 정보 목록 조회 (정렬 순서대로).
     * 
     * @param about 학원 소개 정보
     * @return 상세 정보 목록 (정렬 순서대로)
     */
    @Query("SELECT d FROM AcademyAboutDetails d WHERE d.about = :about ORDER BY d.sortOrder ASC, d.id ASC")
    List<AcademyAboutDetails> findByAboutOrderBySortOrderAscIdAsc(@Param("about") AcademyAbout about);

    /**
     * 모든 상세 정보 목록 조회 (정렬 순서대로).
     * 
     * @return 모든 상세 정보 목록 (정렬 순서대로)
     */
    @Query("SELECT d FROM AcademyAboutDetails d ORDER BY d.sortOrder ASC, d.id ASC")
    List<AcademyAboutDetails> findAllOrderBySortOrderAscIdAsc();

    /**
     * 특정 학원 소개에 속한 상세 정보 개수 조회.
     * 
     * @param about 학원 소개 정보
     * @return 상세 정보 개수
     */
    @Query("SELECT COUNT(d) FROM AcademyAboutDetails d WHERE d.about = :about")
    long countByAbout(@Param("about") AcademyAbout about);

    /**
     * 특정 학원 소개에 속한 상세 정보의 최대 정렬 순서 조회.
     * 
     * @param about 학원 소개 정보
     * @return 최대 정렬 순서 (데이터가 없으면 0)
     */
    @Query("SELECT COALESCE(MAX(d.sortOrder), 0) FROM AcademyAboutDetails d WHERE d.about = :about")
    Integer findMaxSortOrderByAbout(@Param("about") AcademyAbout about);

    /**
     * ID로 상세 정보 조회 (연관관계 포함).
     * 
     * @param id 상세 정보 ID
     * @return 상세 정보 (연관관계 포함)
     */
    @Query("SELECT d FROM AcademyAboutDetails d LEFT JOIN FETCH d.about WHERE d.id = :id")
    Optional<AcademyAboutDetails> findByIdWithAbout(@Param("id") Long id);

    /**
     * 특정 상세 정보의 정렬 순서 업데이트.
     * 
     * @param id 상세 정보 ID
     * @param sortOrder 새로운 정렬 순서
     */
    @Modifying
    @Query("UPDATE AcademyAboutDetails d SET d.sortOrder = :sortOrder WHERE d.id = :id")
    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
}