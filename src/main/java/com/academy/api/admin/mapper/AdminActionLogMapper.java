package com.academy.api.admin.mapper;

import com.academy.api.admin.domain.AdminActionLog;
import com.academy.api.admin.dto.response.ResponseAdminActionLog;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 액션 로그 Mapper.
 * 
 * AdminActionLog 엔티티와 응답 DTO 간 변환을 담당합니다.
 */
@Component
public class AdminActionLogMapper {

    /**
     * AdminActionLog 엔티티를 응답 DTO로 변환.
     * 
     * @param actionLog AdminActionLog 엔티티
     * @return 응답 DTO
     */
    public ResponseAdminActionLog toResponse(AdminActionLog actionLog) {
        return ResponseAdminActionLog.builder()
                .id(actionLog.getId())
                .adminId(actionLog.getAdminId())
                .adminUsername(actionLog.getAdminUsername())
                .actionType(actionLog.getActionType())
                .targetType(actionLog.getTargetType())
                .targetId(actionLog.getTargetId())
                .targetSnapshot(actionLog.getTargetSnapshot())
                .actionDetail(actionLog.getActionDetail())
                .ipAddress(convertIpAddress(actionLog.getIpAddress()))
                .userAgent(actionLog.getUserAgent())
                .createdAt(actionLog.getCreatedAt())
                .build();
    }

    /**
     * AdminActionLog 엔티티를 응답 DTO로 변환 (관리자 이름 포함).
     * 
     * @param actionLog AdminActionLog 엔티티
     * @param adminName 관리자 이름
     * @return 응답 DTO
     */
    public ResponseAdminActionLog toResponse(AdminActionLog actionLog, String adminName) {
        ResponseAdminActionLog response = toResponse(actionLog);
        return ResponseAdminActionLog.builder()
                .id(response.getId())
                .adminId(response.getAdminId())
                .adminUsername(response.getAdminUsername())
                .adminName(adminName)
                .actionType(response.getActionType())
                .targetType(response.getTargetType())
                .targetId(response.getTargetId())
                .targetSnapshot(response.getTargetSnapshot())
                .actionDetail(response.getActionDetail())
                .ipAddress(response.getIpAddress())
                .userAgent(response.getUserAgent())
                .createdAt(response.getCreatedAt())
                .build();
    }

    /**
     * AdminActionLog 엔티티 목록을 응답 DTO 목록으로 변환.
     * 
     * @param actionLogs AdminActionLog 엔티티 목록
     * @return 응답 DTO 목록
     */
    public List<ResponseAdminActionLog> toResponseList(List<AdminActionLog> actionLogs) {
        return actionLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * AdminActionLog 페이지를 응답 목록으로 변환.
     * 
     * @param actionLogPage AdminActionLog 페이지
     * @return 응답 목록
     */
    public ResponseList<ResponseAdminActionLog> toResponseList(Page<AdminActionLog> actionLogPage) {
        List<ResponseAdminActionLog> responses = toResponseList(actionLogPage.getContent());
        return ResponseList.ok(
                responses,
                actionLogPage.getTotalElements(),
                actionLogPage.getNumber(),
                actionLogPage.getSize()
        );
    }

    /**
     * AdminActionLog 페이지를 응답 목록으로 변환 (관리자 이름 포함).
     * 
     * @param actionLogPage AdminActionLog 페이지
     * @param adminNames 관리자 이름 매핑 (관리자 ID -> 이름)
     * @return 응답 목록
     */
    public ResponseList<ResponseAdminActionLog> toResponseListWithNames(Page<AdminActionLog> actionLogPage, 
                                                                       Map<Long, String> adminNames) {
        List<ResponseAdminActionLog> responses = actionLogPage.getContent().stream()
                .map(actionLog -> {
                    String adminName = adminNames.get(actionLog.getAdminId());
                    return toResponse(actionLog, adminName);
                })
                .collect(Collectors.toList());

        return ResponseList.ok(
                responses,
                actionLogPage.getTotalElements(),
                actionLogPage.getNumber(),
                actionLogPage.getSize()
        );
    }

    /**
     * 액션 로그 생성용 도우미 메서드.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param actionType 액션 타입
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param targetSnapshot 대상 스냅샷
     * @param actionDetail 상세 정보
     * @param ipAddress IP 주소 (바이너리)
     * @param userAgent User-Agent
     * @return AdminActionLog 엔티티
     */
    public AdminActionLog createActionLog(Long adminId, String adminUsername, AdminActionType actionType,
                                        AdminTargetType targetType, Long targetId, String targetSnapshot,
                                        Map<String, Object> actionDetail, byte[] ipAddress, String userAgent) {
        return AdminActionLog.builder()
                .adminId(adminId)
                .adminUsername(adminUsername)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .targetSnapshot(targetSnapshot)
                .actionDetail(actionDetail)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * 간단한 액션 로그 생성 (기본 정보만).
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param actionType 액션 타입
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return AdminActionLog 엔티티
     */
    public AdminActionLog createSimpleActionLog(Long adminId, String adminUsername, AdminActionType actionType,
                                              AdminTargetType targetType, Long targetId) {
        return createActionLog(adminId, adminUsername, actionType, targetType, targetId, null, null, null, null);
    }

    /**
     * 액션 상세 정보 생성.
     * 
     * @param beforeData 변경 전 데이터
     * @param afterData 변경 후 데이터
     * @param reason 변경 사유
     * @param additionalInfo 추가 정보
     * @return 액션 상세 정보 맵
     */
    public Map<String, Object> createActionDetail(Object beforeData, Object afterData, String reason, Map<String, Object> additionalInfo) {
        Map<String, Object> detail = new java.util.HashMap<>();
        
        if (beforeData != null) {
            detail.put("before", beforeData);
        }
        if (afterData != null) {
            detail.put("after", afterData);
        }
        if (reason != null && !reason.trim().isEmpty()) {
            detail.put("reason", reason);
        }
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            detail.putAll(additionalInfo);
        }
        
        detail.put("timestamp", System.currentTimeMillis());
        return detail;
    }

    /**
     * IP 주소 바이너리를 문자열로 변환.
     * 
     * @param ipBinary IP 주소 바이너리
     * @return IP 주소 문자열
     */
    private String convertIpAddress(byte[] ipBinary) {
        if (ipBinary == null || ipBinary.length == 0) {
            return null;
        }
        
        try {
            if (ipBinary.length == 4) {
                // IPv4
                return String.format("%d.%d.%d.%d",
                    ipBinary[0] & 0xFF,
                    ipBinary[1] & 0xFF,
                    ipBinary[2] & 0xFF,
                    ipBinary[3] & 0xFF);
            } else if (ipBinary.length == 16) {
                // IPv6 - 간단한 변환 (실제로는 더 복잡한 로직 필요)
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ipBinary.length; i += 2) {
                    if (i > 0) sb.append(":");
                    sb.append(String.format("%02x%02x", ipBinary[i] & 0xFF, ipBinary[i + 1] & 0xFF));
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "IP 변환 오류";
        }
        
        return "알 수 없는 IP 형식";
    }

    /**
     * 문자열 IP 주소를 바이너리로 변환.
     * 
     * @param ipString IP 주소 문자열
     * @return IP 주소 바이너리
     */
    public byte[] convertIpStringToBinary(String ipString) {
        if (ipString == null || ipString.trim().isEmpty()) {
            return null;
        }
        
        try {
            java.net.InetAddress inetAddress = java.net.InetAddress.getByName(ipString);
            return inetAddress.getAddress();
        } catch (Exception e) {
            return null;
        }
    }
}