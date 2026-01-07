package com.academy.api.qna.service;

import com.academy.api.auth.jwt.JwtProvider;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.qna.domain.QnaAnswer;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.dto.*;
import com.academy.api.qna.mapper.QnaMapper;
import com.academy.api.qna.repository.QnaAnswerRepository;
import com.academy.api.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * QnA 서비스 구현체.
 * 
 * - QnA CRUD 비즈니스 로직 처리
 * - 비밀글 접근 제어 및 비밀번호 검증
 * - 답변 생성/수정/삭제 관리
 * - 관리자/공개 API 분리 처리
 * 
 * 보안 정책:
 * - 비밀글은 비밀번호 검증 후 접근 가능
 * - 답변은 관리자만 등록/수정/삭제 가능
 * - 질문 수정/삭제는 본인 비밀번호 확인 필수
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaServiceImpl implements QnaService {

    private final QnaQuestionRepository questionRepository;
    private final QnaAnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final QnaMapper qnaMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final QnaRateLimitService rateLimitService;

    // ========== Public API ==========

    /**
     * 공개 질문 목록 조회.
     * 비밀글은 제목만 표시하고 내용은 숨김.
     */
    @Override
    public ResponseList<ResponseQnaQuestionListItem> getPublicQuestionList(Boolean isAnswered, String searchType, String keyword, Pageable pageable) {
        log.info("[QnaService] 공개 질문 목록 조회 시작. isAnswered={}, searchType={}, keyword={}", 
                isAnswered, searchType, keyword);
        
        Page<QnaQuestion> questionPage = questionRepository.searchQuestionsForPublic(
                isAnswered, searchType, keyword, pageable);
        
        log.debug("[QnaService] 공개 질문 목록 조회 완료. 총 {}개, 현재 페이지 {}개", 
                questionPage.getTotalElements(), questionPage.getNumberOfElements());
        
        return qnaMapper.toListItemResponseList(questionPage);
    }

    /**
     * 공개 질문 상세 조회.
     * 비밀글의 경우 viewToken 검증 필요.
     */
    @Override
    @Transactional
    public ResponseData<ResponseQnaQuestionDetail> getPublicQuestion(Long id, String viewToken) {
        log.info("[QnaService] 공개 질문 상세 조회 시작. id={}, hasToken={}", id, viewToken != null);
        
        QnaQuestion question = questionRepository.findByIdWithAnswer(id)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. id={}", id);
                    return null;
                });

        if (question == null) {
            return ResponseData.error("Q404", "질문을 찾을 수 없습니다.");
        }

        // 비밀글 접근 권한 확인
        if (question.isSecret() && !validateViewToken(question, viewToken)) {
            log.warn("[QnaService] 비밀글 접근 권한 없음. id={}", id);
            return ResponseData.error("Q403", "비밀글 접근 권한이 없습니다.");
        }

        // 조회수 증가 (비동기 처리)
        questionRepository.incrementViewCount(id);
        log.debug("[QnaService] 조회수 증가. id={}", id);

        // 네비게이션 정보 조회
        ResponseQnaNavigation navigation = getQnaNavigation(id);

        ResponseQnaQuestionDetail response = qnaMapper.toDetailResponse(question, navigation);
        
        log.debug("[QnaService] 공개 질문 상세 조회 완료. id={}, title={}", id, question.getTitle());
        return ResponseData.ok("0000", "조회가 완료되었습니다.", response);
    }

    /**
     * 질문 등록.
     */
    @Override
    @Transactional
    public ResponseData<ResponseQnaQuestionCreate> createQuestion(RequestQnaQuestionCreate request, String clientIp) {
        log.info("[QnaService] 질문 생성 시작. authorName={}, title={}, secret={}", 
                request.getAuthorName(), request.getTitle(), request.getSecret());
        
        try {
            // 엔티티 변환 (비밀번호 해시화 포함)
            QnaQuestion question = qnaMapper.toEntity(request, clientIp);
            
            // 질문 저장
            QnaQuestion savedQuestion = questionRepository.save(question);
            
            log.info("[QnaService] 질문 생성 완료. id={}, authorName={}", 
                    savedQuestion.getId(), savedQuestion.getAuthorName());
            
            // 응답 생성
            ResponseQnaQuestionCreate response = qnaMapper.toCreateResponse(savedQuestion);
            return ResponseData.ok("0000", "질문이 등록되었습니다.", response);
            
        } catch (Exception e) {
            log.error("[QnaService] 질문 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("Q500", "질문 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀글 비밀번호 검증 및 토큰 발급.
     */
    @Override
    public ResponseData<ResponseQnaPasswordVerify> verifyPassword(Long id, RequestQnaPasswordVerify request, String clientIp) {
        log.info("[QnaService] 비밀번호 검증 시작. id={}, clientIp={}", id, clientIp);
        
        // Rate Limiting 확인
        if (!rateLimitService.isAttemptAllowed(clientIp)) {
            long lockoutMinutes = rateLimitService.getLockoutMinutesRemaining(clientIp);
            log.warn("[QnaService] Rate Limit 초과. IP={}, lockoutMinutes={}", clientIp, lockoutMinutes);
            
            ResponseQnaPasswordVerify response = ResponseQnaPasswordVerify.builder()
                    .verified(false)
                    .build();
            return ResponseData.error("Q429", 
                    String.format("너무 많은 시도로 인해 %d분 후에 다시 시도해주세요.", lockoutMinutes));
        }
        
        QnaQuestion question = questionRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. id={}", id);
                    return null;
                });

        if (question == null) {
            return ResponseData.error("Q404", "질문을 찾을 수 없습니다.");
        }

        if (!question.isSecret()) {
            log.warn("[QnaService] 비밀글이 아님. id={}", id);
            return ResponseData.error("Q400", "비밀글이 아닙니다.");
        }

        // 비밀번호 검증
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), question.getPasswordHash());
        
        if (!passwordMatches) {
            // 실패 시도 기록
            rateLimitService.recordFailedAttempt(clientIp);
            
            int remainingAttempts = rateLimitService.getRemainingAttempts(clientIp);
            log.warn("[QnaService] 비밀번호 불일치. id={}, remainingAttempts={}", id, remainingAttempts);
            
            ResponseQnaPasswordVerify response = ResponseQnaPasswordVerify.builder()
                    .verified(false)
                    .build();
            
            String errorMessage = remainingAttempts > 0 ? 
                    String.format("비밀번호가 일치하지 않습니다. (남은 시도: %d회)", remainingAttempts) :
                    "너무 많은 시도로 인해 1시간 동안 차단되었습니다.";
                    
            return ResponseData.ok("0000", errorMessage, response);
        }
        
        // 성공 시도 기록 (시도 횟수 초기화)
        rateLimitService.recordSuccessfulAttempt(clientIp);

        // JWT 토큰 생성 (JwtProvider 활용)
        String viewToken = jwtProvider.createQnaViewToken(question.getId(), question.getAuthorName(), 10);
        
        ResponseQnaPasswordVerify response = ResponseQnaPasswordVerify.builder()
                .verified(true)
                .viewToken(viewToken)
                .expiresInSec(600) // 10분
                .build();
        
        log.info("[QnaService] 비밀번호 검증 성공. id={}", id);
        return ResponseData.ok("0000", "비밀번호가 확인되었습니다.", response);
    }

    /**
     * 질문 수정.
     */
    @Override
    @Transactional
    public Response updateQuestion(Long id, RequestQnaQuestionUpdate request) {
        log.info("[QnaService] 질문 수정 시작. id={}, title={}", id, request.getTitle());
        
        QnaQuestion question = questionRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. id={}", id);
                    return null;
                });

        if (question == null) {
            return Response.error("Q404", "질문을 찾을 수 없습니다.");
        }

        // 본인 확인 (비밀번호 검증)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), question.getPasswordHash());
        if (!passwordMatches) {
            log.warn("[QnaService] 수정 권한 없음. id={}", id);
            return Response.error("Q403", "수정 권한이 없습니다.");
        }

        // 답변 완료된 질문 수정 제한 (선택적 정책)
        if (question.isAnswered()) {
            log.warn("[QnaService] 답변 완료된 질문 수정 시도. id={}", id);
            return Response.error("Q400", "답변이 완료된 질문은 수정할 수 없습니다.");
        }

        try {
            // 질문 정보 업데이트
            question.update(
                    request.getTitle(),
                    request.getContent(),
                    request.getSecret() != null ? (request.getSecret() == 1) : false
            );

            questionRepository.save(question);
            
            log.info("[QnaService] 질문 수정 완료. id={}", id);
            return Response.ok("0000", "질문이 수정되었습니다.");
            
        } catch (Exception e) {
            log.error("[QnaService] 질문 수정 실패: {}", e.getMessage(), e);
            return Response.error("Q500", "질문 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 질문 삭제.
     */
    @Override
    @Transactional
    public Response deleteQuestion(Long id, RequestQnaQuestionDelete request) {
        log.info("[QnaService] 질문 삭제 시작. id={}", id);
        
        QnaQuestion question = questionRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. id={}", id);
                    return null;
                });

        if (question == null) {
            return Response.error("Q404", "질문을 찾을 수 없습니다.");
        }

        // 본인 확인 (비밀번호 검증)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), question.getPasswordHash());
        if (!passwordMatches) {
            log.warn("[QnaService] 삭제 권한 없음. id={}", id);
            return Response.error("Q403", "삭제 권한이 없습니다.");
        }

        try {
            // 연관된 답변도 CASCADE로 자동 삭제됨
            questionRepository.delete(question);
            
            log.info("[QnaService] 질문 삭제 완료. id={}", id);
            return Response.ok("0000", "질문이 삭제되었습니다.");
            
        } catch (Exception e) {
            log.error("[QnaService] 질문 삭제 실패: {}", e.getMessage(), e);
            return Response.error("Q500", "질문 삭제 중 오류가 발생했습니다.");
        }
    }

    // ========== Admin API ==========

    /**
     * 관리자 질문 목록 조회.
     * 모든 질문에 접근 가능하며 비밀글도 제한 없이 조회.
     */
    @Override
    public ResponseList<ResponseQnaQuestionListItem> getAdminQuestionList(Boolean isAnswered, Boolean secret, String searchType, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("[QnaService] 관리자 질문 목록 조회 시작. isAnswered={}, secret={}, searchType={}, keyword={}, from={}, to={}", 
                isAnswered, secret, searchType, keyword, startDate, endDate);
        
        Page<QnaQuestion> questionPage = questionRepository.searchQuestionsForAdmin(
                isAnswered, secret, searchType, keyword, startDate, endDate, pageable);
        
        log.debug("[QnaService] 관리자 질문 목록 조회 완료. 총 {}개, 현재 페이지 {}개", 
                questionPage.getTotalElements(), questionPage.getNumberOfElements());
        
        return qnaMapper.toListItemResponseList(questionPage);
    }

    /**
     * 관리자 질문 상세 조회.
     * 비밀글도 토큰 없이 조회 가능.
     */
    @Override
    public ResponseData<ResponseQnaQuestionAdmin> getAdminQuestion(Long id) {
        log.info("[QnaService] 관리자 질문 상세 조회 시작. id={}", id);
        
        QnaQuestion question = questionRepository.findByIdWithAnswer(id)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. id={}", id);
                    return null;
                });

        if (question == null) {
            return ResponseData.error("Q404", "질문을 찾을 수 없습니다.");
        }

        // 네비게이션 정보 조회
        ResponseQnaNavigation navigation = getQnaNavigation(id);

        // 회원 이름 조회 (createdBy가 없으므로 일반 사용자)
        ResponseQnaQuestionAdmin response = qnaMapper.toAdminResponse(question, navigation);
        
        log.debug("[QnaService] 관리자 질문 상세 조회 완료. id={}, title={}", id, question.getTitle());
        return ResponseData.ok("0000", "조회가 완료되었습니다.", response);
    }

    /**
     * 답변 생성/수정 (Upsert).
     */
    @Override
    @Transactional
    public Response upsertAnswer(Long questionId, RequestQnaAnswerUpsert request) {
        log.info("[QnaService] 답변 Upsert 시작. questionId={}", questionId);
        
        QnaQuestion question = questionRepository.findById(questionId)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. questionId={}", questionId);
                    return null;
                });

        if (question == null) {
            return Response.error("Q404", "질문을 찾을 수 없습니다.");
        }

        Long currentAdminId = getCurrentAdminId();
        if (currentAdminId == null) {
            log.warn("[QnaService] 관리자 인증 실패. questionId={}", questionId);
            return Response.error("A403", "관리자 권한이 필요합니다.");
        }

        try {
            QnaAnswer existingAnswer = answerRepository.findByQuestionId(questionId).orElse(null);
            
            if (existingAnswer != null) {
                // 기존 답변 수정
                existingAnswer.update(request.getContent(), currentAdminId);
                answerRepository.save(existingAnswer);
                log.info("[QnaService] 답변 수정 완료. questionId={}, answerId={}", questionId, existingAnswer.getId());
            } else {
                // 새 답변 생성
                QnaAnswer newAnswer = qnaMapper.toAnswerEntity(request, question, currentAdminId);
                answerRepository.save(newAnswer);
                log.info("[QnaService] 답변 생성 완료. questionId={}, answerId={}", questionId, newAnswer.getId());
            }

            // 질문의 답변 완료 상태 업데이트
            question.markAsAnswered();
            questionRepository.save(question);
            
            log.info("[QnaService] 답변 Upsert 완료. questionId={}", questionId);
            return Response.ok("0000", "답변이 저장되었습니다.");
            
        } catch (Exception e) {
            log.error("[QnaService] 답변 Upsert 실패: {}", e.getMessage(), e);
            return Response.error("Q500", "답변 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 답변 삭제.
     */
    @Override
    @Transactional
    public Response deleteAnswer(Long questionId) {
        log.info("[QnaService] 답변 삭제 시작. questionId={}", questionId);
        
        QnaQuestion question = questionRepository.findById(questionId)
                .orElseGet(() -> {
                    log.warn("[QnaService] 질문을 찾을 수 없음. questionId={}", questionId);
                    return null;
                });

        if (question == null) {
            return Response.error("Q404", "질문을 찾을 수 없습니다.");
        }

        QnaAnswer answer = answerRepository.findByQuestionId(questionId)
                .orElseGet(() -> {
                    log.warn("[QnaService] 답변을 찾을 수 없음. questionId={}", questionId);
                    return null;
                });

        if (answer == null) {
            return Response.error("A404", "답변을 찾을 수 없습니다.");
        }

        try {
            // 답변 삭제
            answerRepository.delete(answer);
            
            // 질문의 답변 완료 상태 해제
            question.markAsUnanswered();
            questionRepository.save(question);
            
            log.info("[QnaService] 답변 삭제 완료. questionId={}, answerId={}", questionId, answer.getId());
            return Response.ok("0000", "답변이 삭제되었습니다.");
            
        } catch (Exception e) {
            log.error("[QnaService] 답변 삭제 실패: {}", e.getMessage(), e);
            return Response.error("Q500", "답변 삭제 중 오류가 발생했습니다.");
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * 비밀글 열람 토큰 검증.
     */
    private boolean validateViewToken(QnaQuestion question, String viewToken) {
        if (viewToken == null || viewToken.trim().isEmpty()) {
            return false;
        }
        
        return jwtProvider.validateQnaViewToken(viewToken, question.getId());
    }


    /**
     * 현재 로그인한 관리자 ID 조회.
     */
    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            String username = authentication.getName();
            return memberRepository.findByUsername(username)
                    .map(Member::getId)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("[QnaService] 관리자 ID 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * QnA 네비게이션 정보 조회 (이전글/다음글).
     * 
     * @param currentId 현재 질문 ID
     * @return 네비게이션 정보
     */
    private ResponseQnaNavigation getQnaNavigation(Long currentId) {
        log.debug("[QnaService] 네비게이션 정보 조회 시작. currentId={}", currentId);
        
        // 이전글 조회
        QnaQuestion previousQuestion = questionRepository.findPreviousQuestion(currentId);
        ResponseQnaNavigation.NavigationItem previous = null;
        if (previousQuestion != null) {
            previous = ResponseQnaNavigation.NavigationItem.builder()
                    .id(previousQuestion.getId())
                    .title(previousQuestion.getTitle())
                    .createdAt(previousQuestion.getCreatedAt())
                    .build();
            log.debug("[QnaService] 이전글 조회 완료. previousId={}, title={}", 
                    previousQuestion.getId(), previousQuestion.getTitle());
        }
        
        // 다음글 조회
        QnaQuestion nextQuestion = questionRepository.findNextQuestion(currentId);
        ResponseQnaNavigation.NavigationItem next = null;
        if (nextQuestion != null) {
            next = ResponseQnaNavigation.NavigationItem.builder()
                    .id(nextQuestion.getId())
                    .title(nextQuestion.getTitle())
                    .createdAt(nextQuestion.getCreatedAt())
                    .build();
            log.debug("[QnaService] 다음글 조회 완료. nextId={}, title={}", 
                    nextQuestion.getId(), nextQuestion.getTitle());
        }
        
        ResponseQnaNavigation navigation = ResponseQnaNavigation.of(previous, next);
        log.debug("[QnaService] 네비게이션 정보 조회 완료. hasPrevious={}, hasNext={}", 
                previous != null, next != null);
        
        return navigation;
    }

    /**
     * 관리자 질문 삭제.
     * 
     * @param id 삭제할 질문 ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public Response deleteQuestionByAdmin(Long id) {
        log.info("[QnaService] 관리자 질문 삭제 요청. questionId={}", id);
        
        // 질문 존재 여부 확인
        QnaQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[QnaService] 삭제할 질문을 찾을 수 없음. questionId={}", id);
                    return new BusinessException(ErrorCode.QNA_QUESTION_NOT_FOUND);
                });
                
        log.debug("[QnaService] 삭제 대상 질문 확인 완료. title={}, hasAnswer={}", 
                question.getTitle(), question.getIsAnswered());
        
        // 질문 삭제 (연관된 답변은 CASCADE DELETE로 자동 삭제)
        questionRepository.delete(question);
        
        log.info("[QnaService] 관리자 질문 삭제 완료. questionId={}", id);
        return Response.ok("0000", "질문이 삭제되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseQnaStatistics> getQnaStatistics() {
        log.info("[QnaService] QnA 통계 조회 시작");
        
        try {
            // 통계 데이터 조회
            Long totalCount = questionRepository.countTotalQuestions();
            Long answeredCount = questionRepository.countAnsweredQuestions();
            
            log.debug("[QnaService] 통계 조회 완료. total={}, answered={}, unanswered={}", 
                    totalCount, answeredCount, (totalCount - answeredCount));
            
            // 통계 응답 생성
            ResponseQnaStatistics statistics = ResponseQnaStatistics.of(totalCount, answeredCount);
            
            log.info("[QnaService] QnA 통계 조회 완료. total={}", totalCount);
            return ResponseData.ok("0000", "통계 조회가 완료되었습니다.", statistics);
            
        } catch (Exception e) {
            log.error("[QnaService] QnA 통계 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
}