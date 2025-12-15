package com.academy.api.teacher.service;

import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.teacher.dto.RequestTeacherCreate;
import com.academy.api.teacher.dto.RequestTeacherUpdate;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherListItem;
import org.springframework.data.domain.Pageable;

/**
 * 강사 서비스 인터페이스.
 * 
 * - 강사 CRUD 비즈니스 로직 정의
 * - CategoryUsageChecker 구현: 과목 삭제 시 강사 연결 확인
 * - 과목(카테고리) 연계 처리
 * - 파일 업로드 및 임시파일 변환
 * - 검색 및 필터링 기능
 */
public interface TeacherService extends CategoryUsageChecker {

    /**
     * 강사 목록 조회 (관리자용).
     * 
     * @param keyword 검색 키워드 (강사명)
     * @param pageable 페이징 정보
     * @return 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getTeacherList(String keyword, Pageable pageable);

    /**
     * 공개 강사 목록 조회 (공개용).
     * 
     * @param keyword 검색 키워드 (강사명)
     * @param pageable 페이징 정보
     * @return 공개된 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getPublishedTeacherList(String keyword, Pageable pageable);

    /**
     * 강사 상세 조회.
     * 
     * @param id 강사 ID
     * @return 강사 상세 정보
     */
    ResponseData<ResponseTeacher> getTeacher(Long id);

    /**
     * 강사 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 강사 ID
     */
    ResponseData<Long> createTeacher(RequestTeacherCreate request);

    /**
     * 강사 수정.
     * 
     * @param id 강사 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    ResponseData<ResponseTeacher> updateTeacher(Long id, RequestTeacherUpdate request);

    /**
     * 강사 삭제.
     * 
     * @param id 강사 ID
     * @return 삭제 결과
     */
    Response deleteTeacher(Long id);

    /**
     * 강사 공개/비공개 상태 변경.
     * 
     * @param id 강사 ID
     * @param isPublished 공개 여부
     * @return 상태 변경 결과
     */
    Response updatePublishedStatus(Long id, Boolean isPublished);

    /**
     * 과목별 강사 조회 (관리자용).
     * 
     * @param categoryId 과목 카테고리 ID
     * @param isPublished 공개 여부 필터 (null이면 모든 상태)
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getTeachersBySubject(Long categoryId, Boolean isPublished, Pageable pageable);

    /**
     * 과목별 강사 조회 (공개용).
     * 
     * @param categoryId 과목 카테고리 ID
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 공개 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getPublishedTeachersBySubject(Long categoryId, Pageable pageable);
}