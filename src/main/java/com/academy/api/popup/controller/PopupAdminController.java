package com.academy.api.popup.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.dto.RequestPopupCreate;
import com.academy.api.popup.dto.RequestPopupUpdate;
import com.academy.api.popup.dto.ResponsePopup;
import com.academy.api.popup.dto.ResponsePopupListItem;
import com.academy.api.popup.service.PopupService;
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

/**
 * 팝업 관리자 API 컨트롤러.
 *
 * 팝업의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Popup (Admin)", description = "팝업 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/popups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PopupAdminController {

    private final PopupService popupService;

    /**
     * 관리자용 팝업 목록 조회 (통합 검색).
     *
     * @param keyword 검색 키워드 (제목)
     * @param type 팝업 타입 (IMAGE, YOUTUBE)
     * @param isPublished 공개 여부 필터
     * @param sortType 정렬 방식
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Operation(
        summary = "팝업 목록 조회 (관리자)",
        description = """
                관리자용 팝업 목록을 조회합니다.
                
                주요 기능:
                - 팝업 제목 검색 (부분 일치)
                - 팝업 타입별 필터링 (IMAGE/YOUTUBE)
                - 공개/비공개 상태 필터링
                - 다양한 정렬 옵션
                - 정렬순서별 정렬
                - 페이징 처리
                
                검색 옵션:
                - keyword: 팝업 제목 검색
                - type: IMAGE 또는 YOUTUBE
                - isPublished: 공개 상태 필터
                - sortType: 정렬 방식 (SORT_ORDER_ASC, CREATED_DESC, CREATED_ASC, TITLE_ASC, TITLE_DESC)
                
                반환 정보:
                - 팝업 기본 정보 (ID, 제목, 타입)
                - 노출 기간 정보
                - 정렬순서
                - 공개 여부
                - 등록/수정자 정보 및 일시
                
                관리자는 비공개 팝업도 모두 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                사용 예시:
                - 전체 목록: GET /api/admin/popups
                - 제목 검색: GET /api/admin/popups?keyword=신년
                - 이미지 팝업: GET /api/admin/popups?type=IMAGE
                - 공개 팝업: GET /api/admin/popups?isPublished=true
                - 복합 조건: GET /api/admin/popups?keyword=이벤트&type=IMAGE&isPublished=true
                - 정렬순서별: GET /api/admin/popups?sortType=SORT_ORDER_ASC
                """
    )
    @GetMapping
    public ResponseList<ResponsePopupListItem> getPopupList(
            @Parameter(description = "팝업 제목 검색", example = "신년 이벤트")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "팝업 타입", example = "IMAGE")
            @RequestParam(required = false) String type,
            @Parameter(description = "공개 여부", example = "true")
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "정렬 방식", example = "SORT_ORDER_ASC")
            @RequestParam(required = false) String sortType,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) 
            Pageable pageable) {

        log.info("[PopupAdminController] 팝업 목록 조회 요청. keyword={}, type={}, isPublished={}, sortType={}, page={}, size={}",
                keyword, type, isPublished, sortType, pageable.getPageNumber(), pageable.getPageSize());

        // 문자열 타입을 enum으로 변환
        Popup.PopupType popupType = null;
        if (type != null) {
            try {
                popupType = Popup.PopupType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[PopupAdminController] 유효하지 않은 팝업 타입: {}", type);
                // 잘못된 타입은 무시하고 전체 조회로 진행
            }
        }

        return popupService.getPopupList(keyword, popupType, isPublished, sortType, pageable);
    }

    /**
     * 팝업 상세 조회.
     *
     * @param id 팝업 ID
     * @return 팝업 상세 정보
     */
    @Operation(
        summary = "팝업 상세 조회",
        description = """
                특정 팝업의 상세 정보를 조회합니다.
                
                반환 정보:
                - 팝업 기본 정보 (제목, 타입, 유튜브 URL 등)
                - 위치 및 크기 정보
                - 노출 기간 설정
                - 링크 URL (PC/모바일)
                - 다시 보지 않기 설정
                - 정렬순서
                - 등록/수정자 정보 및 일시
                
                주의사항:
                - 존재하지 않는 ID는 P404 에러 반환
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponsePopup> getPopup(
            @Parameter(description = "팝업 ID", example = "1")
            @PathVariable Long id) {

        log.info("[PopupAdminController] 팝업 상세 조회 요청. id={}", id);
        
        return popupService.getPopup(id);
    }

    /**
     * 팝업 생성.
     *
     * @param request 팝업 생성 요청 데이터
     * @return 생성된 팝업 ID
     */
    @Operation(
        summary = "팝업 생성",
        description = """
                새로운 팝업을 생성합니다.
                
                필수 입력 사항:
                - title (팝업 제목)
                - type (IMAGE 또는 YOUTUBE)
                - widthPx, heightPx (팝업 크기)
                - positionTopPx, positionLeftPx (팝업 위치)
                
                타입별 필수 사항:
                - YOUTUBE: youtubeUrl 필수
                - IMAGE: attachmentFiles 권장
                
                노출 기간 설정:
                - ALWAYS: 항상 노출 (기본값)
                - PERIOD: exposureStartAt, exposureEndAt 필수
                
                파일 처리:
                - IMAGE 타입: 임시파일 → 정식파일 자동 이동
                - attachmentFiles에 tempFileId와 fileName 제공
                
                검증 규칙:
                - 타입별 필수 필드 자동 검증
                - 노출 기간 유효성 검증
                - 크기/위치 범위 검증
                
                주의사항:
                - 파일 처리 실패해도 팝업 생성은 진행
                - 생성 후 ID 반환하여 추가 작업 가능
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createPopup(
            @Parameter(description = "팝업 생성 요청")
            @RequestBody @Valid RequestPopupCreate request) {

        log.info("[PopupAdminController] 팝업 생성 요청. title={}, type={}", 
                request.getTitle(), request.getType());

        return popupService.createPopup(request);
    }

    /**
     * 팝업 수정.
     *
     * @param id 수정할 팝업 ID
     * @param request 팝업 수정 요청 데이터
     * @return 수정 결과
     */
    @Operation(
        summary = "팝업 수정",
        description = """
                기존 팝업을 수정합니다.
                
                수정 가능 항목:
                - 기본 정보 (제목, 타입, URL)
                - 위치 및 크기
                - 노출 기간 설정
                - 링크 URL
                - 정렬순서
                - 공개 여부
                
                부분 수정 지원:
                - null 값은 기존 값 유지
                - 변경하려는 필드만 전송 가능
                
                타입 변경 주의:
                - IMAGE → YOUTUBE: 기존 파일 유지
                - YOUTUBE → IMAGE: 새 파일 업로드 필요
                
                검증 규칙:
                - 수정된 내용도 동일한 검증 적용
                - 노출 기간 및 타입별 필수 필드 검증
                
                주의사항:
                - 존재하지 않는 ID는 P404 에러
                - 수정자 정보 자동 업데이트
                """
    )
    @PutMapping("/{id}")
    public Response updatePopup(
            @Parameter(description = "수정할 팝업 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "팝업 수정 요청")
            @RequestBody @Valid RequestPopupUpdate request) {

        log.info("[PopupAdminController] 팝업 수정 요청. id={}, title={}", id, request.getTitle());

        return popupService.updatePopup(id, request);
    }

    /**
     * 팝업 공개 상태 변경.
     *
     * @param id 대상 팝업 ID
     * @param isPublished 공개 여부 (true: 공개, false: 비공개)
     * @return 변경 결과
     */
    @Operation(
        summary = "팝업 공개 상태 변경",
        description = """
                팝업의 공개/비공개 상태를 변경합니다.
                
                기능:
                - 개별 팝업의 isPublished 상태 토글
                - 빠른 상태 변경을 위한 전용 API
                
                사용법:
                - 공개: PATCH /api/admin/popups/{id}/published?isPublished=true
                - 비공개: PATCH /api/admin/popups/{id}/published?isPublished=false
                
                효과:
                - 공개 → 사용자에게 노출 (노출 기간 내에서)
                - 비공개 → 사용자에게 숨김
                
                주의사항:
                - 존재하지 않는 ID는 P404 에러
                - 수정자 정보 자동 업데이트
                - 노출 기간과 별개로 동작 (공개 + 기간 내 = 실제 노출)
                """
    )
    @PatchMapping("/{id}/published")
    public Response updatePublishedStatus(
            @Parameter(description = "대상 팝업 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "공개 여부", example = "true")
            @RequestParam Boolean isPublished) {

        log.info("[PopupAdminController] 팝업 공개 상태 변경 요청. id={}, isPublished={}", id, isPublished);

        return popupService.updatePublishedStatus(id, isPublished);
    }

    /**
     * 팝업 삭제.
     *
     * @param id 삭제할 팝업 ID
     * @return 삭제 결과
     */
    @Operation(
        summary = "팝업 삭제",
        description = """
                팝업을 완전히 삭제합니다.
                
                삭제 범위:
                - 팝업 정보 완전 삭제
                - IMAGE 타입: 관련 파일도 함께 삭제
                - YOUTUBE 타입: URL 정보만 삭제
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 실제 운영에서는 soft delete 고려 권장
                - 파일 삭제 실패해도 팝업 삭제는 진행
                
                오류 처리:
                - 존재하지 않는 ID는 P404 에러
                - 삭제 권한 확인 (ADMIN 필수)
                """
    )
    @DeleteMapping("/{id}")
    public Response deletePopup(
            @Parameter(description = "삭제할 팝업 ID", example = "1")
            @PathVariable Long id) {

        log.info("[PopupAdminController] 팝업 삭제 요청. id={}", id);

        return popupService.deletePopup(id);
    }
}