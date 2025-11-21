package com.academy.api.auth.controller;

import com.academy.api.auth.dto.*;
import com.academy.api.auth.security.JwtAuthenticationToken;
import com.academy.api.auth.service.AuthService;
import com.academy.api.common.response.ApiResponse;
import com.academy.api.data.responses.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러.
 * 
 * 회원 가입, 로그인, 토큰 재발급, 로그아웃 등 인증 관련 API를 제공합니다.
 */
@Tag(name = "Auth API", description = "회원 가입, 로그인, 토큰 관리")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원 가입.
     */
    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "사용자명 또는 이메일 중복")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        
        Long memberId = authService.signUp(request);
        
        return ResponseEntity.ok(ApiResponse.success(memberId));
    }

    /**
     * 로그인.
     */
    @Operation(summary = "로그인", description = "사용자명과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "계정 정지 또는 삭제")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("로그인 요청: username={}", request.getUsername());
        
        SignInResponse response = authService.signIn(request, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 토큰 재발급.
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token 유효하지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<SignInResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("토큰 재발급 요청");
        
        SignInResponse response = authService.refreshToken(request, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로그아웃.
     */
    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하여 로그아웃합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse<Void>> signOut(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("로그아웃 요청");
        
        authService.signOut(request.getRefreshToken());
        
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 내 정보 조회.
     */
    @Operation(
        summary = "내 정보 조회", 
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile() {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            log.error("인증 정보가 없거나 올바르지 않습니다: {}", authentication);
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("AUTH_REQUIRED", "인증이 필요합니다."));
        }
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Long memberId = jwtAuth.getMemberId();
        log.info("내 정보 조회: memberId={}", memberId);
        
        MemberProfileResponse response = authService.getMyProfile(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 현재 로그인한 사용자 정보 조회 (간소화).
     */
    @Operation(
        summary = "현재 사용자 정보 조회", 
        description = "현재 로그인한 사용자의 기본 정보(ID, 사용자명, 권한 등)를 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/current-user")
    public ResponseData<ResponseCurrentUser> getCurrentUser() {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            log.error("인증 정보가 없거나 올바르지 않습니다: {}", authentication);
            return ResponseData.error("AUTH_REQUIRED", "인증이 필요합니다.");
        }
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Long memberId = jwtAuth.getMemberId();
        log.info("현재 사용자 정보 조회: memberId={}", memberId);
        
        ResponseCurrentUser response = authService.getCurrentUser(memberId);
        
        return ResponseData.ok("0000", "조회 성공", response);
    }

    /**
     * 비밀번호 변경.
     */
    @Operation(
        summary = "비밀번호 변경", 
        description = "현재 비밀번호를 확인한 후 새로운 비밀번호로 변경합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 동일한 비밀번호"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            log.error("인증 정보가 없거나 올바르지 않습니다: {}", authentication);
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("AUTH_REQUIRED", "인증이 필요합니다."));
        }
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Long memberId = jwtAuth.getMemberId();
        log.info("비밀번호 변경 요청: memberId={}", memberId);
        
        authService.changePassword(memberId, request);
        
        return ResponseEntity.ok(ApiResponse.success());
    }
}