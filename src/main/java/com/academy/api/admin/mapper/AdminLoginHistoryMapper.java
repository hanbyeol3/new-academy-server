package com.academy.api.admin.mapper;

import com.academy.api.admin.domain.AdminLoginHistory;
import com.academy.api.admin.dto.response.ResponseAdminLoginHistory;
import com.academy.api.admin.enums.AdminFailReason;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 로그인 이력 Mapper.
 * 
 * AdminLoginHistory 엔티티와 응답 DTO 간 변환을 담당합니다.
 */
@Component
public class AdminLoginHistoryMapper {

    /**
     * AdminLoginHistory 엔티티를 응답 DTO로 변환.
     * 
     * @param loginHistory AdminLoginHistory 엔티티
     * @return 응답 DTO
     */
    public ResponseAdminLoginHistory toResponse(AdminLoginHistory loginHistory) {
        return ResponseAdminLoginHistory.builder()
                .id(loginHistory.getId())
                .adminId(loginHistory.getAdminId())
                .adminUsername(loginHistory.getAdminUsername())
                .success(loginHistory.getSuccess())
                .failReason(loginHistory.getFailReason())
                .ipAddress(convertIpAddress(loginHistory.getIpAddress()))
                .userAgent(loginHistory.getUserAgent())
                .loggedInAt(loginHistory.getLoggedInAt())
                .build();
    }

    /**
     * AdminLoginHistory 엔티티를 응답 DTO로 변환 (관리자 이름 포함).
     * 
     * @param loginHistory AdminLoginHistory 엔티티
     * @param adminName 관리자 이름
     * @return 응답 DTO
     */
    public ResponseAdminLoginHistory toResponse(AdminLoginHistory loginHistory, String adminName) {
        ResponseAdminLoginHistory response = toResponse(loginHistory);
        return ResponseAdminLoginHistory.builder()
                .id(response.getId())
                .adminId(response.getAdminId())
                .adminUsername(response.getAdminUsername())
                .adminName(adminName)
                .success(response.getSuccess())
                .failReason(response.getFailReason())
                .ipAddress(response.getIpAddress())
                .userAgent(response.getUserAgent())
                .loggedInAt(response.getLoggedInAt())
                .build();
    }

    /**
     * AdminLoginHistory 엔티티 목록을 응답 DTO 목록으로 변환.
     * 
     * @param loginHistories AdminLoginHistory 엔티티 목록
     * @return 응답 DTO 목록
     */
    public List<ResponseAdminLoginHistory> toResponseList(List<AdminLoginHistory> loginHistories) {
        return loginHistories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * AdminLoginHistory 페이지를 응답 목록으로 변환.
     * 
     * @param loginHistoryPage AdminLoginHistory 페이지
     * @return 응답 목록
     */
    public ResponseList<ResponseAdminLoginHistory> toResponseList(Page<AdminLoginHistory> loginHistoryPage) {
        List<ResponseAdminLoginHistory> responses = toResponseList(loginHistoryPage.getContent());
        return ResponseList.ok(
                responses,
                loginHistoryPage.getTotalElements(),
                loginHistoryPage.getNumber(),
                loginHistoryPage.getSize()
        );
    }

    /**
     * AdminLoginHistory 페이지를 응답 목록으로 변환 (관리자 이름 포함).
     * 
     * @param loginHistoryPage AdminLoginHistory 페이지
     * @param adminNames 관리자 이름 매핑 (관리자 ID -> 이름)
     * @return 응답 목록
     */
    public ResponseList<ResponseAdminLoginHistory> toResponseListWithNames(Page<AdminLoginHistory> loginHistoryPage, 
                                                                          Map<Long, String> adminNames) {
        List<ResponseAdminLoginHistory> responses = loginHistoryPage.getContent().stream()
                .map(loginHistory -> {
                    String adminName = adminNames.get(loginHistory.getAdminId());
                    return toResponse(loginHistory, adminName);
                })
                .collect(Collectors.toList());

        return ResponseList.ok(
                responses,
                loginHistoryPage.getTotalElements(),
                loginHistoryPage.getNumber(),
                loginHistoryPage.getSize()
        );
    }

    /**
     * 성공한 로그인 이력 생성.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param ipAddress IP 주소 (바이너리)
     * @param userAgent User-Agent
     * @return AdminLoginHistory 엔티티
     */
    public AdminLoginHistory createSuccessLogin(Long adminId, String adminUsername, byte[] ipAddress, String userAgent) {
        return AdminLoginHistory.success(adminId, adminUsername, ipAddress, userAgent);
    }

    /**
     * 성공한 로그인 이력 생성 (IP 문자열 버전).
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param ipString IP 주소 문자열
     * @param userAgent User-Agent
     * @return AdminLoginHistory 엔티티
     */
    public AdminLoginHistory createSuccessLogin(Long adminId, String adminUsername, String ipString, String userAgent) {
        byte[] ipBinary = convertIpStringToBinary(ipString);
        return createSuccessLogin(adminId, adminUsername, ipBinary, userAgent);
    }

    /**
     * 실패한 로그인 이력 생성.
     * 
     * @param adminId 관리자 ID (실패 시 null일 수 있음)
     * @param adminUsername 관리자 사용자명
     * @param failReason 실패 사유
     * @param ipAddress IP 주소 (바이너리)
     * @param userAgent User-Agent
     * @return AdminLoginHistory 엔티티
     */
    public AdminLoginHistory createFailureLogin(Long adminId, String adminUsername, AdminFailReason failReason, 
                                              byte[] ipAddress, String userAgent) {
        return AdminLoginHistory.failure(adminId, adminUsername, failReason, ipAddress, userAgent);
    }

    /**
     * 실패한 로그인 이력 생성 (IP 문자열 버전).
     * 
     * @param adminId 관리자 ID (실패 시 null일 수 있음)
     * @param adminUsername 관리자 사용자명
     * @param failReason 실패 사유
     * @param ipString IP 주소 문자열
     * @param userAgent User-Agent
     * @return AdminLoginHistory 엔티티
     */
    public AdminLoginHistory createFailureLogin(Long adminId, String adminUsername, AdminFailReason failReason, 
                                              String ipString, String userAgent) {
        byte[] ipBinary = convertIpStringToBinary(ipString);
        return createFailureLogin(adminId, adminUsername, failReason, ipBinary, userAgent);
    }

    /**
     * 로그인 통계 생성.
     * 
     * @param totalLogins 총 로그인 수
     * @param successfulLogins 성공 로그인 수
     * @param failedLogins 실패 로그인 수
     * @param successRate 성공률
     * @return 로그인 통계 DTO
     */
    public com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.LoginStatistics createLoginStatistics(
            long totalLogins, long successfulLogins, long failedLogins, double successRate) {
        return new com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.LoginStatistics(
            totalLogins, successfulLogins, failedLogins, successRate);
    }

    /**
     * 일별 로그인 통계 생성.
     * 
     * @param date 날짜
     * @param totalLogins 총 로그인 수
     * @param successfulLogins 성공 로그인 수
     * @param failedLogins 실패 로그인 수
     * @return 일별 로그인 통계 DTO
     */
    public com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.DailyLoginCount createDailyLoginCount(
            java.time.LocalDate date, long totalLogins, long successfulLogins, long failedLogins) {
        return new com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.DailyLoginCount(
            date, totalLogins, successfulLogins, failedLogins);
    }

    /**
     * 의심스러운 로그인 정보 생성.
     * 
     * @param adminUsername 관리자 사용자명
     * @param adminId 관리자 ID
     * @param ipString IP 주소 문자열
     * @param failureCount 실패 횟수
     * @param firstFailure 첫 실패 시각
     * @param lastFailure 마지막 실패 시각
     * @return 의심스러운 로그인 DTO
     */
    public com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.SuspiciousLogin createSuspiciousLogin(
            String adminUsername, Long adminId, String ipString, int failureCount, 
            LocalDateTime firstFailure, LocalDateTime lastFailure) {
        return new com.academy.api.admin.repository.AdminLoginHistoryRepositoryCustom.SuspiciousLogin(
            adminUsername, adminId, ipString, failureCount, firstFailure, lastFailure);
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

    /**
     * HTTP 요청에서 클라이언트 IP 추출.
     * 
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    public String extractClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = null;

        // Proxy를 통한 요청인 경우 실제 IP를 찾기 위한 헤더 확인
        String[] ipHeaders = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {
            clientIp = request.getHeader(header);
            if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
                // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음
                if (clientIp.contains(",")) {
                    clientIp = clientIp.split(",")[0].trim();
                }
                break;
            }
        }

        // 헤더에서 찾지 못한 경우 기본 remote address 사용
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        // IPv6 loopback을 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        return clientIp;
    }
}