package com.academy.api.academy.controller;

import com.academy.api.academy.dto.ResponseAcademyAbout;
import com.academy.api.academy.dto.ResponseAcademyAboutDetails;
import com.academy.api.academy.service.AcademyAboutService;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 학원 소개 정보 공개 컨트롤러.
 */
@Tag(name = "Academy About (Public)", description = "모든 사용자가 접근 가능한 학원 소개 정보 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/academy-about")
@RequiredArgsConstructor
public class AcademyAboutPublicController {

    private final AcademyAboutService academyAboutService;

    @GetMapping
    @Operation(
        summary = "학원 소개 정보 조회 (공개)",
        description = """
                학원 소개 메인 정보를 조회합니다. (공개 API)
                
                특징:
                - 인증 없이 접근 가능한 공개 API
                - 홈페이지나 소개 페이지에서 사용
                - 데이터가 없을 경우 404 에러 반환 (관리자용과 다름)
                
                응답 정보:
                - 메인 타이틀 및 포인트 타이틀
                - 메인 설명 (HTML 형태 가능)
                - 메인 이미지 경로
                - 등록/수정 일시 정보 (민감 정보 제외)
                """
    )
    public ResponseData<ResponseAcademyAbout> getPublicAcademyAbout() {
        log.info("학원 소개 정보 조회 요청 (공개)");
        return academyAboutService.getPublicAcademyAbout();
    }

    @GetMapping("/details")
    @Operation(
        summary = "학원 소개 상세 목록 조회 (공개)",
        description = """
                학원 소개 상세 정보 목록을 조회합니다. (공개 API)
                
                특징:
                - 인증 없이 접근 가능한 공개 API
                - 정렬 순서(sortOrder) 기준으로 정렬된 목록 반환
                - 홈페이지 학원 소개 섹션에서 사용
                
                응답 정보:
                - 상세 정보 ID 및 타이틀
                - 상세 설명 (HTML 형태 가능)
                - 정렬 순서 (화면 표시 순서 결정)
                - 등록/수정 일시 정보
                
                주의사항:
                - 관리자 정보(등록자 ID, 수정자 ID)는 포함되지 않습니다
                - 삭제되거나 비공개 처리된 항목은 표시되지 않습니다
                """
    )
    public ResponseList<ResponseAcademyAboutDetails> getPublicDetailsList() {
        log.info("학원 소개 상세 목록 조회 요청 (공개)");
        return academyAboutService.getPublicDetailsList();
    }
}