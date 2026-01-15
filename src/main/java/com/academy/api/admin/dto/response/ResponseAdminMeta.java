package com.academy.api.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 관리자 메타 정보 응답 DTO.
 * 
 * 관리자 관련 각종 코드/옵션 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "관리자 메타 정보 응답")
public class ResponseAdminMeta {

    @Schema(description = "관리자 권한 목록")
    private List<CodeValue> roles;

    @Schema(description = "계정 상태 목록")
    private List<CodeValue> statuses;

    @Schema(description = "액션 타입 목록")
    private List<CodeValue> actionTypes;

    @Schema(description = "대상 타입 목록")
    private List<CodeValue> targetTypes;

    @Schema(description = "실패 사유 목록")
    private List<CodeValue> failReasons;

    @Schema(description = "관리자 통계")
    private AdminStatistics statistics;

    /**
     * 코드-값 쌍.
     */
    @Schema(description = "코드-값 쌍")
    @Getter
    @Builder
    public static class CodeValue {
        @Schema(description = "코드", example = "ADMIN")
        private String code;
        
        @Schema(description = "한글명", example = "관리자")
        private String name;
    }

    /**
     * 관리자 전체 통계.
     */
    @Schema(description = "관리자 전체 통계")
    @Getter
    @Builder
    public static class AdminStatistics {
        
        @Schema(description = "전체 관리자 수", example = "25")
        private Long totalAdmins;
        
        @Schema(description = "활성 관리자 수", example = "23")
        private Long activeAdmins;
        
        @Schema(description = "정지 관리자 수", example = "1")
        private Long suspendedAdmins;
        
        @Schema(description = "삭제 관리자 수", example = "1")
        private Long deletedAdmins;
        
        @Schema(description = "잠금 관리자 수", example = "0")
        private Long lockedAdmins;
        
        @Schema(description = "이번 달 새 관리자 수", example = "3")
        private Long newAdminsThisMonth;
        
        @Schema(description = "이번 달 총 로그인 수", example = "1250")
        private Long totalLoginsThisMonth;
        
        @Schema(description = "이번 달 성공 로그인 수", example = "1235")
        private Long successfulLoginsThisMonth;
        
        @Schema(description = "이번 달 총 액션 수", example = "5680")
        private Long totalActionsThisMonth;
    }

    /**
     * 기본 메타 정보 생성.
     */
    public static ResponseAdminMeta createDefault() {
        return ResponseAdminMeta.builder()
                .roles(createRoles())
                .statuses(createStatuses())
                .actionTypes(createActionTypes())
                .targetTypes(createTargetTypes())
                .failReasons(createFailReasons())
                .build();
    }

    private static List<CodeValue> createRoles() {
        return List.of(
            CodeValue.builder().code("ADMIN").name("관리자").build(),
            CodeValue.builder().code("SUPER_ADMIN").name("최고관리자").build()
        );
    }

    private static List<CodeValue> createStatuses() {
        return List.of(
            CodeValue.builder().code("ACTIVE").name("활성").build(),
            CodeValue.builder().code("SUSPENDED").name("정지").build(),
            CodeValue.builder().code("DELETED").name("삭제").build()
        );
    }

    private static List<CodeValue> createActionTypes() {
        return List.of(
            CodeValue.builder().code("CREATE").name("생성").build(),
            CodeValue.builder().code("UPDATE").name("수정").build(),
            CodeValue.builder().code("DELETE").name("삭제").build(),
            CodeValue.builder().code("STATUS_CHANGE").name("상태변경").build(),
            CodeValue.builder().code("LOCK_CHANGE").name("잠금변경").build(),
            CodeValue.builder().code("PASSWORD_RESET").name("비밀번호초기화").build(),
            CodeValue.builder().code("PUBLISH").name("게시").build(),
            CodeValue.builder().code("RESTORE").name("복구").build(),
            CodeValue.builder().code("MEMO_UPDATE").name("메모수정").build(),
            CodeValue.builder().code("BATCH_PROCESS").name("일괄처리").build(),
            CodeValue.builder().code("EXPORT").name("내보내기").build(),
            CodeValue.builder().code("CONFIG_CHANGE").name("설정변경").build()
        );
    }

    private static List<CodeValue> createTargetTypes() {
        return List.of(
            CodeValue.builder().code("MEMBERS").name("회원").build(),
            CodeValue.builder().code("NOTICES").name("공지사항").build(),
            CodeValue.builder().code("POPUPS").name("팝업").build(),
            CodeValue.builder().code("TEACHERS").name("강사").build(),
            CodeValue.builder().code("ACADEMIC_SCHEDULES").name("학사일정").build(),
            CodeValue.builder().code("FAQS").name("FAQ").build(),
            CodeValue.builder().code("QNAS").name("Q&A").build(),
            CodeValue.builder().code("INQUIRIES").name("상담문의").build(),
            CodeValue.builder().code("GALLERY_ITEMS").name("갤러리").build(),
            CodeValue.builder().code("CATEGORIES").name("카테고리").build(),
            CodeValue.builder().code("CATEGORY_GROUPS").name("카테고리그룹").build(),
            CodeValue.builder().code("FILES").name("파일").build(),
            CodeValue.builder().code("ACADEMY").name("학원정보").build(),
            CodeValue.builder().code("EXPLANATIONS").name("설명회").build(),
            CodeValue.builder().code("RECRUITMENTS").name("채용공고").build(),
            CodeValue.builder().code("UNIVERSITIES").name("대학정보").build(),
            CodeValue.builder().code("SUCCESS_CASES").name("성공사례").build(),
            CodeValue.builder().code("IMPROVEMENTS").name("성적향상").build(),
            CodeValue.builder().code("STUDENTS").name("학생정보").build(),
            CodeValue.builder().code("SHUTTLES").name("셔틀버스").build(),
            CodeValue.builder().code("FACILITIES").name("시설안내").build(),
            CodeValue.builder().code("SYSTEM_CONFIG").name("시스템설정").build()
        );
    }

    private static List<CodeValue> createFailReasons() {
        return List.of(
            CodeValue.builder().code("BAD_CREDENTIALS").name("잘못된 인증 정보").build(),
            CodeValue.builder().code("SUSPENDED").name("정지된 계정").build(),
            CodeValue.builder().code("DELETED").name("삭제된 계정").build(),
            CodeValue.builder().code("LOCKED").name("잠긴 계정").build(),
            CodeValue.builder().code("USER_NOT_FOUND").name("존재하지 않는 사용자").build(),
            CodeValue.builder().code("PASSWORD_EXPIRED").name("비밀번호 만료").build(),
            CodeValue.builder().code("TOO_MANY_ATTEMPTS").name("너무 많은 로그인 시도").build(),
            CodeValue.builder().code("IP_BLOCKED").name("IP 차단").build(),
            CodeValue.builder().code("TIME_RESTRICTED").name("시간 제한").build(),
            CodeValue.builder().code("INSUFFICIENT_PRIVILEGES").name("권한 부족").build(),
            CodeValue.builder().code("SYSTEM_ERROR").name("시스템 오류").build()
        );
    }
}