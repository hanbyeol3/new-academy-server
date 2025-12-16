package com.academy.api.category.service;

import com.academy.api.category.dto.RequestCategoryCreate;
import com.academy.api.category.dto.RequestCategoryUpdate;
import com.academy.api.category.dto.ResponseCategory;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;

/**
 * 카테고리 서비스 인터페이스.
 */
public interface CategoryService {

    /**
     * 카테고리 목록 조회.
     * 
     * @return 카테고리 목록
     */
    ResponseList<ResponseCategory> getCategoryList();

    /**
     * 카테고리 그룹별 카테고리 목록 조회.
     * 
     * @param categoryGroupId 카테고리 그룹 ID
     * @return 카테고리 목록
     */
    ResponseList<ResponseCategory> getCategoriesByGroupId(Long categoryGroupId);

    /**
     * 카테고리 상세 조회.
     * 
     * @param id 카테고리 ID
     * @return 카테고리 상세 정보
     */
    ResponseData<ResponseCategory> getCategory(Long id);

    /**
     * 카테고리 생성.
     * 
     * @param request 카테고리 생성 요청
     * @return 생성된 카테고리 ID
     */
    ResponseData<Long> createCategory(RequestCategoryCreate request);

    /**
     * 카테고리 수정.
     * 
     * @param id 카테고리 ID
     * @param request 카테고리 수정 요청
     * @return 수정 결과
     */
    Response updateCategory(Long id, RequestCategoryUpdate request);

    /**
     * 카테고리 삭제.
     * 
     * @param id 카테고리 ID
     * @return 삭제 결과
     */
    Response deleteCategory(Long id);
}