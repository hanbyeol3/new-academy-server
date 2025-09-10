package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.GalleryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 갤러리 항목 Repository.
 */
public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long>, GalleryItemRepositoryCustom {

    /**
     * ID로 갤러리 항목 조회 (존재하지 않으면 예외 발생).
     */
    @Query("SELECT g FROM GalleryItem g WHERE g.id = :id")
    Optional<GalleryItem> findByIdWithDetails(@Param("id") Long id);

    /**
     * 제목으로 갤러리 존재 여부 확인.
     */
    boolean existsByTitle(String title);
}