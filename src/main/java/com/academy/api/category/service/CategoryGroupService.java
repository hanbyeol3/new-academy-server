package com.academy.api.category.service;

import com.academy.api.category.dto.RequestCategoryGroupCreate;
import com.academy.api.category.dto.RequestCategoryGroupUpdate;
import com.academy.api.category.dto.ResponseCategoryGroup;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;

/**
 * 카테고리 그룹 서비스 인터페이스.
 */
public interface CategoryGroupService {

    /**
     * 카테고리 그룹 목록 조회.
     * 
     * @return 카테고리 그룹 목록
     */
    ResponseList<ResponseCategoryGroup> getCategoryGroupList();

    /**
     * 카테고리 그룹 상세 조회.
     * 
     * @param id 카테고리 그룹 ID
     * @return 카테고리 그룹 상세 정보
     */
    ResponseData<ResponseCategoryGroup> getCategoryGroup(Long id);

    /**
     * 카테고리 그룹 생성.
     * 
     * @param request 카테고리 그룹 생성 요청
     * @return 생성된 카테고리 그룹 ID
     */
    ResponseData<Long> createCategoryGroup(RequestCategoryGroupCreate request);

    /**
     * 카테고리 그룹 수정.
     * 
     * @param id 카테고리 그룹 ID
     * @param request 카테고리 그룹 수정 요청
     * @return 수정 결과
     */
    Response updateCategoryGroup(Long id, RequestCategoryGroupUpdate request);

    /**
     * 카테고리 그룹 삭제.
     * 
     * @param id 카테고리 그룹 ID
     * @return 삭제 결과
     */
    Response deleteCategoryGroup(Long id);
}