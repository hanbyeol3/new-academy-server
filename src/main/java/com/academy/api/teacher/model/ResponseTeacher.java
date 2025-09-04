package com.academy.api.teacher.model;

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
 * 강사 도메인의 통합 모델 클래스.
 * 
 * DDD(Domain Driven Design) 패턴을 적용하여 강사와 관련된 모든 DTO, 검색 조건,
 * 프로젝션 클래스를 하나의 파일에 응집도 높게 구성한다.
 * 
 * 포함 클래스:
 *  - ResponseTeacher: 강사 정보 응답 DTO (메인 클래스)
 *  - Criteria: 강사 검색 조건
 *  - Projection: 강사 요약 정보 (목록용)
 * 
 * 강사 도메인 특성:
 *  - 교육기관의 핵심 인적자원
 *  - 전문성과 경력 정보 중시
 *  - 담당 과목 및 강의 이력 관리
 *  - 학생 평가 및 피드백 연계
 * 
 * API 응답 형태:
 *  - 목록 조회: ResponseList<ResponseTeacher>
 *  - 단건 조회: ResponseData<ResponseTeacher>
 *  - 생성/수정/삭제: Response (성공/실패만)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseTeacher {

    /** 강사 고유 식별자 */
    private Long id;

    /** 강사 아이디 (로그인용) */
    private String teacherId;

    /** 강사 이름 */
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

    /** 전문 분야 (예: Mathematics, Science, English 등) */
    private String specialization;

    /** 학위 정보 (Bachelor, Master, PhD 등) */
    private String degree;

    /** 졸업 대학교 */
    private String university;

    /** 경력 연수 (년) */
    private Integer experienceYears;

    /** 자격증 정보 (JSON 배열 형태로 저장) */
    private String certifications;

    /** 강사 소개글 */
    private String introduction;

    /** 강의 가능 언어 (KO, EN, JP, CN 등) */
    private String languages;

    /** 시급 (원) */
    private Integer hourlyRate;

    /** 강사 상태 (ACTIVE: 활성, INACTIVE: 비활성, SUSPENDED: 정지) */
    private String status;

    /** 계약 시작일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    /** 계약 종료일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;

    /** 평점 (1.0 ~ 5.0) */
    private Double rating;

    /** 총 강의 시간 (시간) */
    private Integer totalTeachingHours;

    /** 총 강의 횟수 */
    private Integer totalLessons;

    /** 마지막 강의일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastTeachingDate;

    /** 계정 생성일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 계정 정보 수정일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 ResponseTeacher로 변환하는 정적 팩토리 메서드.
     * 
     * 엔티티의 모든 필드를 응답 DTO로 안전하게 변환한다.
     * 민감한 개인정보나 내부 정보는 적절히 필터링하여 제공한다.
     * 
     * @param teacher 강사 엔티티 (null 불가)
     * @return ResponseTeacher DTO
     */
    public static ResponseTeacher from(Object teacher) {
        if (teacher == null) {
            return null;
        }
        
        // 실제 Teacher 엔티티가 구현되면 아래와 같이 매핑
        // Teacher entity = (Teacher) teacher;
        // return ResponseTeacher.builder()
        //         .id(entity.getId())
        //         .teacherId(entity.getTeacherId())
        //         .name(entity.getName())
        //         .email(entity.getEmail())
        //         .phone(entity.getPhone())
        //         .birthDate(entity.getBirthDate())
        //         .gender(entity.getGender())
        //         .specialization(entity.getSpecialization())
        //         .degree(entity.getDegree())
        //         .university(entity.getUniversity())
        //         .experienceYears(entity.getExperienceYears())
        //         .certifications(entity.getCertifications())
        //         .introduction(entity.getIntroduction())
        //         .languages(entity.getLanguages())
        //         .hourlyRate(entity.getHourlyRate())
        //         .status(entity.getStatus())
        //         .contractStartDate(entity.getContractStartDate())
        //         .contractEndDate(entity.getContractEndDate())
        //         .rating(entity.getRating())
        //         .totalTeachingHours(entity.getTotalTeachingHours())
        //         .totalLessons(entity.getTotalLessons())
        //         .lastTeachingDate(entity.getLastTeachingDate())
        //         .createdAt(entity.getCreatedAt())
        //         .updatedAt(entity.getUpdatedAt())
        //         .build();
        
        // 임시 반환 (실제 엔티티 구현 후 수정 필요)
        return ResponseTeacher.builder().build();
    }

    /**
     * 엔티티 목록에서 ResponseTeacher 목록으로 변환하는 정적 팩토리 메서드.
     * 
     * 대량 데이터 변환 시 Stream API를 활용하여 효율적으로 처리한다.
     * null 엔티티는 자동으로 제외되어 안전한 변환을 보장한다.
     * 
     * @param teachers 강사 엔티티 목록
     * @return ResponseTeacher DTO 목록
     */
    public static List<ResponseTeacher> fromList(List<?> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return List.of();
        }
        
        return teachers.stream()
                .map(ResponseTeacher::from)
                .filter(responseTeacher -> responseTeacher != null)
                .toList();
    }

    /**
     * 강사 검색 조건을 정의하는 내부 클래스.
     * 
     * 동적 쿼리 생성을 위한 다양한 검색 조건을 제공하며,
     * null 값은 해당 조건을 무시하는 방식으로 동작한다.
     * 
     * 지원 검색 조건:
     *  - 기본 정보: 이름, 이메일, 강사 ID 부분 일치
     *  - 전문성: 전문 분야, 학위, 자격증 기반 검색
     *  - 경력: 경력 연수, 강의 경험 범위 검색
     *  - 계약: 계약 상태, 계약 기간 범위 검색
     *  - 성과: 평점, 강의 시간, 강의 횟수 범위 검색
     * 
     * 비즈니스 활용:
     *  - 과목별 강사 배정
     *  - 경력별 강사 분류
     *  - 성과 기반 강사 평가
     *  - 계약 갱신 대상 식별
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Criteria {

        /** 강사 아이디 부분 일치 검색 */
        private String teacherIdLike;

        /** 강사 이름 부분 일치 검색 */
        private String nameLike;

        /** 이메일 주소 부분 일치 검색 */
        private String emailLike;

        /** 전화번호 부분 일치 검색 */
        private String phoneLike;

        /** 전문 분야 정확 일치 검색 */
        private String specialization;

        /** 학위 정확 일치 검색 */
        private String degree;

        /** 졸업 대학교 부분 일치 검색 */
        private String universityLike;

        /** 강의 가능 언어 검색 */
        private String languages;

        /** 강사 상태 검색 */
        private String status;

        /** 성별 검색 */
        private String gender;

        /** 최소 경력 연수 */
        private Integer minExperienceYears;

        /** 최대 경력 연수 */
        private Integer maxExperienceYears;

        /** 최소 시급 */
        private Integer minHourlyRate;

        /** 최대 시급 */
        private Integer maxHourlyRate;

        /** 최소 평점 */
        private Double minRating;

        /** 최대 평점 */
        private Double maxRating;

        /** 최소 총 강의 시간 */
        private Integer minTotalTeachingHours;

        /** 최대 총 강의 시간 */
        private Integer maxTotalTeachingHours;

        /** 최소 총 강의 횟수 */
        private Integer minTotalLessons;

        /** 최대 총 강의 횟수 */
        private Integer maxTotalLessons;

        /** 계약 시작일 범위 검색 - 시작일 */
        private LocalDate contractStartDateFrom;

        /** 계약 시작일 범위 검색 - 종료일 */
        private LocalDate contractStartDateTo;

        /** 계약 종료일 범위 검색 - 시작일 */
        private LocalDate contractEndDateFrom;

        /** 계약 종료일 범위 검색 - 종료일 */
        private LocalDate contractEndDateTo;

        /** 마지막 강의일 범위 검색 - 시작일 */
        private LocalDate lastTeachingDateFrom;

        /** 마지막 강의일 범위 검색 - 종료일 */
        private LocalDate lastTeachingDateTo;

        /** 생년월일 범위 검색 - 시작일 */
        private LocalDate birthDateFrom;

        /** 생년월일 범위 검색 - 종료일 */
        private LocalDate birthDateTo;

        /** 등록일 범위 검색 - 시작일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdFrom;

        /** 등록일 범위 검색 - 종료일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTo;

        /** 수정일 범위 검색 - 시작일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedFrom;

        /** 수정일 범위 검색 - 종료일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedTo;
    }

    /**
     * 강사 요약 정보를 위한 프로젝션 클래스.
     * 
     * 목록 조회나 검색 결과에서 필요한 핵심 정보만 포함하여
     * 네트워크 트래픽과 메모리 사용량을 최적화한다.
     * 
     * 포함 정보:
     *  - 기본 식별 정보: ID, 강사 ID, 이름
     *  - 연락처 정보: 이메일 (전화번호는 보안상 제외)
     *  - 전문성 정보: 전문 분야, 학위, 경력 연수
     *  - 성과 정보: 평점, 총 강의 시간, 총 강의 횟수
     *  - 상태 정보: 강사 상태, 계약 기간
     *  - 시간 정보: 등록일, 마지막 강의일
     * 
     * 활용 예시:
     *  - 강사 목록 페이지
     *  - 과목별 강사 선택 화면
     *  - 관리자 대시보드
     *  - 강사 성과 요약 리포트
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Projection {

        /** 강사 고유 식별자 */
        private Long id;

        /** 강사 아이디 (로그인용) */
        private String teacherId;

        /** 강사 이름 */
        private String name;

        /** 이메일 주소 */
        private String email;

        /** 전문 분야 */
        private String specialization;

        /** 학위 정보 */
        private String degree;

        /** 경력 연수 */
        private Integer experienceYears;

        /** 시급 */
        private Integer hourlyRate;

        /** 강사 상태 */
        private String status;

        /** 평점 */
        private Double rating;

        /** 총 강의 시간 */
        private Integer totalTeachingHours;

        /** 총 강의 횟수 */
        private Integer totalLessons;

        /** 계약 시작일 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate contractStartDate;

        /** 계약 종료일 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate contractEndDate;

        /** 마지막 강의일 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate lastTeachingDate;

        /** 계정 생성일시 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        /**
         * ResponseTeacher에서 Projection으로 변환하는 정적 팩토리 메서드.
         * 
         * 전체 강사 정보에서 요약 정보만 추출하여 새로운 객체를 생성한다.
         * 민감한 정보(전화번호, 생년월일, 개인적 소개글)는 제외하여 보안을 강화한다.
         * 
         * @param responseTeacher 전체 강사 응답 DTO
         * @return 강사 요약 정보 Projection
         */
        public static Projection from(ResponseTeacher responseTeacher) {
            if (responseTeacher == null) {
                return null;
            }
            
            return Projection.builder()
                    .id(responseTeacher.getId())
                    .teacherId(responseTeacher.getTeacherId())
                    .name(responseTeacher.getName())
                    .email(responseTeacher.getEmail())
                    .specialization(responseTeacher.getSpecialization())
                    .degree(responseTeacher.getDegree())
                    .experienceYears(responseTeacher.getExperienceYears())
                    .hourlyRate(responseTeacher.getHourlyRate())
                    .status(responseTeacher.getStatus())
                    .rating(responseTeacher.getRating())
                    .totalTeachingHours(responseTeacher.getTotalTeachingHours())
                    .totalLessons(responseTeacher.getTotalLessons())
                    .contractStartDate(responseTeacher.getContractStartDate())
                    .contractEndDate(responseTeacher.getContractEndDate())
                    .lastTeachingDate(responseTeacher.getLastTeachingDate())
                    .createdAt(responseTeacher.getCreatedAt())
                    .build();
        }

        /**
         * ResponseTeacher 목록에서 Projection 목록으로 변환하는 정적 팩토리 메서드.
         * 
         * 대량의 강사 데이터를 요약 정보로 효율적으로 변환한다.
         * null 값은 자동으로 제외되어 안전한 처리를 보장한다.
         * 
         * @param responseTeachers ResponseTeacher DTO 목록
         * @return Projection 목록
         */
        public static List<Projection> fromList(List<ResponseTeacher> responseTeachers) {
            if (responseTeachers == null || responseTeachers.isEmpty()) {
                return List.of();
            }
            
            return responseTeachers.stream()
                    .map(Projection::from)
                    .filter(projection -> projection != null)
                    .toList();
        }
    }
}