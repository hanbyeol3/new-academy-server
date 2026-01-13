package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설명회 예약 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 예약 응답")
public class ResponseExplanationReservation {

    @Schema(description = "예약 ID", example = "1")
    private Long id;

    @Schema(description = "회차 ID", example = "1")
    private Long scheduleId;

    @Schema(description = "예약 상태", example = "CONFIRMED")
    private ReservationStatus status;

    @Schema(description = "취소 주체", example = "USER")
    private CanceledBy canceledBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "취소 시각", example = "2024-01-12 15:30:00")
    private LocalDateTime canceledAt;

    @Schema(description = "신청자 이름", example = "홍길동")
    private String applicantName;

    @Schema(description = "신청자 휴대폰 번호", example = "010-1234-5678")
    private String applicantPhone;

    @Schema(description = "학생 이름", example = "홍학생")
    private String studentName;

    @Schema(description = "학생 휴대폰 번호", example = "010-9876-5432")
    private String studentPhone;

    @Schema(description = "성별", example = "M")
    private Gender gender;

    @Schema(description = "계열", example = "SCIENCE")
    private AcademicTrack academicTrack;

    @Schema(description = "학교명", example = "서울고등학교")
    private String schoolName;

    @Schema(description = "학년", example = "고3")
    private String grade;

    @Schema(description = "메모", example = "특별한 요청사항")
    private String memo;

    @Schema(description = "마케팅 수신 동의", example = "true")
    private Boolean isMarketingAgree;

    @Schema(description = "신청자 IP", example = "192.168.1.100")
    private String clientIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-10 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-10 14:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "설명회 정보 (상세 조회시)")
    private ResponseExplanation explanation;

    @Schema(description = "회차 정보 (상세 조회시)")
    private ResponseExplanationSchedule schedule;
}