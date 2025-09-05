package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * QnA 답변 엔티티를 위한 JPA Repository.
 * 
 * 기본적인 CRUD 연산과 QnA 답변 도메인에 특화된 쿼리 메서드를 제공한다.
 * 질문과 1:1 관계로 답변은 질문당 최대 1개까지만 허용된다.
 * 
 * 주요 기능:
 *  - 기본 CRUD 연산 (JpaRepository 상속)
 *  - 질문별 답변 조회
 *  - 관리자별 답변 조회
 *  - 게시 상태별 답변 관리
 *  - 통계 집계 쿼리
 * 
 * 제약사항:
 *  - questionId에 UNIQUE 제약조건으로 중복 답변 방지
 *  - 답변 생성/삭제 시 연관된 질문의 답변 상태 동기화 필요
 */
@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {

    /**
     * 질문 ID로 답변 조회.
     * 질문당 답변은 최대 1개이므로 Optional을 반환한다.
     * 
     * @param questionId 질문 ID
     * @return 답변 엔티티 (존재하지 않으면 empty)
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.questionId = :questionId")
    Optional<QnaAnswer> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * 게시된 답변만 조회 (공개용).
     * 일반 사용자에게 노출할 답변만 필터링한다.
     * 
     * @param questionId 질문 ID
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.questionId = :questionId AND a.published = true")
    Optional<QnaAnswer> findPublishedByQuestionId(@Param("questionId") Long questionId);

    /**
     * 특정 관리자가 작성한 답변들 조회.
     * 관리자별 답변 이력이나 통계 생성에 사용한다.
     * 
     * @param adminName 관리자명
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.adminName = :adminName ORDER BY a.createdAt DESC")
    List<QnaAnswer> findByAdminName(@Param("adminName") String adminName);

    /**
     * 게시 상태별 답변 조회.
     * 
     * @param published 게시 여부 (true: 게시, false: 숨김)
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.published = :published ORDER BY a.createdAt DESC")
    List<QnaAnswer> findByPublishedStatus(@Param("published") Boolean published);

    /**
     * 비밀 답변만 조회.
     * 
     * @param secret 비밀 답변 여부 (true: 비밀, false: 공개)
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.secret = :secret ORDER BY a.createdAt DESC")
    List<QnaAnswer> findBySecretStatus(@Param("secret") Boolean secret);

    /**
     * 특정 기간 내 작성된 답변 조회.
     * 통계나 리포트 생성 시 사용한다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<QnaAnswer> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 최근 답변들 조회 (관리자용).
     * 관리자 대시보드에서 최근 활동을 표시할 때 사용한다.
     * 
     * @param limit 조회할 답변 수
     */
    @Query(value = "SELECT * FROM qna_answers ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<QnaAnswer> findRecentAnswers(@Param("limit") int limit);

    /**
     * 질문 ID 목록으로 답변들 조회.
     * 질문 목록 페이지에서 답변 여부를 일괄 조회할 때 사용한다.
     * 
     * @param questionIds 질문 ID 목록
     */
    @Query("SELECT a FROM QnaAnswer a WHERE a.questionId IN :questionIds")
    List<QnaAnswer> findByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 답변 존재 여부 확인.
     * 질문에 답변이 이미 등록되어 있는지 확인한다.
     * 
     * @param questionId 질문 ID
     * @return 답변이 존재하면 true, 없으면 false
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM QnaAnswer a WHERE a.questionId = :questionId")
    boolean existsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 게시 상태 변경 (벌크 업데이트).
     * 관리자가 부적절한 답변을 숨기거나 다시 노출시킬 때 사용한다.
     * 
     * @param answerId 답변 ID
     * @param published 게시 여부
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaAnswer a SET a.published = :published WHERE a.id = :answerId")
    int updatePublishedStatus(@Param("answerId") Long answerId, @Param("published") Boolean published);

    /**
     * 비밀 답변 상태 변경 (벌크 업데이트).
     * 관리자가 답변의 비밀 상태를 변경할 때 사용한다.
     * 
     * @param answerId 답변 ID
     * @param secret 비밀 답변 여부
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE QnaAnswer a SET a.secret = :secret WHERE a.id = :answerId")
    int updateSecretStatus(@Param("answerId") Long answerId, @Param("secret") Boolean secret);

    /* ========== 통계 집계 쿼리 ========== */

    /**
     * 전체 답변 수 조회.
     */
    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.published = true")
    Long countTotalAnswers();

    /**
     * 비밀 답변 수 조회.
     */
    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.secret = true AND a.published = true")
    Long countSecretAnswers();

    /**
     * 게시되지 않은 답변 수 조회.
     */
    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.published = false")
    Long countUnpublishedAnswers();

    /**
     * 특정 날짜 이후 등록된 답변 수 조회.
     * 
     * @param date 기준 날짜
     */
    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.createdAt >= :date AND a.published = true")
    Long countAnswersSince(@Param("date") LocalDateTime date);

    /**
     * 관리자별 답변 수 조회.
     * 
     * @param adminName 관리자명
     */
    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.adminName = :adminName AND a.published = true")
    Long countByAdminName(@Param("adminName") String adminName);

    /**
     * 질문과 답변 조인하여 조회 (성능 최적화).
     * N+1 문제를 방지하기 위해 명시적으로 조인한다.
     * 
     * @param questionId 질문 ID
     */
    @Query("SELECT a FROM QnaAnswer a JOIN FETCH a.question WHERE a.questionId = :questionId")
    Optional<QnaAnswer> findByQuestionIdWithQuestion(@Param("questionId") Long questionId);

    /**
     * 답변과 함께 질문 정보도 조회 (관리자용).
     * 답변 목록에서 연관된 질문 정보도 함께 표시할 때 사용한다.
     */
    @Query("SELECT a FROM QnaAnswer a JOIN FETCH a.question q ORDER BY a.createdAt DESC")
    List<QnaAnswer> findAllWithQuestions();

    /**
     * 특정 관리자의 답변과 질문 정보 함께 조회.
     * 
     * @param adminName 관리자명
     */
    @Query("SELECT a FROM QnaAnswer a JOIN FETCH a.question q WHERE a.adminName = :adminName ORDER BY a.createdAt DESC")
    List<QnaAnswer> findByAdminNameWithQuestions(@Param("adminName") String adminName);
}