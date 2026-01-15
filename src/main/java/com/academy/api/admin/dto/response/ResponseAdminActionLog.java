package com.academy.api.admin.dto.response;

import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 관리자 액션 로그 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "관리자 액션 로그 응답")
public class ResponseAdminActionLog {

    @Schema(description = "로그 ID", example = "1")
    private Long id;

    @Schema(description = "관리자 ID", example = "1")
    private Long adminId;

    @Schema(description = "관리자 아이디", example = "admin001")
    private String adminUsername;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String adminName;

    @Schema(description = "액션 타입", example = "CREATE")
    private AdminActionType actionType;

    @Schema(description = "액션 타입 한글명", example = "생성")
    private String actionTypeName;

    @Schema(description = "대상 타입", example = "NOTICES")
    private AdminTargetType targetType;

    @Schema(description = "대상 타입 한글명", example = "공지사항")
    private String targetTypeName;

    @Schema(description = "대상 ID", example = "123")
    private Long targetId;

    @Schema(description = "대상 스냅샷", example = "공지사항 제목")
    private String targetSnapshot;

    @Schema(description = "상세 정보 (JSON)")
    private Map<String, Object> actionDetail;

    @Schema(description = "IP 주소", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "User-Agent", example = "Mozilla/5.0...")
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "액션 발생 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    /**
     * 액션 타입 한글명 반환.
     */
    public String getActionTypeName() {
        if (actionType == null) return null;
        return switch (actionType) {
            case CREATE -> "생성";
            case UPDATE -> "수정";
            case DELETE -> "삭제";
            case STATUS_CHANGE -> "상태변경";
            case LOCK_CHANGE -> "잠금변경";
            case PASSWORD_RESET -> "비밀번호초기화";
            case PUBLISH -> "게시";
            case RESTORE -> "복구";
            case MEMO_UPDATE -> "메모수정";
            case BATCH_PROCESS -> "일괄처리";
            case EXPORT -> "내보내기";
            case CONFIG_CHANGE -> "설정변경";
        };
    }

    /**
     * 대상 타입 한글명 반환.
     */
    public String getTargetTypeName() {
        if (targetType == null) return null;
        return switch (targetType) {
            case MEMBERS -> "회원";
            case NOTICES -> "공지사항";
            case POPUPS -> "팝업";
            case TEACHERS -> "강사";
            case ACADEMIC_SCHEDULES -> "학사일정";
            case FAQS -> "FAQ";
            case QNAS -> "Q&A";
            case INQUIRIES -> "상담문의";
            case GALLERY_ITEMS -> "갤러리";
            case CATEGORIES -> "카테고리";
            case CATEGORY_GROUPS -> "카테고리그룹";
            case FILES -> "파일";
            case ACADEMY -> "학원정보";
            case EXPLANATIONS -> "설명회";
            case RECRUITMENTS -> "채용공고";
            case UNIVERSITIES -> "대학정보";
            case SUCCESS_CASES -> "성공사례";
            case IMPROVEMENTS -> "성적향상";
            case STUDENTS -> "학생정보";
            case SHUTTLES -> "셔틀버스";
            case FACILITIES -> "시설안내";
            case SYSTEM_CONFIG -> "시스템설정";
        };
    }
}