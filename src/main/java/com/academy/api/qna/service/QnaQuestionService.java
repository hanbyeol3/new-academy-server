package com.academy.api.qna.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import com.academy.api.qna.repository.QnaQuestionQueryRepository;
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

/**
 * QnA 질문 서비스 구현체.
 * 
 * - QnA 질문 CRUD 비즈니스 로직 처리
 * - QueryDSL 기반 동적 검색 지원
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaQuestionService {

    private final QnaQuestionRepository questionRepository;
    private final QnaQuestionQueryRepository questionQueryRepository;
    private final QnaAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;

    /** 
     * 질문 목록 조회 및 페이지네이션 처리.
     * - 검색 조건에 따른 동적 쿼리 생성
     * - 성능 최적화를 위한 인덱스 활용 쿼리
     */
    public ResponseList<ResponseQuestion> list(ResponseQuestion.Criteria cond, Pageable pageable) {
        // 입력 파라미터 로깅: 요청 내용 추적 및 디버깅 용도
        log.info("[QnaQuestionService] 목록 조회 시작. 조건={}, 페이지네이션={}", cond, pageable);
        
        try {
            // 리포지토리 계층에서 페이지 결과 조회
            Page<ResponseQuestion> page = questionQueryRepository.search(cond, pageable);
            
            // Spring Data Page를 ResponseList로 변환: API 일관성 유지
            ResponseList<ResponseQuestion> result = ResponseList.from(page);
            
            // 처리 결과 요약 로깅: 성능 모니터링 및 결과 검증
            log.debug("[QnaQuestionService] 목록 조회 완료. 전체={}건, 현재페이지={}, 페이지크기={}, 실제반환={}건", 
                    result.getTotal(), result.getPage(), result.getSize(), result.getItems().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 질문 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseList.error("SYSTEM_ERROR", "질문 목록 조회 중 오류가 발생했습니다");
        }
    }

    /** 
     * 질문 단건 조회 및 조회수 증가 처리.
     * - 조회와 동시에 조회수 자동 증가 (비즈니스 로직)
     * - 엔티티 조회 실패 시 의미 있는 에러 메시지 반환
     */
    @Transactional  // 조회수 증가 업데이트를 위한 쓰기 트랜잭션 필요
    public ResponseData<ResponseQuestion> get(Long id) {
        // 입력 파라미터 로깅
        log.info("[QnaQuestionService] 단건 조회 시작. ID={}", id);
        
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                log.warn("[QnaQuestionService] 질문을 찾을 수 없음. ID={}", id);
                return ResponseData.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 조회수 증가: 비즈니스 로직 처리
            question.incrementViewCount();
            questionRepository.save(question);
            
            // 조회수 증가 완료 로깅
            log.debug("[QnaQuestionService] 조회수 증가 완료. ID={}, 새조회수={}", id, question.getViewCount());

            // 답변 정보 조회
            ResponseAnswer answer = null;
            if (question.isAnswered()) {
                QnaAnswer answerEntity = answerRepository.findByQuestionId(id).orElse(null);
                if (answerEntity != null) {
                    answer = ResponseAnswer.fromPublic(answerEntity);
                    log.debug("[QnaQuestionService] 답변 정보 조회 완료. 답변ID={}", answerEntity.getId());
                }
            }

            // ResponseQuestion 생성 (공개용)
            ResponseQuestion response = ResponseQuestion.fromPublic(question, answer);
            log.debug("[QnaQuestionService] 단건 조회 완료. ID={}", id);
            
            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 질문 조회 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 새로운 질문 생성.
     * - 입력 데이터 검증 후 질문 엔티티 생성
     * - 생성된 질문의 ID를 응답으로 반환
     */
    @Transactional
    public ResponseData<Long> create(RequestQuestionCreate request, String clientIp) {
        log.info("[QnaQuestionService] 질문 생성 시작. 제목={}", request.getTitle());
        
        try {
            // 개인정보 수집 동의 확인
            if (!request.isPrivacyConsentGiven()) {
                log.warn("[QnaQuestionService] 개인정보 수집 동의 없음");
                return ResponseData.error("INVALID_PARAMETER", "개인정보 수집 동의가 필요합니다");
            }

            // 비밀번호 해싱
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            log.debug("[QnaQuestionService] 비밀번호 해싱 완료");

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
            log.info("[QnaQuestionService] 질문 생성 완료. ID={}", savedQuestion.getId());
            
            return ResponseData.ok(savedQuestion.getId());
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 질문 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 생성 중 오류가 발생했습니다");
        }
    }

    /**
     * 기존 질문 수정.
     * - ID로 질문을 찾아 요청 데이터로 업데이트
     * - 비밀번호 검증 필요
     */
    @Transactional
    public Response update(Long id, RequestQuestionUpdate request) {
        log.info("[QnaQuestionService] 질문 수정 시작. ID={}", id);
        
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                log.warn("[QnaQuestionService] 수정할 질문을 찾을 수 없음. ID={}", id);
                return Response.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), question.getPasswordHash())) {
                log.warn("[QnaQuestionService] 비밀번호 불일치. ID={}", id);
                return Response.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 수정 제한
            if (question.isAnswered()) {
                log.warn("[QnaQuestionService] 답변 등록된 질문 수정 시도. ID={}", id);
                return Response.error("INVALID_STATE", "답변이 등록된 질문은 수정할 수 없습니다");
            }

            // 수정할 내용이 있는지 확인
            if (!request.hasUpdates()) {
                log.warn("[QnaQuestionService] 수정할 내용 없음. ID={}", id);
                return Response.error("INVALID_PARAMETER", "수정할 내용을 입력해주세요");
            }

            // 질문 내용 업데이트
            question.update(request.getTitle(), request.getContent(), request.getPhoneNumber(), request.isSecret());
            questionRepository.save(question);
            
            log.info("[QnaQuestionService] 질문 수정 완료. ID={}", id);
            return Response.ok();
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 질문 수정 실패: {}", e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "질문 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 질문 삭제.
     * - ID와 비밀번호로 질문을 찾아 삭제 처리
     * - 답변이 등록된 질문은 삭제 제한
     */
    @Transactional
    public Response delete(Long id, String password) {
        log.info("[QnaQuestionService] 질문 삭제 시작. ID={}", id);
        
        try {
            QnaQuestion question = questionRepository.findById(id).orElse(null);
            if (question == null) {
                log.warn("[QnaQuestionService] 삭제할 질문을 찾을 수 없음. ID={}", id);
                return Response.error("NOT_FOUND", "질문을 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, question.getPasswordHash())) {
                log.warn("[QnaQuestionService] 비밀번호 불일치. ID={}", id);
                return Response.error("UNAUTHORIZED", "비밀번호가 일치하지 않습니다");
            }

            // 답변이 등록된 질문은 삭제 제한
            if (question.isAnswered()) {
                log.warn("[QnaQuestionService] 답변 등록된 질문 삭제 시도. ID={}", id);
                return Response.error("INVALID_STATE", "답변이 등록된 질문은 삭제할 수 없습니다");
            }

            questionRepository.delete(question);
            log.info("[QnaQuestionService] 질문 삭제 완료. ID={}", id);
            
            return Response.ok();
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 질문 삭제 실패: {}", e.getMessage(), e);
            return Response.error("SYSTEM_ERROR", "질문 삭제 중 오류가 발생했습니다");
        }
    }

    /**
     * 회원 ID가 포함된 질문 생성.
     * - 로그인한 사용자가 질문을 작성할 때 사용
     * - member_id가 자동으로 설정됨
     */
    @Transactional
    public ResponseData<Long> createWithMemberId(RequestQuestionCreate request, Long memberId, String clientIp) {
        log.info("[QnaQuestionService] 회원 질문 생성 시작. 회원ID={}, 제목={}", memberId, request.getTitle());
        
        try {
            // 개인정보 수집 동의 확인
            if (!request.isPrivacyConsentGiven()) {
                log.warn("[QnaQuestionService] 개인정보 수집 동의 없음. 회원ID={}", memberId);
                return ResponseData.error("INVALID_PARAMETER", "개인정보 수집 동의가 필요합니다");
            }

            // 비밀번호 해싱
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            log.debug("[QnaQuestionService] 비밀번호 해싱 완료. 회원ID={}", memberId);

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
            log.info("[QnaQuestionService] 회원 질문 생성 완료. 회원ID={}, 질문ID={}", memberId, savedQuestion.getId());
            
            return ResponseData.ok(savedQuestion.getId());
            
        } catch (Exception e) {
            log.error("[QnaQuestionService] 회원 질문 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("SYSTEM_ERROR", "질문 생성 중 오류가 발생했습니다");
        }
    }
}