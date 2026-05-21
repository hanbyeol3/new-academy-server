package com.academy.api.academy.controller;

import com.academy.api.academy.dto.ResponseAcademyInfo;
import com.academy.api.academy.service.AcademyInfoService;
import com.academy.api.data.responses.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 학원 정보 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 학원 정보 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 학원 기본 정보를 조회할 수 있습니다.
 */
@Tag(name = "Academy Info (Public)", description = "일반 사용자용 학원 정보 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/academy-info")
@RequiredArgsConstructor
public class AcademyInfoPublicController {

    private final AcademyInfoService academyInfoService;

    /**
     * 학원 정보 공개 조회.
     * 
     * @return 학원 정보
     */
    @GetMapping
    @Operation(
        summary = "학원 정보 조회 (공개)",
        description = """
                학원 기본 정보를 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 학원 운영에 필요한 모든 기본 정보 제공
                - 단일 설정 테이블로 항상 1개의 레코드만 존재
                - 데이터가 없을 경우 기본값으로 자동 생성 후 반환
                
                조회 정보:
                - 학원 기본 정보 (이름, 캠퍼스명, 대표자, 연락처)
                - 위치 정보 (주소, 좌표)
                - 운영시간 정보
                - 사이트 메타데이터 (SEO)
                - SNS 링크 정보 (카카오톡, 유튜브, 블로그, 인스타그램)
                - 강사진 유튜브 URL (instructorYoutubeUrl)
                
                반환 필드:
                - academyName: 학원명
                - campusName: 캠퍼스명
                - phoneMain: 대표 전화번호
                - phoneConsult: 상담 전화번호
                - email: 이메일
                - address: 주소
                - weekdayHours: 평일 운영시간
                - weekendHours: 주말 운영시간
                - kakaoUrl: 카카오톡 채널 URL
                - youtubeUrl: 유튜브 URL
                - blogUrl: 블로그 URL
                - instagramUrl: 인스타그램 URL
                - instructorYoutubeUrl: 강사진 소개 유튜브 URL
                """
    )
    public ResponseData<ResponseAcademyInfo> getAcademyInfo() {
        log.info("[AcademyInfoPublicController] 학원 정보 공개 조회 요청");
        return academyInfoService.getAcademyInfo();
    }
}