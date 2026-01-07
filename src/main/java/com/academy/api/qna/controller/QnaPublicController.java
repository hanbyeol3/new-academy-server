package com.academy.api.qna.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.qna.dto.*;
import com.academy.api.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * QnA 공개 API 컨트롤러.
 *
 * 모든 사용자가 접근 가능한 QnA 기능을 제공합니다.
 */
@Tag(name = "QnA (Public)", description = "모든 사용자가 접근 가능한 QnA API")
@Slf4j
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaPublicController {

    private final QnaService qnaService;

    /**
     * 질문 목록 조회.
     */
    @GetMapping("/questions")
    @Operation(
        summary = "질문 목록 조회", 
        description = """
                질문 목록을 조회합니다.
                
                선택적 필터:
                - isAnswered (0/1): 답변 완료 여부
                - 검색 (title/content/author_name/통합)
                
                정렬:
                - 기본: 작성일 내림차순 (최신순)
                """
    )
    public ResponseList<ResponseQnaQuestionListItem> getQuestionList(
            @Parameter(description = "답변 완료 여부 (0=미완료, 1=완료)")
            @RequestParam(required = false) Integer isAnswered,
            @Parameter(description = "검색 타입 (title/content/author_name/all)")
            @RequestParam(required = false) String searchType,
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {

        log.info("질문 목록 조회 요청. isAnswered={}, searchType={}, keyword={}", isAnswered, searchType, keyword);
        
        Boolean answeredFilter = isAnswered != null ? (isAnswered == 1) : null;
        return qnaService.getPublicQuestionList(answeredFilter, searchType, keyword, pageable);
    }

    /**
     * 질문 상세 조회.
     */
    @GetMapping("/questions/{id}")
    @Operation(
        summary = "질문 상세 조회",
        description = """
                질문 상세 정보를 조회합니다. 조회수가 자동으로 증가합니다.
                
                비밀글 접근:
                - secret=0: 토큰 없이 조회 가능
                - secret=1: X-QNA-VIEW-TOKEN 헤더가 필요 (미검증 시 403)
                """
    )
    public ResponseData<ResponseQnaQuestionDetail> getQuestion(
            @Parameter(description = "질문 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "비밀글 열람 토큰")
            @RequestHeader(value = "X-QNA-VIEW-TOKEN", required = false) String viewToken) {

        log.info("질문 상세 조회 요청. id={}, hasToken={}", id, viewToken != null);
        return qnaService.getPublicQuestion(id, viewToken);
    }

    /**
     * 질문 등록.
     */
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "질문 등록",
        description = """
                새로운 질문을 등록합니다.
                
                필수 입력:
                - authorName, phoneNumber, title, content, password, privacyConsent
                
                선택 입력:
                - secret (기본값: 0=공개)
                
                서버 자동 저장:
                - password는 해시로 암호화 저장
                - ip_address는 서버에서 추출해 저장
                """
    )
    public ResponseData<ResponseQnaQuestionCreate> createQuestion(
            @Parameter(description = "질문 생성 요청")
            @RequestBody @Valid RequestQnaQuestionCreate request,
            HttpServletRequest httpRequest) {

        log.info("질문 등록 요청. authorName={}, title={}", request.getAuthorName(), request.getTitle());
        
        String clientIp = getClientIp(httpRequest);
        return qnaService.createQuestion(request, clientIp);
    }

    /**
     * 비밀글 비밀번호 검증.
     */
    @PostMapping("/questions/{id}/verify-password")
    @Operation(
        summary = "비밀글 비밀번호 검증",
        description = """
                비밀글의 비밀번호를 검증하고 열람 토큰을 발급합니다.
                
                성공 시:
                - verified: true
                - viewToken: 열람용 JWT 토큰
                - expiresInSec: 토큰 만료 시간 (권장: 600초)
                
                실패 시:
                - verified: false
                """
    )
    public ResponseData<ResponseQnaPasswordVerify> verifyPassword(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "비밀번호 검증 요청")
            @RequestBody @Valid RequestQnaPasswordVerify request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        log.info("비밀번호 검증 요청. id={}, clientIp={}", id, clientIp);
        return qnaService.verifyPassword(id, request, clientIp);
    }

    /**
     * 질문 수정.
     */
    @PutMapping("/questions/{id}")
    @Operation(
        summary = "질문 수정",
        description = """
                본인의 질문을 수정합니다.
                
                권한 확인:
                - password로 본인 인증 필수
                
                수정 제한:
                - is_answered=1이면 수정 제한 (정책에 따라)
                """
    )
    public Response updateQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "질문 수정 요청")
            @RequestBody @Valid RequestQnaQuestionUpdate request) {

        log.info("질문 수정 요청. id={}, title={}", id, request.getTitle());
        return qnaService.updateQuestion(id, request);
    }

    /**
     * 질문 삭제.
     */
    @DeleteMapping("/questions/{id}")
    @Operation(
        summary = "질문 삭제",
        description = """
                본인의 질문을 삭제합니다.
                
                권한 확인:
                - password로 본인 인증 필수
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 연관된 답변도 함께 삭제됩니다 (CASCADE DELETE)
                """
    )
    public Response deleteQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "질문 삭제 요청")
            @RequestBody @Valid RequestQnaQuestionDelete request) {

        log.info("질문 삭제 요청. id={}", id);
        return qnaService.deleteQuestion(id, request);
    }

    /**
     * 클라이언트 IP 주소 추출.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}