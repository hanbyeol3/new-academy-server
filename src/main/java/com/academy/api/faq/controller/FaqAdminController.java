package com.academy.api.faq.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.faq.dto.RequestFaqCreate;
import com.academy.api.faq.dto.RequestFaqPublishedUpdate;
import com.academy.api.faq.dto.RequestFaqUpdate;
import com.academy.api.faq.dto.ResponseFaq;
import com.academy.api.faq.dto.ResponseFaqListItem;
import com.academy.api.faq.service.FaqService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ 관리자 API 컨트롤러.
 *
 * FAQ의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "FaQ (Admin)", description = "FAQ CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/faq")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FaqAdminController {

    private final FaqService faqService;

	/**
	 * 관리자용 FAQ 목록 조회 (모든 상태 포함).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID
	 * @param isPublished 공개 상태
	 * @param sortBy 정렬 기준
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Operation(
        summary = "FAQ 목록 조회 (관리자)",
        description = """
                관리자용 FAQ 목록을 조회합니다.
                
                주요 기능:
                - 키워드 검색 (질문, 답변)
                - 작성자 검색
                - 카테고리 필터링
                - 공개/비공개 상태 필터링
                - 페이징 처리
                - 답변 내용 포함 (FAQ는 목록에서도 답변을 포함)
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - isPublished: 공개 상태 필터
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, TITLE_ASC, TITLE_DESC)
                
                관리자는 모든 상태의 FAQ를 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                예시:
                - GET /api/admin/faq (모든 FAQ)
                - GET /api/admin/faq?keyword=수강신청&searchType=TITLE (질문 제목 검색)
                - GET /api/admin/faq?categoryId=1&isPublished=true (카테고리+공개상태)
                - GET /api/admin/faq?keyword=방법&searchType=CONTENT&sortBy=CREATED_DESC (답변 내용 검색)
                """
    )
    @GetMapping
    public ResponseList<ResponseFaqListItem> getFaqList(
            @Parameter(description = "검색 키워드", example = "수강신청") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "공개 상태 (true=공개, false=비공개)", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, TITLE_ASC, TITLE_DESC)", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("FAQ 목록 조회 요청. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}", 
                keyword, searchType, categoryId, isPublished, sortBy);
        
        return faqService.getFaqListForAdmin(keyword, searchType, categoryId, isPublished, sortBy, pageable);
    }

    /**
     * FAQ 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 FAQ ID
     */
    @Operation(
        summary = "FAQ 생성",
        description = """
                새로운 FAQ를 생성합니다.
                
                필수 입력 사항:
                - title: 질문 제목 (최대 255자)
                - content: 답변 내용 (HTML 가능)
                
                선택 입력 사항:
                - categoryId: 카테고리 ID
                - isPublished: 공개 여부 (기본값: true)
                - inlineImages: 본문 이미지 목록 (임시파일 기반)
                
                파일 정책:
                - INLINE 이미지만 지원 (COVER, ATTACHMENT 없음)
                - 임시 파일 → 정식 파일 자동 변환
                - Content 내 임시 URL → 정식 URL 자동 변환
                
                주의사항:
                - ADMIN 권한 필요
                - 임시파일은 생성 시 정식파일로 변환됨
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createFaq(
            @Parameter(description = "FAQ 생성 요청") 
            @RequestBody @Valid RequestFaqCreate request) {
        
        log.info("FAQ 생성 요청. 질문제목={}, 카테고리ID={}", request.getTitle(), request.getCategoryId());
        
        return faqService.createFaq(request);
    }

    /**
     * FAQ 수정.
     * 
     * @param id FAQ ID
     * @param request 수정 요청 데이터
     * @return 수정된 FAQ 정보
     */
    @Operation(
        summary = "FAQ 수정",
        description = """
                기존 FAQ를 수정합니다.
                
                수정 가능한 항목:
                - title: 질문 제목
                - content: 답변 내용
                - categoryId: 카테고리
                - isPublished: 공개 여부
                
                파일 처리:
                - newInlineImages: 추가할 본문 이미지
                - deleteInlineImageFileIds: 삭제할 본문 이미지 ID
                
                처리 순서:
                1. 삭제 요청된 기존 파일 제거
                2. 새 파일 추가 (임시 → 정식 변환)
                3. Content URL 자동 변환
                
                주의사항:
                - 존재하지 않는 FAQ는 404 오류
                - ADMIN 권한 필요
                - 모든 필드는 선택사항 (null인 경우 기존값 유지)
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseFaq> updateFaq(
            @Parameter(description = "수정할 FAQ ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "수정 요청") 
            @RequestBody @Valid RequestFaqUpdate request) {
        
        log.info("FAQ 수정 요청. id={}, 질문제목={}", id, request.getTitle());
        
        return faqService.updateFaq(id, request);
    }

    /**
     * FAQ 삭제.
     * 
     * @param id FAQ ID
     * @return 삭제 결과
     */
    @Operation(
        summary = "FAQ 삭제",
        description = """
                기존 FAQ를 삭제합니다.
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 연결된 파일들도 함께 삭제됩니다
                - 존재하지 않는 FAQ는 404 오류
                - ADMIN 권한 필요
                
                관련 데이터:
                - FAQ 본문의 이미지 파일들
                - 파일 연결 정보
                
                실제 운영에서는 soft delete 고려 권장
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteFaq(
            @Parameter(description = "삭제할 FAQ ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("FAQ 삭제 요청. id={}", id);
        
        return faqService.deleteFaq(id);
    }

    /**
     * FAQ 공개/비공개 상태 변경.
     * 
     * @param id FAQ ID
     * @param request 공개 상태 변경 요청
     * @return 변경 결과
     */
    @Operation(
        summary = "FAQ 공개/비공개 상태 변경",
        description = """
                FAQ의 공개/비공개 상태를 변경합니다.
                
                요청 데이터:
                - isPublished: 공개 여부 (true=공개, false=비공개)
                
                주의사항:
                - 존재하지 않는 FAQ는 404 오류
                - ADMIN 권한 필요
                - 공개 상태 변경 즉시 적용
                
                비공개 처리 시:
                - 일반 사용자는 조회 불가
                - 관리자는 계속 조회 가능
                
                공개 처리 시:
                - 모든 사용자가 조회 가능
                """
    )
    @PutMapping("/{id}/published")
    public Response updateFaqPublished(
            @Parameter(description = "FAQ ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "공개 상태 변경 요청") 
            @RequestBody @Valid RequestFaqPublishedUpdate request) {
        
        log.info("FAQ 공개 상태 변경 요청. id={}, isPublished={}", id, request.getIsPublished());
        
        return faqService.updateFaqPublished(id, request);
    }

    /**
     * FAQ 상세 조회 (관리자용).
     * 
     * @param id FAQ ID
     * @return FAQ 상세 정보
     */
    @Operation(
        summary = "FAQ 상세 조회 (관리자용)",
        description = """
                FAQ 상세 정보를 조회합니다.
                
                Note: FAQ는 목록에서도 답변을 포함하므로
                상세 조회가 꼭 필요하지는 않지만, 파일 정보 등
                추가 데이터가 필요한 경우를 위해 제공합니다.
                
                응답 정보:
                - 기본 FAQ 정보 (질문, 답변)
                - 카테고리 정보
                - 본문 이미지 파일 목록
                - 생성/수정 정보 (회원 이름 포함)
                
                주의사항:
                - 존재하지 않는 FAQ는 404 오류
                - ADMIN 권한 필요
                - 공개/비공개 상관없이 조회 가능
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseFaq> getFaq(
            @Parameter(description = "FAQ ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("FAQ 상세 조회 요청. id={}", id);
        
        return faqService.getFaq(id);
    }

    /**
     * 카테고리별 FAQ 통계.
     * 
     * @return 카테고리별 통계 정보
     */
    @Operation(
        summary = "카테고리별 FAQ 통계",
        description = """
                카테고리별 FAQ 개수 통계를 조회합니다.
                
                응답 형식:
                - 카테고리명과 해당 FAQ 개수
                - 공개된 FAQ만 집계
                - 개수가 많은 순으로 정렬
                
                활용 방법:
                - 관리자 대시보드
                - 카테고리별 현황 파악
                - 데이터 분석
                
                주의사항:
                - ADMIN 권한 필요
                - 비공개 FAQ는 집계에서 제외
                """
    )
    @GetMapping("/stats/by-category")
    public ResponseData<List<Object[]>> getFaqStatsByCategory() {
        
        log.info("카테고리별 FAQ 통계 조회 요청");
        
        return faqService.getFaqStatsByCategory();
    }
}