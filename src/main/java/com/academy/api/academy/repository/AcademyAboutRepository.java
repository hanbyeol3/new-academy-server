package com.academy.api.academy.repository;

import com.academy.api.academy.domain.AcademyAbout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 학원 소개 정보 Repository.
 * 
 * 단일 설정 테이블 특성:
 * - 항상 1개 row만 존재
 * - CREATE 불필요 (첫 조회시 기본값 생성)
 * - UPDATE만 수행
 */
@Repository
public interface AcademyAboutRepository extends JpaRepository<AcademyAbout, Long> {

    /**
     * 학원 소개 정보 조회 (첫 번째 row).
     * 
     * 단일 설정 테이블이므로 첫 번째 row를 반환합니다.
     * 
     * @return 학원 소개 정보 (없으면 Optional.empty())
     */
    @Query("SELECT a FROM AcademyAbout a ORDER BY a.id ASC")
    Optional<AcademyAbout> findFirstRow();

    /**
     * 학원 소개 정보 존재 여부 확인.
     * 
     * @return 존재하면 true
     */
    @Query("SELECT COUNT(a) > 0 FROM AcademyAbout a")
    boolean exists();

    /**
     * 전체 학원 소개 정보 개수 조회.
     * 
     * @return 레코드 개수
     */
    @Query("SELECT COUNT(a) FROM AcademyAbout a")
    long countAll();
}