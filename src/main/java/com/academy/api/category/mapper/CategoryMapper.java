package com.academy.api.category.mapper;

import com.academy.api.category.domain.Category;
import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.dto.RequestCategoryCreate;
import com.academy.api.category.dto.RequestCategoryUpdate;
import com.academy.api.category.dto.ResponseCategory;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 카테고리 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class CategoryMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param categoryGroup 연결할 카테고리 그룹
     * @return Category 엔티티
     */
    public Category toEntity(RequestCategoryCreate request, CategoryGroup categoryGroup) {
        return Category.builder()
                .categoryGroup(categoryGroup)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .createdBy(request.getCreatedBy())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환.
     * 
     * @param category Category 엔티티
     * @return ResponseCategory DTO
     */
    public ResponseCategory toResponse(Category category) {
        return ResponseCategory.from(category);
    }

    /**
     * 엔티티 리스트를 응답 DTO 리스트로 변환.
     * 
     * @param categories Category 엔티티 리스트
     * @return ResponseCategory DTO 리스트
     */
    public List<ResponseCategory> toResponseList(List<Category> categories) {
        return ResponseCategory.fromList(categories);
    }

    /**
     * 엔티티 페이지를 응답 리스트로 변환.
     * 
     * @param page Category 엔티티 페이지
     * @return ResponseList<ResponseCategory>
     */
    public ResponseList<ResponseCategory> toResponseList(Page<Category> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     * 
     * @param category 수정할 Category 엔티티
     * @param request 수정 요청 DTO
     * @param newCategoryGroup 변경할 카테고리 그룹 (nullable)
     */
    public void updateEntity(Category category, RequestCategoryUpdate request, CategoryGroup newCategoryGroup) {
        // 카테고리 그룹 변경이 요청된 경우
        if (request.getCategoryGroupId() != null && newCategoryGroup != null) {
            category.updateCategoryGroup(newCategoryGroup, request.getUpdatedBy());
        }

        // 기본 정보 업데이트
        category.update(
                request.getName() != null ? request.getName() : category.getName(),
                request.getSlug() != null ? request.getSlug() : category.getSlug(),
                request.getDescription() != null ? request.getDescription() : category.getDescription(),
                request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder(),
                request.getUpdatedBy()
        );
    }
}