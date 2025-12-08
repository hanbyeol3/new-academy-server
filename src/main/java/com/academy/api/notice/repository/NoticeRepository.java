package com.academy.api.notice.repository;

import com.academy.api.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 Repository.
 */
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

    /**
     * 카테고리별 공지사항 조회.
     */
    @Query("SELECT n FROM Notice n WHERE n.category.id = :categoryId ORDER BY n.createdAt DESC")
    List<Notice> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 중요 공지사항 조회.
     */
    @Query("SELECT n FROM Notice n WHERE n.isImportant = true AND n.isPublished = true ORDER BY n.createdAt DESC")
    List<Notice> findImportantNotices();

    /**
     * 공개된 공지사항 조회.
     */
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true ORDER BY n.isImportant DESC, n.createdAt DESC")
    List<Notice> findPublishedNotices();

    /**
     * 노출 가능한 공지사항 조회 (현재 시점 기준).
     */
    @Query("""
        SELECT n FROM Notice n 
        WHERE n.isPublished = true 
        AND (
            n.exposureType = 'ALWAYS' 
            OR (
                n.exposureType = 'PERIOD' 
                AND (:now BETWEEN n.exposureStartAt AND n.exposureEndAt)
            )
        )
        ORDER BY n.isImportant DESC, n.createdAt DESC
        """)
    List<Notice> findExposableNotices(@Param("now") LocalDateTime now);

    /**
     * 조회수 증가.
     */
    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1, n.updatedBy = :updatedBy, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int incrementViewCount(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    /**
     * 중요 공지 설정/해제.
     */
    @Modifying
    @Query("UPDATE Notice n SET n.isImportant = :isImportant, n.updatedBy = :updatedBy, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int updateImportantStatus(@Param("id") Long id, @Param("isImportant") Boolean isImportant, @Param("updatedBy") Long updatedBy);

    /**
     * 공개/비공개 상태 변경.
     */
    @Modifying
    @Query("UPDATE Notice n SET n.isPublished = :isPublished, n.updatedBy = :updatedBy, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int updatePublishedStatus(@Param("id") Long id, @Param("isPublished") Boolean isPublished, @Param("updatedBy") Long updatedBy);
    
    /**
     * 특정 카테고리를 사용하는 공지사항 개수 조회.
     */
    @Query("SELECT COUNT(n) FROM Notice n WHERE n.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 키워드 검색 (제목, 내용에서 LIKE 검색) - Native Query 사용.
     * 
     * @deprecated QueryDSL 기반 searchNoticesForAdmin에 통합됨.
     *             searchType 파라미터를 통한 동적 검색 조건 지원을 위해
     *             NoticeRepositoryImpl.keywordCondition() 메서드 사용 권장.
     */
    @Deprecated
    @Query(value = """
        SELECT n.* FROM notices n 
        WHERE (n.title LIKE CONCAT('%', :keyword, '%') OR n.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY n.created_at DESC
        """, nativeQuery = true)
    List<Notice> findByKeyword(@Param("keyword") String keyword);
}