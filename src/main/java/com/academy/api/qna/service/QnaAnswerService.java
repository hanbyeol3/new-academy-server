package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.qna.model.RequestAnswerCreate;
import com.academy.api.qna.model.RequestAnswerUpdate;
import com.academy.api.qna.model.ResponseAnswer;

/**
 * QnA 답변 비즈니스 로직을 처리하는 서비스 인터페이스.
 * 
 * - QnA 답변의 CRUD 작업을 담당
 * - 질문과 1:1 관계로 답변은 질문당 최대 1개까지만 허용
 * - 관리자 권한을 가진 사용자만 답변 등록/수정/삭제 가능
 * - 통일된 응답 포맷(Response, ResponseData) 사용
 * 
 * 모든 메서드는 다음 원칙을 따름:
 *  1) 단건 조회 → ResponseData<T> 
 *  2) 생성 → ResponseData<Long> (생성된 ID 반환)
 *  3) 수정/삭제 → Response (단순 성공/실패)
 */
public interface QnaAnswerService {

    /**
     * 답변 등록.
     * - 질문당 답변은 최대 1개까지만 허용
     * - 생성된 답변의 ID를 응답으로 반환
     * 
     * @param questionId 질문 ID
     * @param request 답변 등록 요청 데이터
     * @param memberId 답변 작성자(관리자) ID
     * @param adminName 답변 작성자(관리자) 이름
     * @return 생성된 답변의 ID 또는 에러 응답
     */
    ResponseData<Long> create(Long questionId, RequestAnswerCreate request, 
                             Long memberId, String adminName);

    /**
     * 질문에 대한 답변 조회.
     * 
     * @param questionId 질문 ID
     * @param hasSecretAccess 비밀 답변 접근 권한 여부
     * @return 답변 정보 또는 에러 응답
     */
    ResponseData<ResponseAnswer> getByQuestionId(Long questionId, boolean hasSecretAccess);

    /**
     * 답변 수정.
     * - 기존 답변을 찾아 요청 데이터로 업데이트
     * 
     * @param answerId 수정할 답변 ID
     * @param request 답변 수정 요청 데이터
     * @return 수정 성공/실패 응답
     */
    Response update(Long answerId, RequestAnswerUpdate request);

    /**
     * 답변 삭제.
     * - ID로 답변을 찾아 삭제 처리
     * 
     * @param answerId 삭제할 답변 ID
     * @return 삭제 성공/실패 응답
     */
    Response delete(Long answerId);
}