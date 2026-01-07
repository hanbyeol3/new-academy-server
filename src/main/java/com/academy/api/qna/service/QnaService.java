package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * QnA 서비스 인터페이스.
 */
public interface QnaService {

    // ========== Public API ==========

    /**
     * 공개 질문 목록 조회.
     */
    ResponseList<ResponseQnaQuestionListItem> getPublicQuestionList(
            Boolean isAnswered, String searchType, String keyword, Pageable pageable);

    /**
     * 공개 질문 상세 조회.
     */
    ResponseData<ResponseQnaQuestionDetail> getPublicQuestion(Long id, String viewToken);

    /**
     * 질문 등록.
     */
    ResponseData<ResponseQnaQuestionCreate> createQuestion(RequestQnaQuestionCreate request, String clientIp);

    /**
     * 비밀글 비밀번호 검증 및 토큰 발급.
     */
    ResponseData<ResponseQnaPasswordVerify> verifyPassword(Long id, RequestQnaPasswordVerify request, String clientIp);

    /**
     * 질문 수정.
     */
    Response updateQuestion(Long id, RequestQnaQuestionUpdate request);

    /**
     * 질문 삭제.
     */
    Response deleteQuestion(Long id, RequestQnaQuestionDelete request);

    // ========== Admin API ==========

    /**
     * 관리자 질문 목록 조회.
     */
    ResponseList<ResponseQnaQuestionListItem> getAdminQuestionList(
            Boolean isAnswered, Boolean secret, String searchType, String keyword, 
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 관리자 질문 상세 조회.
     */
    ResponseData<ResponseQnaQuestionAdmin> getAdminQuestion(Long id);

    /**
     * 답변 Upsert (생성/수정).
     */
    Response upsertAnswer(Long questionId, RequestQnaAnswerUpsert request);

    /**
     * 답변 삭제.
     */
    Response deleteAnswer(Long questionId);

    /**
     * 관리자 질문 삭제.
     */
    Response deleteQuestionByAdmin(Long id);

    /**
     * QnA 통계 조회.
     */
    ResponseData<ResponseQnaStatistics> getQnaStatistics();
}