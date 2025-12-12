package com.academy.api.teacher.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.teacher.dto.RequestTeacherCreate;
import com.academy.api.teacher.dto.RequestTeacherUpdate;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherListItem;
import com.academy.api.teacher.service.TeacherService;
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
 * 강사 관리자 API 컨트롤러.
 *
 * 강사의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Teacher (Admin)", description = "강사 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TeacherAdminController {

    private final TeacherService teacherService;

    /**
     * 관리자용 강사 목록 조회 (모든 상태 포함).
     *
     * @param keyword 검색 키워드 (강사명)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Operation(
        summary = "강사 목록 조회 (관리자)",
        description = """
                관리자용 강사 목록을 조회합니다.
                
                주요 기능:
                - 강사명 검색 (부분 일치)
                - 공개/비공개 상태 모두 조회 가능
                - 담당 과목 정보 포함
                - 페이징 처리
                
                정렬:
                - 생성일시 내림차순 (기본값)
                
                반환 정보:
                - 강사 기본 정보 (ID, 이름, 경력, 소개)
                - 강사 이미지 정보
                - 담당 과목 목록
                - 공개 여부
                - 등록/수정 일시
                
                관리자는 비공개 강사도 모두 조회할 수 있습니다.
                """
    )
    @GetMapping
    public ResponseList<ResponseTeacherListItem> getTeacherList(
            @Parameter(description = "강사명 검색 키워드", example = "김교수") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[TeacherAdminController] 관리자 강사 목록 조회 요청. keyword={}", keyword);
        return teacherService.getTeacherList(keyword, pageable);
    }

    /**
     * 강사 상세 조회.
     *
     * @param id 강사 ID
     * @return 강사 상세 정보
     */
    @Operation(
        summary = "강사 상세 조회 (관리자)",
        description = """
                강사의 상세 정보를 조회합니다.
                
                특징:
                - 관리자는 비공개 강사도 조회 가능
                - 모든 정보 포함 (메모 필드 포함)
                - 등록자/수정자 정보 포함
                
                반환 정보:
                - 강사 전체 정보 (메모 포함)
                - 강사 이미지 정보
                - 담당 과목 상세 목록
                - 등록자/수정자 정보
                - 등록/수정 일시
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseTeacher> getTeacher(
            @Parameter(description = "강사 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("[TeacherAdminController] 강사 상세 조회 요청. ID={}", id);
        return teacherService.getTeacher(id);
    }

    /**
     * 강사 생성.
     *
     * @param request 강사 생성 요청
     * @return 생성된 강사 ID
     */
    @Operation(
        summary = "강사 생성",
        description = """
                새로운 강사를 생성합니다.
                
                필수 입력 사항:
                - teacherName (강사명)
                
                선택 입력 사항:
                - career (경력/약력 소개)
                - imageTempFileId + imageFileName (강사 이미지)
                - introText (한 줄 소개문)
                - memo (관리자용 메모)
                - isPublished (공개 여부, 기본값: true)
                - subjectCategoryIds (담당 과목 ID 목록)
                
                이미지 처리:
                - imageTempFileId: 임시파일 업로드 후 받은 ID
                - imageFileName: 원본 파일명
                - 임시파일은 정식파일로 자동 변환
                
                과목 연결:
                - subjectCategoryIds: 카테고리 ID 배열
                - 빈 배열 또는 null 가능
                
                주의사항:
                - 강사명 중복 불가
                - 존재하지 않는 카테고리 ID 포함 시 에러
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createTeacher(
            @Parameter(description = "강사 생성 요청") 
            @RequestBody @Valid RequestTeacherCreate request) {
        
        log.info("[TeacherAdminController] 강사 생성 요청. teacherName={}", request.getTeacherName());
        return teacherService.createTeacher(request);
    }

    /**
     * 강사 수정.
     *
     * @param id 강사 ID
     * @param request 강사 수정 요청
     * @return 수정된 강사 정보
     */
    @Operation(
        summary = "강사 수정",
        description = """
                기존 강사 정보를 수정합니다.
                
                수정 가능 항목:
                - teacherName (강사명)
                - career (경력/약력 소개)
                - 이미지 관리 (새 업로드 또는 삭제)
                - introText (한 줄 소개문)
                - memo (관리자용 메모)
                - isPublished (공개 여부)
                - subjectCategoryIds (담당 과목 ID 목록)
                
                이미지 수정 옵션:
                1. 새 이미지 업로드: imageTempFileId + imageFileName
                2. 기존 이미지 삭제: deleteImage = true
                3. 이미지 유지: 위 필드들을 설정하지 않음
                
                과목 수정:
                - 기존 과목은 모두 해제되고 새로 설정됨
                - 빈 배열 설정 시 모든 과목 해제
                
                부분 수정:
                - null인 필드는 기존 값 유지
                - 빈 문자열("")은 실제로 빈 값으로 설정
                
                주의사항:
                - 강사명 중복 검사 (자신 제외)
                - 존재하지 않는 카테고리 ID 포함 시 에러
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseTeacher> updateTeacher(
            @Parameter(description = "수정할 강사 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "강사 수정 요청") 
            @RequestBody @Valid RequestTeacherUpdate request) {
        
        log.info("[TeacherAdminController] 강사 수정 요청. id={}, teacherName={}", id, request.getTeacherName());
        return teacherService.updateTeacher(id, request);
    }

    /**
     * 강사 삭제.
     *
     * @param id 강사 ID
     * @return 삭제 결과
     */
    @Operation(
        summary = "강사 삭제",
        description = """
                강사를 삭제합니다.
                
                삭제 처리:
                - 강사-과목 연결 관계 모두 삭제
                - 강사 이미지 파일 삭제
                - 강사 엔티티 삭제
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 연관된 모든 데이터가 함께 삭제됩니다
                - 실제 운영에서는 soft delete 고려 권장
                
                권한:
                - ADMIN 권한 필수
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteTeacher(
            @Parameter(description = "삭제할 강사 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("[TeacherAdminController] 강사 삭제 요청. id={}", id);
        return teacherService.deleteTeacher(id);
    }

    /**
     * 강사 공개/비공개 상태 변경.
     *
     * @param id 강사 ID
     * @param isPublished 공개 여부
     * @return 상태 변경 결과
     */
    @Operation(
        summary = "강사 공개/비공개 상태 변경",
        description = """
                강사의 공개/비공개 상태를 변경합니다.
                
                기능:
                - 개별 강사의 공개 상태만 변경
                - 다른 정보는 그대로 유지
                
                상태 변경:
                - true: 공개 (일반 사용자에게 노출)
                - false: 비공개 (관리자만 조회 가능)
                
                용도:
                - 긴급하게 강사 노출을 중단해야 할 때
                - 강사 정보 수정 중 임시 비공개
                - 퇴직한 강사의 노출 중단
                
                권한:
                - ADMIN 권한 필수
                """
    )
    @PatchMapping("/{id}/published")
    public Response updatePublishedStatus(
            @Parameter(description = "강사 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "공개 여부", example = "true") 
            @RequestParam Boolean isPublished) {
        
        log.info("[TeacherAdminController] 강사 공개상태 변경 요청. id={}, isPublished={}", id, isPublished);
        return teacherService.updatePublishedStatus(id, isPublished);
    }
}