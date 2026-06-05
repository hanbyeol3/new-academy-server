package com.academy.api.schoolexam.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schoolexam.dto.*;
import com.academy.api.schoolexam.service.SchoolExamService;
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
 * 학교별 시험분석 관리자 API 컨트롤러.
 *
 * 학교별 시험분석의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "SchoolExam (Admin)", description = "학교별 시험분석 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/school-exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SchoolExamAdminController {

    private final SchoolExamService schoolExamService;

    /**
     * 관리자용 시험분석 목록 조회 (모든 상태 포함).
     *
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param schoolLevel 학교급 (MIDDLE, HIGH)
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 상태
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Operation(
        summary = "시험분석 목록 조회 (관리자)",
        description = """
                관리자용 학교별 시험분석 목록을 조회합니다.
                
                주요 기능:
                - 키워드 검색 (제목, 내용)
                - 작성자 검색
                - 학교급 필터링 (중학교/고등학교)
                - 카테고리 필터링
                - 공개/비공개 상태 필터링
                - 페이징 처리
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - schoolLevel: 학교급 필터 (MIDDLE, HIGH)
                - categoryId: 특정 카테고리만
                - isPublished: 공개 상태 필터
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC, SCHOOL_LEVEL_ASC)
                
                관리자는 모든 상태의 시험분석을 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                예시:
                - GET /api/admin/school-exams (모든 시험분석)
                - GET /api/admin/school-exams?keyword=중간고사&searchType=TITLE (제목 검색)
                - GET /api/admin/school-exams?schoolLevel=MIDDLE&categoryId=1 (중학교+카테고리)
                - GET /api/admin/school-exams?keyword=분석&schoolLevel=HIGH&isPublished=false (복합 검색)
                """
    )
    @GetMapping
    public ResponseList<ResponseSchoolExamAdminList> getSchoolExamList(
            @Parameter(description = "검색 키워드", example = "중간고사") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "학교급 (MIDDLE, HIGH)", example = "MIDDLE") 
            @RequestParam(required = false) String schoolLevel,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "공개 상태", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC, SCHOOL_LEVEL_ASC)", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[SchoolExamAdminController] 관리자 시험분석 목록 조회 요청. keyword={}, searchType={}, schoolLevel={}, categoryId={}, isPublished={}, sortBy={}", 
                 keyword, searchType, schoolLevel, categoryId, isPublished, sortBy);
        return schoolExamService.getSchoolExamListForAdmin(keyword, searchType, schoolLevel, categoryId, isPublished, sortBy, pageable);
    }

    /**
     * 시험분석 상세 조회.
     *
     * @param id 시험분석 ID
     * @return 시험분석 상세 정보
     */
    @Operation(
        summary = "시험분석 상세 조회 (관리자)",
        description = """
                시험분석의 상세 정보를 조회합니다.
                
                특징:
                - 관리자는 비공개 시험분석도 조회 가능
                - 조회수는 증가하지 않음 (관리자 조회)
                - 모든 상태의 시험분석 접근 가능
                - 첨부 파일 및 인라인 이미지 정보 포함
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseSchoolExamDetail> getSchoolExamForAdmin(
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id) {
        
        log.info("[SchoolExamAdminController] 시험분석 상세 조회 요청. ID={}", id);
        return schoolExamService.getSchoolExamForAdmin(id);
    }

    /**
     * 시험분석 생성.
     *
     * @param request 생성 요청 데이터
     * @return 생성된 시험분석 ID
     */
    @Operation(
        summary = "시험분석 생성",
        description = """
                새로운 학교별 시험분석을 생성합니다.
                
                필수 입력 사항:
                - 제목 (최대 255자)
                - 내용 (HTML 가능)
                - 학교급 (MIDDLE: 중학교, HIGH: 고등학교)
                
                선택 입력 사항:
                - 게시 여부 (기본값: true)
                - 카테고리 ID
                - 첨부 파일 목록
                - 인라인 이미지 목록
                
                주의사항:
                - 파일 첨부는 별도의 파일 업로드 API 사용
                - 에디터 이미지는 content에 HTML로 포함
                - 학교급은 필수 선택 사항
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createSchoolExam(
            @Parameter(description = "시험분석 생성 요청 데이터")
            @RequestBody @Valid RequestSchoolExamCreate request) {
        
        log.info("[SchoolExamAdminController] 시험분석 생성 요청. 제목={}, 학교급={}", 
                request.getTitle(), request.getSchoolLevel());
        return schoolExamService.createSchoolExam(request);
    }

    /**
     * 시험분석 수정.
     *
     * @param id 시험분석 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    @Operation(
        summary = "시험분석 수정",
        description = """
                기존 시험분석 정보를 수정합니다.
                
                수정 가능 항목:
                - 제목, 내용
                - 학교급 (중학교/고등학교)
                - 게시 여부
                - 카테고리 변경
                - 첨부 파일 추가/삭제
                - 인라인 이미지 추가/삭제
                
                주의사항:
                - null 값인 필드는 수정하지 않음 (기존 값 유지)
                - 파일 변경은 새 파일 추가와 기존 파일 삭제를 통해 처리
                - 학교급 변경 시 신중한 검토 필요
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseSchoolExamDetail> updateSchoolExam(
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "시험분석 수정 요청 데이터")
            @RequestBody @Valid RequestSchoolExamUpdate request) {
        
        log.info("[SchoolExamAdminController] 시험분석 수정 요청. ID={}, 새첨부파일={}, 삭제첨부파일={}", 
                id, 
                request.getNewAttachments() != null ? request.getNewAttachments().size() : 0,
                request.getDeleteAttachmentFileIds() != null ? request.getDeleteAttachmentFileIds().size() : 0);
        return schoolExamService.updateSchoolExam(id, request);
    }

    /**
     * 시험분석 삭제.
     *
     * @param id 시험분석 ID
     * @return 삭제 결과
     */
    @Operation(
        summary = "시험분석 삭제",
        description = """
                시험분석을 완전히 삭제합니다.
                
                주의사항:
                - 삭제된 시험분석은 복구할 수 없습니다
                - 첨부된 파일은 자동으로 삭제됩니다
                - 중요한 시험분석 삭제 시 신중히 검토 필요
                - 실제 운영에서는 soft delete 고려 권장
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteSchoolExam(
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id) {
        
        log.info("[SchoolExamAdminController] 시험분석 삭제 요청. ID={}", id);
        return schoolExamService.deleteSchoolExam(id);
    }

    /**
     * 조회수 증가.
     *
     * @param id 시험분석 ID
     * @return 증가 결과
     */
    @Operation(
        summary = "조회수 수동 증가",
        description = """
                특정 시험분석의 조회수를 수동으로 증가시킵니다.
                
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
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id) {
        
        log.info("[SchoolExamAdminController] 조회수 수동 증가 요청. ID={}", id);
        return schoolExamService.incrementViewCount(id);
    }

    /**
     * 공개/비공개 상태 변경.
     *
     * @param id 시험분석 ID
     * @param request 공개 상태 변경 요청 데이터
     * @return 변경 결과
     */
    @Operation(
        summary = "공개/비공개 상태 변경",
        description = """
                시험분석의 공개 상태를 변경합니다.
                
                필수 입력 사항:
                - isPublished: 공개 여부 (true/false)
                
                기능:
                - true: 공개로 변경 (목록 및 상세 조회 가능)
                - false: 비공개로 변경 (관리자만 조회 가능)
                
                응답 메시지:
                - 공개: "시험분석이 공개로 변경되었습니다."
                - 비공개: "시험분석이 비공개로 변경되었습니다."
                """
    )
    @PatchMapping("/{id}/published")
    public Response updateSchoolExamPublished(
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공개 상태 변경 요청")
            @RequestBody @Valid RequestSchoolExamPublishedUpdate request) {
        
        log.info("[SchoolExamAdminController] 공개 상태 변경 요청. ID={}, 공개여부={}", 
                id, request.getIsPublished());
        return schoolExamService.updateSchoolExamPublished(id, request);
    }

    /**
     * 카테고리별 시험분석 통계.
     *
     * @return 카테고리별 통계 정보
     */
    @Operation(
        summary = "카테고리별 시험분석 통계",
        description = """
                각 카테고리별 시험분석 개수 통계를 조회합니다.
                
                반환 데이터:
                - 카테고리명
                - 해당 카테고리의 시험분석 개수
                
                용도:
                - 관리자 대시보드 통계
                - 카테고리 관리 참고 자료
                """
    )
    @GetMapping("/stats/by-category")
    public ResponseData<List<Object[]>> getSchoolExamStatsByCategory() {
        
        log.info("[SchoolExamAdminController] 카테고리별 통계 조회 요청");
        return schoolExamService.getSchoolExamStatsByCategory();
    }

    /**
     * 학교급별 시험분석 통계.
     *
     * @return 학교급별 통계 정보
     */
    @Operation(
        summary = "학교급별 시험분석 통계",
        description = """
                각 학교급별 시험분석 개수 통계를 조회합니다.
                
                반환 데이터:
                - 학교급 (MIDDLE, HIGH)
                - 해당 학교급의 시험분석 개수
                
                용도:
                - 관리자 대시보드 통계
                - 학교급별 콘텐츠 현황 파악
                """
    )
    @GetMapping("/stats/by-school-level")
    public ResponseData<List<Object[]>> getSchoolExamStatsBySchoolLevel() {
        
        log.info("[SchoolExamAdminController] 학교급별 통계 조회 요청");
        return schoolExamService.getSchoolExamStatsBySchoolLevel();
    }
}