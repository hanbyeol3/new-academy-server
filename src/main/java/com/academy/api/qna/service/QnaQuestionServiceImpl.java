package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import com.academy.api.qna.repository.QnaQuestionRepository;
import com.academy.api.qna.repository.QnaAnswerRepository;
import com.academy.api.qna.domain.QnaAnswer;
import com.academy.api.qna.model.ResponseAnswer;
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
    private final QnaAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 질문 목록 조회.
     */
    @Override
    public ResponseList<ResponseQuestion> list(ResponseQuestion.Criteria cond, Pageable pageable) {
        try {
            Page<QnaQuestion> questionPage = questionRepository.findAll(pageable);
            
            return ResponseList.from(questionPage.map(q -> ResponseQuestion.fromPublic(q, null)));
            
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
    public ResponseData<ResponseQuestion> get(Long id) {
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 조회수 증가
            question.incrementViewCount();
            questionRepository.save(question);

            // 답변 정보 조회
            ResponseAnswer answer = null;
            if (question.isAnswered()) {
                QnaAnswer answerEntity = answerRepository.findByQuestionId(id).orElse(null);
                if (answerEntity != null) {
                    answer = ResponseAnswer.fromPublic(answerEntity);
                }
            }

            ResponseQuestion response = ResponseQuestion.fromPublic(question, answer);
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
    public ResponseData<Long> create(RequestQuestionCreate request, String clientIp) {
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
            
            return ResponseData.ok(savedQuestion.getId());
            
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
    public Response update(Long id, RequestQuestionUpdate request) {
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                return Response.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), question.getPasswordHash())) {
                return Response.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 수정 제한
            if (question.isAnswered()) {
                return Response.error("INVALID_STATE", "답변이 등록된 질문은 수정할 수 없습니다");
            }

            // 수정할 내용이 있는지 확인
            if (!request.hasUpdates()) {
                return Response.error("INVALID_PARAMETER", "수정할 내용을 입력해주세요");
            }

            // 질문 내용 업데이트
            question.update(request.getTitle(), request.getContent(), request.getPhoneNumber(), request.isSecret());
            questionRepository.save(question);

            return Response.ok();
            
        } catch (Exception e) {
            log.error("질문 수정 실패: {}", e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "질문 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 삭제.
     */
    @Override
    @Transactional
    public Response delete(Long id, String password) {
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                return Response.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, question.getPasswordHash())) {
                return Response.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 삭제 제한
            if (question.isAnswered()) {
                return Response.error("INVALID_STATE", "답변이 등록된 질문은 삭제할 수 없습니다");
            }

            questionRepository.delete(question);
            return Response.ok();
            
        } catch (Exception e) {
            log.error("질문 삭제 실패: {}", e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "질문 삭제 중 오류가 발생했습니다");
        }
    }


    /**
     * 회원 ID가 포함된 질문 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createWithMemberId(RequestQuestionCreate request, Long memberId, String clientIp) {
        try {
            // 개인정보 수집 동의 확인
            if (!request.isPrivacyConsentGiven()) {
                return ResponseData.error("INVALID_PARAMETER", "개인정보 수집 동의가 필요합니다");
            }

            // 비밀번호 해싱
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            // 질문 엔티티 생성 (memberId 포함)
            QnaQuestion question = QnaQuestion.builder()
                    .memberId(memberId)  // 로그인 사용자의 ID 설정
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
            
            return ResponseData.ok(savedQuestion.getId());
            
        } catch (Exception e) {
            log.error("회원 질문 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 생성 중 오류가 발생했습니다");
        }
    }
}