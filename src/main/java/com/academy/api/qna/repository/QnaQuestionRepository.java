package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * QnA 질문 엔티티를 위한 JPA Repository.
 * 
 * 기본적인 CRUD 연산과 QnA 질문 도메인에 특화된 쿼리 메서드를 제공한다.
 * 복잡한 동적 검색은 QnaQuestionQueryRepository에서 처리한다.
 * 
 * 주요 기능:
 *  - 기본 CRUD 연산 (JpaRepository 상속)
 *  - 조회수 증가 최적화 쿼리
 *  - 답변 상태 업데이트 쿼리
 *  - 게시글 상태별 조회
 *  - 통계 집계 쿼리
 * 
 * 성능 최적화:
 *  - @Query를 이용한 벌크 연산
 *  - 인덱스 활용한 효율적인 조회
 *  - N+1 문제 방지를 위한 명시적 쿼리
 */
@Repository
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, Long>, JpaSpecificationExecutor<QnaQuestion> {

    /**
     * 게시된 질문만 조회 (공개용).
     * 일반 사용자에게 노출할 질문만 필터링한다.
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.published = true ORDER BY q.pinned DESC, q.createdAt DESC")
    List<QnaQuestion> findAllPublished();

    /**
     * 상단 고정된 질문들 조회.
     * 메인 페이지나 공지사항에서 우선 노출할 질문들을 조회한다.
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.pinned = true AND q.published = true ORDER BY q.createdAt DESC")
    List<QnaQuestion> findAllPinned();

    /**
     * 답변 완료 여부별 질문 조회.
     * 관리자가 미답변 질문을 우선 처리할 때 사용한다.
     * 
     * @param isAnswered 답변 완료 여부 (true: 답변완료, false: 미답변)
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.isAnswered = :isAnswered AND q.published = true ORDER BY q.createdAt ASC")
    List<QnaQuestion> findByAnsweredStatus(@Param("isAnswered") Boolean isAnswered);

    /**
     * 비밀글 여부별 질문 조회.
     * 
     * @param secret 비밀글 여부 (true: 비밀글, false: 공개글)
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.secret = :secret AND q.published = true ORDER BY q.createdAt DESC")
    List<QnaQuestion> findBySecretStatus(@Param("secret") Boolean secret);

    /**
     * 특정 기간 내 작성된 질문 조회.
     * 통계나 리포트 생성 시 사용한다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.createdAt BETWEEN :startDate AND :endDate ORDER BY q.createdAt DESC")
    List<QnaQuestion> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 조회수 상위 질문들 조회.
     * 인기 질문이나 FAQ 후보 질문 선정에 사용한다.
     * 
     * @param limit 조회할 질문 수
     */
    @Query(value = "SELECT * FROM qna_questions WHERE published = true ORDER BY view_count DESC LIMIT :limit", 
           nativeQuery = true)
    List<QnaQuestion> findTopByViewCount(@Param("limit") int limit);

    /**
     * IP 주소로 질문 조회 (관리자용).
     * 부정 사용자나 스팸 질문 추적에 사용한다.
     * 
     * @param ipAddress IP 주소
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.ipAddress = :ipAddress ORDER BY q.createdAt DESC")
    List<QnaQuestion> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 작성자명으로 질문 조회 (부분 일치).
     * 관리자가 특정 작성자의 질문들을 조회할 때 사용한다.
     * 
     * @param authorName 작성자명 (부분 일치)
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.authorName LIKE %:authorName% ORDER BY q.createdAt DESC")
    List<QnaQuestion> findByAuthorNameContaining(@Param("authorName") String authorName);

    /**
     * 조회수 증가 (벌크 업데이트).
     * 개별 엔티티 로딩 없이 직접 데이터베이스에서 조회수를 증가시킨다.
     * 
     * @param questionId 질문 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.viewCount = q.viewCount + 1 WHERE q.id = :questionId")
    int incrementViewCount(@Param("questionId") Long questionId);

    /**
     * 답변 상태 업데이트 (벌크 업데이트).
     * 답변이 등록되었을 때 질문의 답변 상태를 업데이트한다.
     * 
     * @param questionId 질문 ID
     * @param answeredAt 답변 등록 시각
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.isAnswered = true, q.answeredAt = :answeredAt WHERE q.id = :questionId")
    int markAsAnswered(@Param("questionId") Long questionId, @Param("answeredAt") LocalDateTime answeredAt);

    /**
     * 답변 상태 초기화 (벌크 업데이트).
     * 답변이 삭제되었을 때 질문의 답변 상태를 초기화한다.
     * 
     * @param questionId 질문 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.isAnswered = false, q.answeredAt = null WHERE q.id = :questionId")
    int markAsUnanswered(@Param("questionId") Long questionId);

    /**
     * 게시 상태 변경 (벌크 업데이트).
     * 관리자가 부적절한 질문을 숨기거나 다시 노출시킬 때 사용한다.
     * 
     * @param questionId 질문 ID
     * @param published 게시 여부
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.published = :published WHERE q.id = :questionId")
    int updatePublishedStatus(@Param("questionId") Long questionId, @Param("published") Boolean published);

    /**
     * 상단 고정 상태 변경 (벌크 업데이트).
     * 관리자가 중요한 질문을 상단에 고정하거나 해제할 때 사용한다.
     * 
     * @param questionId 질문 ID
     * @param pinned 상단 고정 여부
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaQuestion q SET q.pinned = :pinned WHERE q.id = :questionId")
    int updatePinnedStatus(@Param("questionId") Long questionId, @Param("pinned") Boolean pinned);

    /* ========== 통계 집계 쿼리 ========== */

    /**
     * 전체 질문 수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.published = true")
    Long countTotalQuestions();

    /**
     * 답변 완료된 질문 수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.isAnswered = true AND q.published = true")
    Long countAnsweredQuestions();

    /**
     * 미답변 질문 수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.isAnswered = false AND q.published = true")
    Long countUnansweredQuestions();

    /**
     * 비밀 질문 수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.secret = true AND q.published = true")
    Long countSecretQuestions();

    /**
     * 상단 고정된 질문 수 조회.
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.pinned = true AND q.published = true")
    Long countPinnedQuestions();

    /**
     * 특정 날짜 이후 등록된 질문 수 조회.
     * 
     * @param date 기준 날짜
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.createdAt >= :date AND q.published = true")
    Long countQuestionsSince(@Param("date") LocalDateTime date);

    /**
     * 특정 IP 주소의 질문 수 조회 (스팸 감지용).
     * 
     * @param ipAddress IP 주소
     */
    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.ipAddress = :ipAddress")
    Long countByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 질문 ID와 비밀번호로 질문 조회 (수정/삭제 권한 검증용).
     * 보안을 위해 비밀번호 해시 비교는 서비스 레이어에서 수행한다.
     * 
     * @param id 질문 ID
     * @return 질문 엔티티 (비밀번호 해시 포함)
     */
    @Query("SELECT q FROM QnaQuestion q WHERE q.id = :id")
    Optional<QnaQuestion> findByIdForAuth(@Param("id") Long id);
}