package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * 갤러리 Repository.
 */
public interface GalleryRepository extends JpaRepository<Gallery, Long>, GalleryRepositoryCustom {

    /**
     * 조회수 증가.
     */
    @Modifying
    @Query("UPDATE Gallery n SET n.viewCount = n.viewCount + 1, n.updatedBy = :updatedBy, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int incrementViewCount(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    /**
     * 공개/비공개 상태 변경.
     */
    @Modifying
    @Query("UPDATE Gallery n SET n.isPublished = :isPublished, n.updatedBy = :updatedBy, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    int updatePublishedStatus(@Param("id") Long id, @Param("isPublished") Boolean isPublished, @Param("updatedBy") Long updatedBy);
    
    /**
     * 특정 카테고리를 사용하는 갤러리 개수 조회.
     */
    @Query("SELECT COUNT(n) FROM Gallery n WHERE n.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
}