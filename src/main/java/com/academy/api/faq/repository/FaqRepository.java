package com.academy.api.faq.repository;

import com.academy.api.faq.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * FAQ Repository.
 */
public interface FaqRepository extends JpaRepository<Faq, Long>, FaqRepositoryCustom {

    /**
     * 카테고리별 FAQ 조회.
     */
    @Query("SELECT f FROM Faq f WHERE f.category.id = :categoryId ORDER BY f.createdAt DESC")
    List<Faq> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 공개된 FAQ 조회.
     */
    @Query("SELECT f FROM Faq f WHERE f.isPublished = true ORDER BY f.createdAt DESC")
    List<Faq> findPublishedFaqs();

    /**
     * 공개/비공개 상태 변경.
     */
    @Modifying
    @Query("UPDATE Faq f SET f.isPublished = :isPublished, f.updatedBy = :updatedBy, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :id")
    int updatePublishedStatus(@Param("id") Long id, @Param("isPublished") Boolean isPublished, @Param("updatedBy") Long updatedBy);
    
    /**
     * 특정 카테고리를 사용하는 FAQ 개수 조회.
     */
    @Query("SELECT COUNT(f) FROM Faq f WHERE f.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}