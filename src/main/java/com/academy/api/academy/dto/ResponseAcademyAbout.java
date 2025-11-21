package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 학원 소개 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "학원 소개 정보 응답")
public class ResponseAcademyAbout {

    @Schema(description = "학원 소개 ID", example = "1")
    private Long id;

    @Schema(description = "메인 타이틀", example = "최고의 교육으로 꿈을 실현하는 ABC학원")
    private String mainTitle;

    @Schema(description = "메인 포인트 타이틀", example = "개인 맞춤형 교육으로 성공을 이끄는 학원")
    private String mainPointTitle;

    @Schema(description = "메인 설명", example = "ABC학원은 20년 전통의 교육 노하우로...")
    private String mainDescription;

    @Schema(description = "메인 이미지 경로", example = "/uploads/academy/main-image.jpg")
    private String mainImagePath;

    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "2")
    private Long updatedBy;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}