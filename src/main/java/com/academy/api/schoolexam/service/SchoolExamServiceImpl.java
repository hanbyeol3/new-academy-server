package com.academy.api.schoolexam.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.dto.FileReference;
import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.repository.UploadFileRepository;
import com.academy.api.file.service.FileService;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.domain.SchoolLevel;
import com.academy.api.schoolexam.dto.*;
import com.academy.api.schoolexam.mapper.SchoolExamMapper;
import com.academy.api.schoolexam.repository.SchoolExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 학교별 시험분석 서비스 구현체.
 * 
 * 주요 특징:
 * - 트랜잭션 경계 관리 (@Transactional)
 * - 체계적인 로깅 (info: 주요 비즈니스, debug: 상세 정보)
 * - 카테고리 연계 처리
 * - 파일 서비스 연동 및 content URL 자동 변환
 * - 임시 파일을 정식 파일로 승격 처리
 * - 검색 기능 (제목/내용/작성자/전체)
 * - 예외 상황 처리
 * 
 * Content URL 변환 기능:
 * - 시험분석 생성/수정 시 본문 이미지의 임시 URL을 정식 URL로 자동 변환
 * - 임시 URL: /api/public/files/temp/{tempId} → 정식 URL: /api/public/files/download/{formalId}
 * - 도메인 메서드를 통한 안전한 엔티티 상태 변경
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolExamServiceImpl implements SchoolExamService, CategoryUsageChecker {

    private final SchoolExamRepository schoolExamRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final SchoolExamMapper schoolExamMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final UploadFileRepository uploadFileRepository;
    private final FileService fileService;

    /**
     * [관리자] 시험분석 목록 조회 (모든 상태 포함).
     */
    @Override
    public ResponseList<ResponseSchoolExamAdminList> getSchoolExamListForAdmin(
            String keyword, String searchType, String schoolLevel, Long categoryId, 
            Boolean isPublished, String sortBy, Pageable pageable) {
        
        log.info("[SchoolExamService] 관리자용 시험분석 목록 조회 시작. keyword={}, searchType={}, schoolLevel={}, categoryId={}, isPublished={}, sortBy={}, 페이지={}", 
                keyword, searchType, schoolLevel, categoryId, isPublished, sortBy, pageable);

        // SchoolLevel enum 변환
        SchoolLevel level = null;
        if (schoolLevel != null) {
            level = SchoolLevel.fromString(schoolLevel);
            if (level == null) {
                log.warn("[SchoolExamService] 유효하지 않은 학교급: {}. 필터링 제외", schoolLevel);
            }
        }

        // Repository 호출
        Page<SchoolExam> schoolExamPage = schoolExamRepository.searchSchoolExamsForAdmin(
                keyword, searchType, level, categoryId, isPublished, sortBy, pageable);

        log.debug("[SchoolExamService] 관리자용 목록 조회 완료. 총 {}건, 현재 페이지 {}건", 
                schoolExamPage.getTotalElements(), schoolExamPage.getNumberOfElements());

        return schoolExamMapper.toAdminResponseList(schoolExamPage);
    }

    /**
     * [공개] 시험분석 목록 조회 (공개된 것만).
     */
    @Override
    public ResponseList<ResponseSchoolExamPublicList> getSchoolExamListForPublic(
            String keyword, String searchType, String schoolLevel, Long categoryId, 
            String sortBy, Pageable pageable) {
        
        log.info("[SchoolExamService] 공개용 시험분석 목록 조회 시작. keyword={}, searchType={}, schoolLevel={}, categoryId={}, sortBy={}", 
                keyword, searchType, schoolLevel, categoryId, sortBy);

        // SchoolLevel enum 변환
        SchoolLevel level = null;
        if (schoolLevel != null) {
            level = SchoolLevel.fromString(schoolLevel);
            if (level == null) {
                log.warn("[SchoolExamService] 유효하지 않은 학교급: {}. 필터링 제외", schoolLevel);
            }
        }

        // 공개된 것만 조회
        Page<SchoolExam> schoolExamPage = schoolExamRepository.searchSchoolExamsForPublic(
                keyword, searchType, level, categoryId, sortBy, pageable);

        log.debug("[SchoolExamService] 공개용 목록 조회 완료. 총 {}건, 현재 페이지 {}건", 
                schoolExamPage.getTotalElements(), schoolExamPage.getNumberOfElements());

        return schoolExamMapper.toPublicResponseList(schoolExamPage);
    }

    /**
     * [관리자] 시험분석 상세 조회.
     */
    @Override
    public ResponseData<ResponseSchoolExamDetail> getSchoolExamForAdmin(Long id) {
        log.info("[SchoolExamService] 관리자용 시험분석 상세 조회 시작. ID={}", id);

        SchoolExam schoolExam = findSchoolExamById(id);
        // 관리자용 네비게이션 (모든 시험분석 포함)
        ResponseSchoolExamDetail response = buildDetailResponse(schoolExam, false);
        
        log.debug("[SchoolExamService] 관리자용 상세 조회 완료. ID={}, 제목={}", id, schoolExam.getTitle());

        return ResponseData.ok(response);
    }

    /**
     * [관리자] 시험분석 상세 조회 (학교급 필터 적용).
     */
    @Override
    public ResponseData<ResponseSchoolExamDetail> getSchoolExamForAdmin(Long id, String schoolLevelParam) {
        log.info("[SchoolExamService] 관리자용 시험분석 상세 조회 시작. ID={}, schoolLevel={}", id, schoolLevelParam);

        SchoolExam schoolExam = findSchoolExamById(id);

        // SchoolLevel enum 변환
        SchoolLevel schoolLevel = null;
        if (schoolLevelParam != null) {
            schoolLevel = SchoolLevel.fromString(schoolLevelParam);
            if (schoolLevel == null) {
                log.warn("[SchoolExamService] 유효하지 않은 학교급: {}. 필터링 없이 진행", schoolLevelParam);
            }
        }

        // 관리자용 네비게이션 (모든 시험분석 포함, 학교급 필터 적용)
        ResponseSchoolExamDetail response = buildDetailResponseWithSchoolLevel(schoolExam, false, schoolLevel);
        
        log.debug("[SchoolExamService] 관리자용 상세 조회 완료. ID={}, 제목={}, schoolLevel={}", 
                id, schoolExam.getTitle(), schoolLevel);

        return ResponseData.ok(response);
    }

    /**
     * [공개] 시험분석 상세 조회 (조회수 증가).
     */
    @Override
    @Transactional
    public ResponseData<ResponseSchoolExamDetail> getSchoolExamForPublic(Long id) {
        log.info("[SchoolExamService] 공개용 시험분석 상세 조회 시작. ID={}", id);

        SchoolExam schoolExam = findSchoolExamById(id);

        // 공개 여부 확인
        if (!schoolExam.getIsPublished()) {
            log.warn("[SchoolExamService] 비공개 시험분석 접근 시도. ID={}", id);
            throw new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND);
        }

        // 조회수 증가 - Repository JPQL 메서드 사용 (updatedAt 변경 방지)
        int updatedCount = schoolExamRepository.incrementViewCount(id);
        if (updatedCount == 0) {
            log.warn("[SchoolExamService] 조회수 증가 실패 - 시험분석을 찾을 수 없음. ID={}", id);
        }

        // 공개용 네비게이션 (공개된 것만)
        ResponseSchoolExamDetail response = buildDetailResponse(schoolExam, true);
        
        log.debug("[SchoolExamService] 공개용 상세 조회 완료. ID={}, 제목={}, 조회수={}", 
                id, schoolExam.getTitle(), schoolExam.getViewCount());

        return ResponseData.ok(response);
    }

    /**
     * [공개] 시험분석 상세 조회 (조회수 증가, 학교급 필터 적용).
     */
    @Override
    @Transactional
    public ResponseData<ResponseSchoolExamDetail> getSchoolExamForPublic(Long id, String schoolLevelParam) {
        log.info("[SchoolExamService] 공개용 시험분석 상세 조회 시작. ID={}, schoolLevel={}", id, schoolLevelParam);

        SchoolExam schoolExam = findSchoolExamById(id);

        // 공개 여부 확인
        if (!schoolExam.getIsPublished()) {
            log.warn("[SchoolExamService] 비공개 시험분석 접근 시도. ID={}", id);
            throw new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND);
        }

        // 조회수 증가 - Repository JPQL 메서드 사용 (updatedAt 변경 방지)
        int updatedCount = schoolExamRepository.incrementViewCount(id);
        if (updatedCount == 0) {
            log.warn("[SchoolExamService] 조회수 증가 실패 - 시험분석을 찾을 수 없음. ID={}", id);
        }

        // 현재 게시글의 학교급으로 필터링 (파라미터와 관계없이)
        SchoolLevel schoolLevel = schoolExam.getSchoolLevel();
        
        log.info("[SchoolExamService] 현재 글의 학교급으로 이전/다음글 필터링: {}", schoolLevel);

        // 공개용 네비게이션 (공개된 것만, 현재 글의 학교급으로 필터 적용)
        ResponseSchoolExamDetail response = buildDetailResponseWithSchoolLevel(schoolExam, true, schoolLevel);
        
        log.debug("[SchoolExamService] 공개용 상세 조회 완료. ID={}, 제목={}, 조회수={}, schoolLevel={}", 
                id, schoolExam.getTitle(), schoolExam.getViewCount(), schoolLevel);

        return ResponseData.ok(response);
    }

    /**
     * 시험분석 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createSchoolExam(RequestSchoolExamCreate request) {
        log.info("[SchoolExamService] 시험분석 생성 시작. 제목={}, 학교급={}", 
                request.getTitle(), request.getSchoolLevel());

        // 현재 로그인 사용자 ID 조회
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 카테고리 조회
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
        }

        // 엔티티 생성
        SchoolExam schoolExam = SchoolExam.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .schoolLevel(request.getSchoolLevel())
                .isPublished(request.getIsPublished())
                .category(category)
                .viewCount(request.getViewCount() != null ? request.getViewCount() : 0L)
                .createdBy(currentUserId)
                .build();

        SchoolExam savedSchoolExam = schoolExamRepository.save(schoolExam);
        Long schoolExamId = savedSchoolExam.getId();
        
        // 파일 처리
        Map<String, Long> attachmentTempMap = addFileLinks(schoolExamId, request.getAttachmentFiles(), FileRole.ATTACHMENT);
        Map<String, Long> inlineTempMap = addFileLinks(schoolExamId, request.getInlineImages(), FileRole.INLINE);
        
        // content에서 임시 URL을 정식 URL로 변환
        if (!inlineTempMap.isEmpty()) {
            String updatedContent = fileService.convertTempUrlsInContent(savedSchoolExam.getContent(), inlineTempMap);
            if (!updatedContent.equals(savedSchoolExam.getContent())) {
                savedSchoolExam = schoolExamRepository.findById(schoolExamId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND));
                
                savedSchoolExam.updateContent(updatedContent);
                schoolExamRepository.save(savedSchoolExam);
                log.info("[SchoolExamService] content 내 임시 URL 변환 완료. ID={}", schoolExamId);
            }
        }

        log.info("[SchoolExamService] 시험분석 생성 완료. ID={}, 제목={}", 
                savedSchoolExam.getId(), savedSchoolExam.getTitle());

        return ResponseData.ok("0000", "시험분석이 생성되었습니다.", savedSchoolExam.getId());
    }

    /**
     * 시험분석 수정.
     */
    @Override
    @Transactional
    public ResponseData<ResponseSchoolExamDetail> updateSchoolExam(Long id, RequestSchoolExamUpdate request) {
        log.info("[SchoolExamService] 시험분석 수정 시작. ID={}, " +
                "신규첨부파일={}개, 신규본문이미지={}개, 삭제첨부파일={}개, 삭제본문이미지={}개", 
                id,
                request.getNewAttachments() != null ? request.getNewAttachments().size() : 0,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0,
                request.getDeleteAttachmentFileIds() != null ? request.getDeleteAttachmentFileIds().size() : 0,
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0);

        // 현재 로그인 사용자 ID 조회
        Long currentUserId = SecurityUtils.getCurrentUserId();

        SchoolExam schoolExam = findSchoolExamById(id);

        // 카테고리 처리
        Category category = schoolExam.getCategory();
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
        }

        // 엔티티 업데이트
        schoolExam.update(
                request.getTitle() != null ? request.getTitle() : schoolExam.getTitle(),
                request.getContent() != null ? request.getContent() : schoolExam.getContent(),
                request.getSchoolLevel() != null ? request.getSchoolLevel() : schoolExam.getSchoolLevel(),
                request.getIsPublished() != null ? request.getIsPublished() : schoolExam.getIsPublished(),
                category,
                request.getViewCount() != null ? request.getViewCount() : schoolExam.getViewCount(),
                currentUserId
        );

        // 선택적 파일 처리 (삭제 → 추가 순서)
        deleteSelectedFileLinks(id, request.getDeleteAttachmentFileIds(), FileRole.ATTACHMENT);
        deleteSelectedFileLinks(id, request.getDeleteInlineImageFileIds(), FileRole.INLINE);
        
        Map<String, Long> newAttachmentTempMap = addFileLinks(id, request.getNewAttachments(), FileRole.ATTACHMENT);
        Map<String, Long> newInlineTempMap = addFileLinks(id, request.getNewInlineImages(), FileRole.INLINE);
        
        // Content URL 완전 처리
        String finalContent = schoolExam.getContent();
        
        // 삭제된 이미지 URL 제거
        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            finalContent = fileService.removeDeletedImageUrlsFromContent(finalContent, request.getDeleteInlineImageFileIds());
            log.info("[SchoolExamService] 삭제된 이미지 URL 제거 완료. ID={}, 삭제된이미지={}개", 
                    id, request.getDeleteInlineImageFileIds().size());
        }
        
        // 모든 temp URL을 정식 URL로 변환
        String convertedContent = fileService.convertAllTempUrlsInContent(finalContent);
        
        if (!convertedContent.equals(schoolExam.getContent())) {
            SchoolExam currentSchoolExam = schoolExamRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND));
            
            currentSchoolExam.updateContent(convertedContent);
            schoolExamRepository.save(currentSchoolExam);
            log.info("[SchoolExamService] Content URL 완전 변환 완료. ID={}", id);
        }

        ResponseSchoolExamDetail response = buildDetailResponse(schoolExam, false);

        log.info("[SchoolExamService] 시험분석 수정 완료. ID={}, 제목={}", id, schoolExam.getTitle());

        return ResponseData.ok("0000", "시험분석이 수정되었습니다.", response);
    }

    /**
     * 시험분석 삭제.
     */
    @Override
    @Transactional
    public Response deleteSchoolExam(Long id) {
        log.info("[SchoolExamService] 시험분석 삭제 시작. ID={}", id);

        SchoolExam schoolExam = findSchoolExamById(id);

        // 연관된 파일 링크 삭제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerId("school_exams", id);
        log.info("[SchoolExamService] 파일 링크 삭제 완료. ID={}", id);

        schoolExamRepository.delete(schoolExam);

        log.info("[SchoolExamService] 시험분석 삭제 완료. ID={}, 제목={}", id, schoolExam.getTitle());

        return Response.ok("0000", "시험분석이 삭제되었습니다.");
    }

    /**
     * 조회수 수동 증가.
     */
    @Override
    @Transactional
    public Response incrementViewCount(Long id) {
        log.info("[SchoolExamService] 조회수 수동 증가 시작. ID={}", id);

        // Repository JPQL 메서드 사용 (updatedAt 변경 방지)
        int updatedCount = schoolExamRepository.incrementViewCount(id);
        if (updatedCount == 0) {
            log.warn("[SchoolExamService] 조회수 증가 실패 - 시험분석을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND);
        }

        log.debug("[SchoolExamService] 조회수 증가 완료. ID={}", id);

        return Response.ok("0000", "조회수가 증가되었습니다.");
    }

    /**
     * 공개/비공개 상태 변경.
     */
    @Override
    @Transactional
    public Response updateSchoolExamPublished(Long id, RequestSchoolExamPublishedUpdate request) {
        log.info("[SchoolExamService] 공개 상태 변경 시작. ID={}, 공개여부={}", id, request.getIsPublished());

        SchoolExam schoolExam = findSchoolExamById(id);
        schoolExam.setPublished(request.getIsPublished());

        String message = request.getIsPublished() ? 
                "시험분석이 공개로 변경되었습니다." : "시험분석이 비공개로 변경되었습니다.";

        log.debug("[SchoolExamService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, schoolExam.getIsPublished());

        return Response.ok("0000", message);
    }

    /**
     * 카테고리별 통계 조회.
     */
    @Override
    public ResponseData<List<Object[]>> getSchoolExamStatsByCategory() {
        log.info("[SchoolExamService] 카테고리별 통계 조회 시작");

        List<Object[]> stats = schoolExamRepository.getStatsByCategory();

        log.debug("[SchoolExamService] 카테고리별 통계 조회 완료. 카테고리 수={}", stats.size());

        return ResponseData.ok(stats);
    }

    /**
     * 학교급별 통계 조회.
     */
    @Override
    public ResponseData<List<Object[]>> getSchoolExamStatsBySchoolLevel() {
        log.info("[SchoolExamService] 학교급별 통계 조회 시작");

        List<Object[]> stats = schoolExamRepository.getStatsBySchoolLevel();

        log.debug("[SchoolExamService] 학교급별 통계 조회 완료. 학교급 수={}", stats.size());

        return ResponseData.ok(stats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseSchoolExamLatest> getLatestSchoolExams() {
        log.info("[SchoolExamService] 중등부/고등부별 최신 시험분석 조회 시작");
        
        // 중등부 최신 3개 조회
        PageRequest pageRequest = PageRequest.of(0, 3);
        List<SchoolExam> middleExams = schoolExamRepository.findLatestBySchoolLevel(
                SchoolLevel.MIDDLE, pageRequest);
        
        // 고등부 최신 3개 조회
        List<SchoolExam> highExams = schoolExamRepository.findLatestBySchoolLevel(
                SchoolLevel.HIGH, pageRequest);
        
        // DTO 변환
        List<ResponseSchoolExamSummary> middleList = middleExams.stream()
                .map(exam -> ResponseSchoolExamSummary.builder()
                        .id(exam.getId())
                        .title(exam.getTitle())
                        .categoryName(exam.getCategory() != null ? exam.getCategory().getName() : null)
                        .createdAt(exam.getCreatedAt())
                        .build())
                .toList();
        
        List<ResponseSchoolExamSummary> highList = highExams.stream()
                .map(exam -> ResponseSchoolExamSummary.builder()
                        .id(exam.getId())
                        .title(exam.getTitle())
                        .categoryName(exam.getCategory() != null ? exam.getCategory().getName() : null)
                        .createdAt(exam.getCreatedAt())
                        .build())
                .toList();
        
        ResponseSchoolExamLatest response = ResponseSchoolExamLatest.builder()
                .middle(middleList)
                .high(highList)
                .build();
        
        log.debug("[SchoolExamService] 최신 시험분석 조회 완료. 중등부={}, 고등부={}", 
                middleList.size(), highList.size());
        
        return ResponseData.ok("0000", "최신 시험분석 조회 성공", response);
    }

    /**
     * 카테고리 사용 여부 체크 (CategoryUsageChecker 구현).
     */
    @Override
    public boolean hasDataUsingCategory(Long categoryId) {
        long count = schoolExamRepository.countByCategoryId(categoryId);
        return count > 0;
    }

    /**
     * 도메인 이름 반환 (CategoryUsageChecker 구현).
     */
    @Override
    public String getDomainName() {
        return "학교별 시험분석";
    }

    // ===== Private Helper Methods =====

    /**
     * ID로 시험분석 조회.
     */
    private SchoolExam findSchoolExamById(Long id) {
        return schoolExamRepository.findByIdWithCategory(id)
                .orElseThrow(() -> {
                    log.warn("[SchoolExamService] 시험분석 미존재. ID={}", id);
                    return new BusinessException(ErrorCode.SCHOOL_EXAM_NOT_FOUND);
                });
    }

    /**
     * ID로 카테고리 조회.
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[SchoolExamService] 카테고리 미존재. ID={}", categoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }

    /**
     * 상세 응답 DTO 생성.
     */
    private ResponseSchoolExamDetail buildDetailResponse(SchoolExam schoolExam, boolean isPublicApi) {
        ResponseSchoolExamDetail response = schoolExamMapper.toDetailResponse(schoolExam);

        // 파일 정보 조회 (첨부파일과 본문 이미지)
        List<Object[]> attachmentData = uploadFileLinkRepository.findFileInfosByOwnerAndRole("school_exams", schoolExam.getId(), FileRole.ATTACHMENT);
        List<Object[]> inlineImageData = uploadFileLinkRepository.findFileInfosByOwnerAndRole("school_exams", schoolExam.getId(), FileRole.INLINE);
        
        List<ResponseFileInfo> attachments = attachmentData.stream()
                .map(this::mapToResponseFileInfo)
                .collect(Collectors.toList());
        
        List<ResponseFileInfo> inlineImages = inlineImageData.stream()
                .map(this::mapToResponseFileInfo)
                .collect(Collectors.toList());

        // 네비게이션 정보 조회 - 공개 API와 관리자 API 구분
        ResponseSchoolExamNavigation navigation = isPublicApi ? 
                buildPublicNavigation(schoolExam.getId()) : buildNavigation(schoolExam.getId());

        return ResponseSchoolExamDetail.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .schoolLevel(response.getSchoolLevel())
                .isPublished(response.getIsPublished())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .viewCount(response.getViewCount())
                .attachments(attachments)
                .inlineImages(inlineImages)
                .navigation(navigation)
                .createdBy(response.getCreatedBy())
                .createdByName(response.getCreatedByName())
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(response.getUpdatedByName())
                .updatedAt(response.getUpdatedAt())
                .build();
    }

    /**
     * 네비게이션 정보 생성 (관리자용 - 모든 시험분석).
     */
    private ResponseSchoolExamNavigation buildNavigation(Long currentId) {
        List<SchoolExam> previousList = schoolExamRepository.findPreviousSchoolExam(currentId, PageRequest.of(0, 1));
        List<SchoolExam> nextList = schoolExamRepository.findNextSchoolExam(currentId, PageRequest.of(0, 1));

        if (previousList.isEmpty() && nextList.isEmpty()) {
            return ResponseSchoolExamNavigation.empty();
        }

        if (!previousList.isEmpty() && nextList.isEmpty()) {
            SchoolExam previous = previousList.get(0);
            return ResponseSchoolExamNavigation.withPrevious(previous.getId(), previous.getTitle());
        }

        if (previousList.isEmpty() && !nextList.isEmpty()) {
            SchoolExam next = nextList.get(0);
            return ResponseSchoolExamNavigation.withNext(next.getId(), next.getTitle());
        }

        SchoolExam previous = previousList.get(0);
        SchoolExam next = nextList.get(0);
        return ResponseSchoolExamNavigation.withBoth(
                previous.getId(), previous.getTitle(),
                next.getId(), next.getTitle()
        );
    }

    /**
     * 네비게이션 정보 생성 (공개용 - 공개된 것만).
     */
    private ResponseSchoolExamNavigation buildPublicNavigation(Long currentId) {
        List<SchoolExam> previousList = schoolExamRepository.findPreviousPublicSchoolExam(currentId, PageRequest.of(0, 1));
        List<SchoolExam> nextList = schoolExamRepository.findNextPublicSchoolExam(currentId, PageRequest.of(0, 1));

        if (previousList.isEmpty() && nextList.isEmpty()) {
            return ResponseSchoolExamNavigation.empty();
        }

        if (!previousList.isEmpty() && nextList.isEmpty()) {
            SchoolExam previous = previousList.get(0);
            return ResponseSchoolExamNavigation.withPrevious(previous.getId(), previous.getTitle());
        }

        if (previousList.isEmpty() && !nextList.isEmpty()) {
            SchoolExam next = nextList.get(0);
            return ResponseSchoolExamNavigation.withNext(next.getId(), next.getTitle());
        }

        SchoolExam previous = previousList.get(0);
        SchoolExam next = nextList.get(0);
        return ResponseSchoolExamNavigation.withBoth(
                previous.getId(), previous.getTitle(),
                next.getId(), next.getTitle()
        );
    }

    /**
     * 상세 응답 DTO 생성 (학교급 필터 적용).
     */
    private ResponseSchoolExamDetail buildDetailResponseWithSchoolLevel(SchoolExam schoolExam, boolean isPublicApi, SchoolLevel schoolLevel) {
        ResponseSchoolExamDetail response = schoolExamMapper.toDetailResponse(schoolExam);

        // 파일 정보 조회 (첨부파일과 본문 이미지)
        List<Object[]> attachmentData = uploadFileLinkRepository.findFileInfosByOwnerAndRole("school_exams", schoolExam.getId(), FileRole.ATTACHMENT);
        List<Object[]> inlineImageData = uploadFileLinkRepository.findFileInfosByOwnerAndRole("school_exams", schoolExam.getId(), FileRole.INLINE);
        
        List<ResponseFileInfo> attachments = attachmentData.stream()
                .map(this::mapToResponseFileInfo)
                .collect(Collectors.toList());
        
        List<ResponseFileInfo> inlineImages = inlineImageData.stream()
                .map(this::mapToResponseFileInfo)
                .collect(Collectors.toList());

        // 네비게이션 정보 조회 - 공개 API와 관리자 API 구분, 학교급 필터 적용
        ResponseSchoolExamNavigation navigation = isPublicApi ? 
                buildPublicNavigationWithSchoolLevel(schoolExam.getId(), schoolLevel) : 
                buildNavigationWithSchoolLevel(schoolExam.getId(), schoolLevel);

        return ResponseSchoolExamDetail.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .schoolLevel(response.getSchoolLevel())
                .isPublished(response.getIsPublished())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .viewCount(response.getViewCount())
                .attachments(attachments)
                .inlineImages(inlineImages)
                .navigation(navigation)
                .createdBy(response.getCreatedBy())
                .createdByName(response.getCreatedByName())
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(response.getUpdatedByName())
                .updatedAt(response.getUpdatedAt())
                .build();
    }

    /**
     * 네비게이션 정보 생성 (관리자용, 학교급 필터 적용).
     */
    private ResponseSchoolExamNavigation buildNavigationWithSchoolLevel(Long currentId, SchoolLevel schoolLevel) {
        List<SchoolExam> previousList;
        List<SchoolExam> nextList;
        
        if (schoolLevel != null) {
            previousList = schoolExamRepository.findPreviousSchoolExamBySchoolLevel(currentId, schoolLevel, PageRequest.of(0, 1));
            nextList = schoolExamRepository.findNextSchoolExamBySchoolLevel(currentId, schoolLevel, PageRequest.of(0, 1));
        } else {
            previousList = schoolExamRepository.findPreviousSchoolExam(currentId, PageRequest.of(0, 1));
            nextList = schoolExamRepository.findNextSchoolExam(currentId, PageRequest.of(0, 1));
        }

        if (previousList.isEmpty() && nextList.isEmpty()) {
            return ResponseSchoolExamNavigation.empty();
        }

        if (!previousList.isEmpty() && nextList.isEmpty()) {
            SchoolExam previous = previousList.get(0);
            return ResponseSchoolExamNavigation.withPrevious(previous.getId(), previous.getTitle());
        }

        if (previousList.isEmpty() && !nextList.isEmpty()) {
            SchoolExam next = nextList.get(0);
            return ResponseSchoolExamNavigation.withNext(next.getId(), next.getTitle());
        }

        SchoolExam previous = previousList.get(0);
        SchoolExam next = nextList.get(0);
        return ResponseSchoolExamNavigation.withBoth(
                previous.getId(), previous.getTitle(),
                next.getId(), next.getTitle()
        );
    }

    /**
     * 네비게이션 정보 생성 (공개용, 학교급 필터 적용).
     */
    private ResponseSchoolExamNavigation buildPublicNavigationWithSchoolLevel(Long currentId, SchoolLevel schoolLevel) {
        List<SchoolExam> previousList;
        List<SchoolExam> nextList;
        
        if (schoolLevel != null) {
            previousList = schoolExamRepository.findPreviousPublicSchoolExamBySchoolLevel(currentId, schoolLevel, PageRequest.of(0, 1));
            nextList = schoolExamRepository.findNextPublicSchoolExamBySchoolLevel(currentId, schoolLevel, PageRequest.of(0, 1));
        } else {
            previousList = schoolExamRepository.findPreviousPublicSchoolExam(currentId, PageRequest.of(0, 1));
            nextList = schoolExamRepository.findNextPublicSchoolExam(currentId, PageRequest.of(0, 1));
        }

        if (previousList.isEmpty() && nextList.isEmpty()) {
            return ResponseSchoolExamNavigation.empty();
        }

        if (!previousList.isEmpty() && nextList.isEmpty()) {
            SchoolExam previous = previousList.get(0);
            return ResponseSchoolExamNavigation.withPrevious(previous.getId(), previous.getTitle());
        }

        if (previousList.isEmpty() && !nextList.isEmpty()) {
            SchoolExam next = nextList.get(0);
            return ResponseSchoolExamNavigation.withNext(next.getId(), next.getTitle());
        }

        SchoolExam previous = previousList.get(0);
        SchoolExam next = nextList.get(0);
        return ResponseSchoolExamNavigation.withBoth(
                previous.getId(), previous.getTitle(),
                next.getId(), next.getTitle()
        );
    }

    /**
     * 파일 링크 추가.
     */
    private Map<String, Long> addFileLinks(Long schoolExamId, List<FileReference> fileReferences, FileRole role) {
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[SchoolExamService] 연결할 {}파일 없음. schoolExamId={}", role, schoolExamId);
            return tempToFormalMap;
        }

        log.info("[SchoolExamService] {} 파일 연결 생성 시작. schoolExamId={}, 파일개수={}", 
                role, schoolExamId, fileReferences.size());

        // 임시 파일을 정식 파일로 변환
        for (FileReference fileRef : fileReferences) {
            String tempFileId = fileRef.getFileId();
            String originalFileName = fileRef.getFileName();
            
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, originalFileName);
            if (formalFileId != null) {
                tempToFormalMap.put(tempFileId, formalFileId);
                log.debug("[SchoolExamService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, originalName={}", 
                        tempFileId, formalFileId, originalFileName);
            } else {
                log.warn("[SchoolExamService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, originalName={}, role={}", 
                        tempFileId, originalFileName, role);
            }
        }

        // 성공한 변환들에 대해 파일 연결 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> {
                    if (role == FileRole.ATTACHMENT) {
                        return UploadFileLink.createSchoolExamAttachment(formalFileId, schoolExamId);
                    } else {
                        return UploadFileLink.createSchoolExamInlineImage(formalFileId, schoolExamId);
                    }
                })
                .toList();

        // DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[SchoolExamService] {} 파일 연결 생성 완료. schoolExamId={}, 요청={}개, 성공={}개", 
                role, schoolExamId, fileReferences.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * 선택된 파일 링크 삭제.
     */
    private void deleteSelectedFileLinks(Long schoolExamId, List<Long> fileIds, FileRole role) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[SchoolExamService] 삭제할 {}파일 없음. schoolExamId={}", role, schoolExamId);
            return;
        }

        log.info("[SchoolExamService] {} 파일 링크 삭제 시작. schoolExamId={}, 파일개수={}", 
                role, schoolExamId, fileIds.size());

        // 파일 링크 삭제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
                "school_exams", schoolExamId, role, fileIds);
        
        log.debug("[SchoolExamService] {} 파일 링크 삭제 실행. schoolExamId={}, 삭제대상IDs={}", 
                role, schoolExamId, fileIds);

        log.info("[SchoolExamService] {} 파일 링크 삭제 완료. schoolExamId={}, 대상파일={}개", 
                role, schoolExamId, fileIds.size());
    }

    /**
     * Object[] 데이터를 ResponseFileInfo로 변환.
     */
    private ResponseFileInfo mapToResponseFileInfo(Object[] row) {
        return ResponseFileInfo.builder()
                .fileId(String.valueOf(row[0]))  // Long을 String으로 변환
                .fileName((String) row[1])
                .originalName((String) row[2])
                .ext((String) row[3])
                .size((Long) row[4])
                .url((String) row[5])
                .build();
    }
}