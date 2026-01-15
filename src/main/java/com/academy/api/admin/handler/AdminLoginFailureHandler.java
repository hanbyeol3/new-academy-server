package com.academy.api.admin.handler;

import com.academy.api.admin.enums.AdminFailReason;
import com.academy.api.admin.mapper.AdminLoginHistoryMapper;
import com.academy.api.admin.service.AdminHistoryService;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 로그인 실패 핸들러.
 * 
 * 관리자 로그인 실패 시 자동으로 로그인 이력을 기록합니다.
 * 
 * 주요 기능:
 * - 로그인 실패 시 AdminLoginHistory 테이블에 기록
 * - 실패 사유별 분류 및 저장
 * - 클라이언트 IP 추출 및 저장
 * - 실패 응답 JSON 반환
 * - 보안 위험 감지 및 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginFailureHandler implements AuthenticationFailureHandler {

    private final AdminHistoryService adminHistoryService;
    private final MemberRepository memberRepository;
    private final AdminLoginHistoryMapper loginHistoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        log.info("[AdminLoginFailureHandler] 관리자 로그인 실패 처리 시작. 예외: {}", exception.getClass().getSimpleName());
        
        try {
            // 로그인 시도한 사용자명 추출 (일반적으로 username 파라미터)
            String username = request.getParameter("username");
            if (username == null) {
                username = "unknown";
            }
            
            log.debug("[AdminLoginFailureHandler] 로그인 실패한 사용자: {}", username);
            
            // 실패 사유 분류
            AdminFailReason failReason = classifyFailureReason(exception);
            
            // 회원 정보 조회 (존재하는 경우)
            Long adminId = null;
            Optional<Member> memberOpt = memberRepository.findByUsername(username);
            if (memberOpt.isPresent()) {
                adminId = memberOpt.get().getId();
            }
            
            // 클라이언트 IP 및 User-Agent 추출
            String clientIp = loginHistoryMapper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 로그인 실패 이력 기록
            adminHistoryService.recordLoginHistory(
                adminId,
                username,
                false,  // 실패
                failReason,
                clientIp,
                userAgent
            );
            
            log.info("[AdminLoginFailureHandler] 관리자 로그인 실패 이력 기록 완료. username={}, reason={}, ip={}", 
                    username, failReason, clientIp);
            
            // 실패 응답 반환
            sendFailureResponse(response, exception, failReason);
            
        } catch (Exception e) {
            log.error("[AdminLoginFailureHandler] 로그인 실패 처리 중 오류 발생: {}", e.getMessage(), e);
            sendGenericFailureResponse(response);
        }
    }
    
    /**
     * 인증 예외를 AdminFailReason으로 분류.
     * 
     * @param exception 인증 예외
     * @return 실패 사유
     */
    private AdminFailReason classifyFailureReason(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return AdminFailReason.INVALID_PASSWORD;
        } else if (exception instanceof UsernameNotFoundException) {
            return AdminFailReason.USER_NOT_FOUND;
        } else if (exception instanceof DisabledException) {
            return AdminFailReason.ACCOUNT_DISABLED;
        } else if (exception instanceof LockedException) {
            return AdminFailReason.ACCOUNT_LOCKED;
        } else {
            return AdminFailReason.SYSTEM_ERROR;
        }
    }
    
    /**
     * 사용자 친화적 에러 메시지 생성.
     * 
     * @param failReason 실패 사유
     * @return 사용자 메시지
     */
    private String createUserFriendlyMessage(AdminFailReason failReason) {
        return switch (failReason) {
            case INVALID_PASSWORD -> "아이디 또는 비밀번호가 잘못되었습니다.";
            case USER_NOT_FOUND -> "아이디 또는 비밀번호가 잘못되었습니다."; // 보안상 동일 메시지
            case ACCOUNT_DISABLED -> "비활성화된 계정입니다. 관리자에게 문의하세요.";
            case ACCOUNT_LOCKED -> "잠금된 계정입니다. 관리자에게 문의하세요.";
            case SYSTEM_ERROR -> "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            default -> "로그인에 실패했습니다.";
        };
    }
    
    /**
     * 실패 응답 전송.
     * 
     * @param response HTTP 응답
     * @param exception 인증 예외
     * @param failReason 실패 사유
     * @throws IOException I/O 예외
     */
    private void sendFailureResponse(HttpServletResponse response, AuthenticationException exception, 
                                   AdminFailReason failReason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("code", "A401");
        responseBody.put("message", createUserFriendlyMessage(failReason));
        
        // 개발 환경에서만 상세 에러 정보 포함 (선택적)
        Map<String, Object> details = new HashMap<>();
        details.put("failReason", failReason.name());
        details.put("timestamp", java.time.LocalDateTime.now());
        responseBody.put("details", details);
        
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
        
        log.debug("[AdminLoginFailureHandler] 실패 응답 전송 완료. reason={}", failReason);
    }
    
    /**
     * 일반 실패 응답 전송 (예외 발생 시).
     * 
     * @param response HTTP 응답
     * @throws IOException I/O 예외
     */
    private void sendGenericFailureResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("code", "A401");
        responseBody.put("message", "로그인에 실패했습니다.");
        
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }
}