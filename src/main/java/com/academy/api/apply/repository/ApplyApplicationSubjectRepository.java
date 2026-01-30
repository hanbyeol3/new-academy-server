package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplyApplicationSubject;
import com.academy.api.apply.domain.ApplyApplicationSubjectId;
import com.academy.api.apply.domain.SubjectCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}