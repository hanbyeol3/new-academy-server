package com.academy.api.facility.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.facility.dto.RequestFacilityCreate;
import com.academy.api.facility.dto.RequestFacilityUpdate;
import com.academy.api.facility.dto.ResponseFacility;
import com.academy.api.facility.dto.ResponseFacilityListItem;
import org.springframework.data.domain.Pageable;

/**
 * 시설 서비스 인터페이스.
 */
public interface FacilityService {

    /**
     * 시설 목록 조회 (관리자용).
     * 
     * @param title 검색할 제목 (선택사항)
     * @param isPublished 공개 여부 필터 (선택사항)
     * @param pageable 페이징 정보
     * @return 시설 목록
     */
    ResponseList<ResponseFacilityListItem> getFacilityList(String title, Boolean isPublished, Pageable pageable);

    /**
     * 공개 시설 목록 조회 (공개 API용).
     * 
     * @param pageable 페이징 정보
     * @return 공개된 시설 목록
     */
    ResponseList<ResponseFacilityListItem> getPublicFacilityList(Pageable pageable);

    /**
     * 시설 상세 조회 (관리자용).
     * 
     * @param id 시설 ID
     * @return 시설 상세 정보
     */
    ResponseData<ResponseFacility> getFacility(Long id);

    /**
     * 공개 시설 상세 조회 (공개 API용).
     * 
     * @param id 시설 ID
     * @return 공개된 시설 상세 정보
     */
    ResponseData<ResponseFacility> getPublicFacility(Long id);

    /**
     * 시설 등록.
     * 
     * @param request 시설 등록 요청 데이터
     * @return 등록된 시설 ID
     */
    ResponseData<Long> createFacility(RequestFacilityCreate request);

    /**
     * 시설 수정.
     * 
     * @param id 시설 ID
     * @param request 시설 수정 요청 데이터
     * @return 수정 결과
     */
    Response updateFacility(Long id, RequestFacilityUpdate request);

    /**
     * 시설 공개/비공개 상태 전환.
     * 
     * @param id 시설 ID
     * @return 전환 결과
     */
    Response toggleFacilityPublished(Long id);

    /**
     * 시설 삭제.
     * 
     * @param id 시설 ID
     * @return 삭제 결과
     */
    Response deleteFacility(Long id);
}