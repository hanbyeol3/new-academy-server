package com.academy.api.improvement.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.improvement.dto.*;
import com.academy.api.improvement.service.ImprovementCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * 성적 향상 사례 관리자 API 컨트롤러.
 * 
 * 관리자 권한이 필요한 성적 향상 사례 관리 기능을 제공합니다.
 * 모든 사례의 CRUD 및 상태 관리가 가능합니다.
 */
@Tag(name = "ImprovementCase (Admin)", description = "성적 향상 사례 관리자 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/improvement-cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImprovementCaseAdminController {
    
    private final ImprovementCaseService improvementCaseService;
    
    @Operation(
        summary = "성적 향상 사례 목록 조회 (관리자)",
        description = """
                모든 성적 향상 사례 목록을 조회합니다.
                
                특징:
                - 소프트 삭제된 사례 제외 (별도 필터 제공)
                - 공개/비공개 모든 사례 조회 가능
                - 작성자 유형별 필터링 가능
                - 고정글, 공개 상태 필터 제공
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - writerType: 작성자 유형 (EXTERNAL, ADMIN)
                - division: 학년 구분
                - subject: 과목
                - isPublished: 공개 여부
                - isPinned: 고정글 여부
                - sortBy: 정렬 기준
                
                정렬 옵션:
                - PINNED_FIRST: 고정글 우선 (기본값)
                - CREATED_DESC: 최신순
                - CREATED_ASC: 오래된순
                - VIEW_COUNT_DESC: 조회수 많은순
                
                추가 정보:
                - 등록자/수정자 정보 포함
                - 전체 통계 정보 제공
                """
    )
    @GetMapping
    public ResponseList<ResponseImprovementCaseAdminList> getCaseList(
            @Parameter(description = "검색 키워드", example = "수학")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL")
            @RequestParam(required = false) String searchType,
            
            @Parameter(description = "작성자 유형 (EXTERNAL, ADMIN)", example = "EXTERNAL")
            @RequestParam(required = false) String writerType,
            
            @Parameter(description = "학년 구분", example = "HIGH_3")
            @RequestParam(required = false) String division,
            
            @Parameter(description = "과목", example = "MATH")
            @RequestParam(required = false) String subject,
            
            @Parameter(description = "공개 여부", example = "true")
            @RequestParam(required = false) Boolean isPublished,
            
            @Parameter(description = "고정글 여부", example = "false")
            @RequestParam(required = false) Boolean isPinned,
            
            @Parameter(description = "정렬 기준", example = "PINNED_FIRST")
            @RequestParam(required = false) String sortBy,
            
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("관리자 사례 목록 조회. keyword={}, writerType={}, isPublished={}, isPinned={}",
                keyword, writerType, isPublished, isPinned);
        
        return improvementCaseService.getAdminCaseList(
                keyword, searchType, writerType, division, subject,
                isPublished, isPinned, sortBy, pageable);
    }
    
    @Operation(
        summary = "성적 향상 사례 상세 조회 (관리자)",
        description = """
                성적 향상 사례의 상세 정보를 조회합니다.
                
                특징:
                - 공개/비공개 관계없이 모든 사례 조회 가능
                - 비밀글도 비밀번호 없이 조회 가능
                - 조회수는 증가하지 않음
                - 삭제된 사례도 조회 가능 (별도 표시)
                
                추가 정보:
                - 등록자/수정자 상세 정보
                - 첨부파일 목록
                - 이전글/다음글 정보
                - 비밀번호 설정 여부 (해시값은 미포함)
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseImprovementCaseDetail> getCaseDetail(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("관리자 사례 상세 조회. ID={}", id);
        
        return improvementCaseService.getAdminCaseDetail(id);
    }
    
    @Operation(
        summary = "성적 향상 사례 생성 (관리자)",
        description = """
                관리자가 성적 향상 사례를 생성합니다.
                
                필수 입력:
                - title: 제목
                - authorName: 작성자명 (학생명)
                - content: 내용
                
                선택 입력:
                - writerType: 작성자 유형 (기본값: ADMIN)
                - phoneNumber: 연락처
                - division: 학년 (MIDDLE, HIGH, RETAKE)
                - subject: 과목 (ALL, KOR, ENG, MATH, SOC, SCI)
                - gradeType: 성적 유형 (SCORE: 점수, GRADE: 등급)
                - prevResult: 이전 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)
                - nextResult: 이후 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)
                - isPublished: 공개 여부 (기본값: true)
                - isPinned: 고정글 여부 (기본값: false)
                - isSecret: 비밀글 여부
                - password: 비밀번호 (비밀글인 경우)
                
                파일 첨부:
                - uploadFileIds: 첨부파일 ID 배열
                
                관리자 권한:
                - 고정글 설정 가능
                - 작성자 유형 선택 가능 (대리 작성)
                - 즉시 공개/비공개 설정 가능
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createCase(
            @Parameter(description = "사례 생성 요청")
            @RequestBody @Valid RequestImprovementCaseCreate request,
            
            @Parameter(description = "첨부파일 ID 목록", example = "[1, 2, 3]")
            @RequestParam(required = false) Long[] uploadFileIds) {
        
        log.info("관리자 사례 생성. 제목={}, 작성자유형={}, 고정글={}",
                request.getTitle(), request.getWriterType(), request.getIsPinned());
        
        return improvementCaseService.createAdminCase(request, uploadFileIds);
    }
    
    @Operation(
        summary = "성적 향상 사례 수정 (관리자)",
        description = """
                성적 향상 사례를 수정합니다.
                
                수정 가능 항목:
                - 모든 필드 수정 가능
                - 작성자 정보 수정 가능
                - 고정글 설정 변경 가능
                - 공개 상태 변경 가능
                
                파일 관리:
                - 기존 파일 전체 교체
                - uploadFileIds가 빈 배열이면 모든 파일 제거
                - null이면 기존 파일 유지
                
                주의사항:
                - 작성자 유형은 변경 불가
                - 수정자 정보 자동 기록
                """
    )
    @PutMapping("/{id}")
    public Response updateCase(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "수정 요청")
            @RequestBody @Valid RequestImprovementCaseAdminUpdate request,
            
            @Parameter(description = "첨부파일 ID 목록", example = "[1, 2, 3]")
            @RequestParam(required = false) Long[] uploadFileIds) {
        
        log.info("관리자 사례 수정. ID={}", id);
        
        return improvementCaseService.updateAdminCase(id, request, uploadFileIds);
    }
    
    @Operation(
        summary = "성적 향상 사례 삭제 (관리자)",
        description = """
                성적 향상 사례를 소프트 삭제합니다.
                
                삭제 방식:
                - 소프트 삭제 (deletedAt 설정)
                - 데이터는 보존되나 일반 조회 불가
                - 관리자는 삭제된 사례도 조회 가능
                - 복구 API를 통해 복원 가능
                
                연관 데이터:
                - 첨부파일 링크는 유지
                - 실제 파일은 별도 관리
                
                주의사항:
                - 완전 삭제는 지원하지 않음
                - 삭제 후에도 통계에는 포함될 수 있음
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteCase(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("관리자 사례 삭제. ID={}", id);
        
        return improvementCaseService.deleteAdminCase(id);
    }
    
    @Operation(
        summary = "성적 향상 사례 복구 (관리자)",
        description = """
                소프트 삭제된 성적 향상 사례를 복구합니다.
                
                복구 과정:
                - deletedAt을 null로 설정
                - 기존 상태 그대로 복원
                - 첨부파일 링크도 함께 복구
                
                주의사항:
                - 이미 활성 상태인 사례는 복구 불가
                - 복구 후 즉시 목록에 표시됨
                """
    )
    @PostMapping("/{id}/restore")
    public Response restoreCase(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("관리자 사례 복구. ID={}", id);
        
        return improvementCaseService.restoreAdminCase(id);
    }
    
    @Operation(
        summary = "공개 상태 변경",
        description = """
                성적 향상 사례의 공개 상태를 변경합니다.
                
                상태 변경:
                - true: 공개 (일반 사용자 조회 가능)
                - false: 비공개 (관리자만 조회 가능)
                
                영향 범위:
                - 목록 노출 여부
                - 상세 조회 가능 여부
                - 검색 결과 포함 여부
                """
    )
    @PatchMapping("/{id}/publish")
    public Response updatePublishStatus(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "공개 여부", example = "true")
            @RequestParam Boolean isPublished) {
        
        log.info("공개 상태 변경. ID={}, isPublished={}", id, isPublished);
        
        return improvementCaseService.updatePublishStatus(id, isPublished);
    }
    
    @Operation(
        summary = "고정글 상태 변경",
        description = """
                성적 향상 사례의 고정글 상태를 변경합니다.
                
                상태 변경:
                - true: 고정 (목록 상단에 표시)
                - false: 고정 해제 (일반 정렬)
                
                표시 방식:
                - 고정글은 항상 목록 상단에 표시
                - 고정글끼리는 최신순 정렬
                - 고정글 개수 제한 없음
                """
    )
    @PatchMapping("/{id}/pin")
    public Response updatePinnedStatus(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "고정 여부", example = "true")
            @RequestParam Boolean isPinned) {
        
        log.info("고정글 상태 변경. ID={}, isPinned={}", id, isPinned);
        
        return improvementCaseService.updatePinnedStatus(id, isPinned);
    }
    
    @Operation(
        summary = "조회수 수동 증가",
        description = """
                성적 향상 사례의 조회수를 수동으로 1 증가시킵니다.
                
                주의사항:
                - 일반적으로는 상세 조회 시 자동 증가
                - 관리자만 수동 증가 가능
                """
    )
    @PostMapping("/{id}/increment-view")
    public Response incrementViewCount(
            @Parameter(description = "사례 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("[ImprovementCaseAdminController] 조회수 수동 증가 요청. ID={}", id);
        return improvementCaseService.incrementViewCount(id);
    }
}