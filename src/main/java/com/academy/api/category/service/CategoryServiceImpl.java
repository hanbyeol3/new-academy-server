package com.academy.api.category.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.dto.RequestCategoryCreate;
import com.academy.api.category.dto.RequestCategoryUpdate;
import com.academy.api.category.dto.ResponseCategory;
import com.academy.api.category.mapper.CategoryMapper;
import com.academy.api.category.repository.CategoryGroupRepository;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 서비스 구현체.
 * 
 * - 카테고리 CRUD 비즈니스 로직 처리
 * - 카테고리 그룹별 슬러그 중복 검증
 * - 정렬 순서 자동 관리
 * - 슬러그 유효성 검증 (영문/숫자/하이픈)
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public ResponseData<List<ResponseCategory>> getCategoryList() {
        log.info("[CategoryService] 전체 카테고리 목록 조회 시작");
        
        List<Category> categories = categoryRepository.findAllWithCategoryGroupOrderBySortOrder();
        
        log.debug("[CategoryService] 카테고리 조회 완료. 총 {}개", categories.size());
        
        List<ResponseCategory> response = categoryMapper.toResponseList(categories);
        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<List<ResponseCategory>> getCategoriesByGroupId(Long categoryGroupId) {
        log.info("[CategoryService] 카테고리 그룹별 목록 조회 시작. groupId={}", categoryGroupId);
        
        // 카테고리 그룹 존재 여부 확인
        if (!categoryGroupRepository.existsById(categoryGroupId)) {
            throw new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND);
        }
        
        List<Category> categories = categoryRepository.findByCategoryGroupIdOrderBySortOrder(categoryGroupId);
        
        log.debug("[CategoryService] 카테고리 그룹별 조회 완료. groupId={}, 카테고리수={}", categoryGroupId, categories.size());
        
        List<ResponseCategory> response = categoryMapper.toResponseList(categories);
        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<ResponseCategory> getCategory(Long id) {
        log.info("[CategoryService] 카테고리 상세 조회 시작. ID={}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        log.debug("[CategoryService] 카테고리 조회 완료. ID={}, 카테고리명={}, 슬러그={}", 
                id, category.getName(), category.getSlug());
        
        ResponseCategory response = categoryMapper.toResponse(category);
        return ResponseData.ok(response);
    }

    @Override
    @Transactional
    public ResponseData<Long> createCategory(RequestCategoryCreate request) {
        log.info("[CategoryService] 카테고리 생성 시작. 그룹ID={}, 카테고리명={}, 슬러그={}", 
                request.getCategoryGroupId(), request.getName(), request.getSlug());
        
        // 카테고리 그룹 조회
        CategoryGroup categoryGroup = categoryGroupRepository.findById(request.getCategoryGroupId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));
        
        // 슬러그 유효성 검증
        if (!isValidSlug(request.getSlug())) {
            log.warn("[CategoryService] 잘못된 슬러그 형식. 슬러그={}", request.getSlug());
            throw new BusinessException(ErrorCode.INVALID_SLUG_FORMAT);
        }
        
        // 카테고리 그룹 내 슬러그 중복 검증
        if (categoryRepository.existsByCategoryGroupIdAndSlug(request.getCategoryGroupId(), request.getSlug())) {
            log.warn("[CategoryService] 중복된 슬러그. 그룹ID={}, 슬러그={}", request.getCategoryGroupId(), request.getSlug());
            throw new BusinessException(ErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
        }
        
        // 정렬 순서가 지정되지 않은 경우 자동 설정
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null || sortOrder == 0) {
            Integer maxSortOrder = categoryRepository.findMaxSortOrderByCategoryGroupId(request.getCategoryGroupId());
            sortOrder = maxSortOrder + 1;
            log.debug("[CategoryService] 정렬 순서 자동 설정. 그룹ID={}, 정렬순서={}", request.getCategoryGroupId(), sortOrder);
        }
        
        Category category = categoryMapper.toEntity(request, categoryGroup);
        if (sortOrder != null) {
            category.updateSortOrder(sortOrder, request.getCreatedBy());
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        log.info("[CategoryService] 카테고리 생성 완료. ID={}, 카테고리명={}, 슬러그={}", 
                savedCategory.getId(), savedCategory.getName(), savedCategory.getSlug());
        
        return ResponseData.ok("0000", "카테고리가 생성되었습니다.", savedCategory.getId());
    }

    @Override
    @Transactional
    public Response updateCategory(Long id, RequestCategoryUpdate request) {
        log.info("[CategoryService] 카테고리 수정 시작. ID={}, 요청데이터={}", id, request);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        CategoryGroup newCategoryGroup = null;
        
        // 카테고리 그룹 변경 시 검증
        if (request.getCategoryGroupId() != null && !request.getCategoryGroupId().equals(category.getCategoryGroupId())) {
            newCategoryGroup = categoryGroupRepository.findById(request.getCategoryGroupId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));
        }
        
        // 슬러그 변경 시 검증
        if (request.getSlug() != null) {
            if (!isValidSlug(request.getSlug())) {
                log.warn("[CategoryService] 잘못된 슬러그 형식. 슬러그={}", request.getSlug());
                throw new BusinessException(ErrorCode.INVALID_SLUG_FORMAT);
            }
            
            // 중복 검증 (현재 카테고리 제외)
            Long targetGroupId = request.getCategoryGroupId() != null ? request.getCategoryGroupId() : category.getCategoryGroupId();
            if (categoryRepository.existsByCategoryGroupIdAndSlugAndIdNot(targetGroupId, request.getSlug(), id)) {
                log.warn("[CategoryService] 중복된 슬러그로 수정 시도. 그룹ID={}, 슬러그={}", targetGroupId, request.getSlug());
                throw new BusinessException(ErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
            }
        }
        
        categoryMapper.updateEntity(category, request, newCategoryGroup);
        
        log.info("[CategoryService] 카테고리 수정 완료. ID={}, 카테고리명={}, 슬러그={}", 
                id, category.getName(), category.getSlug());
        
        return Response.ok("0000", "카테고리가 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response deleteCategory(Long id) {
        log.info("[CategoryService] 카테고리 삭제 시작. ID={}", id);
        
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        categoryRepository.deleteById(id);
        
        log.info("[CategoryService] 카테고리 삭제 완료. ID={}", id);
        
        return Response.ok("0000", "카테고리가 삭제되었습니다.");
    }
    
    /**
     * 슬러그 유효성 검증.
     * 영문, 숫자, 하이픈만 허용
     * 
     * @param slug 검증할 슬러그
     * @return 유효 여부
     */
    private boolean isValidSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        return slug.matches("^[a-zA-Z0-9-]+$");
    }
}