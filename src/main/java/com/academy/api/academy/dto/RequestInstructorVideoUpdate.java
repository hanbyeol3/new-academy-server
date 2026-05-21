package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;

/**
 * 강사진 유튜브 URL 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "강사진 유튜브 URL 수정 요청")
public class RequestInstructorVideoUpdate {

    @Size(max = 255, message = "강사진 유튜브 URL은 255자 이하여야 합니다")
    @Schema(description = "강사진 소개 유튜브 URL", example = "https://www.youtube.com/watch?v=example")
    private String instructorYoutubeUrl;
}