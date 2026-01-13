package com.academy.api.explanation.mapper;

import com.academy.api.explanation.domain.Explanation;
import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationSchedule;
import com.academy.api.explanation.dto.*;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.dto.ResponseFileInfo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 설명회 도메인 매퍼.
 * 
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
public class ExplanationMapper {

    /**
     * Explanation Entity를 Response DTO로 변환.
     * 
     * @param explanation 설명회 엔티티
     * @param schedules 회차 목록
     * @return 설명회 응답 DTO
     */
    public ResponseExplanation toResponse(Explanation explanation, List<ExplanationSchedule> schedules) {
        return toResponse(explanation, schedules, List.of());
    }

    /**
     * Explanation Entity를 Response DTO로 변환 (인라인 이미지 포함).
     * 
     * @param explanation 설명회 엔티티
     * @param schedules 회차 목록
     * @param inlineImages 인라인 이미지 목록
     * @return 설명회 응답 DTO
     */
    public ResponseExplanation toResponse(Explanation explanation, List<ExplanationSchedule> schedules, List<ResponseFileInfo> inlineImages) {
        return ResponseExplanation.builder()
                .id(explanation.getId())
                .division(explanation.getDivision())
                .title(explanation.getTitle())
                .content(explanation.getContent())
                .isPublished(explanation.getIsPublished())
                .viewCount(explanation.getViewCount())
                .schedules(schedules.stream()
                        .map(this::toScheduleResponse)
                        .collect(Collectors.toList()))
                .inlineImages(inlineImages)
                .createdAt(explanation.getCreatedAt())
                .updatedAt(explanation.getUpdatedAt())
                .build();
    }

    /**
     * Explanation Entity를 목록용 Response DTO로 변환.
     * 
     * @param explanation 설명회 엔티티
     * @param schedules 회차 목록
     * @return 설명회 목록 응답 DTO
     */
    public ResponseExplanationListItem toListItemResponse(Explanation explanation, List<ExplanationSchedule> schedules) {
        List<ResponseExplanationSchedule> scheduleResponses = schedules.stream()
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());

        // 예약 가능한 회차가 있는지 확인
        boolean hasReservableSchedule = schedules.stream()
                .anyMatch(ExplanationSchedule::isReservable);

        return ResponseExplanationListItem.builder()
                .explanationId(explanation.getId())
                .division(explanation.getDivision())
                .title(explanation.getTitle())
                .isPublished(explanation.getIsPublished())
                .viewCount(explanation.getViewCount())
                .hasReservableSchedule(hasReservableSchedule)
                .schedules(scheduleResponses)
                .createdAt(explanation.getCreatedAt())
                .build();
    }

    /**
     * ExplanationSchedule Entity를 Response DTO로 변환.
     * 
     * @param schedule 회차 엔티티
     * @return 회차 응답 DTO
     */
    public ResponseExplanationSchedule toScheduleResponse(ExplanationSchedule schedule) {
        return ResponseExplanationSchedule.builder()
                .scheduleId(schedule.getId())
                .roundNo(schedule.getRoundNo())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .location(schedule.getLocation())
                .applyStartAt(schedule.getApplyStartAt())
                .applyEndAt(schedule.getApplyEndAt())
                .status(schedule.getStatus())
                .capacity(schedule.getCapacity())
                .reservedCount(schedule.getReservedCount())
                .isReservable(schedule.isReservable())
                .build();
    }

    /**
     * ExplanationReservation Entity를 Response DTO로 변환.
     * 
     * @param reservation 예약 엔티티
     * @return 예약 응답 DTO
     */
    public ResponseExplanationReservation toReservationResponse(ExplanationReservation reservation) {
        return ResponseExplanationReservation.builder()
                .id(reservation.getId())
                .scheduleId(reservation.getScheduleId())
                .status(reservation.getStatus())
                .canceledBy(reservation.getCanceledBy())
                .canceledAt(reservation.getCanceledAt())
                .applicantName(reservation.getApplicantName())
                .applicantPhone(reservation.getApplicantPhone())
                .studentName(reservation.getStudentName())
                .studentPhone(reservation.getStudentPhone())
                .gender(reservation.getGender())
                .academicTrack(reservation.getAcademicTrack())
                .schoolName(reservation.getSchoolName())
                .grade(reservation.getGrade())
                .memo(reservation.getMemo())
                .isMarketingAgree(reservation.getIsMarketingAgree())
                .clientIp(byteArrayToIpString(reservation.getClientIp()))
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * 설명회 생성 요청 DTO를 Entity로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param createdBy 생성자 ID
     * @return 설명회 엔티티
     */
    public Explanation toEntity(RequestExplanationCreate request, Long createdBy) {
        return Explanation.builder()
                .division(request.getDivision())
                .title(request.getTitle())
                .content(request.getContent())
                .isPublished(request.getIsPublished())
                .createdBy(createdBy)
                .build();
    }

    /**
     * 회차 생성 요청 DTO를 Entity로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param explanationId 설명회 ID
     * @param createdBy 생성자 ID
     * @return 회차 엔티티
     */
    public ExplanationSchedule toScheduleEntity(RequestExplanationScheduleCreate request, Long explanationId, Long createdBy) {
        return ExplanationSchedule.builder()
                .explanationId(explanationId)
                .roundNo(request.getRoundNo())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .location(request.getLocation())
                .applyStartAt(request.getApplyStartAt())
                .applyEndAt(request.getApplyEndAt())
                .status(request.getStatus())
                .capacity(request.getCapacity())
                .createdBy(createdBy)
                .build();
    }

    /**
     * 예약 생성 요청 DTO를 Entity로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param clientIp 클라이언트 IP
     * @return 예약 엔티티
     */
    public ExplanationReservation toReservationEntity(RequestExplanationReservationCreate request, String clientIp) {
        return ExplanationReservation.builder()
                .scheduleId(request.getScheduleId())
                .applicantName(request.getApplicantName())
                .applicantPhone(request.getApplicantPhone())
                .studentName(request.getStudentName())
                .studentPhone(request.getStudentPhone())
                .gender(request.getGender())
                .academicTrack(request.getAcademicTrack())
                .schoolName(request.getSchoolName())
                .grade(request.getGrade())
                .memo(request.getMemo())
                .isMarketingAgree(request.getIsMarketingAgree())
                .clientIp(ipStringToByteArray(clientIp))
                .build();
    }

    /**
     * 설명회 목록 페이지를 ResponseList로 변환.
     * 
     * @param page 설명회 페이지
     * @param schedulesByExplanationId 설명회별 회차 목록
     * @return 설명회 목록 응답
     */
    public ResponseList<ResponseExplanationListItem> toListResponse(Page<Explanation> page, 
            java.util.Map<Long, List<ExplanationSchedule>> schedulesByExplanationId) {
        
        List<ResponseExplanationListItem> items = page.getContent().stream()
                .map(explanation -> {
                    List<ExplanationSchedule> schedules = schedulesByExplanationId.getOrDefault(
                            explanation.getId(), List.of());
                    return toListItemResponse(explanation, schedules);
                })
                .collect(Collectors.toList());

        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 예약 목록 페이지를 ResponseList로 변환.
     * 
     * @param page 예약 페이지
     * @return 예약 목록 응답
     */
    public ResponseList<ResponseExplanationReservation> toReservationListResponse(Page<ExplanationReservation> page) {
        List<ResponseExplanationReservation> items = page.getContent().stream()
                .map(this::toReservationResponse)
                .collect(Collectors.toList());

        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * IP 문자열을 바이트 배열로 변환.
     * 
     * @param ipString IP 문자열
     * @return 바이트 배열 (INET6_ATON 형태)
     */
    private byte[] ipStringToByteArray(String ipString) {
        if (ipString == null || ipString.trim().isEmpty()) {
            return null;
        }

        try {
            InetAddress addr = InetAddress.getByName(ipString.trim());
            if (addr instanceof Inet6Address) {
                return addr.getAddress();
            } else {
                // IPv4를 IPv6 매핑 형태로 저장 (::ffff:192.168.1.1)
                byte[] ipv4Bytes = addr.getAddress();
                byte[] ipv6Bytes = new byte[16];
                // IPv4-mapped IPv6 주소 형태 (::ffff:x.x.x.x)
                ipv6Bytes[10] = (byte) 0xff;
                ipv6Bytes[11] = (byte) 0xff;
                System.arraycopy(ipv4Bytes, 0, ipv6Bytes, 12, 4);
                return ipv6Bytes;
            }
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 바이트 배열을 IP 문자열로 변환.
     * 
     * @param ipBytes 바이트 배열
     * @return IP 문자열
     */
    private String byteArrayToIpString(byte[] ipBytes) {
        if (ipBytes == null || ipBytes.length == 0) {
            return null;
        }

        try {
            InetAddress addr = InetAddress.getByAddress(ipBytes);
            String hostAddress = addr.getHostAddress();
            
            // IPv4-mapped IPv6 주소인 경우 IPv4 형태로 변환
            if (hostAddress.startsWith("::ffff:") && hostAddress.contains(".")) {
                return hostAddress.substring(7); // "::ffff:" 제거
            }
            
            return hostAddress;
        } catch (UnknownHostException e) {
            return null;
        }
    }
}