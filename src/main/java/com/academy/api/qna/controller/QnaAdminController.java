package com.academy.api.qna.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.dto.*;
import com.academy.api.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * QnA 관리자 API 컨트롤러.
 *
 * 관리자 권한이 필요한 QnA 관리 기능을 제공합니다.
 */
@Tag(name = "QnA (Admin)", description = "관리자 권한이 필요한 QnA 관리 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class QnaAdminController {

    private final QnaService qnaService;

    /**
     * 관리자용 질문 목록 조회.
     */
    @GetMapping("/questions")
    @Operation(
        summary = "관리자용 질문 목록 조회",
        description = """
                관리자용 질문 목록을 조회합니다. 모든 질문 정보에 접근 가능합니다.
                
                필터 옵션:
                - isAnswered (0/1): 답변 완료 여부
                - secret (0/1): 비밀글 여부
                - 검색 (title/content/author_name/phone_number/통합)
                - from, to: 작성일 범위 검색
                
                정렬:
                - 기본: 작성일 내림차순 (최신순)
                """
    )
    public ResponseList<ResponseQnaQuestionListItem> getQuestionList(
            @Parameter(description = "답변 완료 여부 (0=미완료, 1=완료)")
            @RequestParam(required = false) Integer isAnswered,
            @Parameter(description = "비밀글 여부 (0=공개, 1=비밀)")
            @RequestParam(required = false) Integer secret,
            @Parameter(description = "검색 타입 (title/content/author_name/phone_number/all)")
            @RequestParam(required = false) String searchType,
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 시작일 (yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
            @Parameter(description = "검색 종료일 (yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("관리자 질문 목록 조회 요청. isAnswered={}, secret={}, searchType={}, keyword={}", 
                isAnswered, secret, searchType, keyword);
        
        Boolean answeredFilter = isAnswered != null ? (isAnswered == 1) : null;
        Boolean secretFilter = secret != null ? (secret == 1) : null;
        
        return qnaService.getAdminQuestionList(answeredFilter, secretFilter, searchType, keyword, from, to, pageable);
    }

    /**
     * 관리자용 질문 상세 조회.
     */
    @GetMapping("/questions/{id}")
    @Operation(
        summary = "관리자용 질문 상세 조회",
        description = """
                관리자용 질문 상세 정보를 조회합니다.
                
                포함 정보:
                - 질문 전체 정보 (연락처, IP 주소, 개인정보 동의 등)
                - 답변 정보 (있는 경우)
                
                권한:
                - 비밀글도 토큰 없이 조회 가능
                """
    )
    public ResponseData<ResponseQnaQuestionAdmin> getQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id) {

        log.info("관리자 질문 상세 조회 요청. id={}", id);
        return qnaService.getAdminQuestion(id);
    }

    /**
     * 답변 생성/수정 (Upsert).
     */
    @PutMapping("/questions/{id}/answer")
    @Operation(
        summary = "답변 생성/수정",
        description = """
                질문에 대한 답변을 생성하거나 수정합니다.
                
                동작 방식 (트랜잭션):
                - 답변이 없으면 INSERT
                - 답변이 있으면 UPDATE
                - 질문 테이블의 is_answered=1, answered_at=NOW() 동기화
                
                비즈니스 규칙:
                - 질문당 답변은 1개만 허용
                """
    )
    public Response upsertAnswer(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "답변 생성/수정 요청")
            @RequestBody @Valid RequestQnaAnswerUpsert request) {

        log.info("답변 Upsert 요청. questionId={}", id);
        return qnaService.upsertAnswer(id, request);
    }

    /**
     * 답변 삭제.
     */
    @DeleteMapping("/questions/{id}/answer")
    @Operation(
        summary = "답변 삭제",
        description = """
                질문의 답변을 삭제합니다.
                
                동작 방식 (트랜잭션):
                - qna_answers 테이블에서 삭제
                - 질문 테이블의 is_answered=0, answered_at=NULL 동기화
                
                주의사항:
                - 삭제된 답변은 복구할 수 없습니다
                """
    )
    public Response deleteAnswer(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id) {

        log.info("답변 삭제 요청. questionId={}", id);
        return qnaService.deleteAnswer(id);
    }

    /**
     * 질문 삭제 (관리자 전용).
     */
    @DeleteMapping("/questions/{id}")
    @Operation(
        summary = "질문 삭제",
        description = """
                관리자가 질문을 삭제합니다.
                
                동작 방식 (트랜잭션):
                - 연관된 답변도 함께 삭제 (CASCADE DELETE)
                - qna_questions 테이블에서 삭제
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 답변이 있는 질문도 삭제 가능합니다
                - 비밀번호 검증이 필요하지 않습니다
                """
    )
    public Response deleteQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id) {

        log.info("관리자 질문 삭제 요청. questionId={}", id);
        return qnaService.deleteQuestionByAdmin(id);
    }

    /**
     * QnA 통계 조회.
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "QnA 통계 조회", 
        description = """
                QnA 전체 통계를 조회합니다.
                
                제공 정보:
                - totalCount: 전체 질문 개수
                - answeredCount: 답변 완료된 질문 개수  
                - unansweredCount: 답변 대기 중인 질문 개수
                
                활용 예시:
                - 관리자 대시보드 통계 표시
                - 답변율 계산 (answeredCount / totalCount)
                - 미답변 알림 개수 표시
                """
    )
    public ResponseData<ResponseQnaStatistics> getQnaStatistics() {
        log.info("QnA 통계 조회 요청");
        return qnaService.getQnaStatistics();
    }
}