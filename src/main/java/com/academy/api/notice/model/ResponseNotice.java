package com.academy.api.notice.model;

import com.academy.api.file.dto.UploadFileDto;
import com.academy.api.notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공지사항 도메인의 단일 모델 파일.
 *
 * - 외부 API 응답 DTO: ResponseNotice (바깥 클래스)
 * - 내부 검색 조건: ResponseNotice.Criteria (정적 중첩 클래스)
 * - 조회 전용 프로젝션: ResponseNotice.Projection (정적 중첩 클래스)
 *
 * 목적:
 *  1) 도메인과 밀접한 응답/검색/프로젝션을 한 파일에 모아 가시성 향상
 *  2) import 간소화 및 역할 분리(정적 중첩 클래스로 책임 구분)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "공지사항 응답")
public class ResponseNotice {

    /** 공지사항 ID (PK) */
    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    /** 제목 */
    @Schema(description = "제목", example = "시스템 점검 안내")
    private String title;

    /** 내용(본문) */
    @Schema(description = "내용", example = "2024년 1월 1일 새벽 2시부터 4시까지 시스템 점검이 있습니다.")
    private String content;

    /** 상단 고정 여부 (true: 고정) */
    @Schema(description = "고정 여부", example = "false")
    private Boolean pinned;

    /** 발행 여부 (true: 노출/발행 상태) */
    @Schema(description = "발행 여부", example = "true")
    private Boolean published;

    /** 조회수 */
    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    /** 생성 일시 */
    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Schema(description = "수정일시", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;

    /** 첨부파일 목록 */
    @Schema(description = "첨부파일 목록")
    private List<UploadFileDto> attachedFiles;

    /**
     * QueryDSL용 생성자 (attachedFiles 제외).
     */
    public ResponseNotice(Long id, String title, String content, Boolean pinned, Boolean published, 
                         Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.pinned = pinned;
        this.published = published;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachedFiles = Collections.emptyList();
    }

    /**
     * 엔티티(Notice) → 응답 DTO(ResponseNotice)로 변환하는 팩토리 메서드.
     * - 왜? 엔티티를 그대로 노출하지 않고, API 계약 모델로 매핑하기 위함.
     *
     * @param notice 변환할 Notice 엔티티 (null 아님 가정)
     * @return 변환된 ResponseNotice 인스턴스
     */
    public static ResponseNotice from(final Notice notice) {
        // Builder 패턴으로 필드 매핑: 가독성 및 불변성 향상
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.getPinned())
                .published(notice.getPublished())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티(Notice)와 첨부파일 → 응답 DTO(ResponseNotice)로 변환하는 팩토리 메서드.
     *
     * @param notice 변환할 Notice 엔티티 (null 아님 가정)
     * @param attachedFiles 첨부파일 목록
     * @return 변환된 ResponseNotice 인스턴스
     */
    public static ResponseNotice from(final Notice notice, final List<UploadFileDto> attachedFiles) {
        // Builder 패턴으로 필드 매핑: 가독성 및 불변성 향상
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.getPinned())
                .published(notice.getPublished())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .attachedFiles(attachedFiles)
                .build();
    }

    /**
     * 엔티티 리스트 → 응답 DTO 리스트로 일괄 변환.
     * - 왜? 목록 응답 시 반복 매핑 로직을 한 곳에서 관리하기 위함.
     *
     * @param entities Notice 엔티티 리스트 (null 허용 X 권장)
     * @return 변환된 ResponseNotice 리스트
     */
    public static List<ResponseNotice> fromList(final List<Notice> entities) {
        // Java Stream으로 map 변환 후 collect
        return entities.stream().map(ResponseNotice::from).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 내부 검색 조건 (컨트롤러 바인딩 → 서비스/리포지토리 where 구성)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 공지사항 검색 조건 모델.
     * - API 계약(응답 본문)에는 포함되지 않는 내부 전달용 DTO이다.
     * - null 또는 빈 값인 필드는 where 조건에서 제외하는 방식으로 사용한다.
     */
    @Getter @Setter
    @Schema(description = "공지사항 검색 조건")
    public static class Criteria {
        /** 제목 포함 검색 키워드 (null/빈칸이면 조건 제외) */
        @Schema(description = "제목 포함 검색 키워드", example = "시스템")
        private String titleLike;

        /** 내용 포함 검색 키워드 (null/빈칸이면 조건 제외) */
        @Schema(description = "내용 포함 검색 키워드", example = "점검")
        private String contentLike;

        /** 발행 여부 (null이면 조건 제외) */
        @Schema(description = "발행 여부 필터", example = "true")
        private Boolean published;

        /** 상단 고정 여부 (null이면 조건 제외) */
        @Schema(description = "상단 고정 여부 필터", example = "false")
        private Boolean pinned;

        /** 생성일 시작(이상) — null이면 조건 제외 */
        @Schema(description = "생성일 시작 날짜", example = "2024-01-01 00:00:00")
        private LocalDateTime createdFrom;

        /** 생성일 종료(이하) — null이면 조건 제외 */
        @Schema(description = "생성일 종료 날짜", example = "2024-12-31 23:59:59")
        private LocalDateTime createdTo;

        /** 수정일 시작(이상) — null이면 조건 제외 */
        @Schema(description = "수정일 시작 날짜", example = "2024-01-01 00:00:00")
        private LocalDateTime updatedFrom;

        /** 수정일 종료(이하) — null이면 조건 제외 */
        @Schema(description = "수정일 종료 날짜", example = "2024-12-31 23:59:59")
        private LocalDateTime updatedTo;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 조회 전용 프로젝션 (목록 요약 등에 사용)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 공지사항 요약 프로젝션(뷰).
     * - 목록/카드뷰 등에 필요한 최소 필드만 담는다.
     * - QueryDSL constructor/projection 의 대상 타입으로도 사용 가능.
     */
    @Getter
    @AllArgsConstructor
    public static class Projection {
        /** 공지사항 ID */
        private final Long id;

        /** 제목 */
        private final String title;

        /** 상단 고정 여부 */
        private final Boolean pinned;

        /** 발행 여부 */
        private final Boolean published;

        /** 생성일시 */
        private final LocalDateTime createdAt;
    }
}