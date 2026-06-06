package com.academy.api.improvement.repository;

import com.academy.api.improvement.domain.ImprovementCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 성적 향상 사례 Repository.
 * 
 * 성적 향상 사례의 데이터 접근을 담당합니다.
 * 소프트 삭제를 고려하여 deletedAt이 null인 데이터만 조회합니다.
 */
@Repository
public interface ImprovementCaseRepository extends JpaRepository<ImprovementCase, Long>, ImprovementCaseRepositoryCustom {
    
    /**
     * ID로 삭제되지 않은 사례 조회.
     * 
     * @param id 사례 ID
     * @return 사례 엔티티
     */
    @Query("SELECT ic FROM ImprovementCase ic WHERE ic.id = :id AND ic.deletedAt IS NULL")
    Optional<ImprovementCase> findByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * ID로 삭제되지 않고 공개된 사례 조회.
     * 
     * @param id 사례 ID
     * @return 사례 엔티티
     */
    @Query("SELECT ic FROM ImprovementCase ic WHERE ic.id = :id AND ic.deletedAt IS NULL AND ic.isPublished = true")
    Optional<ImprovementCase> findByIdAndPublishedAndNotDeleted(@Param("id") Long id);
    
    /**
     * 조회수 증가.
     * 
     * @param id 사례 ID
     */
    @Modifying
    @Query("UPDATE ImprovementCase ic SET ic.viewCount = ic.viewCount + 1 WHERE ic.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 이전 사례 조회 (관리자용 - 모든 사례).
     * 
     * @param currentId 현재 사례 ID
     * @param pageable 페이징 정보 (limit 1)
     * @return 이전 사례 목록
     */
    @Query("""
        SELECT ic FROM ImprovementCase ic 
        WHERE ic.deletedAt IS NULL 
        AND (
            (ic.createdAt > (SELECT ic2.createdAt FROM ImprovementCase ic2 WHERE ic2.id = :currentId))
            OR (ic.createdAt = (SELECT ic3.createdAt FROM ImprovementCase ic3 WHERE ic3.id = :currentId) AND ic.id > :currentId)
        )
        ORDER BY ic.createdAt ASC, ic.id ASC
        """)
    List<ImprovementCase> findPreviousCase(@Param("currentId") Long currentId, Pageable pageable);
    
    /**
     * 다음 사례 조회 (관리자용 - 모든 사례).
     * 
     * @param currentId 현재 사례 ID
     * @param pageable 페이징 정보 (limit 1)
     * @return 다음 사례 목록
     */
    @Query("""
        SELECT ic FROM ImprovementCase ic 
        WHERE ic.deletedAt IS NULL 
        AND (
            (ic.createdAt < (SELECT ic2.createdAt FROM ImprovementCase ic2 WHERE ic2.id = :currentId))
            OR (ic.createdAt = (SELECT ic3.createdAt FROM ImprovementCase ic3 WHERE ic3.id = :currentId) AND ic.id < :currentId)
        )
        ORDER BY ic.createdAt DESC, ic.id DESC
        """)
    List<ImprovementCase> findNextCase(@Param("currentId") Long currentId, Pageable pageable);
    
    /**
     * 이전 공개 사례 조회 (공개용).
     * 
     * @param currentId 현재 사례 ID
     * @param pageable 페이징 정보 (limit 1)
     * @return 이전 사례 목록
     */
    @Query("""
        SELECT ic FROM ImprovementCase ic 
        WHERE ic.deletedAt IS NULL AND ic.isPublished = true
        AND (
            (ic.createdAt > (SELECT ic2.createdAt FROM ImprovementCase ic2 WHERE ic2.id = :currentId))
            OR (ic.createdAt = (SELECT ic3.createdAt FROM ImprovementCase ic3 WHERE ic3.id = :currentId) AND ic.id > :currentId)
        )
        ORDER BY ic.createdAt ASC, ic.id ASC
        """)
    List<ImprovementCase> findPreviousPublicCase(@Param("currentId") Long currentId, Pageable pageable);
    
    /**
     * 다음 공개 사례 조회 (공개용).
     * 
     * @param currentId 현재 사례 ID
     * @param pageable 페이징 정보 (limit 1)
     * @return 다음 사례 목록
     */
    @Query("""
        SELECT ic FROM ImprovementCase ic 
        WHERE ic.deletedAt IS NULL AND ic.isPublished = true
        AND (
            (ic.createdAt < (SELECT ic2.createdAt FROM ImprovementCase ic2 WHERE ic2.id = :currentId))
            OR (ic.createdAt = (SELECT ic3.createdAt FROM ImprovementCase ic3 WHERE ic3.id = :currentId) AND ic.id < :currentId)
        )
        ORDER BY ic.createdAt DESC, ic.id DESC
        """)
    List<ImprovementCase> findNextPublicCase(@Param("currentId") Long currentId, Pageable pageable);
    
    /**
     * 작성자 이름과 비밀번호로 사례 조회 (외부 작성자 수정/삭제용).
     * 
     * @param id 사례 ID
     * @param authorName 작성자 이름
     * @return 사례 엔티티
     */
    @Query("SELECT ic FROM ImprovementCase ic WHERE ic.id = :id AND ic.authorName = :authorName AND ic.deletedAt IS NULL")
    Optional<ImprovementCase> findByIdAndAuthorNameAndNotDeleted(@Param("id") Long id, @Param("authorName") String authorName);
}