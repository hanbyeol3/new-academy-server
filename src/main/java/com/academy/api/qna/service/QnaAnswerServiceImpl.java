package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.qna.domain.QnaAnswer;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.RequestAnswerCreate;
import com.academy.api.qna.model.RequestAnswerUpdate;
import com.academy.api.qna.model.ResponseAnswer;
import com.academy.api.qna.repository.QnaAnswerRepository;
import com.academy.api.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * QnA 답변 서비스 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaAnswerServiceImpl implements QnaAnswerService {

    private final QnaAnswerRepository answerRepository;
    private final QnaQuestionRepository questionRepository;

    /**
     * 답변 등록.
     */
    @Override
    @Transactional
    public ResponseData<Long> create(Long questionId, RequestAnswerCreate request, 
                                   Long memberId, String adminName) {
        try {
            // 질문 존재 여부 확인
            QnaQuestion question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 이미 답변이 존재하는지 확인
            if (answerRepository.existsByQuestionId(questionId)) {
                return ResponseData.error("ALREADY_ANSWERED", "이미 답변이 등록된 질문입니다");
            }

            // 답변 엔티티 생성
            QnaAnswer answer = QnaAnswer.create(
                questionId,
                adminName,
                request.getContent(),
                request.getSecret(),
                request.getPublished()
            );

            // 답변 저장
            QnaAnswer savedAnswer = answerRepository.save(answer);

            // 질문의 답변 상태 업데이트
            question.markAsAnswered(LocalDateTime.now());
            questionRepository.save(question);

            log.info("답변 등록 성공: questionId={}, answerId={}, adminName={}", 
                    questionId, savedAnswer.getId(), adminName);

            return ResponseData.ok(savedAnswer.getId());

        } catch (Exception e) {
            log.error("답변 등록 실패: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "답변 등록 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문에 대한 답변 조회.
     */
    @Override
    public ResponseData<ResponseAnswer> getByQuestionId(Long questionId, boolean hasSecretAccess) {
        try {
            QnaAnswer answer = answerRepository.findByQuestionId(questionId).orElse(null);
            if (answer == null) {
                return ResponseData.error("NOT_FOUND", "답변을 찾을 수 없습니다");
            }

            // 게시되지 않은 답변은 관리자만 조회 가능
            if (!answer.isPublished() && !hasSecretAccess) {
                return ResponseData.error("NOT_FOUND", "답변을 찾을 수 없습니다");
            }

            ResponseAnswer response = ResponseAnswer.from(answer, hasSecretAccess);
            return ResponseData.ok(response);

        } catch (Exception e) {
            log.error("답변 조회 실패: questionId={}, error={}", questionId, e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "답변 조회 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional
    public Response update(Long answerId, RequestAnswerUpdate request) {
        try {
            QnaAnswer answer = answerRepository.findById(answerId).orElse(null);
            if (answer == null) {
                return Response.error("NOT_FOUND", "답변을 찾을 수 없습니다");
            }

            // 수정할 내용이 있는지 확인
            if (!request.hasUpdates()) {
                return Response.error("INVALID_PARAMETER", "수정할 내용을 입력해주세요");
            }

            // TODO: 도메인 클래스에 업데이트 메서드 추가 필요
            answerRepository.save(answer);
            return Response.ok();

        } catch (Exception e) {
            log.error("답변 수정 실패: answerId={}, error={}", answerId, e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "답변 수정 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional
    public Response delete(Long answerId) {
        try {
            QnaAnswer answer = answerRepository.findById(answerId).orElse(null);
            if (answer == null) {
                return Response.error("NOT_FOUND", "답변을 찾을 수 없습니다");
            }

            Long questionId = answer.getQuestionId();
            answerRepository.delete(answer);

            // 질문의 답변 상태 리셋
            QnaQuestion question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                // TODO: 도메인 클래스에 markAsUnanswered 메서드 추가 필요
                questionRepository.save(question);
            }

            return Response.ok();

        } catch (Exception e) {
            log.error("답변 삭제 실패: answerId={}, error={}", answerId, e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "답변 삭제 중 오류가 발생했습니다");
        }
    }
}