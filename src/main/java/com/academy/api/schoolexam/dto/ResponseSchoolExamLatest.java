package com.academy.api.schoolexam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 학교별 시험분석 최신 목록 응답 DTO.
 * 중등부/고등부별로 최신 3개씩 제공
 */
@Getter
@Builder
@Schema(description = "학교별 시험분석 최신 목록 (중등부/고등부별)")
public class ResponseSchoolExamLatest {

    @Schema(description = "중등부 최신 시험분석 목록 (최대 3개)")
    private List<ResponseSchoolExamSummary> middle;

    @Schema(description = "고등부 최신 시험분석 목록 (최대 3개)")
    private List<ResponseSchoolExamSummary> high;
}