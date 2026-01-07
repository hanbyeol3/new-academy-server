package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * QnA 답변 Repository.
 */
@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {

    /**
     * 질문 ID로 답변 조회.
     */
    Optional<QnaAnswer> findByQuestionId(Long questionId);

    /**
     * 질문 ID로 답변 존재 여부 확인.
     */
    boolean existsByQuestionId(Long questionId);

    /**
     * 질문 ID로 답변 삭제.
     */
    void deleteByQuestionId(Long questionId);

    /**
     * 특정 관리자가 작성한 답변 개수.
     */
    long countByCreatedBy(Long createdBy);
}