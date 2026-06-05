package com.academy.api.schoolexam.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schoolexam.dto.ResponseSchoolExamDetail;
import com.academy.api.schoolexam.dto.ResponseSchoolExamPublicList;
import com.academy.api.schoolexam.service.SchoolExamService;
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
 * 학교별 시험분석 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 학교별 시험분석 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개된 시험분석만 조회됩니다.
 */
@Tag(name = "SchoolExam (Public)", description = "일반 사용자용 학교별 시험분석 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/school-exams")
@RequiredArgsConstructor
public class SchoolExamPublicController {

    private final SchoolExamService schoolExamService;

    @Operation(
        summary = "시험분석 목록 조회 (공개)",
        description = """
                공개된 학교별 시험분석 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개된 시험분석만 표시
                - 비공개 시험분석은 제외
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - schoolLevel: 학교급 필터 (MIDDLE, HIGH)
                - categoryId: 특정 카테고리만
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC, SCHOOL_LEVEL_ASC)
                
                정렬 옵션:
                - CREATED_DESC: 생성일시 내림차순 (기본값)
                - CREATED_ASC: 생성일시 오름차순
                - VIEW_COUNT_DESC: 조회수 내림차순
                - SCHOOL_LEVEL_ASC: 학교급 오름차순 (중학교→고등학교)
                
                예시:
                - GET /api/school-exams (모든 공개 시험분석)
                - GET /api/school-exams?schoolLevel=MIDDLE (중학교 시험분석만)
                - GET /api/school-exams?keyword=중간고사&schoolLevel=HIGH (고등학교 중간고사 검색)
                - GET /api/school-exams?categoryId=1&sortBy=VIEW_COUNT_DESC (특정 카테고리, 조회수순)
                """
    )
    @GetMapping
    public ResponseList<ResponseSchoolExamPublicList> getSchoolExamList(
            @Parameter(description = "검색 키워드", example = "중간고사") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "학교급 (MIDDLE, HIGH)", example = "MIDDLE") 
            @RequestParam(required = false) String schoolLevel,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC, SCHOOL_LEVEL_ASC)", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[SchoolExamPublicController] 공개 시험분석 목록 조회 요청. keyword={}, searchType={}, schoolLevel={}, categoryId={}, sortBy={}", 
                keyword, searchType, schoolLevel, categoryId, sortBy);
        
        // 공개용은 기본 정렬 적용
        String effectiveSortBy = sortBy != null ? sortBy : "CREATED_DESC";
        
        return schoolExamService.getSchoolExamListForPublic(keyword, searchType, schoolLevel, categoryId, effectiveSortBy, pageable);
    }

    @Operation(
        summary = "시험분석 상세 조회 (공개)",
        description = """
                학교별 시험분석의 상세 정보를 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개된 시험분석만 조회 가능
                - 조회 시 자동으로 조회수 증가
                - 비공개 시험분석 조회 시 404 에러
                - 첨부 파일 및 인라인 이미지 정보 포함
                - 이전글/다음글 네비게이션 정보 포함
                
                자동 처리:
                - 조회수 1 증가
                - 조회 시각 기록 (로그)
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseSchoolExamDetail> getSchoolExam(
            @Parameter(description = "시험분석 ID", example = "1") @PathVariable Long id) {
        
        log.info("[SchoolExamPublicController] 공개 시험분석 상세 조회 요청. ID={}", id);
        
        // 공개용은 조회수 자동 증가
        return schoolExamService.getSchoolExamForPublic(id);
    }
}