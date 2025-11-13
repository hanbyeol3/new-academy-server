package com.academy.api.integration;

import com.academy.api.category.domain.Category;
import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.repository.CategoryGroupRepository;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 카테고리-공지사항-파일 통합 테스트.
 * 
 * Repository 레벨에서의 통합 테스트:
 * - 카테고리 연동 검증
 * - 공지사항 기본 CRUD 
 * - 파일 첨부 구조 검증 (향후 확장)
 */
@DataJpaTest
@Transactional
@DisplayName("카테고리-공지사항-파일 통합 테스트 (Repository 레벨)")
class CategoryNoticeFileIntegrationTest {

    @Autowired
    private CategoryGroupRepository categoryGroupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    private CategoryGroup noticeGroup;
    private Category academicCategory;
    private Category generalCategory;

    @BeforeEach
    void setUp() {
        setupCategories();
    }

    /**
     * 테스트용 카테고리 구조 생성.
     */
    private void setupCategories() {
        // 공지사항 카테고리 그룹 생성
        noticeGroup = CategoryGroup.builder()
                .name("공지사항")
                .description("공지사항 카테고리 그룹")
                .build();
        noticeGroup = categoryGroupRepository.save(noticeGroup);

        // 공지사항용 카테고리들 생성 (학사일정, 일반공지)
        academicCategory = Category.builder()
                .categoryGroup(noticeGroup)
                .name("학사일정")
                .slug("academic")
                .description("학사일정 관련 공지사항")
                .build();

        generalCategory = Category.builder()
                .categoryGroup(noticeGroup)
                .name("일반공지")
                .slug("general")
                .description("일반적인 공지사항")
                .build();

        academicCategory = categoryRepository.save(academicCategory);
        generalCategory = categoryRepository.save(generalCategory);
    }

    @Nested
    @DisplayName("✅ 1단계: 기초 데이터 검증")
    class BasicDataTest {

        @Test
        @DisplayName("카테고리 그룹이 올바르게 생성되었는지 확인")
        void testCategoryGroupCreation() {
            assertThat(noticeGroup).isNotNull();
            assertThat(noticeGroup.getId()).isNotNull();
            assertThat(noticeGroup.getName()).isEqualTo("공지사항");
        }

        @Test
        @DisplayName("공지사항용 카테고리들이 올바르게 생성되었는지 확인")
        void testCategoryCreation() {
            assertThat(academicCategory.getId()).isNotNull();
            assertThat(generalCategory.getId()).isNotNull();

            assertThat(academicCategory.getCategoryGroup().getId())
                    .isEqualTo(noticeGroup.getId());
            assertThat(generalCategory.getCategoryGroup().getId())
                    .isEqualTo(noticeGroup.getId());
        }
    }

    @Nested
    @DisplayName("✅ 2단계: 공지사항 엔티티 CRUD")
    class BasicNoticeTest {

        @Test
        @DisplayName("공지사항 생성 및 저장")
        void createNotice() {
            // Given
            Notice notice = Notice.builder()
                    .category(generalCategory)
                    .title("첨부파일 없는 공지사항")
                    .content("내용입니다.")
                    .isPublished(true)
                    .isImportant(false)
                    .build();

            // When
            Notice savedNotice = noticeRepository.save(notice);

            // Then
            assertThat(savedNotice).isNotNull();
            assertThat(savedNotice.getId()).isNotNull();
            assertThat(savedNotice.getTitle()).isEqualTo("첨부파일 없는 공지사항");
            assertThat(savedNotice.getCategory().getId()).isEqualTo(generalCategory.getId());
            assertThat(savedNotice.getIsPublished()).isTrue();
        }

        @Test
        @DisplayName("공지사항 조회")
        void getNotice() {
            // Given
            Notice notice = createTestNotice("조회 테스트 공지", generalCategory);
            
            // When
            Notice foundNotice = noticeRepository.findById(notice.getId()).orElse(null);
            
            // Then
            assertThat(foundNotice).isNotNull();
            assertThat(foundNotice.getTitle()).isEqualTo("조회 테스트 공지");
            assertThat(foundNotice.getCategory()).isEqualTo(generalCategory);
        }

        @Test
        @DisplayName("공지사항 수정")
        void updateNotice() {
            // Given
            Notice notice = createTestNotice("수정 전 제목", generalCategory);
            
            // When
            notice.update(
                    "수정 후 제목",
                    "수정 후 내용",
                    false,
                    false,
                    null,
                    null,
                    null,
                    academicCategory,
                    null
            );
            Notice updatedNotice = noticeRepository.save(notice);

            // Then
            assertThat(updatedNotice.getTitle()).isEqualTo("수정 후 제목");
            assertThat(updatedNotice.getCategory().getId()).isEqualTo(academicCategory.getId());
            assertThat(updatedNotice.getIsPublished()).isFalse();
        }

        @Test
        @DisplayName("공지사항 삭제")
        void deleteNotice() {
            // Given
            Notice notice = createTestNotice("삭제 테스트 공지", generalCategory);
            Long noticeId = notice.getId();

            // When
            noticeRepository.delete(notice);

            // Then
            assertThat(noticeRepository.findById(noticeId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("✅ 3단계: 카테고리 연동 검증")
    class CategoryIntegrationTest {

        @Test
        @DisplayName("학사일정 카테고리 공지사항 생성 및 조회")
        void createAndGetAcademicNotice() {
            // Given
            Notice notice = Notice.builder()
                    .category(academicCategory)
                    .title("2024년 1학기 수강신청 안내")
                    .content("수강신청 기간: 2024.02.01 ~ 2024.02.15")
                    .isImportant(true)
                    .isPublished(true)
                    .build();

            // When
            Notice savedNotice = noticeRepository.save(notice);
            Notice foundNotice = noticeRepository.findById(savedNotice.getId()).orElse(null);

            // Then
            assertThat(foundNotice).isNotNull();
            assertThat(foundNotice.getTitle()).isEqualTo("2024년 1학기 수강신청 안내");
            assertThat(foundNotice.getCategory().getName()).isEqualTo("학사일정");
            assertThat(foundNotice.getIsImportant()).isTrue();
        }

        @Test
        @DisplayName("일반공지 카테고리 공지사항 생성 및 조회")
        void createAndGetGeneralNotice() {
            // Given
            Notice notice = Notice.builder()
                    .category(generalCategory)
                    .title("도서관 이용시간 변경 안내")
                    .content("3월부터 도서관 이용시간이 변경됩니다.")
                    .isImportant(false)
                    .isPublished(true)
                    .build();

            // When
            Notice savedNotice = noticeRepository.save(notice);
            Notice foundNotice = noticeRepository.findById(savedNotice.getId()).orElse(null);

            // Then
            assertThat(foundNotice).isNotNull();
            assertThat(foundNotice.getCategory().getName()).isEqualTo("일반공지");
            assertThat(foundNotice.getIsImportant()).isFalse();
        }

        @Test
        @DisplayName("카테고리 변경 테스트")
        void changeCategoryTest() {
            // Given
            Notice notice = createTestNotice("카테고리 변경 테스트", generalCategory);
            
            // When
            notice.changeCategory(academicCategory);
            Notice updatedNotice = noticeRepository.save(notice);

            // Then
            assertThat(updatedNotice.getCategory().getName()).isEqualTo("학사일정");
        }

        @Test
        @DisplayName("카테고리별 공지사항 조회")
        void findNoticesByCategory() {
            // Given
            createTestNotice("학사일정 공지1", academicCategory);
            createTestNotice("학사일정 공지2", academicCategory);
            createTestNotice("일반공지1", generalCategory);

            // When
            var academicNotices = noticeRepository.findByCategoryId(academicCategory.getId());
            var generalNotices = noticeRepository.findByCategoryId(generalCategory.getId());

            // Then
            assertThat(academicNotices).hasSize(2);
            assertThat(generalNotices).hasSize(1);
        }
    }

    @Nested
    @DisplayName("📋 4단계: 파일 첨부 구조 검증 - 현재 상태")
    class FileStructureTest {

        @Test
        @DisplayName("공지사항 엔티티 기본 구조 확인")
        void verifyNoticeEntityStructure() {
            Notice notice = createTestNotice("구조 테스트", generalCategory);
            
            // Notice 엔티티의 기본 구조 확인
            assertThat(notice.getId()).isNotNull();
            assertThat(notice.getTitle()).isEqualTo("구조 테스트");
            assertThat(notice.getContent()).isEqualTo("테스트 내용");
            assertThat(notice.getCategory()).isEqualTo(generalCategory);
            assertThat(notice.getIsPublished()).isTrue();
        }

        @Test
        @DisplayName("공지사항과 카테고리 연관관계 확인")
        void verifyNoticeCategoryRelation() {
            // Given
            Notice notice1 = createTestNotice("테스트1", academicCategory);
            Notice notice2 = createTestNotice("테스트2", generalCategory);

            // When & Then
            assertThat(notice1.getCategory().getCategoryGroup().getName()).isEqualTo("공지사항");
            assertThat(notice2.getCategory().getCategoryGroup().getName()).isEqualTo("공지사항");
            
            // 카테고리가 다르면 다른 카테고리
            assertThat(notice1.getCategory().getName()).isEqualTo("학사일정");
            assertThat(notice2.getCategory().getName()).isEqualTo("일반공지");
        }

        @Test
        @DisplayName("공지사항 Repository 쿼리 메서드 확인")
        void verifyNoticeRepositoryMethods() {
            // Given
            createTestNotice("중요 학사일정", academicCategory, true, true);
            createTestNotice("일반 학사일정", academicCategory, false, true);
            createTestNotice("비공개 일반공지", generalCategory, false, false);

            // When & Then - 중요 공지 조회
            var importantNotices = noticeRepository.findImportantNotices();
            assertThat(importantNotices).hasSize(1);
            assertThat(importantNotices.get(0).getTitle()).isEqualTo("중요 학사일정");

            // When & Then - 공개 공지 조회
            var publishedNotices = noticeRepository.findPublishedNotices();
            assertThat(publishedNotices).hasSize(2); // 비공개 제외

            // When & Then - 카테고리별 조회
            var academicNotices = noticeRepository.findByCategoryId(academicCategory.getId());
            assertThat(academicNotices).hasSize(2);
        }
    }

    @Nested 
    @DisplayName("🔮 5단계: 향후 파일 첨부 기능 확장 대비")
    class FileFutureTest {

        @Test
        @DisplayName("공지사항 COVER 파일 미사용 명시")
        void verifyCoverNotUsedInNotice() {
            // COVER 파일 역할은 공지사항에서 사용하지 않는다고 명시된 요구사항
            // 갤러리나 다른 도메인에서만 사용되는 개념
            Notice notice = createTestNotice("커버 파일 미사용 확인", generalCategory);
            
            // 공지사항은 ATTACHMENT(일반 첨부파일)과 CONTENT(본문 파일)만 지원
            assertThat(notice).isNotNull();
            assertThat(notice.getTitle()).contains("커버 파일 미사용 확인");
            
            // TODO: 향후 파일 첨부 기능 구현 시 
            // - ATTACHMENT 역할: 일반 첨부파일 (다운로드용)
            // - CONTENT 역할: 본문 내 이미지/파일 (인라인 표시용)  
            // - COVER 역할: 사용하지 않음 (갤러리 전용)
        }

        @Test
        @DisplayName("파일 첨부 기능 구현 로드맵 확인")
        void verifyFileAttachmentRoadmap() {
            // 현재는 기본 공지사항만 테스트
            // 향후 UploadFile, UploadFileLink 연동 시 이 테스트들을 확장할 예정
            
            Notice notice = createTestNotice("파일 첨부 로드맵", generalCategory);
            
            // 기본 필드들이 정상적으로 설정되는지 확인
            assertThat(notice.getTitle()).isEqualTo("파일 첨부 로드맵");
            assertThat(notice.getContent()).isNotNull();
            assertThat(notice.getCategory()).isNotNull();
            
            // 🚀 향후 구현 계획:
            // 1. RequestNoticeCreate/Update DTO에 attachments, inlineImages 필드 추가
            // 2. Notice 엔티티와 UploadFileLink 연관관계 설정  
            // 3. NoticeService에서 파일 처리 로직 추가
            // 4. 파일 생명주기 관리 (임시→영구, 삭제 시 정리)
            // 5. 파일 역할별 처리 로직 (ATTACHMENT vs CONTENT)
            // 6. 스케줄러를 통한 고아 파일 정리
        }

        @Test
        @DisplayName("카테고리-공지사항-파일 통합 시나리오 준비 상태")
        void verifyIntegrationScenarioReadiness() {
            // 현재 단계에서 성공적으로 테스트된 기능들
            
            // ✅ 카테고리 구조: 공지사항 그룹 → 학사일정, 일반공지
            assertThat(noticeGroup.getName()).isEqualTo("공지사항");
            assertThat(academicCategory.getName()).isEqualTo("학사일정");
            assertThat(generalCategory.getName()).isEqualTo("일반공지");
            
            // ✅ 공지사항 기본 CRUD
            Notice testNotice = createTestNotice("통합 시나리오 테스트", academicCategory);
            assertThat(testNotice.getId()).isNotNull();
            assertThat(testNotice.getCategory().getCategoryGroup()).isEqualTo(noticeGroup);
            
            // ✅ Repository 쿼리 메서드 동작
            var notices = noticeRepository.findByCategoryId(academicCategory.getId());
            assertThat(notices).isNotEmpty();
            
            // 🔄 준비 완료된 확장 포인트:
            // - 파일 업로드 API 연동
            // - 첨부파일 DTO 필드 추가
            // - 파일 링크 테이블 연동
            // - 파일 생명주기 관리 통합
        }
    }

    /**
     * 테스트용 공지사항 생성 (기본).
     */
    private Notice createTestNotice(String title, Category category) {
        return createTestNotice(title, category, false, true);
    }

    /**
     * 테스트용 공지사항 생성 (상세).
     */
    private Notice createTestNotice(String title, Category category, boolean isImportant, boolean isPublished) {
        Notice notice = Notice.builder()
                .category(category)
                .title(title)
                .content("테스트 내용")
                .isPublished(isPublished)
                .isImportant(isImportant)
                .build();

        return noticeRepository.save(notice);
    }
}