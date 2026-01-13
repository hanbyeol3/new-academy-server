package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.Explanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 설명회 리포지토리.
 */
public interface ExplanationRepository extends JpaRepository<Explanation, Long>, ExplanationRepositoryCustom {

    /**
     * 게시된 설명회 조회.
     * 
     * @param id 설명회 ID
     * @return 설명회
     */
    @Query("SELECT e FROM Explanation e WHERE e.id = :id AND e.isPublished = true")
    Optional<Explanation> findByIdAndPublished(@Param("id") Long id);

    /**
     * 조회수 증가.
     * 
     * @param id 설명회 ID
     */
    @Modifying
    @Query("UPDATE Explanation e SET e.viewCount = e.viewCount + 1 WHERE e.id = :id")
    void incrementViewCount(@Param("id") Long id);
}