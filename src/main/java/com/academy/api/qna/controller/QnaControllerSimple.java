package com.academy.api.qna.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.model.RequestQuestionCreate;
import com.academy.api.qna.model.RequestQuestionUpdate;
import com.academy.api.qna.model.ResponseQuestion;
import com.academy.api.qna.service.QnaQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * QnA 공개 API 컨트롤러 (간단 버전).
 */
@Tag(name = "QnA 간단 API", description = "QnA 질문/답변 기본 API")
@Slf4j
@RestController
@RequestMapping("/api/qna-simple")
@RequiredArgsConstructor
public class QnaControllerSimple {

    private final QnaQuestionService questionService;

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

    @Operation(summary = "질문 목록 조회", description = "QnA 질문 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "질문 목록 조회 성공")
    @GetMapping("/questions")
    public ResponseEntity<ResponseList<ResponseQuestion.Projection>> getQuestions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("질문 목록 조회 요청. 페이지={}", pageable.getPageNumber());
        
        ResponseList<ResponseQuestion.Projection> response = questionService.getQuestions(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "질문 상세 조회", description = "질문의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "질문 조회 성공")
    @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ResponseData<ResponseQuestion>> getQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId) {
        
        log.info("질문 상세 조회 요청. ID={}", questionId);
        
        ResponseData<ResponseQuestion> response = questionService.getQuestion(questionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "새로운 질문 등록", description = "회원/비회원이 새로운 질문을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "질문 등록 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터")
    @PostMapping("/questions")
    public ResponseEntity<ResponseData<ResponseQuestion>> createQuestion(
            @Valid @RequestBody RequestQuestionCreate request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("질문 등록 요청. 작성자={}, IP={}", request.getAuthorName(), clientIp);
        
        ResponseData<ResponseQuestion> response = questionService.createQuestion(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "질문 수정", description = "작성자가 본인의 질문을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "질문 수정 성공")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<ResponseData<ResponseQuestion>> updateQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Valid @RequestBody RequestQuestionUpdate request) {
        
        log.info("질문 수정 요청. ID={}", questionId);
        
        ResponseData<ResponseQuestion> response = questionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "질문 삭제", description = "작성자가 본인의 질문을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "질문 삭제 성공")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음")
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<ResponseData<Void>> deleteQuestion(
            @Parameter(description = "질문 ID") @PathVariable Long questionId,
            @Parameter(description = "비밀번호") @RequestParam String password) {
        
        log.info("질문 삭제 요청. ID={}", questionId);
        
        ResponseData<Void> response = questionService.deleteQuestion(questionId, password);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "QnA 통계 조회", description = "전체 질문 수, 답변 완료율 등의 통계 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "통계 조회 성공")
    @GetMapping("/statistics")
    public ResponseEntity<ResponseData<ResponseQuestion.Summary>> getStatistics() {
        
        log.info("QnA 통계 조회 요청");
        
        ResponseData<ResponseQuestion.Summary> response = questionService.getStatistics();
        return ResponseEntity.ok(response);
    }
}