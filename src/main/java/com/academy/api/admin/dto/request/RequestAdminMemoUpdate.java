package com.academy.api.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 메모 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 메모 수정 요청")
public class RequestAdminMemoUpdate {

    @Size(max = 500, message = "메모는 500자 이하여야 합니다")
    @Schema(description = "관리자 메모", example = "우수 관리자, 특별 권한 부여")
    private String memo;
}