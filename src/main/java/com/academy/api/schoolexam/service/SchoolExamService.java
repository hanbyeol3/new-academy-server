package com.academy.api.schoolexam.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schoolexam.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 학교별 시험분석 서비스 인터페이스.
 */
public interface SchoolExamService {

    /**
     * 관리자용 시험분석 목록 조회 (모든 상태 포함).
     */
    ResponseList<ResponseSchoolExamAdminList> getSchoolExamListForAdmin(
        String keyword,
        String searchType,
        String schoolLevel,
        Long categoryId,
        Boolean isPublished,
        String sortBy,
        Pageable pageable
    );

    /**
     * 공개용 시험분석 목록 조회 (공개된 것만).
     */
    ResponseList<ResponseSchoolExamPublicList> getSchoolExamListForPublic(
        String keyword,
        String searchType,
        String schoolLevel,
        Long categoryId,
        String sortBy,
        Pageable pageable
    );

    /**
     * 관리자용 시험분석 상세 조회.
     */
    ResponseData<ResponseSchoolExamDetail> getSchoolExamForAdmin(Long id);

    /**
     * 관리자용 시험분석 상세 조회 (학교급 필터 적용).
     */
    ResponseData<ResponseSchoolExamDetail> getSchoolExamForAdmin(Long id, String schoolLevel);

    /**
     * 공개용 시험분석 상세 조회 (조회수 증가).
     */
    ResponseData<ResponseSchoolExamDetail> getSchoolExamForPublic(Long id);

    /**
     * 공개용 시험분석 상세 조회 (조회수 증가, 학교급 필터 적용).
     */
    ResponseData<ResponseSchoolExamDetail> getSchoolExamForPublic(Long id, String schoolLevel);

    /**
     * 시험분석 생성.
     */
    ResponseData<Long> createSchoolExam(RequestSchoolExamCreate request);

    /**
     * 시험분석 수정.
     */
    ResponseData<ResponseSchoolExamDetail> updateSchoolExam(Long id, RequestSchoolExamUpdate request);

    /**
     * 시험분석 삭제.
     */
    Response deleteSchoolExam(Long id);

    /**
     * 조회수 수동 증가.
     */
    Response incrementViewCount(Long id);

    /**
     * 공개/비공개 상태 변경.
     */
    Response updateSchoolExamPublished(Long id, RequestSchoolExamPublishedUpdate request);

    /**
     * 카테고리별 통계 조회.
     */
    ResponseData<List<Object[]>> getSchoolExamStatsByCategory();

    /**
     * 학교급별 통계 조회.
     */
    ResponseData<List<Object[]>> getSchoolExamStatsBySchoolLevel();
    
    /**
     * 중등부/고등부별 최신 시험분석 3개씩 조회.
     * 공개된 시험분석만 조회하며, 각 학교급별로 최신 3개를 반환합니다.
     */
    ResponseData<ResponseSchoolExamLatest> getLatestSchoolExams();
}