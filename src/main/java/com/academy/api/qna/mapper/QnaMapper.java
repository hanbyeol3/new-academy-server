package com.academy.api.qna.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.domain.QnaAnswer;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * QnA Entity ↔ DTO 변환 매퍼.
 * 
 * QnA 질문과 답변의 모든 변환 로직을 담당합니다.
 * 비밀번호 해싱, IP 주소 변환, 보안 정책 적용 등 추가 비즈니스 로직도 포함합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QnaMapper {

    private final PasswordEncoder passwordEncoder;

    /**
     * 질문 생성 요청 DTO를 Entity로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param ipAddress 클라이언트 IP 주소
     * @return QnaQuestion Entity
     */
    public QnaQuestion toEntity(RequestQnaQuestionCreate request, String ipAddress) {
        return QnaQuestion.builder()
                .authorName(request.getAuthorName())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .title(request.getTitle())
                .content(request.getContent())
                .secret(request.getSecret() != null && request.getSecret() == 1)
                .privacyConsent(request.getPrivacyConsent() != null && request.getPrivacyConsent() == 1)
                .ipAddress(ipAddress)
                .build();
    }

    /**
     * 답변 생성 요청 DTO를 Entity로 변환.
     * 
     * @param questionId 질문 ID
     * @param request 답변 생성 요청 DTO
     * @param createdBy 답변 작성자 ID
     * @return QnaAnswer Entity
     */
    public QnaAnswer toAnswerEntity(Long questionId, RequestQnaAnswerUpsert request, Long createdBy) {
        return QnaAnswer.create(questionId, request.getContent(), createdBy);
    }

    /**
     * Entity를 질문 목록 응답 DTO로 변환 (Public용).
     */
    public ResponseQnaQuestionListItem toListItem(QnaQuestion entity) {
        return ResponseQnaQuestionListItem.from(entity);
    }

    /**
     * Entity를 질문 목록 응답 DTO로 변환 (Admin용).
     */
    public ResponseQnaQuestionListItem toListItemForAdmin(QnaQuestion entity) {
        return ResponseQnaQuestionListItem.fromForAdmin(entity);
    }

    /**
     * Entity를 질문 상세 응답 DTO로 변환 (Public용).
     */
    public ResponseQnaQuestionDetail toDetailResponse(QnaQuestion entity, QnaAnswer answer, ResponseQnaNavigation navigation) {
        ResponseQnaAnswer answerDto = answer != null ? ResponseQnaAnswer.from(answer) : null;
        return ResponseQnaQuestionDetail.from(entity, answerDto, navigation);
    }
    
    /**
     * Entity를 질문 상세 응답 DTO로 변환 (Public용 - 답변 작성자 이름 포함).
     */
    public ResponseQnaQuestionDetail toDetailResponse(QnaQuestion entity, QnaAnswer answer, ResponseQnaNavigation navigation, 
                                                      String answerCreatedByName) {
        ResponseQnaAnswer answerDto = null;
        if (answer != null) {
            answerDto = ResponseQnaAnswer.fromWithNames(answer, answerCreatedByName, null);
        }
        return ResponseQnaQuestionDetail.from(entity, answerDto, navigation);
    }

    /**
     * Entity를 질문 관리자용 상세 응답 DTO로 변환.
     */
    public ResponseQnaQuestionAdmin toAdminResponse(QnaQuestion entity, QnaAnswer answer, ResponseQnaNavigation navigation, String deletedByName) {
        ResponseQnaAnswer answerDto = answer != null ? ResponseQnaAnswer.from(answer) : null;
        return ResponseQnaQuestionAdmin.from(entity, answerDto, navigation, deletedByName);
    }
    
    /**
     * Entity를 질문 관리자용 상세 응답 DTO로 변환 (답변 작성자 이름 포함).
     */
    public ResponseQnaQuestionAdmin toAdminResponse(QnaQuestion entity, QnaAnswer answer, ResponseQnaNavigation navigation, 
                                                    String deletedByName, String answerCreatedByName) {
        ResponseQnaAnswer answerDto = null;
        if (answer != null) {
            answerDto = ResponseQnaAnswer.fromWithNames(answer, answerCreatedByName, null);
        }
        return ResponseQnaQuestionAdmin.from(entity, answerDto, navigation, deletedByName);
    }

    /**
     * Entity를 답변 응답 DTO로 변환.
     */
    public ResponseQnaAnswer toAnswerResponse(QnaAnswer entity) {
        return ResponseQnaAnswer.from(entity);
    }

    /**
     * Entity 목록을 목록 응답 DTO로 변환 (Public용).
     */
    public List<ResponseQnaQuestionListItem> toListItems(List<QnaQuestion> entities) {
        return ResponseQnaQuestionListItem.fromList(entities);
    }

    /**
     * Entity 목록을 목록 응답 DTO로 변환 (Admin용).
     */
    public List<ResponseQnaQuestionListItem> toListItemsForAdmin(List<QnaQuestion> entities) {
        return ResponseQnaQuestionListItem.fromListForAdmin(entities);
    }

    /**
     * Entity Page를 ResponseList로 변환 (Public용).
     */
    public ResponseList<ResponseQnaQuestionListItem> toListItemResponseList(Page<QnaQuestion> page) {
        List<ResponseQnaQuestionListItem> items = toListItems(page.getContent());
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * Entity Page를 ResponseList로 변환 (Admin용).
     */
    public ResponseList<ResponseQnaQuestionListItem> toListItemResponseListForAdmin(Page<QnaQuestion> page) {
        List<ResponseQnaQuestionListItem> items = toListItemsForAdmin(page.getContent());
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 질문 생성 응답 DTO 생성.
     */
    public ResponseQnaQuestionCreate toCreateResponse(QnaQuestion entity) {
        return ResponseQnaQuestionCreate.success(entity.getId(), entity.getCreatedAt());
    }

    /**
     * 비밀글 접근 거부 응답 생성.
     */
    public ResponseQnaQuestionDetail toAccessDeniedResponse(QnaQuestion entity, ResponseQnaNavigation navigation) {
        return ResponseQnaQuestionDetail.createAccessDenied(entity, navigation);
    }

    /**
     * 비밀번호 검증.
     * 
     * @param rawPassword 입력된 비밀번호
     * @param encodedPassword 저장된 해시 비밀번호
     * @return 검증 결과
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.warn("[QnaMapper] 비밀번호 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }


    /**
     * RequestQnaAnswerUpsert를 QnaAnswer Entity로 변환.
     */
    public QnaAnswer toAnswerEntity(RequestQnaAnswerUpsert request, QnaQuestion question, Long createdBy) {
        return QnaAnswer.builder()
                .questionId(question.getId())
                .content(request.getContent())
                .createdBy(createdBy)
                .build();
    }
}