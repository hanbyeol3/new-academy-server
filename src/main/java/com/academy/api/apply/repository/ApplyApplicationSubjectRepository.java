package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplicationDivision;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplyApplicationSubject;
import com.academy.api.apply.domain.ApplyApplicationSubjectId;
import com.academy.api.apply.domain.SubjectCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 원서접수 과목 Repository.
 */
@Repository
public interface ApplyApplicationSubjectRepository extends JpaRepository<ApplyApplicationSubject, ApplyApplicationSubjectId> {

    /**
     * 원서접수 ID로 과목 목록 조회.
     */
    List<ApplyApplicationSubject> findByApplyId(Long applyId);

    /**
     * 원서접수 ID의 과목들 삭제.
     */
    @Modifying
    @Query("DELETE FROM ApplyApplicationSubject s WHERE s.applyId = :applyId")
    void deleteByApplyId(@Param("applyId") Long applyId);

    /**
     * 원서접수 ID와 과목 코드로 조회.
     */
    ApplyApplicationSubject findByApplyIdAndSubjectCode(Long applyId, SubjectCode subjectCode);

    /**
     * 원서접수 ID의 과목 개수 조회.
     */
    Long countByApplyId(Long applyId);

    /**
     * 과목별 원서접수 개수 조회.
     */
    Long countBySubjectCode(SubjectCode subjectCode);

    /**
     * 기간(접수일)·구분별 과목 신청 건수 집계 (접수취소 제외).
     *
     * @param startDate 접수일 시작
     * @param endDate 접수일 종료
     * @param division 구분 필터 (null이면 전체)
     * @param excludeStatus 집계 제외 상태 (접수취소)
     * @return [subjectCode, count] 배열 목록 (건수 내림차순)
     */
    @Query("SELECT s.subjectCode, COUNT(s) FROM ApplyApplicationSubject s " +
           "JOIN s.applyApplication a " +
           "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
           "AND a.status <> :excludeStatus " +
           "AND (:division IS NULL OR a.division = :division) " +
           "GROUP BY s.subjectCode " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> findSubjectStatistics(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("division") ApplicationDivision division,
                                         @Param("excludeStatus") ApplicationStatus excludeStatus);
}