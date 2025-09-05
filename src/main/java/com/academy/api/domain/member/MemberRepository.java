package com.academy.api.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}