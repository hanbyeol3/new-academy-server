package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
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
 * - 통일된 응답 포맷(Response, ResponseData, ResponseList) 사용
 * 
 * 모든 메서드는 다음 원칙을 따름:
 *  1) 목록 조회 → ResponseList<T>
 *  2) 단건 조회 → ResponseData<T> 
 *  3) 생성 → ResponseData<Long> (생성된 ID 반환)
 *  4) 수정/삭제 → Response (단순 성공/실패)
 */
public interface QnaQuestionService {

    /** 
     * 질문 목록 조회 (페이지네이션 포함).
     * - 검색 조건에 따른 필터링 및 정렬 지원
     * - 결과는 ResponseList 형태로 페이지 정보와 함께 반환
     * 
     * @param cond 검색 조건 (제목, 내용, 답변상태 등)
     * @param pageable 페이지네이션 정보 (페이지 번호, 크기, 정렬)
     * @return 검색된 질문 목록과 페이지 정보를 포함한 ResponseList
     */
    ResponseList<ResponseQuestion> list(ResponseQuestion.Criteria cond, Pageable pageable);

    /** 
     * 질문 단건 조회.
     * - ID로 특정 질문을 조회하며 조회수 자동 증가
     * - 존재하지 않는 경우 에러 응답 반환
     * 
     * @param id 조회할 질문 ID
     * @return 조회된 질문 정보 또는 에러 응답
     */
    ResponseData<ResponseQuestion> get(Long id);

    /** 
     * 새로운 질문 생성.
     * - 입력 데이터 검증 후 질문 엔티티 생성
     * - 생성된 질문의 ID를 응답으로 반환
     * 
     * @param request 질문 생성 요청 데이터
     * @param clientIp 클라이언트 IP 주소
     * @return 생성된 질문의 ID 또는 에러 응답
     */
    ResponseData<Long> create(RequestQuestionCreate request, String clientIp);

    /** 
     * 기존 질문 수정.
     * - ID로 질문을 찾아 요청 데이터로 업데이트
     * - 존재하지 않는 경우 에러 응답 반환
     * 
     * @param id 수정할 질문 ID
     * @param request 질문 수정 요청 데이터
     * @return 수정 성공/실패 응답
     */
    Response update(Long id, RequestQuestionUpdate request);

    /** 
     * 질문 삭제.
     * - ID와 비밀번호로 질문을 찾아 삭제 처리
     * - 존재하지 않는 경우 에러 응답 반환
     * 
     * @param id 삭제할 질문 ID
     * @param password 삭제 확인용 비밀번호
     * @return 삭제 성공/실패 응답
     */
    Response delete(Long id, String password);

    /**
     * 회원 ID가 포함된 질문 생성.
     * - 로그인한 사용자가 질문을 작성할 때 사용
     * - member_id가 자동으로 설정됨
     * 
     * @param request 질문 생성 요청 데이터
     * @param memberId 회원 ID
     * @param clientIp 클라이언트 IP 주소
     * @return 생성된 질문의 ID 또는 에러 응답
     */
    ResponseData<Long> createWithMemberId(RequestQuestionCreate request, Long memberId, String clientIp);
}