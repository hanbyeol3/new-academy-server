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
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    int incrementViewCount(@Param("id") Long id);

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
}