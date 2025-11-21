package com.academy.api.academy.mapper;

import com.academy.api.academy.domain.AcademyInfo;
import com.academy.api.academy.dto.RequestAcademyInfoUpdate;
import com.academy.api.academy.dto.ResponseAcademyInfo;
import org.springframework.stereotype.Component;

/**
 * 학원 정보 매퍼.
 * 
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Component
public class AcademyInfoMapper {

    /**
     * Entity → Response DTO 변환.
     * 
     * @param entity 학원 정보 엔티티
     * @return 학원 정보 응답 DTO
     */
    public ResponseAcademyInfo toResponse(AcademyInfo entity) {
        if (entity == null) {
            return null;
        }

        return ResponseAcademyInfo.builder()
                .id(entity.getId())
                .academyName(entity.getAcademyName())
                .campusName(entity.getCampusName())
                .domainUrl(entity.getDomainUrl())
                .businessNo(entity.getBusinessNo())
                .ceoName(entity.getCeoName())
                .ownerName(entity.getOwnerName())
                .phoneMain(entity.getPhoneMain())
                .phoneConsult(entity.getPhoneConsult())
                .fax(entity.getFax())
                .email(entity.getEmail())
                .postalCode(entity.getPostalCode())
                .address(entity.getAddress())
                .addressDetail(entity.getAddressDetail())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .weekdayHours(entity.getWeekdayHours())
                .weekendHours(entity.getWeekendHours())
                .siteTitle(entity.getSiteTitle())
                .siteDesc(entity.getSiteDesc())
                .siteKeywords(entity.getSiteKeywords())
                .ogImagePath(entity.getOgImagePath())
                .kakaoUrl(entity.getKakaoUrl())
                .youtubeUrl(entity.getYoutubeUrl())
                .blogUrl(entity.getBlogUrl())
                .instagramUrl(entity.getInstagramUrl())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Request DTO → Entity 변환 (생성용).
     * 
     * @param request 수정 요청 DTO
     * @param createdBy 등록자 ID
     * @return 학원 정보 엔티티
     */
    public AcademyInfo toEntity(RequestAcademyInfoUpdate request, Long createdBy) {
        if (request == null) {
            return null;
        }

        return AcademyInfo.builder()
                .academyName(request.getAcademyName())
                .campusName(request.getCampusName())
                .domainUrl(request.getDomainUrl())
                .businessNo(request.getBusinessNo())
                .ceoName(request.getCeoName())
                .ownerName(request.getOwnerName())
                .phoneMain(request.getPhoneMain())
                .phoneConsult(request.getPhoneConsult())
                .fax(request.getFax())
                .email(request.getEmail())
                .postalCode(request.getPostalCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .weekdayHours(request.getWeekdayHours())
                .weekendHours(request.getWeekendHours())
                .siteTitle(request.getSiteTitle())
                .siteDesc(request.getSiteDesc())
                .siteKeywords(request.getSiteKeywords())
                .ogImagePath(request.getOgImagePath())
                .kakaoUrl(request.getKakaoUrl())
                .youtubeUrl(request.getYoutubeUrl())
                .blogUrl(request.getBlogUrl())
                .instagramUrl(request.getInstagramUrl())
                .createdBy(createdBy)
                .build();
    }

    /**
     * Request DTO로 Entity 업데이트.
     * 
     * @param entity 기존 엔티티
     * @param request 수정 요청 DTO
     * @param updatedBy 수정자 ID
     */
    public void updateEntity(AcademyInfo entity, RequestAcademyInfoUpdate request, Long updatedBy) {
        if (entity == null || request == null) {
            return;
        }

        // 기본 정보 업데이트
        entity.updateBasicInfo(
                request.getAcademyName(),
                request.getCampusName(),
                request.getPhoneMain(),
                request.getPhoneConsult(),
                request.getEmail(),
                request.getPostalCode(),
                request.getAddress(),
                request.getAddressDetail(),
                updatedBy
        );

        // 사업자 정보 업데이트
        entity.updateBusinessInfo(
                request.getBusinessNo(),
                request.getCeoName(),
                request.getOwnerName(),
                updatedBy
        );

        // 운영시간 정보 업데이트
        entity.updateOperatingHours(
                request.getWeekdayHours(),
                request.getWeekendHours(),
                updatedBy
        );

        // 위치 정보 업데이트
        entity.updateLocation(
                request.getPostalCode(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getLatitude(),
                request.getLongitude(),
                updatedBy
        );

        // 사이트 메타 정보 업데이트
        entity.updateSiteMetadata(
                request.getSiteTitle(),
                request.getSiteDesc(),
                request.getSiteKeywords(),
                request.getOgImagePath(),
                updatedBy
        );

        // SNS 링크 정보 업데이트
        entity.updateSocialLinks(
                request.getKakaoUrl(),
                request.getYoutubeUrl(),
                request.getBlogUrl(),
                request.getInstagramUrl(),
                updatedBy
        );

        // 도메인 URL 별도 업데이트 (업데이트 메서드가 없으므로 직접 설정)
        // 향후 AcademyInfo 엔티티에 updateDomainUrl 메서드 추가 고려
    }

    /**
     * 기본 학원 정보 생성.
     * 
     * @param createdBy 등록자 ID
     * @return 기본값으로 설정된 학원 정보 엔티티
     */
    public AcademyInfo createDefaultAcademyInfo(Long createdBy) {
        return AcademyInfo.builder()
                .academyName("학원명을 입력하세요")
                .campusName("본점")
                .domainUrl("")
                .businessNo("")
                .ceoName("")
                .ownerName("")
                .phoneMain("")
                .phoneConsult("")
                .fax("")
                .email("")
                .postalCode("")
                .address("")
                .addressDetail("")
                .latitude(null)
                .longitude(null)
                .weekdayHours("")
                .weekendHours("")
                .siteTitle("학원 홈페이지")
                .siteDesc("학원 소개")
                .siteKeywords("")
                .ogImagePath("")
                .kakaoUrl("")
                .youtubeUrl("")
                .blogUrl("")
                .instagramUrl("")
                .createdBy(createdBy)
                .build();
    }
}