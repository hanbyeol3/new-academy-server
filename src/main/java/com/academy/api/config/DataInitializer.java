package com.academy.api.config;

import com.academy.api.domain.member.Member;
import com.academy.api.domain.member.MemberRole;
import com.academy.api.domain.member.MemberRepository;
import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.domain.ExplanationEventStatus;
import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.repository.ExplanationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ExplanationEventRepository explanationEventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== 샘플 데이터 초기화 시작 ===");

        // 관리자 계정 역할 업데이트
        updateAdminRole();

        // 샘플 설명회 데이터 생성
        createSampleExplanationEvents();

        log.info("=== 샘플 데이터 초기화 완료 ===");
    }

    private void updateAdminRole() {
        memberRepository.findByUsername("admin")
                .ifPresent(member -> {
                    // Member 엔티티에 역할 업데이트 메서드가 없으므로 리플렉션 사용
                    try {
                        var roleField = Member.class.getDeclaredField("role");
                        roleField.setAccessible(true);
                        roleField.set(member, MemberRole.ADMIN);
                        memberRepository.save(member);
                        log.info("관리자 역할 업데이트 완료: {}", member.getUsername());
                    } catch (Exception e) {
                        log.error("관리자 역할 업데이트 실패", e);
                    }
                });
    }

    private void createSampleExplanationEvents() {
        // 샘플 설명회 1: 현재 예약 가능 (중등부)
        if (!explanationEventRepository.existsByTitle("중등부 과학 특강 설명회")) {
            ExplanationEvent event1 = ExplanationEvent.builder()
                    .division(ExplanationDivision.MIDDLE)
                    .title("중등부 과학 특강 설명회")
                    .content("중등부 학생들을 위한 과학 특강 프로그램을 소개합니다. 실험과 이론을 병행한 재미있는 과학 수업입니다.")
                    .startAt(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0).withSecond(0).withNano(0))
                    .endAt(LocalDateTime.now().plusDays(7).withHour(17).withMinute(0).withSecond(0).withNano(0))
                    .applyStartAt(LocalDateTime.now().minusHours(1))
                    .applyEndAt(LocalDateTime.now().plusDays(6).withHour(23).withMinute(59).withSecond(59))
                    .location("강남 캠퍼스 A동 101호")
                    .capacity(30)
                    .status(ExplanationEventStatus.RESERVABLE)
                    .pinned(true)
                    .published(true)
                    .build();

            explanationEventRepository.save(event1);
            log.info("샘플 설명회 생성: {}", event1.getTitle());
        }

        // 샘플 설명회 2: 정원 제한 없음 (고등부)
        if (!explanationEventRepository.existsByTitle("고등부 입시 전략 설명회")) {
            ExplanationEvent event2 = ExplanationEvent.builder()
                    .division(ExplanationDivision.HIGH)
                    .title("고등부 입시 전략 설명회")
                    .content("대학 입시를 위한 전략적 학습 방법과 진로 지도 프로그램을 소개합니다.")
                    .startAt(LocalDateTime.now().plusDays(10).withHour(19).withMinute(0).withSecond(0).withNano(0))
                    .endAt(LocalDateTime.now().plusDays(10).withHour(21).withMinute(0).withSecond(0).withNano(0))
                    .applyStartAt(LocalDateTime.now().minusHours(1))
                    .applyEndAt(LocalDateTime.now().plusDays(9).withHour(18).withMinute(0).withSecond(0))
                    .location("온라인 (Zoom)")
                    .capacity(0) // 무제한
                    .status(ExplanationEventStatus.RESERVABLE)
                    .pinned(false)
                    .published(true)
                    .build();

            explanationEventRepository.save(event2);
            log.info("샘플 설명회 생성: {}", event2.getTitle());
        }

        // 샘플 설명회 3: 예약 마감 (고등부)
        if (!explanationEventRepository.existsByTitle("고등부 수학 집중 과정 설명회")) {
            ExplanationEvent event3 = ExplanationEvent.builder()
                    .division(ExplanationDivision.HIGH)
                    .title("고등부 수학 집중 과정 설명회")
                    .content("고등부 수학의 심화 과정과 문제 해결 전략을 다룹니다.")
                    .startAt(LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0))
                    .endAt(LocalDateTime.now().plusDays(5).withHour(18).withMinute(0).withSecond(0).withNano(0))
                    .applyStartAt(LocalDateTime.now().minusDays(2))
                    .applyEndAt(LocalDateTime.now().minusHours(1)) // 이미 신청 마감
                    .location("강남 캠퍼스 B동 201호")
                    .capacity(20)
                    .status(ExplanationEventStatus.CLOSED)
                    .pinned(false)
                    .published(true)
                    .build();

            explanationEventRepository.save(event3);
            log.info("샘플 설명회 생성 (마감): {}", event3.getTitle());
        }
    }
}