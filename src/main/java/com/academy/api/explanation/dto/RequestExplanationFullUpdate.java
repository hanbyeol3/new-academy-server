package com.academy.api.explanation.dto;

import com.academy.api.file.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 설명회 통합 수정 요청 DTO.
 * 
 * 기본 정보와 회차 정보를 한 번에 수정할 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 통합 수정 요청 (기본 정보 + 회차 관리)")
public class RequestExplanationFullUpdate {

    @Valid
    @NotNull(message = "기본 정보를 입력해주세요")
    @Schema(description = "설명회 기본 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    private BasicInfo basic;

    @Valid
    @Schema(description = "회차 관리 정보")
    private ScheduleOperations schedules;

    /**
     * 설명회 기본 정보.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "설명회 기본 정보")
    public static class BasicInfo {
        
        @NotBlank(message = "설명회 제목을 입력해주세요")
        @Size(max = 255, message = "설명회 제목은 255자 이하여야 합니다")
        @Schema(description = "설명회 제목", 
                example = "2024 고등부 입학설명회",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @Schema(description = "설명회 내용", 
                example = "고등부 교육과정 및 입학 절차에 대한 상세한 안내를 제공합니다.")
        private String content;

        @Schema(description = "게시 여부", example = "true")
        private Boolean isPublished;

        @Schema(description = "새로 추가할 본문 이미지 목록 (임시파일 → 정식파일 변환)")
        private List<FileReference> newInlineImages;
        
        @Schema(description = "삭제할 기존 본문이미지 파일 ID 목록 (정식파일 Long ID)")
        private List<Long> deleteInlineImageFileIds;
    }

    /**
     * 회차 관리 정보.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "회차 관리 정보 (수정/추가/삭제)")
    public static class ScheduleOperations {
        
        @Valid
        @Schema(description = "수정할 기존 회차 목록")
        private List<UpdateSchedule> update = new ArrayList<>();

        @Valid
        @Schema(description = "새로 추가할 회차 목록")
        private List<RequestExplanationScheduleCreate> create = new ArrayList<>();

        @Schema(description = "삭제할 회차 ID 목록")
        private List<Long> delete = new ArrayList<>();
    }

    /**
     * 기존 회차 수정 정보.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "기존 회차 수정 정보")
    public static class UpdateSchedule extends RequestExplanationScheduleUpdate {
        
        @NotNull(message = "회차 ID를 입력해주세요")
        @Schema(description = "수정할 회차 ID", 
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Long scheduleId;
    }
}