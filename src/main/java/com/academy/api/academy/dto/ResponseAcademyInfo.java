package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 학원 기본 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "학원 기본 정보 응답")
public class ResponseAcademyInfo {

    @Schema(description = "학원 정보 ID", example = "1")
    private Long id;

    @Schema(description = "학원명", example = "한별학원")
    private String academyName;

    @Schema(description = "캠퍼스명", example = "본점")
    private String campusName;

    @Schema(description = "사이트 도메인 URL", example = "https://academy.example.com")
    private String domainUrl;

    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    private String businessNo;

    @Schema(description = "대표자명", example = "홍길동")
    private String ceoName;

    @Schema(description = "원장명", example = "김원장")
    private String ownerName;

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    private String phoneMain;

    @Schema(description = "상담 전화번호", example = "02-1234-5679")
    private String phoneConsult;

    @Schema(description = "팩스번호", example = "02-1234-5680")
    private String fax;

    @Schema(description = "이메일", example = "info@academy.com")
    private String email;

    @Schema(description = "우편번호", example = "12345")
    private String postalCode;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "상세주소", example = "한별빌딩 3층")
    private String addressDetail;

    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "평일 운영시간", example = "09:00 - 22:00")
    private String weekdayHours;

    @Schema(description = "주말 운영시간", example = "09:00 - 18:00")
    private String weekendHours;

    @Schema(description = "사이트 제목", example = "한별학원 | 최고의 교육 서비스")
    private String siteTitle;

    @Schema(description = "사이트 설명", example = "최고의 교육 서비스를 제공하는 한별학원입니다.")
    private String siteDesc;

    @Schema(description = "사이트 키워드", example = "학원, 교육, 과외, 수학, 영어")
    private String siteKeywords;

    @Schema(description = "OG 이미지 경로", example = "/images/og-image.jpg")
    private String ogImagePath;

    @Schema(description = "카카오톡 채널 URL", example = "http://pf.kakao.com/_academy")
    private String kakaoUrl;

    @Schema(description = "유튜브 URL", example = "https://youtube.com/@academy")
    private String youtubeUrl;

    @Schema(description = "블로그 URL", example = "https://blog.academy.com")
    private String blogUrl;

    @Schema(description = "인스타그램 URL", example = "https://instagram.com/academy")
    private String instagramUrl;

    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "2")
    private Long updatedBy;

    @Schema(description = "수정일시", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}