package com.academy.api.notice.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticeSearch;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeSimple;
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
@Tag(name = "공지사항 관리 (관리자)", description = "공지사항 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NoticeAdminController {

    private final NoticeService noticeService;

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
                
                정렬 옵션:
                - CREATED_DESC: 생성일시 내림차순 (기본값)
                - CREATED_ASC: 생성일시 오름차순
                - IMPORTANT_FIRST: 중요 공지 우선
                - VIEW_COUNT_DESC: 조회수 내림차순
                
                관리자는 모든 상태의 공지사항을 조회할 수 있습니다.
                """
    )
    @GetMapping
    public ResponseList<ResponseNoticeSimple> getNoticeList(
            @Parameter(description = "검색 조건") RequestNoticeSearch searchCondition,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[NoticeAdminController] 관리자 공지사항 목록 조회 요청");
        return noticeService.getNoticeListForAdmin(searchCondition, pageable);
    }

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
    public ResponseData<ResponseNotice> getNotice(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        
        log.info("[NoticeAdminController] 공지사항 상세 조회 요청. ID={}", id);
        return noticeService.getNotice(id);
    }

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
    public Response updateNotice(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청 데이터")
            @RequestBody @Valid RequestNoticeUpdate request) {
        
        log.info("[NoticeAdminController] 공지사항 수정 요청. ID={}", id);
        return noticeService.updateNotice(id, request);
    }

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

    @Operation(
        summary = "공개/비공개 상태 변경",
        description = """
                공지사항의 공개 상태를 변경합니다.
                
                특별 처리:
                - 비공개 → 공개 변경 시 기간이 만료된 경우 자동으로 상시 노출로 변경
                - 공개 → 비공개 변경 시 즉시 목록에서 숨겨짐
                
                사용법:
                - true: 공개 (일반 사용자에게 노출)
                - false: 비공개 (관리자만 확인 가능)
                """
    )
    @PatchMapping("/{id}/published")
    public Response togglePublished(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공개 여부", example = "true") @RequestParam Boolean isPublished) {
        
        log.info("[NoticeAdminController] 공개 상태 변경 요청. ID={}, 공개여부={}", id, isPublished);
        return noticeService.togglePublished(id, isPublished);
    }

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