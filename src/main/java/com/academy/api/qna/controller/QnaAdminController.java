package com.academy.api.qna.controller;

import com.academy.api.auth.security.JwtAuthenticationToken;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.qna.model.RequestAnswerCreate;
import com.academy.api.qna.model.RequestAnswerUpdate;
import com.academy.api.qna.model.ResponseAnswer;
import com.academy.api.qna.service.QnaAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * QnA 관리자 전용 API 컨트롤러.
 * 
 * 관리자 권한을 가진 사용자만 접근 가능한 QnA 관리 기능을 제공합니다.
 */
@Tag(name = "QnA 관리자 API", description = "관리자 전용 QnA 답변 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
public class QnaAdminController {

    private final QnaAnswerService answerService;

    /**
     * 답변 등록.
     */
    @Operation(
        summary = "답변 등록", 
        description = "관리자가 특정 질문에 대한 답변을 등록합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답변 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 답변이 등록된 질문")
    })
    @PostMapping("/questions/{questionId}/answer")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createAnswer(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Valid @RequestBody RequestAnswerCreate request) {
        
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return ResponseData.error("AUTH_REQUIRED", "인증이 필요합니다.");
        }
        
        Long memberId = jwtAuth.getMemberId();
        String adminName = jwtAuth.getUsername();
        
        log.info("답변 등록 요청: questionId={}, adminId={}, adminName={}", 
                questionId, memberId, adminName);
        
        return answerService.create(questionId, request, memberId, adminName);
    }

    /**
     * 답변 수정.
     */
    @PutMapping("/answers/{answerId}")
    public Response updateAnswer(
            @Parameter(description = "답변 ID") @PathVariable Long answerId,
            @Valid @RequestBody RequestAnswerUpdate request) {
        
        log.info("답변 수정 요청: answerId={}", answerId);
        
        return answerService.update(answerId, request);
    }

    /**
     * 답변 삭제.
     */
    @DeleteMapping("/answers/{answerId}")
    public Response deleteAnswer(
            @Parameter(description = "답변 ID") @PathVariable Long answerId) {
        
        log.info("답변 삭제 요청: answerId={}", answerId);
        
        return answerService.delete(answerId);
    }

    /**
     * 질문에 대한 답변 조회.
     */
    @GetMapping("/questions/{questionId}/answer")
    public ResponseData<ResponseAnswer> getAnswer(
            @Parameter(description = "질문 ID") @PathVariable Long questionId) {
        
        log.info("답변 조회 요청: questionId={}", questionId);
        
        return answerService.getByQuestionId(questionId, true); // 관리자는 벤밀 답변 접근 가능
    }
}