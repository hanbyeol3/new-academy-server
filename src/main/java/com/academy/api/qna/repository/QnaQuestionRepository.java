package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QnaQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * QnA 질문 Repository.
 */
@Repository
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, Long>, QnaQuestionRepositoryCustom {

    /**
     * 답변 상태별 질문 조회.
     */
    Page<QnaQuestion> findByIsAnswered(Boolean isAnswered, Pageable pageable);

    /**
     * 비밀글 여부별 질문 조회.
     */
    Page<QnaQuestion> findBySecret(Boolean secret, Pageable pageable);

    /**
     * 답변 상태와 비밀글 여부별 질문 조회.
     */
    Page<QnaQuestion> findByIsAnsweredAndSecret(Boolean isAnswered, Boolean secret, Pageable pageable);

    /**
     * 조회수 증가 (동시성 문제 방지를 위한 쿼리 수정).
     * 
     * @param id 질문 ID
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 제목 또는 내용으로 검색 (관리자용).
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.title LIKE %:keyword% OR q.content LIKE %:keyword%")
    Page<QnaQuestion> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 작성자명으로 검색 (관리자용).
     */
    Page<QnaQuestion> findByAuthorNameContaining(String authorName, Pageable pageable);

    /**
     * 연락처로 검색 (관리자용).
     */
    Page<QnaQuestion> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);

    /**
     * 날짜 범위로 검색 (관리자용).
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.createdAt BETWEEN :startDate AND :endDate")
    Page<QnaQuestion> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);

    /**
     * 특정 IP로 작성된 질문들 조회 (관리자용).
     */
    Page<QnaQuestion> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * 개인정보 미동의 질문들 조회 (관리자용).
     */
    Page<QnaQuestion> findByPrivacyConsent(Boolean privacyConsent, Pageable pageable);

    /**
     * ID와 비밀번호 해시로 질문 조회 (비회원 본인인증용).
     */
    Optional<QnaQuestion> findByIdAndPasswordHash(Long id, String passwordHash);

    /**
     * 질문과 답변을 함께 조회 (Fetch Join 사용).
     */
    @Query("SELECT q FROM QnaQuestion q LEFT JOIN FETCH q.answer WHERE q.id = :id")
    Optional<QnaQuestion> findByIdWithAnswer(@Param("id") Long id);

    /**
     * 전체 질문 개수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q")
    Long countTotalQuestions();

    /**
     * 답변 완료된 질문 개수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.isAnswered = true")
    Long countAnsweredQuestions();

    /**
     * 답변 대기 중인 질문 개수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.isAnswered = false")
    Long countUnansweredQuestions();
}