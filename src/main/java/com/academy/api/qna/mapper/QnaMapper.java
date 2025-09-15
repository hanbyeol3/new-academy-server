package com.academy.api.qna.mapper;

import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.ResponseAnswer;
import com.academy.api.qna.model.ResponseQuestion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QnA 도메인 Mapper.
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
public class QnaMapper {

    /**
     * QnaQuestion 엔티티를 공개 응답 DTO로 변환.
     * 일반 사용자용 (개인정보 최대 마스킹).
     */
    public ResponseQuestion toPublicResponse(QnaQuestion question) {
        return ResponseQuestion.fromPublic(question);
    }

    /**
     * QnaQuestion 엔티티를 공개 응답 DTO로 변환 (답변 포함).
     */
    public ResponseQuestion toPublicResponse(QnaQuestion question, ResponseAnswer answer) {
        return ResponseQuestion.fromPublic(question, answer);
    }

    /**
     * QnaQuestion 엔티티를 관리자 응답 DTO로 변환.
     * 관리자용 (모든 정보 노출).
     */
    public ResponseQuestion toAdminResponse(QnaQuestion question) {
        return ResponseQuestion.fromAdmin(question);
    }

    /**
     * QnaQuestion 엔티티를 관리자 응답 DTO로 변환 (답변 포함).
     */
    public ResponseQuestion toAdminResponse(QnaQuestion question, ResponseAnswer answer) {
        return ResponseQuestion.fromAdmin(question, answer);
    }

    /**
     * QnaQuestion 엔티티를 권한별 응답 DTO로 변환.
     * 
     * @param question 질문 엔티티
     * @param isAuthorOrAdmin 작성자 본인 또는 관리자 여부
     * @param hasSecretAccess 비밀글 접근 권한 여부
     * @param answer 답변 정보
     */
    public ResponseQuestion toResponse(QnaQuestion question, boolean isAuthorOrAdmin, 
                                     boolean hasSecretAccess, ResponseAnswer answer) {
        return ResponseQuestion.from(question, isAuthorOrAdmin, hasSecretAccess, answer);
    }

    /**
     * QnaQuestion 엔티티를 권한별 응답 DTO로 변환 (답변 없음).
     */
    public ResponseQuestion toResponse(QnaQuestion question, boolean isAuthorOrAdmin, 
                                     boolean hasSecretAccess) {
        return ResponseQuestion.from(question, isAuthorOrAdmin, hasSecretAccess);
    }

    /**
     * QnaQuestion 엔티티 목록을 응답 DTO 목록으로 변환.
     */
    public List<ResponseQuestion> toResponseList(List<QnaQuestion> questions, 
                                               boolean isAuthorOrAdmin, 
                                               boolean hasSecretAccess) {
        return ResponseQuestion.fromList(questions, isAuthorOrAdmin, hasSecretAccess);
    }

    /**
     * QnaQuestion 엔티티를 목록용 프로젝션으로 변환.
     */
    public ResponseQuestion.Projection toProjection(QnaQuestion question) {
        return ResponseQuestion.Projection.fromEntity(question);
    }

    /**
     * QnaQuestion 엔티티 목록을 프로젝션 목록으로 변환.
     */
    public List<ResponseQuestion.Projection> toProjectionList(List<QnaQuestion> questions) {
        return ResponseQuestion.Projection.fromEntityList(questions);
    }
}