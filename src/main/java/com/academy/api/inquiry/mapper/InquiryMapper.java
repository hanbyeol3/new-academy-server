package com.academy.api.inquiry.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.inquiry.domain.Inquiry;
import com.academy.api.inquiry.domain.InquiryLog;
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
     * 생성 요청 DTO를 Entity로 변환.
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
                .inquirySourceType(request.getInquirySourceTypeEnum())
                .sourceType(request.getSourceType())
                .utmSource(request.getUtmSource())
                .utmMedium(request.getUtmMedium())
                .utmCampaign(request.getUtmCampaign())
                .clientIp(convertIpToBytes(request.getClientIp()))
                .createdBy(null) // 서비스에서 설정
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
        return InquiryLog.builder()
                .inquiry(inquiry)
                .logType(com.academy.api.inquiry.domain.LogType.CREATE)
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
        String logContent = adminName + " 생성 : 관리자 등록";
        return InquiryLog.builder()
                .inquiry(inquiry)
                .logType(com.academy.api.inquiry.domain.LogType.CREATE)
                .logContent(logContent)
                .createdBy(adminId)
                .build();
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
            request.getInquirySourceTypeEnum() != null ? request.getInquirySourceTypeEnum() : entity.getInquirySourceType(),
            request.getSourceType() != null ? request.getSourceType() : entity.getSourceType(),
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
        return ResponseInquiryStats.basic(
            statusCounts[0], // NEW
            statusCounts[1], // IN_PROGRESS
            statusCounts[2], // DONE
            statusCounts[3], // REJECTED
            statusCounts[4]  // SPAM
        );
    }
}