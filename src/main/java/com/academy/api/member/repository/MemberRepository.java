package com.academy.api.member.repository;

import com.academy.api.member.domain.Member;
import com.academy.api.member.domain.MemberRole;
import com.academy.api.member.domain.MemberStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 리포지토리.
 * 
 * 회원 엔티티에 대한 데이터 액세스를 담당합니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 사용자명으로 회원 조회.
     * 
     * @param username 로그인 사용자명
     * @return 회원 엔티티 (Optional)
     */
    Optional<Member> findByUsername(String username);

    /**
     * 이메일 주소로 회원 조회.
     * 
     * @param emailAddress 이메일 주소
     * @return 회원 엔티티 (Optional)
     */
    Optional<Member> findByEmailAddress(String emailAddress);

    /**
     * 사용자명 중복 확인.
     * 
     * @param username 확인할 사용자명
     * @return 중복이면 true
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 주소 중복 확인.
     * 
     * @param emailAddress 확인할 이메일 주소
     * @return 중복이면 true
     */
    boolean existsByEmailAddress(String emailAddress);

    /**
     * 활성 상태인 회원만 사용자명으로 조회.
     * 
     * @param username 로그인 사용자명
     * @return 활성 상태인 회원 엔티티 (Optional)
     */
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.status = 'ACTIVE'")
    Optional<Member> findActiveByUsername(@Param("username") String username);

    /**
     * 전화번호로 회원 존재 여부 확인.
     * 
     * @param phoneNumber 전화번호
     * @return 존재하면 true
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // ===== Admin 관리용 메서드들 =====

    /**
     * 관리자 권한별 회원 수 조회.
     * 
     * @param roles 권한 목록
     * @return 해당 권한의 회원 수
     */
    long countByRoleIn(List<MemberRole> roles);

    /**
     * 관리자 권한별 활성 회원 수 조회.
     * 
     * @param roles 권한 목록
     * @param status 회원 상태
     * @return 해당 권한의 활성 회원 수
     */
    long countByRoleInAndStatus(List<MemberRole> roles, MemberStatus status);

    /**
     * 관리자 권한별 잠긴 회원 수 조회.
     * 
     * @param roles 권한 목록
     * @param locked 잠금 여부
     * @return 해당 권한의 잠긴 회원 수
     */
    long countByRoleInAndLocked(List<MemberRole> roles, boolean locked);

    /**
     * 특정 기간 이후 생성된 관리자 수 조회.
     * 
     * @param roles 권한 목록
     * @param createdAt 생성 시각
     * @return 해당 기간 이후 생성된 관리자 수
     */
    long countByRoleInAndCreatedAtGreaterThanEqual(List<MemberRole> roles, LocalDateTime createdAt);

    /**
     * 관리자 권한 회원들을 검색 조건으로 조회.
     * 
     * @param roles 권한 목록
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Query("SELECT m FROM Member m WHERE m.role IN :roles AND " +
           "(m.username LIKE %:keyword% OR m.memberName LIKE %:keyword% OR " +
           "m.phoneNumber LIKE %:keyword% OR m.emailAddress LIKE %:keyword%)")
    Page<Member> findByRoleInAndUsernameContainingOrMemberNameContainingOrPhoneNumberContainingOrEmailAddressContaining(
        @Param("roles") List<MemberRole> roles,
        @Param("keyword") String keyword,
        @Param("keyword") String memberName,
        @Param("keyword") String phoneNumber,
        @Param("keyword") String emailAddress,
        Pageable pageable);

    /**
     * 관리자 권한 + 상태 + 잠금 조건으로 조회.
     * 
     * @param roles 권한 목록
     * @param status 회원 상태
     * @param locked 잠금 여부
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Member> findByRoleInAndStatusAndLocked(List<MemberRole> roles, MemberStatus status, Boolean locked, Pageable pageable);

    /**
     * 관리자 권한 + 상태 조건으로 조회.
     * 
     * @param roles 권한 목록
     * @param status 회원 상태
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Member> findByRoleInAndStatus(List<MemberRole> roles, MemberStatus status, Pageable pageable);

    /**
     * 관리자 권한 + 잠금 조건으로 조회.
     * 
     * @param roles 권한 목록
     * @param locked 잠금 여부
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Member> findByRoleInAndLocked(List<MemberRole> roles, Boolean locked, Pageable pageable);

    /**
     * 관리자 권한 회원을 생성일 역순으로 조회.
     * 
     * @param roles 권한 목록
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Member> findByRoleInOrderByCreatedAtDesc(List<MemberRole> roles, Pageable pageable);

    /**
     * 사용자명 중복 확인 (특정 상태만).
     * 
     * @param username 사용자명
     * @param statuses 허용할 상태 목록
     * @return 중복이면 true
     */
    boolean existsByUsernameAndStatusIn(String username, List<MemberStatus> statuses);

    /**
     * 이메일 중복 확인 (특정 상태만).
     * 
     * @param emailAddress 이메일 주소
     * @param statuses 허용할 상태 목록
     * @return 중복이면 true
     */
    boolean existsByEmailAddressAndStatusIn(String emailAddress, List<MemberStatus> statuses);
}