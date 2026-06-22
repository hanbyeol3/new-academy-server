package com.academy.api.improvement.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.improvement.domain.Division;
import com.academy.api.improvement.domain.Subject;
import com.academy.api.improvement.domain.WriterType;
import com.academy.api.improvement.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 성적 향상 사례 Service 인터페이스.
 * 
 * 성적 향상 사례의 비즈니스 로직을 정의합니다.
 */
public interface ImprovementCaseService {
    
    // ==================== 공개 API ====================
    
    /**
     * [공개] 성적 향상 사례 목록 조회.
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param division 학년 구분
     * @param subjectEnum 과목
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 사례 목록
     */
    ResponseList<ResponseImprovementCasePublicList> getPublicCaseList(
        String keyword,
        String searchType,
        String division,
        String subject,
        String sortBy,
        Pageable pageable
    );
    
    /**
     * [공개] 성적 향상 사례 상세 조회.
     * 
     * @param id 사례 ID
     * @return 사례 상세 정보
     */
    ResponseData<ResponseImprovementCaseDetail> getPublicCaseDetail(Long id);
    
    /**
     * [공개] 비밀글 상세 조회.
     * 
     * @param id 사례 ID
     * @param password 비밀번호
     * @return 사례 상세 정보
     */
    ResponseData<ResponseImprovementCaseDetail> getSecretCaseDetail(Long id, String password);
    
    /**
     * [공개] 성적 향상 사례 생성 (외부 작성자).
     * 
     * @param request 생성 요청
     * @param uploadFileIds 첨부파일 ID 목록
     * @return 생성된 사례 ID
     */
    ResponseData<Long> createPublicCase(RequestImprovementCaseCreate request, Long[] uploadFileIds);
    
    /**
     * [공개] 성적 향상 사례 수정 (작성자 본인).
     * 
     * @param id 사례 ID
     * @param request 수정 요청 (비밀번호 포함)
     * @param uploadFileIds 첨부파일 ID 목록
     * @return 수정 결과
     */
    Response updatePublicCase(Long id, RequestImprovementCaseUpdate request, Long[] uploadFileIds);
    
    /**
     * [공개] 성적 향상 사례 삭제 (작성자 본인).
     * 
     * @param id 사례 ID
     * @param request 삭제 요청 (작성자명, 비밀번호 포함)
     * @return 삭제 결과
     */
    Response deletePublicCase(Long id, RequestImprovementCaseDelete request);
    
    // ==================== 관리자 API ====================
    
    /**
     * [관리자] 성적 향상 사례 목록 조회.
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param writerType 작성자 유형
     * @param division 학년 구분
     * @param subjectEnum 과목
     * @param isPublished 공개 여부
     * @param isPinned 고정글 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 사례 목록
     */
    ResponseList<ResponseImprovementCaseAdminList> getAdminCaseList(
        String keyword,
        String searchType,
        String writerType,
        String division,
        String subject,
        Boolean isPublished,
        Boolean isPinned,
        String sortBy,
        Pageable pageable
    );
    
    /**
     * [관리자] 성적 향상 사례 상세 조회.
     * 
     * @param id 사례 ID
     * @return 사례 상세 정보
     */
    ResponseData<ResponseImprovementCaseDetail> getAdminCaseDetail(Long id);
    
    /**
     * [관리자] 성적 향상 사례 생성.
     * 
     * @param request 생성 요청
     * @param uploadFileIds 첨부파일 ID 목록
     * @return 생성된 사례 ID
     */
    ResponseData<Long> createAdminCase(RequestImprovementCaseCreate request, Long[] uploadFileIds);
    
    /**
     * [관리자] 성적 향상 사례 수정.
     * 
     * @param id 사례 ID
     * @param request 수정 요청
     * @param uploadFileIds 첨부파일 ID 목록
     * @return 수정 결과
     */
    Response updateAdminCase(Long id, RequestImprovementCaseAdminUpdate request, Long[] uploadFileIds);
    
    /**
     * [관리자] 성적 향상 사례 삭제 (소프트 삭제).
     * 
     * @param id 사례 ID
     * @return 삭제 결과
     */
    Response deleteAdminCase(Long id);
    
    /**
     * [관리자] 성적 향상 사례 복구.
     * 
     * @param id 사례 ID
     * @return 복구 결과
     */
    Response restoreAdminCase(Long id);
    
    /**
     * [관리자] 공개 상태 변경.
     * 
     * @param id 사례 ID
     * @param isPublished 공개 여부
     * @return 변경 결과
     */
    Response updatePublishStatus(Long id, Boolean isPublished);
    
    /**
     * [관리자] 고정글 상태 변경.
     * 
     * @param id 사례 ID
     * @param isPinned 고정 여부
     * @return 변경 결과
     */
    Response updatePinnedStatus(Long id, Boolean isPinned);
}