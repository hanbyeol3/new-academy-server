package com.academy.api.notice.controller;

import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticePublishedUpdate;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNoticeDetail;
import com.academy.api.notice.dto.ResponseNoticeAdminList;
import com.academy.api.notice.service.NoticeService;
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
 * 공지사항 관리자 API 컨트롤러.
 *
 * 공지사항의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Notice (Admin)", description = "공지사항 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NoticeAdminController {

    private final NoticeService noticeService;

	/**
	 * 관리자용 공지사항 목록 조회 (모든 상태 포함).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID
	 * @param isImportant 중요 공지 여부
	 * @param isPublished 공개 상태
	 * @param exposureType 노출 기간 유형 (ALWAYS, PERIOD)
	 * @param sortBy 정렬 기준
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Operation(
        summary = "공지사항 목록 조회 (관리자)",
        description = """
                관리자용 공지사항 목록을 조회합니다.
                
                주요 기능:
                - 키워드 검색 (제목, 내용)
                - 작성자 검색
                - 카테고리 필터링
                - 중요 공지 필터링
                - 공개/비공개 상태 필터링
                - 노출 기간 유형 필터링
                - 페이징 처리
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - isImportant: 중요 공지 필터
                - isPublished: 공개 상태 필터
                - exposureType: 노출 기간 유형 (ALWAYS, PERIOD)
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, IMPORTANT_FIRST, VIEW_COUNT_DESC)
                
                관리자는 모든 상태의 공지사항을 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                예시:
                - GET /api/admin/notices (모든 공지사항)
                - GET /api/admin/notices?keyword=학사일정&searchType=TITLE (제목 검색)
                - GET /api/admin/notices?categoryId=1&isImportant=true (카테고리+중요공지)
                - GET /api/admin/notices?keyword=공지&isPublished=false&sortBy=CREATED_DESC (복합 검색)
                """
    )
    @GetMapping
    public ResponseList<ResponseNoticeAdminList> getNoticeList(
            @Parameter(description = "검색 키워드", example = "학사일정") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "중요 공지만 조회", example = "true") 
            @RequestParam(required = false) Boolean isImportant,
            @Parameter(description = "공개 상태", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "노출 기간 유형 (ALWAYS, PERIOD)", example = "ALWAYS") 
            @RequestParam(required = false) String exposureType,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, IMPORTANT_FIRST, VIEW_COUNT_DESC)", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[NoticeAdminController] 관리자 공지사항 목록 조회 요청. keyword={}, searchType={}, categoryId={}, isImportant={}, isPublished={}, exposureType={}, sortBy={}", 
                 keyword, searchType, categoryId, isImportant, isPublished, exposureType, sortBy);
        return noticeService.getNoticeListForAdmin(keyword, searchType, categoryId, isImportant, isPublished, exposureType, sortBy, pageable);
    }

	/**
	 * 공지사항 상세 조회.
	 *
	 * @param id 공지사항 ID
	 * @return 공지사항 상세 정보
	 */
    @Operation(
        summary = "공지사항 상세 조회 (관리자)",
        description = """
                공지사항의 상세 정보를 조회합니다.
                
                특징:
                - 관리자는 비공개 공지사항도 조회 가능
                - 조회수는 증가하지 않음 (관리자 조회)
                - 모든 상태의 공지사항 접근 가능
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseNoticeDetail> getNoticeForAdmin(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        
        log.info("[NoticeAdminController] 공지사항 상세 조회 요청. ID={}", id);
        return noticeService.getNoticeForAdmin(id);
    }

	/**
	 * 공지사항 생성.
	 *
	 * @param request 생성 요청 데이터
	 * @return 생성된 공지사항 ID
	 */
    @Operation(
        summary = "공지사항 생성",
        description = """
                새로운 공지사항을 생성합니다.
                
                필수 입력 사항:
                - 제목 (최대 255자)
                - 내용 (HTML 가능)
                
                선택 입력 사항:
                - 중요 공지 여부 (기본값: false)
                - 게시 여부 (기본값: true)
                - 노출 기간 유형 (기본값: ALWAYS)
                - 게시 시작/종료일시 (PERIOD 타입인 경우)
                - 카테고리 ID
                - 첨부 파일 그룹 ID
                
                주의사항:
                - PERIOD 타입 선택 시 시작/종료일시 필수
                - 파일 첨부는 별도의 파일 업로드 API 사용
                - 에디터 이미지는 content에 HTML로 포함
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createNotice(
            @Parameter(description = "공지사항 생성 요청 데이터")
            @RequestBody @Valid RequestNoticeCreate request) {
        
        log.info("[NoticeAdminController] 공지사항 생성 요청. 제목={}", request.getTitle());
        return noticeService.createNotice(request);
    }

	/**
	 * 공지사항 수정.
	 *
	 * @param id 공지사항 ID
	 * @param request 수정 요청 데이터
	 * @return 수정 결과
	 */
    @Operation(
        summary = "공지사항 수정",
        description = """
                기존 공지사항 정보를 수정합니다.
                
                수정 가능 항목:
                - 제목, 내용
                - 중요 공지 여부
                - 게시 여부
                - 노출 기간 설정 (유형, 시작/종료일시)
                - 카테고리 변경
                - 첨부 파일 그룹 변경
                
                주의사항:
                - null 값인 필드는 수정하지 않음 (기존 값 유지)
                - 노출 기간 변경 시 유효성 검증 수행
                - 파일 변경은 별도 API 사용 권장
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseNoticeDetail> updateNotice(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청 데이터")
            @RequestBody @Valid RequestNoticeUpdate request) {
        
        log.info("🔄🔄🔄 [NoticeAdminController] 공지사항 수정 요청!!! ID={}, 새첨부파일={}, 삭제첨부파일={}", 
                id, 
                request.getNewAttachments() != null ? request.getNewAttachments().size() : 0,
                request.getDeleteAttachmentFileIds() != null ? request.getDeleteAttachmentFileIds().size() : 0);
        return noticeService.updateNotice(id, request);
    }

	/**
	 * 공지사항 삭제.
	 *
	 * @param id 공지사항 ID
	 * @return 삭제 결과
	 */
    @Operation(
        summary = "공지사항 삭제",
        description = """
                공지사항을 완전히 삭제합니다.
                
                주의사항:
                - 삭제된 공지사항은 복구할 수 없습니다
                - 첨부된 파일은 자동으로 삭제되지 않음
                - 중요한 공지사항 삭제 시 신중히 검토 필요
                - 실제 운영에서는 soft delete 고려 권장
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteNotice(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        
        log.info("[NoticeAdminController] 공지사항 삭제 요청. ID={}", id);
        return noticeService.deleteNotice(id);
    }

	/**
	 * 조회수 증가.
	 *
	 * @param id 공지사항 ID
	 * @return 증가 결과
	 */
    @Operation(
        summary = "조회수 수동 증가",
        description = """
                특정 공지사항의 조회수를 수동으로 증가시킵니다.
                
                사용 목적:
                - 테스트 또는 데이터 보정용
                - 외부 시스템 연동 시 조회수 반영
                
                주의사항:
                - 일반적으로는 상세 조회 시 자동 증가
                - 관리자만 수동 증가 가능
                """
    )
    @PostMapping("/{id}/increment-view")
    public Response incrementViewCount(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        
        log.info("[NoticeAdminController] 조회수 수동 증가 요청. ID={}", id);
        return noticeService.incrementViewCount(id);
    }

	/**
	 * 중요 공지 설정/해제.
	 *
	 * @param id 공지사항 ID
	 * @param isImportant 중요 공지 여부
	 * @return 변경 결과
	 */
    @Operation(
        summary = "중요 공지 설정/해제",
        description = """
                공지사항의 중요 공지 상태를 변경합니다.
                
                기능:
                - 중요 공지로 설정 시 목록 상단에 우선 노출
                - 중요 공지 해제 시 일반 공지로 분류
                
                사용법:
                - true: 중요 공지로 설정
                - false: 일반 공지로 변경
                """
    )
    @PatchMapping("/{id}/important")
    public Response toggleImportant(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "중요 공지 여부", example = "true") @RequestParam Boolean isImportant) {
        
        log.info("[NoticeAdminController] 중요 공지 상태 변경 요청. ID={}, 중요공지={}", id, isImportant);
        return noticeService.toggleImportant(id, isImportant);
    }

	/**
	 * 공개/비공개 상태 변경.
	 *
	 * @param id 공지사항 ID
	 * @param request 공개 상태 변경 요청 데이터
	 * @return 변경 결과
	 */
    @Operation(
        summary = "공개/비공개 상태 변경",
        description = """
                공지사항의 공개 상태를 변경합니다.
                
                필수 입력 사항:
                - isPublished: 공개 여부 (true/false)
                
                선택 입력 사항:
                - makePermanent: 상시 게시 설정 여부 (기본값: false)
                
                특별 처리:
                - 비공개 → 공개 변경 시 makePermanent=true인 경우 상시 노출로 변경
                - 기간이 만료된 공지사항을 공개로 변경할 때 유용
                - 공개 → 비공개 변경 시 즉시 목록에서 숨겨짐
                
                응답 메시지:
                - 일반 공개: "공지사항이 공개로 변경되었습니다."
                - 영구 공개: "공지사항이 공개되었고 게시기간이 상시로 설정되었습니다."
                """
    )
    @PatchMapping("/{id}/published")
    public Response updateNoticePublished(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공개 상태 변경 요청")
            @RequestBody @Valid RequestNoticePublishedUpdate request) {
        
        log.info("[NoticeAdminController] 공개 상태 변경 요청. ID={}, 공개여부={}, 상시게시={}", 
                id, request.getIsPublished(), request.getMakePermanent());
        return noticeService.updateNoticePublished(id, request);
    }

	/**
	 * 카테고리별 공지사항 통계.
	 *
	 * @return 카테고리별 통계 정보
	 */
    @Operation(
        summary = "카테고리별 공지사항 통계",
        description = """
                각 카테고리별 공지사항 개수 통계를 조회합니다.
                
                반환 데이터:
                - 카테고리명
                - 해당 카테고리의 공지사항 개수
                
                용도:
                - 관리자 대시보드 통계
                - 카테고리 관리 참고 자료
                """
    )
    @GetMapping("/stats/by-category")
    public ResponseData<List<Object[]>> getNoticeStatsByCategory() {
        
        log.info("[NoticeAdminController] 카테고리별 통계 조회 요청");
        return noticeService.getNoticeStatsByCategory();
    }
}