package com.academy.api.teacher.service;

import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.teacher.dto.RequestMainTeacherBatch;
import com.academy.api.teacher.dto.RequestMainTeacherOrder;
import com.academy.api.teacher.dto.RequestTeacherCreate;
import com.academy.api.teacher.dto.RequestTeacherUpdate;
import com.academy.api.teacher.dto.ResponseMainManagementData;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherByCategory;
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
     * 강사 목록 조회 (관리자용 - 통합 검색).
     * 
     * @param keyword 검색 키워드 (강사명)
     * @param categoryId 과목 카테고리 ID (null이면 전체 과목)
     * @param isPublished 공개 여부 필터 (null이면 모든 상태)
     * @param sortType 정렬 방식 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getTeacherList(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable);

    /**
     * 공개 강사 목록 조회 (공개용).
     * 
     * @param keyword 검색 키워드 (강사명)
     * @param categoryId 과목 카테고리 ID (null이면 전체 과목)
     * @param pageable 페이징 정보
     * @return 공개된 강사 목록
     */
    ResponseList<ResponseTeacherListItem> getPublishedTeacherList(String keyword, Long categoryId, Pageable pageable);

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
     * 카테고리별 강사 목록 조회 (공개용).
     * 과목 그룹(ID=4)의 모든 카테고리별로 공개된 강사 목록을 그룹화하여 반환.
     * 
     * @return 카테고리별 강사 목록
     */
    ResponseData<java.util.List<ResponseTeacherByCategory>> getTeachersByCategory();

    /**
     * 카테고리별 강사 순서 변경.
     * 특정 과목 카테고리 내에서 강사들의 표시 순서를 변경합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param request 정렬된 강사 ID 목록을 담은 요청
     * @return 순서 변경 결과
     */
    Response updateCategoryTeacherOrder(Long categoryId, com.academy.api.category.dto.RequestTeacherOrderUpdate request);

    /**
     * 메인 강사 여부 변경.
     * 메인 해제 시 mainSortOrder를 0으로 초기화합니다.
     * 
     * @param id 강사 ID
     * @param isMain 메인 노출 여부
     * @return 상태 변경 결과
     */
    Response updateMainStatus(Long id, Boolean isMain);

    /**
     * 메인 강사 순서 일괄 변경.
     * is_main = 1인 강사만 순서 변경 가능합니다.
     * 
     * @param request 강사별 순서 정보
     * @return 순서 변경 결과
     */
    Response updateMainTeacherOrder(RequestMainTeacherOrder request);

    /**
     * 메인 강사 목록 조회.
     * 공개되고 메인 노출 설정된 강사 목록을 조회합니다.
     * 
     * @return 메인 강사 목록 (main_sort_order ASC, id DESC)
     */
    ResponseList<ResponseTeacherListItem> getMainTeacherList();
    
    /**
     * 메인 강사 관리 화면용 데이터 조회.
     * 메인으로 설정 가능한 강사와 현재 메인 강사 목록을 함께 제공합니다.
     * 
     * @param categoryId 과목 ID (null이면 전체)
     * @return 메인 강사 관리 데이터
     */
    ResponseData<ResponseMainManagementData> getMainManagementData(Long categoryId);
    
    /**
     * 메인 강사 일괄 처리.
     * 메인 강사 추가/제거/순서 변경/소개글 수정을 한 번에 처리합니다.
     * 
     * @param request 일괄 처리 요청
     * @return 처리 결과
     */
    Response updateMainTeachersBatch(RequestMainTeacherBatch request);

}