package com.academy.api.admin.handler;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 로그인 성공 핸들러.
 * 
 * 관리자 로그인 성공 시 자동으로 로그인 이력을 기록합니다.
 * 
 * 주요 기능:
 * - 로그인 성공 시 AdminLoginHistory 테이블에 기록
 * - 클라이언트 IP 추출 및 저장
 * - 성공 응답 JSON 반환
 * - 로그인 통계 정보 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AdminHistoryService adminHistoryService;
    private final MemberRepository memberRepository;
    private final AdminLoginHistoryMapper loginHistoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        log.info("[AdminLoginSuccessHandler] 관리자 로그인 성공 처리 시작");
        
        try {
            // 인증된 사용자 정보 추출
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            log.debug("[AdminLoginSuccessHandler] 로그인 성공한 사용자: {}", username);
            
            // 회원 정보 조회
            Optional<Member> memberOpt = memberRepository.findByUsername(username);
            if (memberOpt.isEmpty()) {
                log.warn("[AdminLoginSuccessHandler] 로그인 성공했으나 회원 정보를 찾을 수 없음: {}", username);
                sendErrorResponse(response, "회원 정보를 찾을 수 없습니다.");
                return;
            }
            
            Member member = memberOpt.get();
            
            // 관리자 권한 확인
            if (!isAdminRole(member.getRole().name())) {
                log.warn("[AdminLoginSuccessHandler] 비관리자 계정 로그인 시도: {} (role: {})", username, member.getRole());
                sendErrorResponse(response, "관리자 권한이 필요합니다.");
                return;
            }
            
            // 클라이언트 IP 및 User-Agent 추출
            String clientIp = loginHistoryMapper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 로그인 이력 기록
            adminHistoryService.recordLoginHistory(
                member.getId(),
                username,
                true,  // 성공
                null,  // 실패 사유 없음
                clientIp,
                userAgent
            );
            
            log.info("[AdminLoginSuccessHandler] 관리자 로그인 이력 기록 완료. adminId={}, ip={}", 
                    member.getId(), clientIp);
            
            // 성공 응답 반환
            sendSuccessResponse(response, member);
            
        } catch (Exception e) {
            log.error("[AdminLoginSuccessHandler] 로그인 성공 처리 중 오류 발생: {}", e.getMessage(), e);
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 관리자 권한 여부 확인.
     * 
     * @param role 사용자 권한
     * @return 관리자 권한 여부
     */
    private boolean isAdminRole(String role) {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }
    
    /**
     * 성공 응답 전송.
     * 
     * @param response HTTP 응답
     * @param member 로그인한 관리자 정보
     * @throws IOException I/O 예외
     */
    private void sendSuccessResponse(HttpServletResponse response, Member member) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("code", "0000");
        responseBody.put("message", "로그인이 완료되었습니다.");
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", member.getId());
        userData.put("username", member.getUsername());
        userData.put("memberName", member.getMemberName());
        userData.put("role", member.getRole().name());
        userData.put("email", member.getEmailAddress());
        
        responseBody.put("data", userData);
        
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
        
        log.debug("[AdminLoginSuccessHandler] 성공 응답 전송 완료");
    }
    
    /**
     * 에러 응답 전송.
     * 
     * @param response HTTP 응답
     * @param message 에러 메시지
     * @throws IOException I/O 예외
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("code", "A401");
        responseBody.put("message", message);
        
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
        
        log.debug("[AdminLoginSuccessHandler] 에러 응답 전송 완료: {}", message);
    }
}