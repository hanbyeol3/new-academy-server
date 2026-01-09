package com.academy.api.faq.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.faq.dto.RequestFaqCreate;
import com.academy.api.faq.dto.RequestFaqPublishedUpdate;
import com.academy.api.faq.dto.RequestFaqUpdate;
import com.academy.api.faq.dto.ResponseFaq;
import com.academy.api.faq.dto.ResponseFaqListItem;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * FAQ 서비스 인터페이스.
 * 주요 기능:
 * - FAQ CRUD 작업
 * - 검색 및 페이징 처리 (질문, 답변, 질문+답변)
 * - 공개/비공개 상태 관리
 * - 파일 연계 처리 (INLINE 이미지만)
 */
public interface FaqService {

    /**
     * FAQ 목록 조회 (검색 + 페이징).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과 (답변 내용 포함)
     */
    ResponseList<ResponseFaqListItem> getFaqList(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable);

    /**
     * 관리자용 FAQ 목록 조회 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과 (답변 내용 포함)
     */
    ResponseList<ResponseFaqListItem> getFaqListForAdmin(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable);

    /**
     * 공개용 FAQ 목록 조회 (공개된 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과 (답변 내용 포함)
     */
    ResponseList<ResponseFaqListItem> getPublishedFaqList(String keyword, String searchType, Long categoryId, String sortBy, Pageable pageable);

    /**
     * FAQ 상세 조회.
     * Note: FAQ는 목록에서 답변을 포함하므로 별도 상세 API는 필요하지 않지만,
     * 파일 정보 등 추가 데이터가 필요한 경우를 위해 제공.
     * 
     * @param id FAQ ID
     * @return FAQ 상세 정보
     */
    ResponseData<ResponseFaq> getFaq(Long id);

    /**
     * FAQ 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 FAQ ID
     */
    ResponseData<Long> createFaq(RequestFaqCreate request);

    /**
     * FAQ 수정.
     * 
     * @param id FAQ ID
     * @param request 수정 요청 데이터
     * @return 수정 결과 (완전한 FAQ 정보 포함)
     */
    ResponseData<ResponseFaq> updateFaq(Long id, RequestFaqUpdate request);

    /**
     * FAQ 삭제.
     * 
     * @param id FAQ ID
     * @return 삭제 결과
     */
    Response deleteFaq(Long id);

    /**
     * 공개/비공개 상태 변경.
     * 
     * @param id FAQ ID
     * @param isPublished 공개 여부
     * @return 변경 결과
     */
    Response togglePublished(Long id, Boolean isPublished);

    /**
     * 공개/비공개 상태 변경.
     * 
     * @param id FAQ ID
     * @param request 공개 상태 변경 요청 데이터
     * @return 변경 결과
     */
    Response updateFaqPublished(Long id, RequestFaqPublishedUpdate request);


    /**
     * 카테고리별 FAQ 통계.
     * 
     * @return 카테고리별 통계 정보
     */
    ResponseData<List<Object[]>> getFaqStatsByCategory();
}