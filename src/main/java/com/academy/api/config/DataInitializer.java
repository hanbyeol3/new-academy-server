package com.academy.api.config;

import com.academy.api.member.domain.Member;
import com.academy.api.member.domain.MemberRole;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.domain.ExplanationEventStatus;
import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.repository.ExplanationEventRepository;
import com.academy.api.gallery.domain.GalleryItem;
import com.academy.api.gallery.repository.GalleryItemRepository;
import com.academy.api.schedule.domain.AcademicSchedule;
import com.academy.api.schedule.domain.ScheduleCategory;
import com.academy.api.schedule.repository.AcademicScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ExplanationEventRepository explanationEventRepository;
    private final GalleryItemRepository galleryItemRepository;
    private final AcademicScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== 샘플 데이터 초기화 시작 ===");

        // 관리자 계정 역할 업데이트
        updateAdminRole();

        // 샘플 설명회 데이터 생성
        createSampleExplanationEvents();

        // 샘플 갤러리 데이터 생성
        createSampleGalleryItems();

        // 샘플 학사일정 데이터 생성
        createSampleAcademicSchedules();

        log.info("=== 샘플 데이터 초기화 완료 ===");
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

    private void createSampleGalleryItems() {
        log.info("갤러리 샘플 데이터 생성 시작");

        // 샘플 갤러리 1: 파일 ID 기반
        if (!galleryItemRepository.existsByTitle("학원 전경")) {
            GalleryItem item1 = GalleryItem.builder()
                    .title("학원 전경")
                    .description("아름다운 가을 캠퍼스 전경입니다.")
                    .imageFileId("f6a1e3b2-1234-5678-9abc-def012345678")
                    .sortOrder(1)
                    .published(true)
                    .build();

            galleryItemRepository.save(item1);
            log.info("샘플 갤러리 생성: {}", item1.getTitle());
        }

        // 샘플 갤러리 2: 직접 URL
        if (!galleryItemRepository.existsByTitle("학원 로비")) {
            GalleryItem item2 = GalleryItem.builder()
                    .title("학원 로비")
                    .description("넓고 쾌적한 로비 공간입니다.")
                    .imageUrl("https://example.com/static/lobby.jpg")
                    .sortOrder(2)
                    .published(true)
                    .build();

            galleryItemRepository.save(item2);
            log.info("샘플 갤러리 생성: {}", item2.getTitle());
        }

        // 샘플 갤러리 3: 파일 ID 기반
        if (!galleryItemRepository.existsByTitle("도서관")) {
            GalleryItem item3 = GalleryItem.builder()
                    .title("도서관")
                    .description("조용하고 집중할 수 있는 도서관입니다.")
                    .imageFileId("a7b2f4c3-5678-9abc-def0-123456789abc")
                    .sortOrder(3)
                    .published(true)
                    .build();

            galleryItemRepository.save(item3);
            log.info("샘플 갤러리 생성: {}", item3.getTitle());
        }

        // 샘플 갤러리 4: 직접 URL
        if (!galleryItemRepository.existsByTitle("쉼터 공간")) {
            GalleryItem item4 = GalleryItem.builder()
                    .title("쉼터 공간")
                    .description("학생들이 휴식을 취할 수 있는 공간입니다.")
                    .imageUrl("https://example.com/static/rest-area.jpg")
                    .sortOrder(4)
                    .published(true)
                    .build();

            galleryItemRepository.save(item4);
            log.info("샘플 갤러리 생성: {}", item4.getTitle());
        }

        // 샘플 갤러리 5: 직접 URL
        if (!galleryItemRepository.existsByTitle("실험실")) {
            GalleryItem item5 = GalleryItem.builder()
                    .title("실험실")
                    .description("최신 장비를 갖춘 과학 실험실입니다.")
                    .imageUrl("https://example.com/static/lab.jpg")
                    .sortOrder(5)
                    .published(true)
                    .build();

            galleryItemRepository.save(item5);
            log.info("샘플 갤러리 생성: {}", item5.getTitle());
        }

        // 샘플 갤러리 6: 파일 ID 기반
        if (!galleryItemRepository.existsByTitle("운동장")) {
            GalleryItem item6 = GalleryItem.builder()
                    .title("운동장")
                    .description("넓은 운동장에서 체육 활동을 할 수 있습니다.")
                    .imageFileId("c8d3e5f4-9abc-def0-1234-56789abcdef0")
                    .sortOrder(6)
                    .published(true)
                    .build();

            galleryItemRepository.save(item6);
            log.info("샘플 갤러리 생성: {}", item6.getTitle());
        }

        // 샘플 갤러리 7: 비공개 항목
        if (!galleryItemRepository.existsByTitle("비공개 항목")) {
            GalleryItem item7 = GalleryItem.builder()
                    .title("비공개 항목")
                    .description("관리자만 볼 수 있는 테스트 항목입니다.")
                    .imageUrl("https://example.com/static/private.jpg")
                    .sortOrder(7)
                    .published(false)
                    .build();

            galleryItemRepository.save(item7);
            log.info("샘플 갤러리 생성 (비공개): {}", item7.getTitle());
        }
    }

    private void createSampleAcademicSchedules() {
        log.info("학사일정 샘플 데이터 생성 시작");

        // 2025년 9월 일정들
        if (!scheduleRepository.existsByTitle("가을학기 개강")) {
            AcademicSchedule item1 = AcademicSchedule.builder()
                    .category(ScheduleCategory.OPEN_CLOSE)
                    .startDate(LocalDate.of(2025, 9, 2))
                    .endDate(LocalDate.of(2025, 9, 2))
                    .title("가을학기 개강")
                    .published(true)
                    .color("#3B82F6")
                    .build();

            scheduleRepository.save(item1);
            log.info("샘플 학사일정 생성: {}", item1.getTitle());
        }

        if (!scheduleRepository.existsByTitle("9월 교육청 모의고사")) {
            AcademicSchedule item2 = AcademicSchedule.builder()
                    .category(ScheduleCategory.EXAM)
                    .startDate(LocalDate.of(2025, 9, 4))
                    .endDate(LocalDate.of(2025, 9, 4))
                    .title("9월 교육청 모의고사")
                    .published(true)
                    .color("#EF4444")
                    .build();

            scheduleRepository.save(item2);
            log.info("샘플 학사일정 생성: {}", item2.getTitle());
        }

        if (!scheduleRepository.existsByTitle("중간고사")) {
            AcademicSchedule item3 = AcademicSchedule.builder()
                    .category(ScheduleCategory.EXAM)
                    .startDate(LocalDate.of(2025, 9, 16))
                    .endDate(LocalDate.of(2025, 9, 18))
                    .title("중간고사")
                    .published(true)
                    .color("#EF4444")
                    .build();

            scheduleRepository.save(item3);
            log.info("샘플 학사일정 생성: {}", item3.getTitle());
        }

        if (!scheduleRepository.existsByTitle("수강신청 기간")) {
            AcademicSchedule item4 = AcademicSchedule.builder()
                    .category(ScheduleCategory.NOTICE)
                    .startDate(LocalDate.of(2025, 9, 25))
                    .endDate(LocalDate.of(2025, 9, 27))
                    .title("수강신청 기간")
                    .published(true)
                    .color("#F59E0B")
                    .build();

            scheduleRepository.save(item4);
            log.info("샘플 학사일정 생성: {}", item4.getTitle());
        }

        // 2025년 10월 일정들
        if (!scheduleRepository.existsByTitle("체육대회")) {
            AcademicSchedule item5 = AcademicSchedule.builder()
                    .category(ScheduleCategory.EVENT)
                    .startDate(LocalDate.of(2025, 10, 5))
                    .endDate(LocalDate.of(2025, 10, 5))
                    .title("체육대회")
                    .published(true)
                    .color("#10B981")
                    .build();

            scheduleRepository.save(item5);
            log.info("샘플 학사일정 생성: {}", item5.getTitle());
        }

        if (!scheduleRepository.existsByTitle("가을 특강 주간")) {
            AcademicSchedule item6 = AcademicSchedule.builder()
                    .category(ScheduleCategory.EVENT)
                    .startDate(LocalDate.of(2025, 10, 14))
                    .endDate(LocalDate.of(2025, 10, 18))
                    .title("가을 특강 주간")
                    .published(true)
                    .color("#10B981")
                    .build();

            scheduleRepository.save(item6);
            log.info("샘플 학사일정 생성: {}", item6.getTitle());
        }

        // 비공개 일정
        if (!scheduleRepository.existsByTitle("내부 교사 연수")) {
            AcademicSchedule item7 = AcademicSchedule.builder()
                    .category(ScheduleCategory.ETC)
                    .startDate(LocalDate.of(2025, 9, 30))
                    .endDate(LocalDate.of(2025, 9, 30))
                    .title("내부 교사 연수")
                    .published(false)
                    .color("#6B7280")
                    .build();

            scheduleRepository.save(item7);
            log.info("샘플 학사일정 생성 (비공개): {}", item7.getTitle());
        }
    }
}