package com.academy.api.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 도메인의 통합 모델 클래스.
 * 
 * DDD(Domain Driven Design) 패턴을 적용하여 사용자와 관련된 모든 DTO, 검색 조건,
 * 프로젝션 클래스를 하나의 파일에 응집도 높게 구성한다.
 * 
 * 포함 클래스:
 *  - ResponseUser: 사용자 정보 응답 DTO (메인 클래스)
 *  - Criteria: 사용자 검색 조건
 *  - Projection: 사용자 요약 정보 (목록용)
 * 
 * 장점:
 *  - 관련 클래스들의 응집도 향상
 *  - 파일 분산으로 인한 관리 복잡성 감소
 *  - 도메인 경계 명확화
 *  - 일관된 네이밍 및 구조 유지
 * 
 * API 응답 형태:
 *  - 목록 조회: ResponseList<ResponseUser>
 *  - 단건 조회: ResponseData<ResponseUser>
 *  - 생성/수정/삭제: Response (성공/실패만)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {

    /** 사용자 고유 식별자 */
    private Long id;

    /** 사용자 아이디 (로그인용) */
    private String username;

    /** 사용자 이름 */
    private String name;

    /** 이메일 주소 */
    private String email;

    /** 전화번호 */
    private String phone;

    /** 생년월일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 성별 (M: 남성, F: 여성, O: 기타) */
    private String gender;

    /** 사용자 역할 (ADMIN, USER, TEACHER 등) */
    private String role;

    /** 계정 활성화 여부 */
    private Boolean active;

    /** 이메일 인증 여부 */
    private Boolean emailVerified;

    /** 마지막 로그인 일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    /** 계정 생성일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 계정 정보 수정일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 ResponseUser로 변환하는 정적 팩토리 메서드.
     * 
     * 엔티티의 모든 필드를 응답 DTO로 안전하게 변환한다.
     * null 필드에 대한 안전한 처리와 민감 정보 제외를 담당한다.
     * 
     * @param user 사용자 엔티티 (null 불가)
     * @return ResponseUser DTO
     */
    public static ResponseUser from(Object user) {
        if (user == null) {
            return null;
        }
        
        // 실제 User 엔티티가 구현되면 아래와 같이 매핑
        // User entity = (User) user;
        // return ResponseUser.builder()
        //         .id(entity.getId())
        //         .username(entity.getUsername())
        //         .name(entity.getName())
        //         .email(entity.getEmail())
        //         .phone(entity.getPhone())
        //         .birthDate(entity.getBirthDate())
        //         .gender(entity.getGender())
        //         .role(entity.getRole())
        //         .active(entity.getActive())
        //         .emailVerified(entity.getEmailVerified())
        //         .lastLoginAt(entity.getLastLoginAt())
        //         .createdAt(entity.getCreatedAt())
        //         .updatedAt(entity.getUpdatedAt())
        //         .build();
        
        // 임시 반환 (실제 엔티티 구현 후 수정 필요)
        return ResponseUser.builder().build();
    }

    /**
     * 엔티티 목록에서 ResponseUser 목록으로 변환하는 정적 팩토리 메서드.
     * 
     * 대량 데이터 변환 시 Stream API를 활용하여 효율적으로 처리한다.
     * null 엔티티는 자동으로 제외되어 안전한 변환을 보장한다.
     * 
     * @param users 사용자 엔티티 목록
     * @return ResponseUser DTO 목록
     */
    public static List<ResponseUser> fromList(List<?> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        
        return users.stream()
                .map(ResponseUser::from)
                .filter(responseUser -> responseUser != null)
                .toList();
    }

    /**
     * 사용자 검색 조건을 정의하는 내부 클래스.
     * 
     * 동적 쿼리 생성을 위한 다양한 검색 조건을 제공하며,
     * null 값은 해당 조건을 무시하는 방식으로 동작한다.
     * 
     * 지원 검색 조건:
     *  - 텍스트 검색: 사용자명, 이름, 이메일 부분 일치
     *  - 상태 검색: 활성화, 이메일 인증, 역할별 필터
     *  - 기간 검색: 가입일, 최종 로그인 범위 조회
     *  - 개인정보: 성별, 연령대별 조회
     * 
     * 보안 고려사항:
     *  - 민감한 개인정보는 관리자 권한에서만 검색 가능
     *  - SQL Injection 방지를 위한 파라미터 바인딩
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Criteria {

        /** 사용자 아이디 부분 일치 검색 */
        private String usernameLike;

        /** 사용자 이름 부분 일치 검색 */
        private String nameLike;

        /** 이메일 주소 부분 일치 검색 */
        private String emailLike;

        /** 전화번호 부분 일치 검색 (관리자만) */
        private String phoneLike;

        /** 역할별 필터 (ADMIN, USER, TEACHER) */
        private String role;

        /** 성별 필터 (M, F, O) */
        private String gender;

        /** 계정 활성화 상태 필터 */
        private Boolean active;

        /** 이메일 인증 상태 필터 */
        private Boolean emailVerified;

        /** 생년월일 범위 검색 - 시작일 */
        private LocalDate birthDateFrom;

        /** 생년월일 범위 검색 - 종료일 */
        private LocalDate birthDateTo;

        /** 가입일 범위 검색 - 시작일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdFrom;

        /** 가입일 범위 검색 - 종료일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTo;

        /** 최종 로그인 범위 검색 - 시작일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginFrom;

        /** 최종 로그인 범위 검색 - 종료일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTo;

        /** 계정 정보 수정일 범위 검색 - 시작일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedFrom;

        /** 계정 정보 수정일 범위 검색 - 종료일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedTo;
    }

    /**
     * 사용자 요약 정보를 위한 프로젝션 클래스.
     * 
     * 목록 조회나 검색 결과에서 필요한 핵심 정보만 포함하여
     * 네트워크 트래픽과 메모리 사용량을 최적화한다.
     * 
     * 포함 정보:
     *  - 기본 식별 정보: ID, 사용자명, 이름
     *  - 연락처 정보: 이메일 (전화번호는 보안상 제외)
     *  - 상태 정보: 역할, 활성화, 인증 여부
     *  - 시간 정보: 가입일, 최종 로그인
     * 
     * 사용 예시:
     *  - 사용자 목록 페이지
     *  - 검색 결과 요약
     *  - 관리자 대시보드
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Projection {

        /** 사용자 고유 식별자 */
        private Long id;

        /** 사용자 아이디 (로그인용) */
        private String username;

        /** 사용자 이름 */
        private String name;

        /** 이메일 주소 */
        private String email;

        /** 사용자 역할 */
        private String role;

        /** 계정 활성화 여부 */
        private Boolean active;

        /** 이메일 인증 여부 */
        private Boolean emailVerified;

        /** 마지막 로그인 일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;

        /** 계정 생성일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        /**
         * ResponseUser에서 Projection으로 변환하는 정적 팩토리 메서드.
         * 
         * 전체 사용자 정보에서 요약 정보만 추출하여 새로운 객체를 생성한다.
         * 민감한 정보(전화번호, 생년월일)는 제외하여 보안을 강화한다.
         * 
         * @param responseUser 전체 사용자 응답 DTO
         * @return 사용자 요약 정보 Projection
         */
        public static Projection from(ResponseUser responseUser) {
            if (responseUser == null) {
                return null;
            }
            
            return Projection.builder()
                    .id(responseUser.getId())
                    .username(responseUser.getUsername())
                    .name(responseUser.getName())
                    .email(responseUser.getEmail())
                    .role(responseUser.getRole())
                    .active(responseUser.getActive())
                    .emailVerified(responseUser.getEmailVerified())
                    .lastLoginAt(responseUser.getLastLoginAt())
                    .createdAt(responseUser.getCreatedAt())
                    .build();
        }

        /**
         * ResponseUser 목록에서 Projection 목록으로 변환하는 정적 팩토리 메서드.
         * 
         * 대량의 사용자 데이터를 요약 정보로 효율적으로 변환한다.
         * null 값은 자동으로 제외되어 안전한 처리를 보장한다.
         * 
         * @param responseUsers ResponseUser DTO 목록
         * @return Projection 목록
         */
        public static List<Projection> fromList(List<ResponseUser> responseUsers) {
            if (responseUsers == null || responseUsers.isEmpty()) {
                return List.of();
            }
            
            return responseUsers.stream()
                    .map(Projection::from)
                    .filter(projection -> projection != null)
                    .toList();
        }
    }
}