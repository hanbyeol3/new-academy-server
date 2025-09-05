package com.academy.api.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 리포지토리.
 * 
 * Refresh Token의 생성, 조회, 폐기 등을 담당합니다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 값으로 RefreshToken 조회.
     * 
     * @param token 토큰 값
     * @return RefreshToken 엔티티 (Optional)
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 유효한 토큰만 조회 (폐기되지 않고 만료되지 않은 토큰).
     * 
     * @param token 토큰 값
     * @return 유효한 RefreshToken 엔티티 (Optional)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * 회원의 모든 RefreshToken 조회.
     * 
     * @param memberId 회원 ID
     * @return RefreshToken 목록
     */
    List<RefreshToken> findByMemberId(Long memberId);

    /**
     * 회원의 유효한 RefreshToken만 조회.
     * 
     * @param memberId 회원 ID
     * @return 유효한 RefreshToken 목록
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.memberId = :memberId AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    /**
     * 회원의 모든 RefreshToken 폐기.
     * 
     * @param memberId 회원 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.memberId = :memberId AND rt.revoked = false")
    int revokeAllByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 토큰 폐기.
     * 
     * @param token 토큰 값
     * @return 업데이트된 행 수
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);

    /**
     * 만료된 토큰 정리.
     * 
     * @param now 현재 시각
     * @return 삭제된 행 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 회원별 유효한 토큰 개수 조회.
     * 
     * @param memberId 회원 ID
     * @return 유효한 토큰 개수
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.memberId = :memberId AND rt.revoked = false AND rt.expiresAt > :now")
    long countValidByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}