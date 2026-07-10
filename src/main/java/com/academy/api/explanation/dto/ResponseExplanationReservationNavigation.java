package com.academy.api.explanation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설명회 예약 네비게이션 정보 DTO (이전글/다음글).
 */
@Getter
@Builder
@Schema(description = "설명회 예약 네비게이션 정보")
public class ResponseExplanationReservationNavigation {

	@Schema(description = "이전 예약")
	private NavigationItem previous;

	@Schema(description = "다음 예약")
	private NavigationItem next;

	/**
	 * 네비게이션 아이템 (이전글/다음글).
	 */
	@Getter
	@Builder
	@Schema(description = "네비게이션 아이템")
	public static class NavigationItem {

		@Schema(description = "예약 ID", example = "122")
		private Long id;

		@Schema(description = "신청자명", example = "홍길동")
		private String applicantName;

		@Schema(description = "학생명", example = "홍학생")
		private String studentName;

		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		@Schema(description = "신청 시각", example = "2025-11-24 09:00:00")
		private LocalDateTime createdAt;
	}

	/**
	 * 네비게이션 정보 생성.
	 */
	public static ResponseExplanationReservationNavigation of(NavigationItem previous, NavigationItem next) {
		return ResponseExplanationReservationNavigation.builder()
				.previous(previous)
				.next(next)
				.build();
	}
}