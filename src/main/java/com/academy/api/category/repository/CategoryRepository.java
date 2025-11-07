package com.academy.api.category.repository;

import com.academy.api.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리 그룹 ID로 카테고리 목록 조회 (정렬 순서, 생성일시 순).
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @return Category 리스트
     */
    @Query("SELECT c FROM Category c WHERE c.categoryGroup.id = :categoryGroupId ORDER BY c.sortOrder ASC, c.createdAt ASC")
    List<Category> findByCategoryGroupIdOrderBySortOrder(@Param("categoryGroupId") Long categoryGroupId);

    /**
     * 슬러그로 카테고리 조회.
     * 
     * @param slug 슬러그
     * @return Category Optional
     */
    Optional<Category> findBySlug(String slug);

    /**
     * 카테고리 그룹 ID와 슬러그로 카테고리 조회.
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @param slug 슬러그
     * @return Category Optional
     */
    @Query("SELECT c FROM Category c WHERE c.categoryGroup.id = :categoryGroupId AND c.slug = :slug")
    Optional<Category> findByCategoryGroupIdAndSlug(@Param("categoryGroupId") Long categoryGroupId, @Param("slug") String slug);

    /**
     * 카테고리 그룹 내에서 슬러그 중복 검사.
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @param slug 슬러그
     * @return 존재 여부
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.categoryGroup.id = :categoryGroupId AND c.slug = :slug")
    boolean existsByCategoryGroupIdAndSlug(@Param("categoryGroupId") Long categoryGroupId, @Param("slug") String slug);

    /**
     * 특정 ID를 제외하고 카테고리 그룹 내에서 슬러그 중복 검사 (수정 시 사용).
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @param slug 슬러그
     * @param id 제외할 카테고리 ID
     * @return 존재 여부
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.categoryGroup.id = :categoryGroupId AND c.slug = :slug AND c.id != :id")
    boolean existsByCategoryGroupIdAndSlugAndIdNot(@Param("categoryGroupId") Long categoryGroupId, @Param("slug") String slug, @Param("id") Long id);

    /**
     * 카테고리 그룹 ID로 카테고리 개수 조회.
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @return 카테고리 개수
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.categoryGroup.id = :categoryGroupId")
    long countByCategoryGroupId(@Param("categoryGroupId") Long categoryGroupId);

    /**
     * 모든 카테고리 조회 (카테고리 그룹별, 정렬 순서별).
     * 
     * @return Category 리스트
     */
    @Query("SELECT c FROM Category c JOIN FETCH c.categoryGroup cg ORDER BY cg.name ASC, c.sortOrder ASC, c.createdAt ASC")
    List<Category> findAllWithCategoryGroupOrderBySortOrder();

    /**
     * 카테고리명으로 검색 (부분 일치).
     * 
     * @param keyword 검색 키워드
     * @return Category 리스트
     */
    @Query("SELECT c FROM Category c JOIN FETCH c.categoryGroup WHERE c.name LIKE %:keyword% ORDER BY c.categoryGroup.name ASC, c.sortOrder ASC")
    List<Category> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 특정 카테고리 그룹 내에서 최대 정렬 순서 조회.
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @return 최대 정렬 순서 (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.categoryGroup.id = :categoryGroupId")
    Integer findMaxSortOrderByCategoryGroupId(@Param("categoryGroupId") Long categoryGroupId);
}