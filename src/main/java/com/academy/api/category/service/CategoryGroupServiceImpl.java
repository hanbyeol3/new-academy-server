package com.academy.api.category.service;

import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.dto.RequestCategoryGroupCreate;
import com.academy.api.category.dto.RequestCategoryGroupUpdate;
import com.academy.api.category.dto.ResponseCategoryGroup;
import com.academy.api.category.mapper.CategoryGroupMapper;
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
 * 카테고리 그룹 서비스 구현체.
 * 
 * - 카테고리 그룹 CRUD 비즈니스 로직 처리
 * - 그룹명 중복 검증 및 데이터 무결성 보장
 * - 하위 카테고리 존재 여부 확인 (삭제 시)
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
public class CategoryGroupServiceImpl implements CategoryGroupService {

    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryGroupMapper categoryGroupMapper;

    @Override
    public ResponseData<List<ResponseCategoryGroup>> getCategoryGroupList() {
        log.info("[CategoryGroupService] 카테고리 그룹 목록 조회 시작");
        
        List<CategoryGroup> categoryGroups = categoryGroupRepository.findAllOrderByCreatedAtDesc();
        
        log.debug("[CategoryGroupService] 카테고리 그룹 조회 완료. 총 {}개", categoryGroups.size());
        
        List<ResponseCategoryGroup> response = categoryGroupMapper.toResponseList(categoryGroups);
        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<ResponseCategoryGroup> getCategoryGroup(Long id) {
        log.info("[CategoryGroupService] 카테고리 그룹 상세 조회 시작. ID={}", id);
        
        CategoryGroup categoryGroup = categoryGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));
        
        log.debug("[CategoryGroupService] 카테고리 그룹 조회 완료. ID={}, 그룹명={}", id, categoryGroup.getName());
        
        ResponseCategoryGroup response = categoryGroupMapper.toResponse(categoryGroup);
        return ResponseData.ok(response);
    }

    @Override
    @Transactional
    public ResponseData<Long> createCategoryGroup(RequestCategoryGroupCreate request) {
        log.info("[CategoryGroupService] 카테고리 그룹 생성 시작. 그룹명={}", request.getName());
        
        // 그룹명 중복 검증
        if (categoryGroupRepository.existsByName(request.getName())) {
            log.warn("[CategoryGroupService] 중복된 그룹명. 그룹명={}", request.getName());
            throw new BusinessException(ErrorCode.CATEGORY_GROUP_ALREADY_EXISTS);
        }
        
        CategoryGroup categoryGroup = categoryGroupMapper.toEntity(request);
        CategoryGroup savedCategoryGroup = categoryGroupRepository.save(categoryGroup);
        
        log.info("[CategoryGroupService] 카테고리 그룹 생성 완료. ID={}, 그룹명={}", 
                savedCategoryGroup.getId(), savedCategoryGroup.getName());
        
        return ResponseData.ok("0000", "카테고리 그룹이 생성되었습니다.", savedCategoryGroup.getId());
    }

    @Override
    @Transactional
    public Response updateCategoryGroup(Long id, RequestCategoryGroupUpdate request) {
        log.info("[CategoryGroupService] 카테고리 그룹 수정 시작. ID={}, 요청데이터={}", id, request);
        
        CategoryGroup categoryGroup = categoryGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));
        
        // 그룹명 변경 시 중복 검증
        if (request.getName() != null && !request.getName().equals(categoryGroup.getName())) {
            if (categoryGroupRepository.existsByNameAndIdNot(request.getName(), id)) {
                log.warn("[CategoryGroupService] 중복된 그룹명으로 수정 시도. 그룹명={}", request.getName());
                throw new BusinessException(ErrorCode.CATEGORY_GROUP_ALREADY_EXISTS);
            }
        }
        
        categoryGroupMapper.updateEntity(categoryGroup, request);
        
        log.info("[CategoryGroupService] 카테고리 그룹 수정 완료. ID={}, 그룹명={}", id, categoryGroup.getName());
        
        return Response.ok("0000", "카테고리 그룹이 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response deleteCategoryGroup(Long id) {
        log.info("[CategoryGroupService] 카테고리 그룹 삭제 시작. ID={}", id);
        
        if (!categoryGroupRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_GROUP_NOT_FOUND);
        }
        
        // 하위 카테고리 존재 여부 확인
        long categoryCount = categoryRepository.countByCategoryGroupId(id);
        if (categoryCount > 0) {
            log.warn("[CategoryGroupService] 하위 카테고리가 존재하여 삭제 불가. ID={}, 하위카테고리수={}", id, categoryCount);
            throw new BusinessException(ErrorCode.CATEGORY_GROUP_HAS_CATEGORIES);
        }
        
        categoryGroupRepository.deleteById(id);
        
        log.info("[CategoryGroupService] 카테고리 그룹 삭제 완료. ID={}", id);
        
        return Response.ok("0000", "카테고리 그룹이 삭제되었습니다.");
    }
}