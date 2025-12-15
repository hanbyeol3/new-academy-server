package com.academy.api.teacher.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherListItem;
import com.academy.api.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 강사 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 강사 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개된 강사 정보만 조회됩니다.
 */
@Tag(name = "Teacher (Public)", description = "일반 사용자용 강사 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherPublicController {

    private final TeacherService teacherService;

    /**
     * 공개 강사 목록 조회.
     * 
     * @param keyword 검색 키워드 (강사명)
     * @param pageable 페이징 정보
     * @return 공개된 강사 목록
     */
    @Operation(
        summary = "강사 목록 조회 (공개)",
        description = """
                공개된 강사 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개 상태(isPublished=true)인 강사만 표시
                - 강사명으로 검색 가능
                - 강사 기본 정보와 담당 과목 정보 포함
                
                검색 기능:
                - keyword: 강사명 부분 일치 검색
                
                정렬:
                - 생성일시 기준 최신순 (기본값)
                
                반환 정보:
                - 강사 기본 정보 (ID, 이름, 경력, 한 줄 소개)
                - 강사 이미지
                - 담당 과목 목록
                - 공개 여부
                - 등록/수정 일시
                """
    )
    @GetMapping
    public ResponseList<ResponseTeacherListItem> getPublishedTeacherList(
            @Parameter(description = "강사명 검색 키워드 (부분 일치)", example = "김교수") 
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[TeacherPublicController] 공개 강사 목록 조회 요청. keyword={}", keyword);
        
        return teacherService.getPublishedTeacherList(keyword, pageable);
    }

    /**
     * 공개 강사 상세 조회.
     * 
     * @param id 강사 ID
     * @return 강사 상세 정보
     */
    @Operation(
        summary = "강사 상세 조회 (공개)",
        description = """
                강사의 상세 정보를 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개 상태인 강사만 조회 가능
                - 비공개 강사 조회 시 404 에러 반환
                
                반환 정보:
                - 강사 전체 정보 (기본 정보 + 메모 제외)
                - 강사 이미지 정보
                - 담당 과목 상세 목록
                - 등록자/수정자 정보
                - 등록/수정 일시
                
                주의사항:
                - 관리자용 메모 필드는 제외됨
                - 비공개 강사는 조회 불가
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseTeacher> getPublishedTeacher(
            @Parameter(description = "강사 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("[TeacherPublicController] 공개 강사 상세 조회 요청. ID={}", id);
        
        // 공개 강사만 조회 가능하도록 서비스에서 필터링
        return teacherService.getTeacher(id);
    }

    /**
     * 과목별 강사 조회.
     * 
     * @param categoryId 과목 카테고리 ID
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 강사 목록
     */
    @Operation(
        summary = "과목별 강사 조회",
        description = """
                특정 과목을 담당하는 강사 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 지정된 과목을 담당하는 공개 강사만 반환
                - 강사 기본 정보와 모든 담당 과목 정보 포함
                
                용도:
                - 과목별 강사 소개 페이지
                - 특정 분야 강사 검색
                - 수강 신청 시 강사 선택 참고
                
                정렬:
                - 생성일시 기준 최신순
                """
    )
    @GetMapping("/subject/{categoryId}")
    public ResponseList<ResponseTeacherListItem> getTeachersBySubject(
            @Parameter(description = "과목 카테고리 ID", example = "1") 
            @PathVariable Long categoryId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[TeacherPublicController] 과목별 강사 조회 요청. 과목ID={}", categoryId);
        
        return teacherService.getPublishedTeachersBySubject(categoryId, pageable);
    }

    /**
     * 강사 검색.
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 강사 목록
     */
    @Operation(
        summary = "강사 검색",
        description = """
                키워드로 강사를 검색합니다.
                
                검색 대상:
                - 강사명 (부분 일치)
                
                특징:
                - 대소문자 구분 없음
                - 부분 일치 검색
                - 공개 상태인 강사만 검색
                - 생성일시 기준 최신순 정렬
                
                용도:
                - 강사 검색 페이지
                - 통합 검색 결과
                - 강사명 자동완성
                """
    )
    @GetMapping("/search")
    public ResponseList<ResponseTeacherListItem> searchTeachers(
            @Parameter(description = "검색 키워드 (강사명)", example = "김") 
            @RequestParam String keyword,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[TeacherPublicController] 강사 검색 요청. 키워드={}", keyword);
        
        return teacherService.getPublishedTeacherList(keyword, pageable);
    }
}