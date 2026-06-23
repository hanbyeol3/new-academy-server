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
import org.springframework.web.bind.annotation.*;

/**
 * 성적 향상 사례 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 성적 향상 사례 기능을 제공합니다.
 * 학생/학부모가 직접 작성하거나 공개된 사례를 조회할 수 있습니다.
 */
@Tag(name = "ImprovementCase (Public)", description = "성적 향상 사례 공개 API")
@Slf4j
@RestController
@RequestMapping("/api/improvement-cases")
@RequiredArgsConstructor
public class ImprovementCasePublicController {
    
    private final ImprovementCaseService improvementCaseService;
    
    @Operation(
        summary = "성적 향상 사례 목록 조회",
        description = """
                공개된 성적 향상 사례 목록을 조회합니다.
                
                특징:
                - 소프트 삭제된 사례는 제외
                - 공개 상태인 사례만 표시
                - 고정글이 상단에 우선 표시
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - division: 학년 구분
                - subject: 과목
                - sortBy: 정렬 기준
                
                정렬 옵션:
                - PINNED_FIRST: 고정글 우선 (기본값)
                - CREATED_DESC: 최신순
                - CREATED_ASC: 오래된순
                - VIEW_COUNT_DESC: 조회수 많은순
                """
    )
    @GetMapping
    public ResponseList<ResponseImprovementCasePublicList> getCaseList(
            @Parameter(description = "검색 키워드", example = "수학")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL")
            @RequestParam(required = false) String searchType,
            
            @Parameter(description = "학년 구분", example = "HIGH_3")
            @RequestParam(required = false) String division,
            
            @Parameter(description = "과목", example = "MATH")
            @RequestParam(required = false) String subject,
            
            @Parameter(description = "정렬 기준", example = "PINNED_FIRST")
            @RequestParam(required = false) String sortBy,
            
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("성적 향상 사례 목록 조회. keyword={}, searchType={}, division={}, subject={}",
                keyword, searchType, division, subject);
        
        return improvementCaseService.getPublicCaseList(
                keyword, searchType, division, subject, sortBy, pageable);
    }
    
    @Operation(
        summary = "성적 향상 사례 상세 조회",
        description = """
                성적 향상 사례의 상세 정보를 조회합니다.
                
                특징:
                - 공개된 사례만 조회 가능
                - 조회시 자동으로 조회수 1 증가
                - 비밀번호가 설정된 글의 경우 401 에러 반환
                - 삭제된 사례 조회시 404 에러
                
                비밀번호 보호 글 조회:
                - 별도의 비밀번호 검증 API 사용
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseImprovementCaseDetail> getCaseDetail(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("성적 향상 사례 상세 조회. ID={}", id);
        
        return improvementCaseService.getPublicCaseDetail(id);
    }
    
    @Operation(
        summary = "비밀번호 보호 글 상세 조회",
        description = """
                비밀번호로 보호된 성적 향상 사례를 조회합니다.
                
                특징:
                - 비밀번호 검증 후 내용 표시
                - BCrypt로 암호화된 비밀번호 검증
                - 잘못된 비밀번호시 401 에러
                - 조회시 자동으로 조회수 1 증가
                """
    )
    @PostMapping("/{id}/verify")
    public ResponseData<ResponseImprovementCaseDetail> getSecretCaseDetail(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "비밀번호 검증 요청")
            @RequestBody @Valid RequestPasswordVerify request) {
        
        log.info("비밀번호 보호 글 상세 조회. ID={}", id);
        
        return improvementCaseService.getSecretCaseDetail(id, request.getPassword());
    }
    
    @Operation(
        summary = "성적 향상 사례 작성",
        description = """
                학생/학부모가 직접 성적 향상 사례를 작성합니다.
                
                필수 입력:
                - title: 제목
                - authorName: 작성자명
                - content: 내용
                
                선택 입력:
                - phoneNumber: 연락처
                - division: 학년 (MIDDLE, HIGH, RETAKE)
                - subject: 과목 (ALL, KOR, ENG, MATH, SOC, SCI)
                - gradeType: 성적 유형 (SCORE: 점수, GRADE: 등급)
                - prevResult: 이전 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)
                - nextResult: 이후 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)
                - password: 비밀번호 (외부 작성자의 경우 필수)
                
                파일 첨부:
                - uploadFileIds: 첨부파일 ID 배열
                - 성적표, 수상 증명서 등 첨부 가능
                
                주의사항:
                - 작성자 유형은 자동으로 EXTERNAL로 설정
                - 외부 작성자의 경우 비밀번호 필수
                - 첨부파일은 미리 업로드 후 ID 전달
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createCase(
            @Parameter(description = "사례 생성 요청")
            @RequestBody @Valid RequestImprovementCaseCreate request,
            
            @Parameter(description = "첨부파일 ID 목록", example = "[1, 2, 3]")
            @RequestParam(required = false) Long[] uploadFileIds) {
        
        log.info("성적 향상 사례 작성. 제목={}, 작성자={}", request.getTitle(), request.getAuthorName());
        
        return improvementCaseService.createPublicCase(request, uploadFileIds);
    }
    
    @Operation(
        summary = "성적 향상 사례 수정",
        description = """
                작성자가 본인이 작성한 사례를 수정합니다.
                
                검증 절차:
                - 외부 작성자(EXTERNAL)만 수정 가능
                - 비밀번호 검증 필수
                - 관리자가 작성한 사례는 수정 불가
                
                수정 가능 항목:
                - 제목, 내용
                - 학년, 과목
                - 성적 변화 정보
                - 공개 여부
                - 첨부파일
                
                주의사항:
                - 작성자명, 연락처는 수정 불가
                - 고정글 설정은 관리자만 가능
                """
    )
    @PutMapping("/{id}")
    public Response updateCase(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "수정 요청")
            @RequestBody @Valid RequestImprovementCaseUpdate request,
            
            @Parameter(description = "첨부파일 ID 목록", example = "[1, 2, 3]")
            @RequestParam(required = false) Long[] uploadFileIds) {
        
        log.info("성적 향상 사례 수정. ID={}", id);
        
        return improvementCaseService.updatePublicCase(id, request, uploadFileIds);
    }
    
    @Operation(
        summary = "성적 향상 사례 삭제",
        description = """
                작성자가 본인이 작성한 사례를 삭제합니다.
                
                검증 절차:
                - 외부 작성자(EXTERNAL)만 삭제 가능
                - 비밀번호 검증 필수
                
                삭제 방식:
                - 소프트 삭제 (deletedAt 설정)
                - 데이터는 보존되나 목록/조회 불가
                - 관리자는 복구 가능
                
                주의사항:
                - 관리자가 작성한 사례는 삭제 불가
                - 삭제 후 복구는 관리자에게 문의
                """
    )
    @PostMapping("/{id}/delete")
    public Response deleteCase(
            @Parameter(description = "사례 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "삭제 요청")
            @RequestBody @Valid RequestImprovementCaseDelete request) {
        
        log.info("성적 향상 사례 삭제 요청. ID={}", id);
        
        return improvementCaseService.deletePublicCase(id, request);
    }
}