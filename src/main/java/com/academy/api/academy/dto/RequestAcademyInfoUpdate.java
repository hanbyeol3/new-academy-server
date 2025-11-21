package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 학원 기본 정보 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학원 기본 정보 수정 요청")
public class RequestAcademyInfoUpdate {

    @NotBlank(message = "학원명을 입력해주세요")
    @Size(max = 120, message = "학원명은 120자 이하여야 합니다")
    @Schema(description = "학원명", example = "한별학원", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String academyName;

    @NotBlank(message = "캠퍼스명을 입력해주세요")
    @Size(max = 120, message = "캠퍼스명은 120자 이하여야 합니다")
    @Schema(description = "캠퍼스명", example = "본점", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String campusName;

    @Size(max = 255, message = "도메인 URL은 255자 이하여야 합니다")
    @Schema(description = "사이트 도메인 URL", example = "https://academy.example.com")
    private String domainUrl;

    @Size(max = 20, message = "사업자 등록번호는 20자 이하여야 합니다")
    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    private String businessNo;

    @Size(max = 50, message = "대표자명은 50자 이하여야 합니다")
    @Schema(description = "대표자명", example = "홍길동")
    private String ceoName;

    @Size(max = 50, message = "원장명은 50자 이하여야 합니다")
    @Schema(description = "원장명", example = "김원장")
    private String ownerName;

    @Size(max = 30, message = "대표 전화번호는 30자 이하여야 합니다")
    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    private String phoneMain;

    @Size(max = 30, message = "상담 전화번호는 30자 이하여야 합니다")
    @Schema(description = "상담 전화번호", example = "02-1234-5679")
    private String phoneConsult;

    @Size(max = 30, message = "팩스번호는 30자 이하여야 합니다")
    @Schema(description = "팩스번호", example = "02-1234-5680")
    private String fax;

    @Email(message = "올바른 이메일 형식을 입력해주세요")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    @Schema(description = "이메일", example = "info@academy.com")
    private String email;

    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다")
    @Schema(description = "우편번호", example = "12345")
    private String postalCode;

    @Size(max = 255, message = "주소는 255자 이하여야 합니다")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Size(max = 255, message = "상세주소는 255자 이하여야 합니다")
    @Schema(description = "상세주소", example = "한별빌딩 3층")
    private String addressDetail;

    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    @Size(max = 100, message = "평일 운영시간은 100자 이하여야 합니다")
    @Schema(description = "평일 운영시간", example = "09:00 - 22:00")
    private String weekdayHours;

    @Size(max = 100, message = "주말 운영시간은 100자 이하여야 합니다")
    @Schema(description = "주말 운영시간", example = "09:00 - 18:00")
    private String weekendHours;

    @Size(max = 150, message = "사이트 제목은 150자 이하여야 합니다")
    @Schema(description = "사이트 제목", example = "한별학원 | 최고의 교육 서비스")
    private String siteTitle;

    @Size(max = 255, message = "사이트 설명은 255자 이하여야 합니다")
    @Schema(description = "사이트 설명", example = "최고의 교육 서비스를 제공하는 한별학원입니다.")
    private String siteDesc;

    @Size(max = 255, message = "사이트 키워드는 255자 이하여야 합니다")
    @Schema(description = "사이트 키워드", example = "학원, 교육, 과외, 수학, 영어")
    private String siteKeywords;

    @Size(max = 255, message = "OG 이미지 경로는 255자 이하여야 합니다")
    @Schema(description = "OG 이미지 경로", example = "/images/og-image.jpg")
    private String ogImagePath;

    @Size(max = 255, message = "카카오톡 채널 URL은 255자 이하여야 합니다")
    @Schema(description = "카카오톡 채널 URL", example = "http://pf.kakao.com/_academy")
    private String kakaoUrl;

    @Size(max = 255, message = "유튜브 URL은 255자 이하여야 합니다")
    @Schema(description = "유튜브 URL", example = "https://youtube.com/@academy")
    private String youtubeUrl;

    @Size(max = 255, message = "블로그 URL은 255자 이하여야 합니다")
    @Schema(description = "블로그 URL", example = "https://blog.academy.com")
    private String blogUrl;

    @Size(max = 255, message = "인스타그램 URL은 255자 이하여야 합니다")
    @Schema(description = "인스타그램 URL", example = "https://instagram.com/academy")
    private String instagramUrl;
}