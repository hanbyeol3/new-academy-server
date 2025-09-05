package com.academy.api.qna.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import com.academy.api.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QnA 질문 서비스 구현체.
 * 
 * QnaQuestionService 인터페이스의 구현체로 기본적인 CRUD 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaQuestionServiceImpl implements QnaQuestionService {

    private final QnaQuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 질문 목록 조회.
     */
    @Override
    public ResponseList<ResponseQuestion.Projection> getQuestions(Pageable pageable) {
        try {
            Page<QnaQuestion> questionPage = questionRepository.findAll(pageable);
            
            List<ResponseQuestion.Projection> projections = questionPage.getContent().stream()
                    .map(ResponseQuestion.Projection::fromEntity)
                    .collect(Collectors.toList());

            return ResponseList.from(questionPage.map(q -> ResponseQuestion.Projection.fromEntity(q)));
            
        } catch (Exception e) {
            log.error("질문 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseList.error("SYSTEM_ERROR", "질문 목록 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 상세 조회.
     */
    @Override
    @Transactional
    public ResponseData<ResponseQuestion> getQuestion(Long questionId) {
        try {
            QnaQuestion question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 조회수 증가
            question.incrementViewCount();
            questionRepository.save(question);

            ResponseQuestion response = ResponseQuestion.fromPublic(question);
            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("질문 조회 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 생성.
     */
    @Override
    @Transactional
    public ResponseData<ResponseQuestion> createQuestion(RequestQuestionCreate request, String clientIp) {
        try {
            // 개인정보 수집 동의 확인
            if (!request.isPrivacyConsentGiven()) {
                return ResponseData.error("INVALID_PARAMETER", "개인정보 수집 동의가 필요합니다");
            }

            // 비밀번호 해싱
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            // 질문 엔티티 생성
            QnaQuestion question = QnaQuestion.builder()
                    .authorName(request.getAuthorName())
                    .phoneNumber(request.getPhoneNumber())
                    .passwordHash(hashedPassword)
                    .title(request.getTitle())
                    .content(request.getContent())
                    .secret(request.isSecret())
                    .privacyConsent(request.isPrivacyConsentGiven())
                    .ipAddress(clientIp)
                    .build();

            QnaQuestion savedQuestion = questionRepository.save(question);
            ResponseQuestion response = ResponseQuestion.fromAdmin(savedQuestion);

            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("질문 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 생성 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 수정.
     */
    @Override
    @Transactional
    public ResponseData<ResponseQuestion> updateQuestion(Long questionId, RequestQuestionUpdate request) {
        try {
            QnaQuestion question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), question.getPasswordHash())) {
                return ResponseData.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 수정 제한
            if (question.isAnswered()) {
                return ResponseData.error("INVALID_STATE", "답변이 등록된 질문은 수정할 수 없습니다");
            }

            // 수정할 내용이 있는지 확인
            if (!request.hasUpdates()) {
                return ResponseData.error("INVALID_PARAMETER", "수정할 내용을 입력해주세요");
            }

            // 질문 내용 업데이트
            question.update(request.getTitle(), request.getContent(), request.getPhoneNumber(), request.isSecret());
            QnaQuestion updatedQuestion = questionRepository.save(question);
            ResponseQuestion response = ResponseQuestion.fromAdmin(updatedQuestion);

            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("질문 수정 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 삭제.
     */
    @Override
    @Transactional
    public ResponseData<Void> deleteQuestion(Long questionId, String password) {
        try {
            QnaQuestion question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, question.getPasswordHash())) {
                return ResponseData.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 삭제 제한
            if (question.isAnswered()) {
                return ResponseData.error("INVALID_STATE", "답변이 등록된 질문은 삭제할 수 없습니다");
            }

            questionRepository.delete(question);
            return ResponseData.ok(null);
            
        } catch (Exception e) {
            log.error("질문 삭제 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 삭제 중 오류가 발생했습니다");
        }
    }

    /**
     * 통계 조회.
     */
    @Override
    public ResponseData<ResponseQuestion.Summary> getStatistics() {
        try {
            Long total = questionRepository.countTotalQuestions();
            Long answered = questionRepository.countAnsweredQuestions();
            Long secret = questionRepository.countSecretQuestions();
            Long pinned = questionRepository.countPinnedQuestions();

            ResponseQuestion.Summary summary = ResponseQuestion.Summary.create(
                total != null ? total : 0L,
                answered != null ? answered : 0L,
                secret != null ? secret : 0L,
                pinned != null ? pinned : 0L
            );

            return ResponseData.ok(summary);
            
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "통계 조회 중 오류가 발생했습니다");
        }
    }
}