package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QnaQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * QnA 질문 Repository Custom Interface.
 * 
 * QueryDSL을 활용한 동적 쿼리 처리를 담당합니다.
 */
public interface QnaQuestionRepositoryCustom {

    /**
     * 공개용 질문 목록 검색 (동적 조건).
     * 
     * @param isAnswered 답변 완료 여부 필터 (null이면 전체)
     * @param searchType 검색 타입 (title, content, author_name, 통합)
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<QnaQuestion> searchQuestionsForPublic(Boolean isAnswered, String searchType, 
                                              String keyword, Pageable pageable);

    /**
     * 관리자용 질문 목록 검색 (동적 조건).
     * 
     * @param isAnswered 답변 완료 여부 필터 (null이면 전체)
     * @param secret 비밀글 여부 필터 (null이면 전체)
     * @param searchType 검색 타입 (title, content, author_name, phone_number, 통합)
     * @param keyword 검색 키워드
     * @param startDate 검색 시작일 (null이면 무제한)
     * @param endDate 검색 종료일 (null이면 무제한)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<QnaQuestion> searchQuestionsForAdmin(Boolean isAnswered, Boolean secret, String searchType,
                                            String keyword, LocalDateTime startDate, LocalDateTime endDate,
                                            Pageable pageable);

    /**
     * 이전 질문 조회.
     * 
     * @param currentId 현재 질문 ID
     * @return 이전 질문 (없으면 null)
     */
    QnaQuestion findPreviousQuestion(Long currentId);

    /**
     * 다음 질문 조회.
     * 
     * @param currentId 현재 질문 ID
     * @return 다음 질문 (없으면 null)
     */
    QnaQuestion findNextQuestion(Long currentId);
}