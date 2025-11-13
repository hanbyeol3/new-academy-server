package com.academy.api.category.mapper;

import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.dto.RequestCategoryGroupCreate;
import com.academy.api.category.dto.RequestCategoryGroupUpdate;
import com.academy.api.category.dto.ResponseCategoryGroup;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 카테고리 그룹 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class CategoryGroupMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     * 
     * @param request 생성 요청 DTO
     * @return CategoryGroup 엔티티
     */
    public CategoryGroup toEntity(RequestCategoryGroupCreate request) {
        return CategoryGroup.builder()
                .slug(request.getSlug())
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환.
     * 
     * @param categoryGroup CategoryGroup 엔티티
     * @return ResponseCategoryGroup DTO
     */
    public ResponseCategoryGroup toResponse(CategoryGroup categoryGroup) {
        return ResponseCategoryGroup.from(categoryGroup);
    }

    /**
     * 엔티티 리스트를 응답 DTO 리스트로 변환.
     * 
     * @param categoryGroups CategoryGroup 엔티티 리스트
     * @return ResponseCategoryGroup DTO 리스트
     */
    public List<ResponseCategoryGroup> toResponseList(List<CategoryGroup> categoryGroups) {
        return ResponseCategoryGroup.fromList(categoryGroups);
    }

    /**
     * 엔티티 페이지를 응답 리스트로 변환.
     * 
     * @param page CategoryGroup 엔티티 페이지
     * @return ResponseList<ResponseCategoryGroup>
     */
    public ResponseList<ResponseCategoryGroup> toResponseList(Page<CategoryGroup> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     * 
     * @param categoryGroup 수정할 CategoryGroup 엔티티
     * @param request 수정 요청 DTO
     */
    public void updateEntity(CategoryGroup categoryGroup, RequestCategoryGroupUpdate request) {
        categoryGroup.update(
                request.getName() != null ? request.getName() : categoryGroup.getName(),
                request.getDescription() != null ? request.getDescription() : categoryGroup.getDescription(),
                request.getUpdatedBy()
        );
    }
}