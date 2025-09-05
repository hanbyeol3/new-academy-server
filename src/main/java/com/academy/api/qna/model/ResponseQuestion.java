package com.academy.api.qna.model;

import com.academy.api.qna.domain.QnaQuestion;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QnA 질문 도메인의 통합 응답 모델 클래스.
 * 
 * Notice 패턴을 따라 질문과 관련된 모든 응답 DTO, 검색 조건, 프로젝션을 하나의 파일에 구성한다.
 * 비밀글 처리와 개인정보 보호를 위한 마스킹 기능을 포함한다.
 * 
 * 포함 클래스:
 *  - ResponseQuestion: 질문 상세 정보 응답 DTO (메인 클래스)
 *  - Criteria: 질문 검색 조건
 *  - Projection: 질문 목록용 요약 정보
 *  - Summary: 통계용 요약 정보
 * 
 * 보안 고려사항:
 *  - 작성자 이름 마스킹 (홍*동, 김**) 
 *  - 전화번호 마스킹 (010-1234-****)
 *  - 비밀글 내용은 권한 검증 후에만 노출
 *  - IP 주소는 관리자에게만 노출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "QnA 질문 응답")
public class ResponseQuestion {

    /** 질문 ID (PK) */
    @Schema(description = "질문 ID", example = "1")
    private Long id;

    /** 작성자 이름 (마스킹 처리됨) */
    @Schema(description = "작성자 이름", example = "홍*동")
    private String authorName;

    /** 연락처 전화번호 (마스킹 처리됨) */
    @Schema(description = "연락처", example = "010-1234-****")
    private String phoneNumber;

    /** 질문 제목 */
    @Schema(description = "질문 제목", example = "온라인 수강 신청 관련 문의")
    private String title;

    /** 질문 본문 (비밀글인 경우 권한 검증 필요) */
    @Schema(description = "질문 내용", example = "온라인 수강 신청은 어떻게 하나요?")
    private String content;

    /** 비밀글 여부 */
    @Schema(description = "비밀글 여부", example = "false")
    private Boolean secret;

    /** 상단 고정 여부 */
    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean pinned;

    /** 게시 여부 */
    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    /** 조회수 */
    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    /** 답변 등록 여부 */
    @Schema(description = "답변 완료 여부", example = "false")
    private Boolean isAnswered;

    /** 답변 등록 시각 */
    @Schema(description = "답변 등록 시각", example = "2024-01-15T14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answeredAt;

    /** 개인정보 수집 동의 여부 */
    @Schema(description = "개인정보 수집 동의", example = "true")
    private Boolean privacyConsent;

    /** 작성자 IP 주소 (관리자만 조회 가능) */
    @Schema(description = "작성자 IP", example = "192.168.1.1")
    private String ipAddress;

    /** 생성 일시 */
    @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /** 답변 정보 */
    @Schema(description = "답변 정보")
    private ResponseAnswer answer;

    /**
     * 엔티티에서 ResponseQuestion으로 변환하는 정적 팩토리 메서드.
     * 
     * @param question 질문 엔티티
     * @param isAuthorOrAdmin 작성자 본인 또는 관리자 여부
     * @param hasSecretAccess 비밀글 접근 권한 여부
     * @param answer 답변 정보
     * @return ResponseQuestion DTO
     */
    public static ResponseQuestion from(QnaQuestion question, boolean isAuthorOrAdmin, boolean hasSecretAccess, ResponseAnswer answer) {
        if (question == null) {
            return null;
        }

        ResponseQuestionBuilder builder = ResponseQuestion.builder()
                .id(question.getId())
                .authorName(maskAuthorName(question.getAuthorName()))
                .title(question.getTitle())
                .secret(question.getSecret())
                .pinned(question.getPinned())
                .published(question.getPublished())
                .viewCount(question.getViewCount())
                .isAnswered(question.getIsAnswered())
                .answeredAt(question.getAnsweredAt())
                .privacyConsent(question.getPrivacyConsent())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .answer(answer);

        // 작성자 본인이거나 관리자인 경우에만 상세 정보 제공
        if (isAuthorOrAdmin) {
            builder.phoneNumber(maskPhoneNumber(question.getPhoneNumber()))
                   .ipAddress(question.getIpAddress());
        }

        // 비밀글 접근 권한이 있는 경우에만 내용 제공
        if (!question.isSecret() || hasSecretAccess) {
            builder.content(question.getContent());
        }

        return builder.build();
    }

    /**
     * 기존 호환성을 위한 오버로드 메서드.
     */
    public static ResponseQuestion from(QnaQuestion question, boolean isAuthorOrAdmin, boolean hasSecretAccess) {
        return from(question, isAuthorOrAdmin, hasSecretAccess, null);
    }

    /**
     * 공개용 변환 - 일반 사용자용 (개인정보 최대 마스킹).
     */
    public static ResponseQuestion fromPublic(QnaQuestion question) {
        return from(question, false, false, null);
    }

    /**
     * 공개용 변환 - 답변 포함.
     */
    public static ResponseQuestion fromPublic(QnaQuestion question, ResponseAnswer answer) {
        return from(question, false, false, answer);
    }

    /**
     * 관리자용 변환 - 모든 정보 노출.
     */
    public static ResponseQuestion fromAdmin(QnaQuestion question) {
        return from(question, true, true, null);
    }

    /**
     * 관리자용 변환 - 답변 포함.
     */
    public static ResponseQuestion fromAdmin(QnaQuestion question, ResponseAnswer answer) {
        return from(question, true, true, answer);
    }

    /**
     * 엔티티 목록을 ResponseQuestion 목록으로 변환.
     */
    public static List<ResponseQuestion> fromList(List<QnaQuestion> questions, 
                                                boolean isAuthorOrAdmin, 
                                                boolean hasSecretAccess) {
        if (questions == null || questions.isEmpty()) {
            return List.of();
        }

        return questions.stream()
                .map(question -> from(question, isAuthorOrAdmin, hasSecretAccess))
                .collect(Collectors.toList());
    }

    /**
     * 작성자 이름 마스킹 처리.
     * 예: "홍길동" → "홍*동", "김철수" → "김**"
     */
    private static String maskAuthorName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        
        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            masked.append("*");
        }
        masked.append(name.charAt(name.length() - 1));
        
        return masked.toString();
    }

    /**
     * 전화번호 마스킹 처리.
     * 예: "010-1234-5678" → "010-1234-****"
     */
    private static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        
        // 마지막 4자리를 *로 마스킹
        return phone.substring(0, phone.length() - 4) + "****";
    }

    /**
     * QnA 질문 검색 조건을 정의하는 내부 클래스.
     * 
     * 동적 쿼리 생성을 위한 다양한 검색 조건을 제공하며,
     * null 값은 해당 조건을 무시하는 방식으로 동작한다.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Criteria {

        /** 키워드 검색 (제목, 내용, 작성자명에서 검색) */
        private String keyword;

        /** 검색 필드 지정 (title, content, author) */
        private String searchField;

        /** 비밀글 필터 (exclude: 제외, only: 비밀글만, include: 전체) */
        private String secret;

        /** 답변 완료 여부 필터 */
        private Boolean isAnswered;

        /** 상단 고정 여부 필터 */
        private Boolean pinned;

        /** 게시 여부 필터 */
        private Boolean published;

        /** 작성일 시작 날짜 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dateFrom;

        /** 작성일 종료 날짜 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dateTo;

        /** IP 주소 검색 (관리자만) */
        private String ipAddress;

        /** 작성자 이름 검색 (관리자만) */
        private String authorName;

        /** 전화번호 검색 (관리자만) */
        private String phoneNumber;
    }

    /**
     * 질문 목록용 프로젝션 클래스.
     * 목록 조회 시 필요한 핵심 정보만 포함하여 성능을 최적화한다.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Projection {

        /** 질문 ID */
        private Long id;

        /** 질문 제목 */
        private String title;

        /** 작성자 이름 (마스킹) */
        private String authorName;

        /** 비밀글 여부 */
        private Boolean secret;

        /** 상단 고정 여부 */
        private Boolean pinned;

        /** 답변 완료 여부 */
        private Boolean isAnswered;

        /** 조회수 */
        private Long viewCount;

        /** 작성일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        /**
         * ResponseQuestion에서 Projection으로 변환.
         */
        public static Projection from(ResponseQuestion response) {
            if (response == null) {
                return null;
            }

            return Projection.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .authorName(response.getAuthorName())
                    .secret(response.getSecret())
                    .pinned(response.getPinned())
                    .isAnswered(response.getIsAnswered())
                    .viewCount(response.getViewCount())
                    .createdAt(response.getCreatedAt())
                    .build();
        }

        /**
         * 엔티티에서 직접 Projection으로 변환 (성능 최적화).
         */
        public static Projection fromEntity(QnaQuestion question) {
            if (question == null) {
                return null;
            }

            return Projection.builder()
                    .id(question.getId())
                    .title(question.getTitle())
                    .authorName(maskAuthorName(question.getAuthorName()))
                    .secret(question.getSecret())
                    .pinned(question.getPinned())
                    .isAnswered(question.getIsAnswered())
                    .viewCount(question.getViewCount())
                    .createdAt(question.getCreatedAt())
                    .build();
        }

        /**
         * 엔티티 목록에서 Projection 목록으로 변환.
         */
        public static List<Projection> fromEntityList(List<QnaQuestion> questions) {
            if (questions == null || questions.isEmpty()) {
                return List.of();
            }

            return questions.stream()
                    .map(Projection::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    /**
     * QnA 통계용 요약 정보 클래스.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        /** 전체 질문 수 */
        private Long totalQuestions;

        /** 답변 완료 질문 수 */
        private Long answeredQuestions;

        /** 미답변 질문 수 */
        private Long unansweredQuestions;

        /** 비밀 질문 수 */
        private Long secretQuestions;

        /** 고정 질문 수 */
        private Long pinnedQuestions;

        /** 답변률 (백분율) */
        private Double answerRate;

        /**
         * 답변률 계산.
         */
        public static Summary create(long total, long answered, long secret, long pinned) {
            double rate = total > 0 ? (double) answered / total * 100 : 0.0;

            return Summary.builder()
                    .totalQuestions(total)
                    .answeredQuestions(answered)
                    .unansweredQuestions(total - answered)
                    .secretQuestions(secret)
                    .pinnedQuestions(pinned)
                    .answerRate(Math.round(rate * 100.0) / 100.0) // 소수점 2자리 반올림
                    .build();
        }
    }
}