package com.academy.api.academy.service;

import com.academy.api.academy.dto.RequestAcademyInfoUpdate;
import com.academy.api.academy.dto.RequestInstructorVideoUpdate;
import com.academy.api.academy.dto.ResponseAcademyInfo;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;

/**
 * 학원 정보 서비스 인터페이스.
 */
public interface AcademyInfoService {

    /**
     * 학원 정보 조회.
     * 
     * @return 학원 정보
     */
    ResponseData<ResponseAcademyInfo> getAcademyInfo();

    /**
     * 학원 정보 수정.
     * 
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    Response updateAcademyInfo(RequestAcademyInfoUpdate request);

    /**
     * 강사진 유튜브 URL 수정.
     * 
     * @param request 강사진 유튜브 URL 수정 요청 데이터
     * @return 수정 결과
     */
    Response updateInstructorVideo(RequestInstructorVideoUpdate request);
}