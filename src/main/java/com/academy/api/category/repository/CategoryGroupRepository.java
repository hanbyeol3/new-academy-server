package com.academy.api.category.repository;

import com.academy.api.category.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 그룹 Repository.
 */
public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {

    /**
     * 그룹명으로 카테고리 그룹 조회.
     * 
     * @param name 그룹명
     * @return CategoryGroup Optional
     */
    Optional<CategoryGroup> findByName(String name);

    /**
     * 그룹명으로 존재 여부 확인.
     * 
     * @param name 그룹명
     * @return 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 특정 ID를 제외하고 그룹명으로 존재 여부 확인 (수정 시 사용).
     * 
     * @param name 그룹명
     * @param id 제외할 ID
     * @return 존재 여부
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 생성일시 내림차순으로 모든 카테고리 그룹 조회.
     * 
     * @return CategoryGroup 리스트
     */
    @Query("SELECT cg FROM CategoryGroup cg ORDER BY cg.createdAt DESC")
    List<CategoryGroup> findAllOrderByCreatedAtDesc();

    /**
     * 그룹명으로 검색 (부분 일치).
     * 
     * @param keyword 검색 키워드
     * @return CategoryGroup 리스트
     */
    @Query("SELECT cg FROM CategoryGroup cg WHERE cg.name LIKE %:keyword% ORDER BY cg.createdAt DESC")
    List<CategoryGroup> findByNameContaining(@Param("keyword") String keyword);
}