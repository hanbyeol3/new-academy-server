package com.academy.api.academy.service;

import com.academy.api.academy.dto.*;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;

/**
 * 학원 소개 정보 통합 서비스 인터페이스.
 * 
 * AcademyAbout과 AcademyAboutDetails 모든 비즈니스 로직을 처리합니다.
 */
public interface AcademyAboutService {

    // ==================== AcademyAbout 관련 기능 ====================

    /**
     * 학원 소개 정보 조회 (관리자용).
     * 
     * @return 학원 소개 정보
     */
    ResponseData<ResponseAcademyAbout> getAcademyAbout();

    /**
     * 학원 소개 정보 수정.
     * 
     * @param request 수정 요청 데이터
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateAcademyAbout(RequestAcademyAboutUpdate request, Long updatedBy);

    /**
     * 학원 소개 정보 조회 (공개용).
     * 
     * @return 학원 소개 정보
     */
    ResponseData<ResponseAcademyAbout> getPublicAcademyAbout();

    // ==================== AcademyAboutDetails 관련 기능 ====================

    /**
     * 학원 소개 상세 목록 조회 (관리자용).
     * 
     * @return 상세 목록
     */
    ResponseList<ResponseAcademyAboutDetails> getDetailsList();

    /**
     * 학원 소개 상세 정보 생성.
     * 
     * @param request 생성 요청 데이터
     * @param createdBy 등록자 ID
     * @return 생성된 상세 정보 ID
     */
    ResponseData<Long> createDetails(RequestAcademyAboutDetailsCreate request, Long createdBy);

    /**
     * 학원 소개 상세 정보 수정.
     * 
     * @param id 수정할 상세 정보 ID
     * @param request 수정 요청 데이터
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateDetails(Long id, RequestAcademyAboutDetailsUpdate request, Long updatedBy);

    /**
     * 학원 소개 상세 정보 삭제.
     * 
     * @param id 삭제할 상세 정보 ID
     * @return 삭제 결과
     */
    Response deleteDetails(Long id);

    /**
     * 학원 소개 상세 정보 순서 변경.
     * 
     * @param request 순서 변경 요청 데이터
     * @param updatedBy 수정자 ID
     * @return 순서 변경 결과
     */
    Response updateDetailsOrder(RequestDetailsOrderUpdate request, Long updatedBy);

    /**
     * 학원 소개 상세 목록 조회 (공개용).
     * 
     * @return 상세 목록 (공개용)
     */
    ResponseList<ResponseAcademyAboutDetails> getPublicDetailsList();
}