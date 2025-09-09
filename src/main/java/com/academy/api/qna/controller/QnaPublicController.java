package com.academy.api.qna.controller;

import com.academy.api.auth.security.JwtAuthenticationToken;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.academy.api.qna.service.QnaQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * QnA 공개 API 컨트롤러.
 * 
 * 비회원도 접근 가능한 QnA 조회 기능을 제공합니다.
 */
@Tag(name = "QnA 공개 API", description = "비회원도 접근 가능한 QnA 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/qna-simple")
@RequiredArgsConstructor
public class QnaPublicController {

    private final QnaQuestionService questionService;

    /**
     * 질문 목록 조회 (공개).
     */
    @Operation(
        summary = "질문 목록 조회", 
        description = """
            모든 질문 목록을 페이지네이션으로 조회합니다. 비회원도 접근 가능합니다.
            
            검색 조건:
            - titleLike: 제목 포함 검색
            - contentLike: 내용 포함 검색  
            - secret: 비밀글 필터 (exclude/only/include)
            - isAnswered: 답변 완료 여부
            - createdFrom/createdTo: 작성일 범위 검색
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/questions")
    public ResponseList<ResponseQuestion> getQuestions(
            @Parameter(description = "검색 조건") ResponseQuestion.Criteria cond,
            @Parameter(description = "페이지네이션 정보") @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("공개 질문 목록 조회 요청");
        
        return questionService.list(cond, pageable);
    }

    /**
     * 질문 상세 조회 (공개).
     */
    @Operation(
        summary = "질문 상세 조회", 
        description = "특정 질문의 상세 정보를 조회합니다. 조회수가 1 증가합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    })
    @GetMapping("/questions/{questionId}")
    public ResponseData<ResponseQuestion> getQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId) {
        
        log.info("공개 질문 상세 조회 요청: questionId={}", questionId);
        
        return questionService.get(questionId);
    }

    /**
     * 새로운 질문 생성 (비회원).
     */
    @Operation(
        summary = "질문 등록", 
        description = "비회원이 새로운 질문을 등록합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "질문 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터")
    })
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createQuestion(
            @Valid @RequestBody RequestQuestionCreate request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("비회원 질문 등록 요청. IP={}", clientIp);
        
        return questionService.create(request, clientIp);
    }

    /**
     * 질문 수정.
     */
    @Operation(
        summary = "질문 수정", 
        description = "기존 질문을 수정합니다. 비밀번호 확인이 필요합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    })
    @PutMapping("/questions/{questionId}")
    public Response updateQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Valid @RequestBody RequestQuestionUpdate request) {
        
        log.info("질문 수정 요청. questionId={}", questionId);
        
        return questionService.update(questionId, request);
    }

    /**
     * 질문 삭제.
     */
    @Operation(
        summary = "질문 삭제", 
        description = "기존 질문을 삭제합니다. 비밀번호 확인이 필요합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    })
    @DeleteMapping("/questions/{questionId}")
    public Response deleteQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Parameter(description = "비밀번호") @RequestParam String password) {
        
        log.info("질문 삭제 요청. questionId={}", questionId);
        
        return questionService.delete(questionId, password);
    }

    /**
     * 인증된 사용자의 질문 등록.
     */
    @Operation(
        summary = "질문 등록 (인증 사용자)", 
        description = "로그인한 회원이 새로운 질문을 등록합니다. member_id가 자동으로 설정됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "질문 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/questions/member")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createQuestionWithMember(
            @Valid @RequestBody RequestQuestionCreate request,
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            HttpServletRequest httpRequest) {
        
        if (authentication == null) {
            return ResponseData.error("AUTH_REQUIRED", "인증이 필요합니다.");
        }
        
        Long memberId = authentication.getMemberId();
        String username = authentication.getUsername();
        String clientIp = getClientIpAddress(httpRequest);
        
        log.info("인증된 사용자의 질문 등록 요청. memberId={}, username={}, IP={}", 
                memberId, username, clientIp);
        
        return questionService.createWithMemberId(request, memberId, clientIp);
    }

    /**
     * 클라이언트 IP 주소 추출.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

}