package com.academy.api.auth.service;

import com.academy.api.auth.dto.*;
import com.academy.api.auth.jwt.JwtProvider;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.member.domain.*;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.member.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 서비스.
 * 
 * 회원 가입, 로그인, 토큰 재발급 등 인증 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원 가입.
     * 
     * @param request 회원가입 요청 정보
     * @return 생성된 회원 ID
     */
    @Transactional
    public Long signUp(SignUpRequest request) {
        // 사용자명 중복 검사
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATE);
        }

        // 이메일 중복 검사 (이메일이 있는 경우)
        if (request.getEmailAddress() != null && 
            memberRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 권한 설정 (기본값: USER)
        MemberRole memberRole = MemberRole.USER;
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            memberRole = MemberRole.ADMIN;
        }

        // 회원 엔티티 생성
        Member member = Member.builder()
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .memberName(request.getMemberName())
                .phoneNumber(request.getPhoneNumber())
                .emailAddress(request.getEmailAddress())
                .role(memberRole)
                .build();

        Member savedMember = memberRepository.save(member);
        
        log.info("새 회원 가입: username={}, memberId={}", 
                request.getUsername(), savedMember.getId());

        return savedMember.getId();
    }

    /**
     * 로그인.
     * 
     * @param request 로그인 요청 정보
     * @param httpRequest HTTP 요청 (User-Agent, IP 추출용)
     * @return 로그인 응답 (토큰 포함)
     */
    @Transactional
    public SignInResponse signIn(SignInRequest request, HttpServletRequest httpRequest) {
        // 회원 조회
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        // 계정 상태 확인
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_SUSPENDED);
        }
        if (member.getStatus() == MemberStatus.DELETED) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DELETED);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(member);
        String refreshToken = jwtProvider.createRefreshToken(member);

        // Refresh Token 저장
        saveRefreshToken(member.getId(), refreshToken, httpRequest);

        // 마지막 로그인 시각 업데이트
        member.updateLastLoginAt();
        memberRepository.save(member);

        log.info("로그인 성공: username={}, memberId={}", request.getUsername(), member.getId());

        return SignInResponse.of(
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpirationSeconds(),
                member
        );
    }

    /**
     * 토큰 재발급.
     * 
     * @param request 토큰 재발급 요청
     * @param httpRequest HTTP 요청
     * @return 새로운 토큰
     */
    @Transactional
    public SignInResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.getRefreshToken();

        // Refresh Token 조회 및 검증
        RefreshToken refreshToken = refreshTokenRepository
                .findValidByToken(refreshTokenValue, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        // 회원 조회
        Member member = memberRepository.findById(refreshToken.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 계정 상태 재확인
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_SUSPENDED);
        }

        // 새로운 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(member);
        String newRefreshToken = jwtProvider.createRefreshToken(member);

        // 기존 토큰 폐기
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // 새 Refresh Token 저장
        saveRefreshToken(member.getId(), newRefreshToken, httpRequest);

        log.info("토큰 재발급: memberId={}", member.getId());

        return SignInResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtProvider.getAccessTokenExpirationSeconds(),
                member
        );
    }

    /**
     * 로그아웃.
     * 
     * @param refreshTokenValue Refresh Token
     */
    @Transactional
    public void signOut(String refreshTokenValue) {
        int revokedCount = refreshTokenRepository.revokeByToken(refreshTokenValue);
        
        if (revokedCount > 0) {
            log.info("로그아웃 처리: revokedTokens={}", revokedCount);
        } else {
            log.warn("로그아웃 시 토큰을 찾을 수 없음: token={}", refreshTokenValue);
        }
    }

    /**
     * 내 정보 조회.
     * 
     * @param memberId 회원 ID
     * @return 회원 프로필 정보
     */
    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.from(member);
    }

    /**
     * 비밀번호 변경.
     * 
     * @param memberId 회원 ID
     * @param request 비밀번호 변경 요청
     */
    @Transactional
    public void changePassword(Long memberId, ChangePasswordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }

        // 새 비밀번호가 기존과 동일한지 확인
        if (passwordEncoder.matches(request.getNewPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.MEMBER_SAME_PASSWORD);
        }

        // 새 비밀번호로 변경
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        member.changePassword(newPasswordHash);
        memberRepository.save(member);

        // 기존 모든 Refresh Token 폐기 (보안을 위해)
        refreshTokenRepository.revokeAllByMemberId(memberId);

        log.info("비밀번호 변경: memberId={}", memberId);
    }

    /**
     * Refresh Token 저장.
     * 
     * @param memberId 회원 ID
     * @param refreshToken 토큰 값
     * @param httpRequest HTTP 요청
     */
    private void saveRefreshToken(Long memberId, String refreshToken, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        RefreshToken tokenEntity = RefreshToken.builder()
                .memberId(memberId)
                .token(refreshToken)
                .expiresAt(jwtProvider.calculateRefreshTokenExpiration())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(tokenEntity);
    }

    /**
     * 클라이언트 IP 주소 추출.
     * 
     * @param request HTTP 요청
     * @return IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}