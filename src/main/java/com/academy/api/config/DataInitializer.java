package com.academy.api.config;

import com.academy.api.member.domain.Member;
import com.academy.api.member.domain.MemberRole;
import com.academy.api.member.repository.MemberRepository;
// 삭제된 서비스 Import 제거
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private DataSource dataSource;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // H2 데이터베이스인 경우에만 샘플 데이터 생성
        if (isH2Database()) {
            log.info("=== H2 데이터베이스 감지: 샘플 데이터 초기화 시작 ===");
            initializeSampleData();
            log.info("=== 샘플 데이터 초기화 완료 ===");
        } else {
            log.info("=== MySQL 데이터베이스 연결됨: 샘플 데이터 초기화 건너뜀 ===");
        }
    }
    
    /**
     * 현재 연결된 데이터베이스가 H2인지 확인
     */
    private boolean isH2Database() {
        try {
            String jdbcUrl = dataSource.getConnection().getMetaData().getURL();
            boolean isH2 = jdbcUrl.contains("h2");
            log.info("데이터베이스 URL: {}, H2 여부: {}", jdbcUrl, isH2);
            return isH2;
        } catch (Exception e) {
            log.warn("데이터베이스 타입 확인 실패, H2로 가정", e);
            return true;
        }
    }
    
    /**
     * 샘플 데이터 초기화
     */
    private void initializeSampleData() {
        // 관리자 계정 역할 업데이트만 수행 (삭제된 서비스 제외)
        updateAdminRole();
        log.info("기본 관리자 계정 설정 완료. 다른 샘플 데이터는 각 도메인 구현 후 추가 예정");
    }

    private void updateAdminRole() {
        // 기존 admin 계정 업데이트
        memberRepository.findByUsername("admin")
                .ifPresent(member -> {
                    updateMemberRole(member, "admin");
                });
        
        // gallery_admin 계정 업데이트
        memberRepository.findByUsername("gallery_admin")
                .ifPresent(member -> {
                    updateMemberRole(member, "gallery_admin");
                });
        
        // testadmin 계정 업데이트
        memberRepository.findByUsername("testadmin")
                .ifPresent(member -> {
                    updateMemberRole(member, "testadmin");
                });
    }
    
    private void updateMemberRole(Member member, String username) {
        try {
            var roleField = Member.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(member, MemberRole.ADMIN);
            memberRepository.save(member);
            log.info("관리자 역할 업데이트 완료: {}", username);
        } catch (Exception e) {
            log.error("관리자 역할 업데이트 실패: {}", username, e);
        }
    }

    // 삭제된 서비스들의 샘플 데이터 생성 메서드들 제거
    // 각 도메인 구현 시 해당 DataInitializer에 추가 예정
}