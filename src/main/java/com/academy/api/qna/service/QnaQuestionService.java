package com.academy.api.qna.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import org.springframework.data.domain.Pageable;

/**
 * QnA 질문 비즈니스 로직을 처리하는 서비스 인터페이스.
 * 
 * - QnA 질문의 CRUD 작업을 담당
 * - 컨트롤러와 리포지토리 계층 사이의 비즈니스 로직 추상화
 * - 통일된 응답 포맷(ResponseData, ResponseList) 사용
 * 
 * 모든 메서드는 다음 원칙을 따름:
 *  1) 목록 조회 → ResponseList<T>
 *  2) 단건 조회 → ResponseData<T> 
 *  3) 생성/수정/삭제 → ResponseData<T> (결과 데이터 포함)
 */
public interface QnaQuestionService {

    /** 
     * 질문 목록 조회 (페이지네이션 포함).
     * - 페이지네이션과 기본 정렬(최신순) 지원
     * - 결과는 ResponseList 형태로 페이지 정보와 함께 반환
     * 
     * @param pageable 페이지네이션 정보 (페이지 번호, 크기, 정렬)
     * @return 검색된 질문 목록과 페이지 정보를 포함한 ResponseList
     */
    ResponseList<ResponseQuestion.Projection> getQuestions(Pageable pageable);

    /** 
     * 질문 단건 조회.
     * - ID로 특정 질문을 조회하며 조회수 자동 증가
     * - 존재하지 않는 경우 에러 응답 반환
     * 
     * @param questionId 조회할 질문 ID
     * @return 조회된 질문 정보 또는 에러 응답
     */
    ResponseData<ResponseQuestion> getQuestion(Long questionId);

    /** 
     * 새로운 질문 생성.
     * - 입력 데이터 검증 후 질문 엔티티 생성
     * - 개인정보 수집 동의 확인 및 비밀번호 해싱
     * - 클라이언트 IP 주소 자동 저장
     * 
     * @param request 질문 생성 요청 데이터
     * @param clientIp 클라이언트 IP 주소
     * @return 생성된 질문 정보 또는 에러 응답
     */
    ResponseData<ResponseQuestion> createQuestion(RequestQuestionCreate request, String clientIp);

    /** 
     * 기존 질문 수정.
     * - ID와 비밀번호로 질문을 찾아 요청 데이터로 업데이트
     * - 비밀번호 검증 및 답변 완료 상태 확인
     * 
     * @param questionId 수정할 질문 ID
     * @param request 질문 수정 요청 데이터
     * @return 수정된 질문 정보 또는 에러 응답
     */
    ResponseData<ResponseQuestion> updateQuestion(Long questionId, RequestQuestionUpdate request);

    /** 
     * 질문 삭제.
     * - ID와 비밀번호로 질문을 찾아 삭제 처리
     * - 비밀번호 검증 및 답변 완료 상태 확인
     * 
     * @param questionId 삭제할 질문 ID
     * @param password 삭제 확인용 비밀번호
     * @return 삭제 성공/실패 응답
     */
    ResponseData<Void> deleteQuestion(Long questionId, String password);

    /** 
     * QnA 통계 조회.
     * - 전체 질문 수, 답변 완료율, 비밀글 수 등 통계 정보 반환
     * 
     * @return QnA 통계 정보 또는 에러 응답
     */
    ResponseData<ResponseQuestion.Summary> getStatistics();
}