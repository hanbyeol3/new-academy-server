package com.academy.api.academy.controller;

import com.academy.api.academy.dto.RequestAcademyInfoUpdate;
import com.academy.api.academy.dto.RequestInstructorVideoUpdate;
import com.academy.api.academy.dto.ResponseAcademyInfo;
import com.academy.api.academy.service.AcademyInfoService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 학원 정보 관리자 컨트롤러.
 */
@Tag(name = "Academy Info (Admin)", description = "관리자 권한이 필요한 학원 정보 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/academy-info")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AcademyInfoAdminController {

    private final AcademyInfoService academyInfoService;

    @GetMapping
    @Operation(
        summary = "학원 정보 조회",
        description = """
                학원 기본 정보를 조회합니다.
                
                특징:
                - 단일 설정 테이블로 항상 1개의 레코드만 존재
                - 데이터가 없을 경우 기본값으로 자동 생성 후 반환
                - 학원 운영에 필요한 모든 기본 정보 포함
                
                조회 정보:
                - 학원 기본 정보 (이름, 대표자, 연락처)
                - 위치 정보 (주소, 좌표)
                - 운영시간 정보
                - 사이트 메타데이터 (SEO)
                - SNS 링크 정보
                """
    )
    public ResponseData<ResponseAcademyInfo> getAcademyInfo() {
        log.info("학원 정보 조회 요청");
        return academyInfoService.getAcademyInfo();
    }

    @PutMapping
    @Operation(
        summary = "학원 정보 수정",
        description = """
                학원 기본 정보를 수정합니다.
                
                필수 입력 사항:
                - 학원명 (120자 이하)
                - 캠퍼스명 (120자 이하)
                
                선택 입력 사항:
                - 사업자 정보 (사업자번호, 대표자명, 원장명)
                - 연락처 정보 (전화번호, 상담전화, 팩스, 이메일)
                - 위치 정보 (우편번호, 주소, 상세주소, 좌표)
                - 운영시간 정보
                - 사이트 메타데이터
                - SNS 링크 정보
                
                주의사항:
                - 수정 시각과 수정자 정보가 자동으로 업데이트됩니다
                - 데이터가 없을 경우 새로 생성 후 수정됩니다
                """
    )
    public Response updateAcademyInfo(
            @Parameter(description = "학원 정보 수정 요청") 
            @RequestBody @Valid RequestAcademyInfoUpdate request) {
        
        log.info("학원 정보 수정 요청. academyName={}", 
                request.getAcademyName());
        
        return academyInfoService.updateAcademyInfo(request);
    }

    @PutMapping("/instructor-video")
    @Operation(
        summary = "강사진 유튜브 URL 수정",
        description = """
                강사진 소개 유튜브 URL을 수정합니다.
                
                특징:
                - 강사진 소개 영상 URL만 개별 수정
                - 다른 학원 정보에는 영향 없음
                - 관리자 권한 필요
                
                입력 사항:
                - instructorYoutubeUrl: 유튜브 URL (255자 이하)
                
                사용 예시:
                - https://www.youtube.com/watch?v=example
                - https://youtu.be/example
                - 빈 문자열로 설정 시 URL 제거
                
                주의사항:
                - 수정 시각과 수정자 정보가 자동으로 업데이트됩니다
                - 데이터가 없을 경우 새로 생성 후 수정됩니다
                """
    )
    public Response updateInstructorVideo(
            @Parameter(description = "강사진 유튜브 URL 수정 요청") 
            @RequestBody @Valid RequestInstructorVideoUpdate request) {
        
        log.info("강사진 유튜브 URL 수정 요청. url={}", 
                request.getInstructorYoutubeUrl());
        
        return academyInfoService.updateInstructorVideo(request);
    }
}