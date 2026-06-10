package com.academy.api.inquiry.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.inquiry.domain.*;
import com.academy.api.inquiry.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 상담신청 Entity ↔ DTO 변환 매퍼.
 * 
 * 상담신청과 상담이력의 모든 변환 로직을 담당합니다.
 * IP 주소 변환, 회원 이름 조회 등 추가 비즈니스 로직도 포함합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryMapper {

    /**
     * 생성 요청 DTO를 Entity로 변환 (외부 접수용 - createdBy null).
     * 
     * @param request 생성 요청 DTO
     * @return Inquiry Entity
     */
    public Inquiry toEntity(RequestInquiryCreate request) {
        return Inquiry.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .content(request.getContent())
                .status(request.getStatusEnum())
                .assigneeName(request.getAssigneeName())
                .adminMemo(request.getAdminMemo())
                .inquiryChannel(request.getInquiryChannelEnum())
                .inflowSource(request.getInflowSourceEnum())
                .inflowSourceEtc(request.getInflowSourceEtc())
                .landingPath(request.getLandingPath())
                .utmSource(request.getUtmSource())
                .utmMedium(request.getUtmMedium())
                .utmCampaign(request.getUtmCampaign())
                .clientIp(convertIpToBytes(request.getClientIp()))
                .createdBy(null) // 외부 접수 시 null
                .build();
    }

    /**
     * 생성 요청 DTO를 Entity로 변환 (관리자 등록용 - createdBy 설정).
     * 
     * @param request 생성 요청 DTO
     * @param createdBy 등록자 ID (관리자)
     * @return Inquiry Entity
     */
    public Inquiry toEntityWithCreatedBy(RequestInquiryCreate request, Long createdBy) {
        return Inquiry.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .content(request.getContent())
                .status(request.getStatusEnum())
                .assigneeName(request.getAssigneeName())
                .adminMemo(request.getAdminMemo())
                .inquiryChannel(request.getInquiryChannelEnum())
                .inflowSource(request.getInflowSourceEnum())
                .inflowSourceEtc(request.getInflowSourceEtc())
                .landingPath(request.getLandingPath())
                .utmSource(request.getUtmSource())
                .utmMedium(request.getUtmMedium())
                .utmCampaign(request.getUtmCampaign())
                .clientIp(convertIpToBytes(request.getClientIp()))
                .createdBy(createdBy) // 관리자 ID 설정
                .build();
    }

    /**
     * Entity를 상세 응답 DTO로 변환.
     * 
     * @param entity Inquiry Entity
     * @return ResponseInquiry DTO
     */
    public ResponseInquiry toResponse(Inquiry entity) {
        return ResponseInquiry.from(entity);
    }

    /**
     * Entity를 상세 응답 DTO로 변환 (회원 이름, 이력 포함).
     * 
     * @param entity Inquiry Entity
     * @param createdByName 등록자 이름
     * @param updatedByName 수정자 이름
     * @param logs 상담이력 목록
     * @return ResponseInquiry DTO
     */
    public ResponseInquiry toResponseWithDetails(Inquiry entity, String createdByName, 
                                               String updatedByName, List<ResponseInquiryLog> logs) {
        return ResponseInquiry.fromWithNames(entity, createdByName, updatedByName, logs);
    }

    /**
     * Entity를 목록 응답 DTO로 변환.
     * 
     * @param entity Inquiry Entity
     * @return ResponseInquiryListItem DTO
     */
    public ResponseInquiryListItem toListItem(Inquiry entity) {
        return ResponseInquiryListItem.from(entity);
    }

    /**
     * Entity를 목록 응답 DTO로 변환 (회원 이름 포함).
     * 
     * @param entity Inquiry Entity
     * @param createdByName 등록자 이름
     * @param updatedByName 수정자 이름
     * @return ResponseInquiryListItem DTO
     */
    public ResponseInquiryListItem toListItemWithNames(Inquiry entity, String createdByName, String updatedByName) {
        return ResponseInquiryListItem.fromWithNames(entity, createdByName, updatedByName);
    }

    /**
     * Entity 목록을 목록 응답 DTO로 변환.
     * 
     * @param entities Inquiry Entity 목록
     * @return ResponseInquiryListItem DTO 목록
     */
    public List<ResponseInquiryListItem> toListItems(List<Inquiry> entities) {
        return ResponseInquiryListItem.fromList(entities);
    }

    /**
     * Entity Page를 ResponseList로 변환.
     * 
     * @param page Inquiry Page
     * @return ResponseList<ResponseInquiryListItem>
     */
    public ResponseList<ResponseInquiryListItem> toListItemResponseList(Page<Inquiry> page) {
        List<ResponseInquiryListItem> items = toListItems(page.getContent());
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 이력 생성 요청 DTO를 Entity로 변환.
     * 
     * @param inquiryId 상담신청 ID
     * @param request 이력 생성 요청 DTO
     * @param inquiry Inquiry Entity (연관관계 설정용)
     * @return InquiryLog Entity
     */
    public InquiryLog toLogEntity(Long inquiryId, RequestInquiryLogCreate request, Inquiry inquiry) {
        return InquiryLog.builder()
                .inquiry(inquiry)
                .logType(request.getLogTypeEnum())
                .contactChannel(request.getContactChannelEnum())
                .logContent(request.getLogContent())
                .nextStatus(request.getNextStatusEnum())
                .nextAssignee(request.getNextAssignee())
                .createdBy(null) // 서비스에서 설정
                .build();
    }

    /**
     * 이력 Entity를 응답 DTO로 변환.
     * 
     * @param entity InquiryLog Entity
     * @return ResponseInquiryLog DTO
     */
    public ResponseInquiryLog toLogResponse(InquiryLog entity) {
        return ResponseInquiryLog.from(entity);
    }

    /**
     * 이력 Entity를 응답 DTO로 변환 (회원 이름 포함).
     * 
     * @param entity InquiryLog Entity
     * @param createdByName 등록자 이름
     * @param updatedByName 수정자 이름
     * @return ResponseInquiryLog DTO
     */
    public ResponseInquiryLog toLogResponseWithNames(InquiryLog entity, String createdByName, String updatedByName) {
        return ResponseInquiryLog.fromWithNames(entity, createdByName, updatedByName);
    }

    /**
     * 이력 Entity 목록을 응답 DTO 목록으로 변환.
     * 
     * @param entities InquiryLog Entity 목록
     * @return ResponseInquiryLog DTO 목록
     */
    public List<ResponseInquiryLog> toLogResponses(List<InquiryLog> entities) {
        return ResponseInquiryLog.fromList(entities);
    }

    /**
     * 시스템 생성 이력 Entity 생성 (외부 접수용).
     * 
     * @param inquiry Inquiry Entity
     * @return InquiryLog Entity
     */
    public InquiryLog createSystemCreateLog(Inquiry inquiry) {
        // 웹 간편상담은 contactChannel 없이, 다른 채널은 contactChannel 설정
        ContactChannel contactChannel = null;
        if (inquiry.getInquiryChannel() != null && 
            inquiry.getInquiryChannel() != InquiryChannel.WEB_SIMPLE_FORM) {
            // inquiry_channel이 CALL, VISIT, KAKAO 등인 경우 동일한 contact_channel 설정
            try {
                contactChannel = ContactChannel.valueOf(inquiry.getInquiryChannel().name());
            } catch (IllegalArgumentException e) {
                // 매칭되지 않는 경우 null 유지
            }
        }
        
        return InquiryLog.builder()
                .inquiry(inquiry)
                .logType(InquiryLogType.CREATE)
                .contactChannel(contactChannel)
                .logContent("외부 등록")
                .createdBy(null) // 시스템 생성
                .build();
    }

    /**
     * 관리자 생성 이력 Entity 생성.
     * 
     * @param inquiry Inquiry Entity
     * @param adminName 관리자 이름
     * @param adminId 관리자 ID
     * @return InquiryLog Entity
     */
    public InquiryLog createAdminCreateLog(Inquiry inquiry, String adminName, Long adminId) {
        // inquiry_channel에 따라 logContent와 contactChannel 설정
        String logContent;
        ContactChannel contactChannel = null;
        
        if (inquiry.getInquiryChannel() == InquiryChannel.WEB_SIMPLE_FORM) {
            // 웹 간편상담으로 관리자가 직접 등록한 경우
            logContent = adminName + " 생성 : 관리자 등록";
        } else {
            // 전화, 방문, 카카오톡 등으로 직접 접수한 경우
            logContent = adminName + " 생성 : " + getChannelDescription(inquiry.getInquiryChannel());
            
            // contact_channel 설정 (CALL, VISIT, KAKAO 등)
            try {
                contactChannel = ContactChannel.valueOf(inquiry.getInquiryChannel().name());
            } catch (IllegalArgumentException e) {
                // 매칭되지 않는 경우 null 유지
            }
        }
        
        return InquiryLog.builder()
                .inquiry(inquiry)
                .logType(InquiryLogType.CREATE)
                .contactChannel(contactChannel)
                .logContent(logContent)
                .createdBy(adminId)
                .build();
    }
    
    /**
     * InquiryChannel에 대한 한글 설명 반환.
     */
    private String getChannelDescription(InquiryChannel channel) {
        if (channel == null) return "관리자 등록";
        
        switch (channel) {
            case WEB_SIMPLE_FORM: return "웹 간편상담 접수";
            case CALL: return "전화 접수";
            case VISIT: return "방문 접수";
            case KAKAO: return "카카오톡 접수";
            case NAVER_TALK: return "네이버 톡톡 접수";
            case INSTAGRAM_DM: return "인스타그램 DM 접수";
            case ETC: return "기타 경로 접수";
            default: return "관리자 등록";
        }
    }

    /**
     * IP 주소 문자열을 바이트 배열로 변환.
     * 
     * @param ipAddress IP 주소 문자열
     * @return 바이트 배열
     */
    private byte[] convertIpToBytes(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return null;
        }
        
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress.trim());
            return inetAddress.getAddress();
        } catch (UnknownHostException e) {
            log.warn("[InquiryMapper] IP 주소 변환 실패: {}", ipAddress, e);
            return null;
        }
    }

    /**
     * 바이트 배열을 IP 주소 문자열로 변환.
     * 
     * @param ipBytes 바이트 배열
     * @return IP 주소 문자열
     */
    private String convertBytesToIp(byte[] ipBytes) {
        if (ipBytes == null) {
            return null;
        }
        
        try {
            InetAddress inetAddress = InetAddress.getByAddress(ipBytes);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("[InquiryMapper] 바이트 배열을 IP로 변환 실패", e);
            return null;
        }
    }

    /**
     * 수정 요청 DTO의 내용으로 Entity 업데이트.
     * 
     * @param entity 업데이트할 Inquiry Entity
     * @param request 수정 요청 DTO
     * @param updatedBy 수정자 ID
     */
    public void updateEntityFromRequest(Inquiry entity, RequestInquiryUpdate request, Long updatedBy) {
        entity.update(
            request.getName() != null ? request.getName() : entity.getName(),
            request.getPhoneNumber() != null ? request.getPhoneNumber() : entity.getPhoneNumber(),
            request.getContent() != null ? request.getContent() : entity.getContent(),
            request.getStatusEnum() != null ? request.getStatusEnum() : entity.getStatus(),
            request.getAssigneeName() != null ? request.getAssigneeName() : entity.getAssigneeName(),
            request.getAdminMemo() != null ? request.getAdminMemo() : entity.getAdminMemo(),
            request.getInquiryChannelEnum() != null ? request.getInquiryChannelEnum() : entity.getInquiryChannel(),
            request.getInflowSourceEnum() != null ? request.getInflowSourceEnum() : entity.getInflowSource(),
            request.getInflowSourceEtc() != null ? request.getInflowSourceEtc() : entity.getInflowSourceEtc(),
            request.getLandingPath() != null ? request.getLandingPath() : entity.getLandingPath(),
            request.getUtmSource() != null ? request.getUtmSource() : entity.getUtmSource(),
            request.getUtmMedium() != null ? request.getUtmMedium() : entity.getUtmMedium(),
            request.getUtmCampaign() != null ? request.getUtmCampaign() : entity.getUtmCampaign(),
            updatedBy
        );
    }

    /**
     * 통계 데이터를 응답 DTO로 변환.
     * 
     * @param statusCounts 상태별 개수 배열 [NEW, IN_PROGRESS, DONE, REJECTED, SPAM]
     * @return ResponseInquiryStats DTO
     */
    public ResponseInquiryStats toStatsResponse(Long[] statusCounts) {
        return ResponseInquiryStats.of(
            statusCounts[0], // NEW
            statusCounts[1], // IN_PROGRESS
            statusCounts[2], // DONE
            statusCounts[3], // REJECTED
            statusCounts[4]  // SPAM
        );
    }
}