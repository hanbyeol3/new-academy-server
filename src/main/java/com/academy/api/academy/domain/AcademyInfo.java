package com.academy.api.academy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 학원 기본 정보 엔티티.
 * 
 * academy_info 테이블과 매핑되며 학원의 기본 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 학원 기본 정보 관리 (이름, 주소, 연락처)
 * - 사업자 정보 및 운영진 정보 관리
 * - 사이트 메타데이터 및 SEO 정보 관리
 * - SNS 링크 및 위치 정보 관리
 */
@Entity
@Table(name = "academy_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AcademyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 학원명 */
    @Column(name = "academy_name", nullable = false, length = 120)
    private String academyName;

    /** 캠퍼스명 */
    @Column(name = "campus_name", nullable = false, length = 120)
    private String campusName;

    /** 사이트 도메인 URL */
    @Column(name = "domain_url", length = 255)
    private String domainUrl;

    /** 사업자 등록번호 */
    @Column(name = "business_no", length = 20)
    private String businessNo;

    /** 대표자명 */
    @Column(name = "ceo_name", length = 50)
    private String ceoName;

    /** 원장명 */
    @Column(name = "owner_name", length = 50)
    private String ownerName;

    /** 대표 전화번호 */
    @Column(name = "phone_main", length = 30)
    private String phoneMain;

    /** 상담 전화번호 */
    @Column(name = "phone_consult", length = 30)
    private String phoneConsult;

    /** 팩스번호 */
    @Column(name = "fax", length = 30)
    private String fax;

    /** 이메일 */
    @Column(name = "email", length = 100)
    private String email;

    /** 우편번호 */
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    /** 주소 */
    @Column(name = "address", length = 255)
    private String address;

    /** 상세주소 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    /** 위도 */
    @Column(name = "latitude", precision = 10, scale = 4)
    private BigDecimal latitude;

    /** 경도 */
    @Column(name = "longitude", precision = 10, scale = 4)
    private BigDecimal longitude;

    /** 평일 운영시간 */
    @Column(name = "weekday_hours", length = 100)
    private String weekdayHours;

    /** 주말 운영시간 */
    @Column(name = "weekend_hours", length = 100)
    private String weekendHours;

    /** 사이트 제목 */
    @Column(name = "site_title", length = 150)
    private String siteTitle;

    /** 사이트 설명 */
    @Column(name = "site_desc", length = 255)
    private String siteDesc;

    /** 사이트 키워드 */
    @Column(name = "site_keywords", length = 255)
    private String siteKeywords;

    /** OG 이미지 경로 */
    @Column(name = "og_image_path", length = 255)
    private String ogImagePath;

    /** 카카오톡 채널 URL */
    @Column(name = "kakao_url", length = 255)
    private String kakaoUrl;

    /** 유튜브 URL */
    @Column(name = "youtube_url", length = 255)
    private String youtubeUrl;

    /** 블로그 URL */
    @Column(name = "blog_url", length = 255)
    private String blogUrl;

    /** 인스타그램 URL */
    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 학원 정보 생성자.
     */
    @Builder
    private AcademyInfo(String academyName, String campusName, String domainUrl, String businessNo,
                       String ceoName, String ownerName, String phoneMain, String phoneConsult,
                       String fax, String email, String postalCode, String address, String addressDetail,
                       BigDecimal latitude, BigDecimal longitude, String weekdayHours, String weekendHours,
                       String siteTitle, String siteDesc, String siteKeywords, String ogImagePath,
                       String kakaoUrl, String youtubeUrl, String blogUrl, String instagramUrl, Long createdBy) {
        this.academyName = academyName;
        this.campusName = campusName;
        this.domainUrl = domainUrl;
        this.businessNo = businessNo;
        this.ceoName = ceoName;
        this.ownerName = ownerName;
        this.phoneMain = phoneMain;
        this.phoneConsult = phoneConsult;
        this.fax = fax;
        this.email = email;
        this.postalCode = postalCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weekdayHours = weekdayHours;
        this.weekendHours = weekendHours;
        this.siteTitle = siteTitle;
        this.siteDesc = siteDesc;
        this.siteKeywords = siteKeywords;
        this.ogImagePath = ogImagePath;
        this.kakaoUrl = kakaoUrl;
        this.youtubeUrl = youtubeUrl;
        this.blogUrl = blogUrl;
        this.instagramUrl = instagramUrl;
        this.createdBy = createdBy;
    }

    /**
     * 학원 기본 정보 업데이트.
     */
    public void updateBasicInfo(String academyName, String campusName, String phoneMain, String phoneConsult,
                               String email, String postalCode, String address, String addressDetail, Long updatedBy) {
        this.academyName = academyName;
        this.campusName = campusName;
        this.phoneMain = phoneMain;
        this.phoneConsult = phoneConsult;
        this.email = email;
        this.postalCode = postalCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.updatedBy = updatedBy;
    }

    /**
     * 사업자 정보 업데이트.
     */
    public void updateBusinessInfo(String businessNo, String ceoName, String ownerName, Long updatedBy) {
        this.businessNo = businessNo;
        this.ceoName = ceoName;
        this.ownerName = ownerName;
        this.updatedBy = updatedBy;
    }

    /**
     * 운영시간 정보 업데이트.
     */
    public void updateOperatingHours(String weekdayHours, String weekendHours, Long updatedBy) {
        this.weekdayHours = weekdayHours;
        this.weekendHours = weekendHours;
        this.updatedBy = updatedBy;
    }

    /**
     * 위치 정보 업데이트.
     */
    public void updateLocation(String postalCode, String address, String addressDetail, 
                              BigDecimal latitude, BigDecimal longitude, Long updatedBy) {
        this.postalCode = postalCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedBy = updatedBy;
    }

    /**
     * 사이트 메타 정보 업데이트.
     */
    public void updateSiteMetadata(String siteTitle, String siteDesc, String siteKeywords, 
                                  String ogImagePath, Long updatedBy) {
        this.siteTitle = siteTitle;
        this.siteDesc = siteDesc;
        this.siteKeywords = siteKeywords;
        this.ogImagePath = ogImagePath;
        this.updatedBy = updatedBy;
    }

    /**
     * SNS 링크 정보 업데이트.
     */
    public void updateSocialLinks(String kakaoUrl, String youtubeUrl, String blogUrl, 
                                 String instagramUrl, Long updatedBy) {
        this.kakaoUrl = kakaoUrl;
        this.youtubeUrl = youtubeUrl;
        this.blogUrl = blogUrl;
        this.instagramUrl = instagramUrl;
        this.updatedBy = updatedBy;
    }
}