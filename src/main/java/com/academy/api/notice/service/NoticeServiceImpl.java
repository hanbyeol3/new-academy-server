package com.academy.api.notice.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.domain.NoticeSearchType;
import com.academy.api.file.dto.FileReference;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticePublishedUpdate;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNoticeDetail;
import com.academy.api.notice.dto.ResponseNoticeAdminList;
import com.academy.api.notice.dto.ResponseNoticeNavigation;
import com.academy.api.notice.dto.ResponseNoticePublicList;
import com.academy.api.notice.mapper.NoticeMapper;
import com.academy.api.notice.repository.NoticeRepository;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공지사항 서비스 구현체.
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
 * - 공지사항 생성/수정 시 본문 이미지의 임시 URL을 정식 URL로 자동 변환
 * - 임시 URL: /api/public/files/temp/{tempId} → 정식 URL: /api/public/files/download/{formalId}
 * - 도메인 메서드를 통한 안전한 엔티티 상태 변경
 * 
 * 로깅 레벨 원칙:
 * - info: 주요 비즈니스 로직 시작점과 완료
 * - debug: 처리 단계별 상세 정보
 * - warn: 예상 가능한 예외 상황
 * - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService, CategoryUsageChecker {

    private final NoticeRepository noticeRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final NoticeMapper noticeMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final FileService fileService;

	/**
	 * [관리자] 공지사항 목록 조회 (모든 상태 포함).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID (null이면 전체 카테고리)
	 * @param isImportant 중요 공지 여부 (null이면 모든 상태)
	 * @param isPublished 공개 상태 (null이면 모든 상태)
	 * @param exposureType 노출 기간 유형 (null이면 모든 유형)
	 * @param sortBy 정렬 기준 (null이면 기본 정렬)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Override
    public ResponseList<ResponseNoticeAdminList> getNoticeListForAdmin(String keyword, String searchType, Long categoryId, Boolean isImportant, Boolean isPublished, String exposureType, String sortBy, Pageable pageable) {
        log.info("[NoticeService] 관리자용 공지사항 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isImportant={}, isPublished={}, exposureType={}, sortBy={}, 페이지={}", 
                keyword, searchType, categoryId, isImportant, isPublished, exposureType, sortBy, pageable);

        // searchType enum 변환
        NoticeSearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = NoticeSearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[NoticeService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = NoticeSearchType.ALL;
            }
        }
        
        // exposureType enum 변환
        ExposureType effectiveExposureType = null;
        if (exposureType != null) {
            try {
                effectiveExposureType = ExposureType.valueOf(exposureType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[NoticeService] 유효하지 않은 exposureType 무시. exposureType={}", exposureType);
            }
        }
        
        // ✅ 단일 경로: QueryDSL 통합 처리 (searchType 포함)
        Page<Notice> noticePage = noticeRepository.searchNoticesForAdmin(keyword, effectiveSearchType, categoryId, isImportant, isPublished, effectiveExposureType, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        List<Notice> notices = noticePage.getContent();
        
        log.debug("[NoticeService] 관리자 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), notices.size());
        
        // 회원 이름 포함하여 DTO 변환
        List<ResponseNoticeAdminList> items = notices.stream()
                .map(notice -> {
                    String createdByName = getMemberName(notice.getCreatedBy());
                    String updatedByName = getMemberName(notice.getUpdatedBy());
                    return ResponseNoticeAdminList.fromWithNames(notice, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(
                items,
                noticePage.getTotalElements(),
                noticePage.getNumber(),
                noticePage.getSize()
        );
    }

	/**
	 * 공개용 공지사항 목록 조회 (노출 가능한 것만).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID (null이면 전체 카테고리)
	 * @param isImportant 중요 공지 여부 (null이면 모든 상태)
	 * @param isPublished 공개 상태 (null이면 모든 상태)
	 * @param exposureType 노출 기간 유형 (null이면 모든 유형)
	 * @param sortBy 정렬 기준 (null이면 기본 정렬)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Override
    public ResponseList<ResponseNoticePublicList> getNoticeListForPublic(String keyword, String searchType, Long categoryId, Boolean isImportant, Boolean isPublished, String exposureType, String sortBy, Pageable pageable) {
        log.info("[NoticeService] 공개용 공지사항 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isImportant={}, isPublished={}, exposureType={}, sortBy={}, 페이지={}", 
                keyword, searchType, categoryId, isImportant, isPublished, exposureType, sortBy, pageable);

        // searchType enum 변환
        NoticeSearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = NoticeSearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[NoticeService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = NoticeSearchType.ALL;
            }
        }
        
        // exposureType enum 변환
        ExposureType effectiveExposureType = null;
        if (exposureType != null) {
            try {
                effectiveExposureType = ExposureType.valueOf(exposureType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[NoticeService] 유효하지 않은 exposureType 무시. exposureType={}", exposureType);
            }
        }
        
        Page<Notice> noticePage = noticeRepository.searchExposableNotices(keyword, effectiveSearchType, categoryId, isImportant, isPublished, effectiveExposureType, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        
        log.debug("[NoticeService] 공개 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    /**
     * 공지사항 상세 조회 (파일 목록 포함) - 관리자용 기본.
     * 
     * JOIN을 활용하여 첨부파일과 본문이미지 목록을 함께 조회합니다.
     * 파일 역할별로 분리하여 제공합니다.
     * 
     * @param id 공지사항 ID
     * @return 공지사항 상세 정보 (파일 목록 포함)
     */
    public ResponseData<ResponseNoticeDetail> getNoticeWithFiles(Long id) {
        return getNoticeWithFiles(id, false);
    }
    
    /**
     * 공지사항 상세 조회 (파일 목록 포함) - 공개/관리자 구분.
     * 
     * @param id 공지사항 ID
     * @param isPublicApi 공개 API 호출 여부
     * @return 공지사항 상세 정보 (파일 목록 포함)
     */
    public ResponseData<ResponseNoticeDetail> getNoticeWithFiles(Long id, boolean isPublicApi) {
        log.info("[NoticeService] 공지사항 상세 조회 (파일 포함) 시작. ID={}, isPublic={}", id, isPublicApi);
        
        Notice notice = findNoticeById(id);
        
        // 첨부파일 목록 조회
        log.info("[NoticeService] 첨부파일 조회 시작. ownerTable=notices, ownerId={}, role=ATTACHMENT", id);
        List<Object[]> attachmentData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "notices", id, FileRole.ATTACHMENT);
        log.info("[NoticeService] 첨부파일 쿼리 결과 개수: {}", attachmentData.size());
        
        if (!attachmentData.isEmpty()) {
            for (int i = 0; i < attachmentData.size(); i++) {
                Object[] row = attachmentData.get(i);
                log.info("[NoticeService] 첨부파일[{}] 원본데이터: fileId={}, fileName={}, originalName={}, ext={}, size={}, url={}", 
                        i, row[0], row[1], row[2], row[3], row[4], row[5]);
            }
        }
        
        List<ResponseFileInfo> attachments = attachmentData.stream()
                .map(this::mapToResponseFileInfo)
                .toList();
        
        // 본문 이미지 목록 조회  
        log.info("[NoticeService] 본문이미지 조회 시작. ownerTable=notices, ownerId={}, role=INLINE", id);
        List<Object[]> inlineImageData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "notices", id, FileRole.INLINE);
        log.info("[NoticeService] 본문이미지 쿼리 결과 개수: {}", inlineImageData.size());
        
        List<ResponseFileInfo> inlineImages = inlineImageData.stream()
                .map(this::mapToResponseFileInfo)
                .toList();
        
        log.info("[NoticeService] 공지사항 조회 완료. ID={}, 제목={}, 조회수={}, 첨부파일={}개, 본문이미지={}개", 
                id, notice.getTitle(), notice.getViewCount(), attachments.size(), inlineImages.size());
        
        // 회원 이름 조회
        String createdByName = getMemberName(notice.getCreatedBy());
        String updatedByName = getMemberName(notice.getUpdatedBy());
        
        // 이전글/다음글 조회 - 공개 API와 관리자 API 구분
        ResponseNoticeNavigation navigation = isPublicApi ? 
                getPublicNoticeNavigation(id) : getNoticeNavigation(id);
        
        // ResponseNotice 생성 (파일 목록 및 회원 이름 포함)
        ResponseNoticeDetail response = ResponseNoticeDetail.fromWithNames(notice, createdByName, updatedByName);
        
        // 파일 정보 및 네비게이션 정보 설정
        response = ResponseNoticeDetail.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .isImportant(response.getIsImportant())
                .isPublished(response.getIsPublished())
                .exposureType(response.getExposureType())
                .exposureStartAt(response.getExposureStartAt())
                .exposureEndAt(response.getExposureEndAt())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .viewCount(response.getViewCount())
                .attachments(attachments)
                .inlineImages(inlineImages)
                .exposable(response.getExposable())
                .navigation(navigation)
                .createdBy(response.getCreatedBy())
                .createdByName(response.getCreatedByName())
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(response.getUpdatedByName())
                .updatedAt(response.getUpdatedAt())
                .build();
        
        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<ResponseNoticeDetail> getNoticeForAdmin(Long id) {
        return getNoticeWithFiles(id);
    }

    @Override
    @Transactional
    public ResponseData<ResponseNoticeDetail> getNoticeForPublic(Long id) {
        log.info("[NoticeService] 공지사항 상세 조회 (조회수 증가) 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        Long beforeViewCount = notice.getViewCount();
        
        // 조회수 증가
        notice.incrementViewCount();
        
        log.debug("[NoticeService] 조회수 증가 완료. ID={}, 이전조회수={}, 현재조회수={}", 
                id, beforeViewCount, notice.getViewCount());
        
        // 파일 정보를 포함한 상세 조회 - 공개용 네비게이션 사용
        return getNoticeWithFiles(id, true);
    }


    /**
     * 공지사항 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 공지사항 ID
     */
    @Override
    @Transactional
    public ResponseData<Long> createNotice(RequestNoticeCreate request) {
        log.info("[NoticeService] 공지사항 생성 시작. 제목={}, 카테고리ID={}, 첨부파일={}개, 본문이미지={}개", 
                request.getTitle(), request.getCategoryId(), 
                request.getAttachmentFiles() != null ? request.getAttachmentFiles().size() : 0,
                request.getInlineImages() != null ? request.getInlineImages().size() : 0);
        
        // 카테고리 조회 (있는 경우만)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[NoticeService] 카테고리 조회 완료. ID={}, 카테고리명={}", 
                    request.getCategoryId(), category.getName());
        }
        
        // 공지사항 생성
        Notice notice = noticeMapper.toEntity(request, category);
        Notice savedNotice = noticeRepository.save(notice);
        Long noticeId = savedNotice.getId();
        
        // 파일 연결 처리 및 content URL 변환
        Map<String, Long> attachmentTempMap = createFileLinkFromTempFiles(noticeId, request.getAttachmentFiles(), FileRole.ATTACHMENT);
        Map<String, Long> inlineTempMap = createFileLinkFromTempFiles(noticeId, request.getInlineImages(), FileRole.INLINE);
        
        // content에서 임시 URL을 정식 URL로 변환 (본문 이미지만 해당)
        if (!inlineTempMap.isEmpty()) {
            String updatedContent = fileService.convertTempUrlsInContent(savedNotice.getContent(), inlineTempMap);
            if (!updatedContent.equals(savedNotice.getContent())) {
                // content가 변경된 경우 DB 업데이트
                savedNotice = noticeRepository.findById(noticeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
                
                // 도메인 메서드를 사용해서 content 업데이트
                savedNotice.updateContent(updatedContent);
                noticeRepository.save(savedNotice);
                log.info("[NoticeService] content 내 임시 URL 변환 완료. ID={}", noticeId);
            }
        }
        
        log.info("[NoticeService] 공지사항 생성 완료. ID={}, 제목={}", savedNotice.getId(), savedNotice.getTitle());
        
        return ResponseData.ok("0000", "공지사항이 생성되었습니다.", savedNotice.getId());
    }

    /**
     * 공지사항 수정 (파일 치환 포함).
     * 
     * 제공해주신 치환 정책을 적용합니다:
     * 1. 기존 파일 연결 삭제 (DELETE)
     * 2. 새로운 파일 연결 생성 (INSERT)
     * 
     * @param id 공지사항 ID
     * @param request 수정 요청 정보
     * @return 응답 정보
     */
    @Override
    @Transactional
    public ResponseData<ResponseNoticeDetail> updateNotice(Long id, RequestNoticeUpdate request) {
        log.info("🔄 [NoticeService] 공지사항 수정 시작!!! ID={}, " +
                "신규첨부파일={}개, 신규본문이미지={}개, 삭제첨부파일={}개, 삭제본문이미지={}개", 
                id,
                request.getNewAttachments() != null ? request.getNewAttachments().size() : 0,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0,
                request.getDeleteAttachmentFileIds() != null ? request.getDeleteAttachmentFileIds().size() : 0,
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0);
        
        Notice notice = findNoticeById(id);
        
        // 카테고리 변경 처리
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[NoticeService] 카테고리 변경. 기존={}, 신규={}", 
                    notice.getCategory() != null ? notice.getCategory().getName() : "없음", 
                    category.getName());
        }
        
        // 엔티티 업데이트
        noticeMapper.updateEntity(notice, request, category);
        
        // 선택적 파일 처리 (삭제 → 추가 순서)
        log.info("🔄 [NoticeService] 선택적 파일 처리 시작. " +
                "삭제 첨부파일={}개, 삭제 본문이미지={}개, 신규 첨부파일={}개, 신규 본문이미지={}개", 
                request.getDeleteAttachmentFileIds() != null ? request.getDeleteAttachmentFileIds().size() : 0,
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0,
                request.getNewAttachments() != null ? request.getNewAttachments().size() : 0,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0);
        
        // 1. 선택된 파일 삭제
        deleteSelectedFileLinks(id, request.getDeleteAttachmentFileIds(), FileRole.ATTACHMENT);
        deleteSelectedFileLinks(id, request.getDeleteInlineImageFileIds(), FileRole.INLINE);
        
        // 2. 새 파일 추가
        Map<String, Long> newAttachmentTempMap = addFileLinks(id, request.getNewAttachments(), FileRole.ATTACHMENT);
        Map<String, Long> newInlineTempMap = addFileLinks(id, request.getNewInlineImages(), FileRole.INLINE);
        
        
        // 4. 파일 처리 결과 로깅
        log.info("[NoticeService] 파일 처리 결과. ID={}, 새첨부파일={}개, 새이미지={}개", 
                id, newAttachmentTempMap.size(), newInlineTempMap.size());
        
        // 5. Content URL 완전 처리
        String finalContent = notice.getContent();
        
        // 5-1. 삭제된 이미지 URL 제거
        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            finalContent = fileService.removeDeletedImageUrlsFromContent(finalContent, request.getDeleteInlineImageFileIds());
            log.info("[NoticeService] 삭제된 이미지 URL 제거 완료. ID={}, 삭제된이미지={}개", 
                    id, request.getDeleteInlineImageFileIds().size());
        }
        
        // 5-2. 모든 temp URL을 정식 URL로 변환 (기존 + 신규 포함)
        String convertedContent = fileService.convertAllTempUrlsInContent(finalContent);
        
        // 5-3. Content가 변경된 경우 업데이트
        if (!convertedContent.equals(notice.getContent())) {
            // 엔티티 다시 조회하여 최신 상태 확보
            Notice currentNotice = noticeRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
            
            // 도메인 메서드를 사용해서 content 업데이트
            currentNotice.updateContent(convertedContent);
            noticeRepository.save(currentNotice);
            log.info("[NoticeService] Content URL 완전 변환 완료. ID={}, 최종content길이={}", 
                    id, convertedContent.length());
        }
        
        log.info("[NoticeService] 공지사항 수정 완료. ID={}, 제목={}", id, notice.getTitle());
        
        // 6. 완전한 공지사항 정보 반환 (파일 정보 포함)
        ResponseNoticeDetail updatedNotice = getNoticeWithFiles(id).getData();
        
        return ResponseData.ok("0000", "공지사항이 수정되었습니다.", updatedNotice);
    }

    /**
     * 공지사항 삭제.
     * 
     * @param id 삭제할 공지사항 ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public Response deleteNotice(Long id) {
        log.info("[NoticeService] 공지사항 삭제 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        String title = notice.getTitle();
        
        noticeRepository.delete(notice);
        
        log.info("[NoticeService] 공지사항 삭제 완료. ID={}, 제목={}", id, title);
        
        return Response.ok("0000", "공지사항이 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response incrementViewCount(Long id) {
        log.info("[NoticeService] 조회수 증가 시작. ID={}", id);
        
        int updatedCount = noticeRepository.incrementViewCount(id);
        if (updatedCount == 0) {
            log.warn("[NoticeService] 조회수 증가 실패 - 공지사항을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        
        log.debug("[NoticeService] 조회수 증가 완료. ID={}", id);
        
        return Response.ok("0000", "조회수가 증가되었습니다.");
    }

    @Override
    @Transactional
    public Response toggleImportant(Long id, Boolean isImportant) {
        log.info("[NoticeService] 중요 공지 상태 변경 시작. ID={}, 중요공지={}", id, isImportant);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        int updatedCount = noticeRepository.updateImportantStatus(id, isImportant, currentUserId);
        if (updatedCount == 0) {
            log.warn("[NoticeService] 중요 공지 상태 변경 실패 - 공지사항을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        
        log.info("[NoticeService] 중요 공지 상태 변경 완료. ID={}, 중요공지={}", id, isImportant);
        
        String message = isImportant ? "중요 공지로 설정되었습니다." : "중요 공지가 해제되었습니다.";
        return Response.ok("0000", message);
    }

    @Override
    @Transactional
    public Response togglePublished(Long id, Boolean isPublished) {
        log.info("[NoticeService] 공개 상태 변경 시작. ID={}, 공개여부={}", id, isPublished);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 비공개 → 공개 변경 시 특별 처리를 위해 엔티티를 조회
        if (isPublished) {
            Notice notice = findNoticeById(id);
            notice.togglePublished();
            // 변경사항을 저장하고 updatedBy 설정을 위해 Repository 업데이트 호출
            noticeRepository.save(notice);
            noticeRepository.updatePublishedStatus(id, isPublished, currentUserId);
            log.debug("[NoticeService] 공개 상태 변경 (특별 처리 포함). ID={}, 노출타입={}, 수정자ID={}", 
                    id, notice.getExposureType(), currentUserId);
        } else {
            int updatedCount = noticeRepository.updatePublishedStatus(id, isPublished, currentUserId);
            if (updatedCount == 0) {
                log.warn("[NoticeService] 공개 상태 변경 실패 - 공지사항을 찾을 수 없음. ID={}", id);
                throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
            }
        }
        
        log.info("[NoticeService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, isPublished);
        
        String message = isPublished ? "공지사항이 공개되었습니다." : "공지사항이 비공개되었습니다.";
        return Response.ok("0000", message);
    }

    @Override
    @Transactional
    public Response updateNoticePublished(Long id, RequestNoticePublishedUpdate request) {
        log.info("[NoticeService] 공개 상태 변경 (영구 게시 옵션 포함) 시작. ID={}, 공개여부={}, 상시게시={}", 
                id, request.getIsPublished(), request.getMakePermanent());
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Notice notice = findNoticeById(id);
        
        if (request.getIsPublished()) {
            // 공개로 변경
            notice.setPublished(true);
            
            // makePermanent가 true인 경우 상시 게시로 설정
            if (Boolean.TRUE.equals(request.getMakePermanent())) {
                notice.setExposureTypeToAlways();
                log.debug("[NoticeService] 상시 게시로 설정. ID={}, exposureType=ALWAYS", id);
            }
            
            // Repository를 통해 updatedBy 필드 업데이트
            noticeRepository.updatePublishedStatus(id, true, currentUserId);
            
            log.info("[NoticeService] 공개 상태 변경 완료. ID={}, 공개여부={}, 노출타입={}, 상시게시요청={}", 
                    id, true, notice.getExposureType(), request.getMakePermanent());
            
            // 응답 메시지 분기
            String message = Boolean.TRUE.equals(request.getMakePermanent()) ? 
                "공지사항이 공개되었고 게시기간이 상시로 설정되었습니다." : 
                "공지사항이 공개로 변경되었습니다.";
                
            return Response.ok("0000", message);
        } else {
            // 비공개로 변경 (makePermanent는 무시)
            int updatedCount = noticeRepository.updatePublishedStatus(id, false, currentUserId);
            if (updatedCount == 0) {
                log.warn("[NoticeService] 공개 상태 변경 실패 - 공지사항을 찾을 수 없음. ID={}", id);
                throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
            }
            
            log.info("[NoticeService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, false);
            
            return Response.ok("0000", "공지사항이 비공개로 변경되었습니다.");
        }
    }

    @Override
    public ResponseList<ResponseNoticePublicList> getRecentNotices(int limit) {
        log.info("[NoticeService] 최근 공지사항 조회 시작. 개수={}", limit);
        
        List<Notice> notices = noticeRepository.findRecentNotices(limit);
        List<ResponseNoticePublicList> response = noticeMapper.toSimpleResponseList(notices);
        
        log.debug("[NoticeService] 최근 공지사항 조회 완료. 반환개수={}", response.size());
        
        return ResponseList.ok(response, response.size(), 0, response.size());
    }

    @Override
    public ResponseData<List<Object[]>> getNoticeStatsByCategory() {
        log.info("[NoticeService] 카테고리별 공지사항 통계 조회 시작");
        
        List<Object[]> stats = noticeRepository.getNoticeStatsByCategory();
        
        log.debug("[NoticeService] 카테고리별 통계 조회 완료. 카테고리수={}", stats.size());
        
        return ResponseData.ok(stats);
    }

    /**
     * 공지사항 조회 도우미 메서드.
     */
    private Notice findNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[NoticeService] 공지사항을 찾을 수 없음. ID={}", id);
                    return new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
                });
    }

    /**
     * 카테고리 조회 도우미 메서드.
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[NoticeService] 카테고리를 찾을 수 없음. ID={}", categoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }

    /**
     * 회원 이름 조회 도우미 메서드.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return "Unknown";
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }

    /**
     * 이전글/다음글 네비게이션 정보 조회 도우미 메서드.
     */
    /**
     * 관리자용 네비게이션 정보 조회 (모든 공지사항 대상).
     */
    private ResponseNoticeNavigation getNoticeNavigation(Long currentId) {
        log.debug("[NoticeService] 관리자용 네비게이션 정보 조회 시작. currentId={}", currentId);
        
        // 이전글 조회 (모든 공지사항)
        Notice previousNotice = noticeRepository.findPreviousNotice(currentId);
        ResponseNoticeNavigation.NavigationItem previous = null;
        if (previousNotice != null) {
            previous = ResponseNoticeNavigation.NavigationItem.builder()
                    .id(previousNotice.getId())
                    .title(previousNotice.getTitle())
                    .createdAt(previousNotice.getCreatedAt())
                    .build();
            log.debug("[NoticeService] 이전글 조회 완료. previousId={}, title={}", 
                    previousNotice.getId(), previousNotice.getTitle());
        }
        
        // 다음글 조회 (모든 공지사항)
        Notice nextNotice = noticeRepository.findNextNotice(currentId);
        ResponseNoticeNavigation.NavigationItem next = null;
        if (nextNotice != null) {
            next = ResponseNoticeNavigation.NavigationItem.builder()
                    .id(nextNotice.getId())
                    .title(nextNotice.getTitle())
                    .createdAt(nextNotice.getCreatedAt())
                    .build();
            log.debug("[NoticeService] 다음글 조회 완료. nextId={}, title={}", 
                    nextNotice.getId(), nextNotice.getTitle());
        }
        
        ResponseNoticeNavigation navigation = ResponseNoticeNavigation.of(previous, next);
        log.debug("[NoticeService] 관리자용 네비게이션 정보 조회 완료. hasPrevious={}, hasNext={}", 
                previous != null, next != null);
        
        return navigation;
    }
    
    /**
     * 공개용 네비게이션 정보 조회 (공개된 공지사항만 대상).
     */
    private ResponseNoticeNavigation getPublicNoticeNavigation(Long currentId) {
        log.debug("[NoticeService] 공개용 네비게이션 정보 조회 시작. currentId={}", currentId);
        
        // 이전글 조회 (공개된 것만)
        Notice previousNotice = noticeRepository.findPreviousPublicNotice(currentId);
        ResponseNoticeNavigation.NavigationItem previous = null;
        if (previousNotice != null) {
            previous = ResponseNoticeNavigation.NavigationItem.builder()
                    .id(previousNotice.getId())
                    .title(previousNotice.getTitle())
                    .createdAt(previousNotice.getCreatedAt())
                    .build();
            log.debug("[NoticeService] 공개 이전글 조회 완료. previousId={}, title={}", 
                    previousNotice.getId(), previousNotice.getTitle());
        }
        
        // 다음글 조회 (공개된 것만)
        Notice nextNotice = noticeRepository.findNextPublicNotice(currentId);
        ResponseNoticeNavigation.NavigationItem next = null;
        if (nextNotice != null) {
            next = ResponseNoticeNavigation.NavigationItem.builder()
                    .id(nextNotice.getId())
                    .title(nextNotice.getTitle())
                    .createdAt(nextNotice.getCreatedAt())
                    .build();
            log.debug("[NoticeService] 공개 다음글 조회 완료. nextId={}, title={}", 
                    nextNotice.getId(), nextNotice.getTitle());
        }
        
        ResponseNoticeNavigation navigation = ResponseNoticeNavigation.of(previous, next);
        log.debug("[NoticeService] 공개용 네비게이션 정보 조회 완료. hasPrevious={}, hasNext={}", 
                previous != null, next != null);
        
        return navigation;
    }

    /**
     * Object[] 데이터를 ResponseFileInfo로 변환하는 도우미 메서드.
     * 
     * @param row [fileId, fileName, originalName, ext, size, url] 배열
     * @return ResponseFileInfo 인스턴스
     */
    private ResponseFileInfo mapToResponseFileInfo(Object[] row) {
        return ResponseFileInfo.builder()
                .fileId(String.valueOf(row[0]))  // Long을 String으로 변환
                .fileName((String) row[1])
                .originalName((String) row[2])   // 원본 파일명 추가
                .ext((String) row[3])
                .size((Long) row[4])
                .url((String) row[5])
                .build();
    }

    /**
     * 파일 연결 생성 및 임시 파일을 정식 파일로 승격.
     * 
     * 임시 파일을 정식 파일로 변환하고 UploadFileLink를 생성하여 공지사항과 연결합니다.
     * Content URL 변환을 위한 임시-정식 파일 ID 매핑을 반환합니다.
     * 
     * @param noticeId 공지사항 ID
     * @param fileReferences 파일 참조 목록 (파일ID + 원본명)
     * @param role 파일 역할 (ATTACHMENT 또는 INLINE)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinks(Long noticeId, List<FileReference> fileReferences, FileRole role) {
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[NoticeService] 연결할 {}파일 없음. noticeId={}", role, noticeId);
            return tempToFormalMap;
        }

        log.info("[NoticeService] {} 파일 연결 생성 시작. noticeId={}, 파일개수={}", role, noticeId, fileReferences.size());

        // 이미 선언된 tempToFormalMap 사용
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환 (원본명 포함)
        for (FileReference fileRef : fileReferences) {
            String tempFileId = fileRef.getFileId();
            String originalFileName = fileRef.getFileName();
            
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, originalFileName);
            if (formalFileId != null) {
                tempToFormalMap.put(tempFileId, formalFileId);
                log.debug("[NoticeService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, originalName={}", 
                        tempFileId, formalFileId, originalFileName);
            } else {
                log.warn("[NoticeService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, originalName={}, role={}", 
                        tempFileId, originalFileName, role);
            }
        }

        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> {
                    if (role == FileRole.ATTACHMENT) {
                        return UploadFileLink.createNoticeAttachment(formalFileId, noticeId);
                    } else {
                        return UploadFileLink.createNoticeInlineImage(formalFileId, noticeId);
                    }
                })
                .toList();

        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[NoticeService] {} 파일 연결 생성 완료. noticeId={}, 요청={}개, 성공={}개", 
                role, noticeId, fileReferences.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * 임시파일 정보를 기반으로 파일 연결 생성 (새로운 방식).
     * 
     * @param noticeId 공지사항 ID
     * @param tempFileInfos 임시파일 정보 목록 (tempFileId + fileName)
     * @param role 파일 역할 (ATTACHMENT 또는 INLINE)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinkFromTempFiles(Long noticeId, List<?> tempFileInfos, FileRole role) {
        log.info("🔥 [NoticeService] createFileLinkFromTempFiles 호출됨!!! noticeId={}, role={}, tempFileInfos={}", 
                noticeId, role, tempFileInfos);
        
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (tempFileInfos == null || tempFileInfos.isEmpty()) {
            log.info("⚠️ [NoticeService] 연결할 {}파일 없음. noticeId={}", role, noticeId);
            return tempToFormalMap;
        }
        
        log.info("🚀 [NoticeService] {} 파일 연결 생성 시작. noticeId={}, 파일개수={}", role, noticeId, tempFileInfos.size());
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환
        for (Object tempFileInfo : tempFileInfos) {
            String tempFileId = null;
            String fileName = null;
            
            // 타입에 따라 처리
            if (tempFileInfo instanceof RequestNoticeCreate.AttachmentFileInfo) {
                RequestNoticeCreate.AttachmentFileInfo info = (RequestNoticeCreate.AttachmentFileInfo) tempFileInfo;
                tempFileId = info.getTempFileId();
                fileName = info.getFileName();
            } else if (tempFileInfo instanceof RequestNoticeCreate.InlineImageInfo) {
                RequestNoticeCreate.InlineImageInfo info = (RequestNoticeCreate.InlineImageInfo) tempFileInfo;
                tempFileId = info.getTempFileId();
                fileName = info.getFileName();
            }
            
            if (tempFileId != null) {
                Long formalFileId = fileService.promoteToFormalFile(tempFileId, fileName);
                if (formalFileId != null) {
                    tempToFormalMap.put(tempFileId, formalFileId);
                    log.debug("[NoticeService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, fileName={}", 
                            tempFileId, formalFileId, fileName);
                } else {
                    log.warn("[NoticeService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, fileName={}, role={}", 
                            tempFileId, fileName, role);
                }
            }
        }
        
        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> {
                    if (role == FileRole.ATTACHMENT) {
                        return UploadFileLink.createNoticeAttachment(formalFileId, noticeId);
                    } else {
                        return UploadFileLink.createNoticeInlineImage(formalFileId, noticeId);
                    }
                })
                .toList();
                
        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[NoticeService] {} 파일 연결 생성 완료. noticeId={}, 요청={}개, 성공={}개", 
                role, noticeId, tempFileInfos.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * fileId에서 원본 파일명을 추출.
     * 임시 파일에서 원본 파일명 정보를 가져옵니다.
     */
    private String extractOriginalFileName(String fileId) {
        try {
            // 파일 정보 조회를 통해 원본 파일명 획득
            var fileInfoResponse = fileService.getFileInfo(fileId);
            if (fileInfoResponse.getData() != null) {
                return fileInfoResponse.getData().getOriginalFileName();
            }
        } catch (Exception e) {
            log.warn("[NoticeService] 파일 정보 조회 실패. fileId={}, error={}", fileId, e.getMessage());
        }
        
        // 실패 시 기본값
        return fileId + ".tmp";
    }

    /**
     * 선택된 파일 연결 삭제.
     * 
     * @param noticeId 공지사항 ID
     * @param fileIds 삭제할 파일 ID 목록
     * @param role 파일 역할
     */
    private void deleteSelectedFileLinks(Long noticeId, List<Long> fileIds, FileRole role) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[NoticeService] 삭제할 {} 파일 없음. noticeId={}", role, noticeId);
            return;
        }
        
        log.info("🗑️ [NoticeService] {} 파일 선택 삭제 실행. noticeId={}, 삭제파일={}개", 
                role, noticeId, fileIds.size());
        
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
                "notices", noticeId, role, fileIds);
        
        log.debug("[NoticeService] {} 파일 선택 삭제 완료. noticeId={}, 삭제된파일IDs={}", 
                role, noticeId, fileIds);
    }

    /**
     * 새 파일 추가 (기존 파일은 유지).
     * 
     * 기존 파일들은 그대로 유지하고 새로운 파일만 추가합니다.
     * 내부적으로 createFileLinks를 호출하여 임시-정식 파일 ID 매핑을 반환합니다.
     * 
     * @param noticeId 공지사항 ID
     * @param fileReferences 새로 추가할 파일 참조 목록 (파일ID + 원본명)
     * @param role 파일 역할 (ATTACHMENT 또는 INLINE)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> addFileLinks(Long noticeId, List<FileReference> fileReferences, FileRole role) {
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[NoticeService] 추가할 {} 파일 없음. noticeId={}", role, noticeId);
            return new HashMap<>();
        }
        
        log.info("➕ [NoticeService] {} 파일 추가 실행. noticeId={}, 추가파일={}개", 
                role, noticeId, fileReferences.size());
        
        // 기존 createFileLinks 메서드 재사용하여 임시-정식 ID 매핑 반환
        Map<String, Long> tempToFormalMap = createFileLinks(noticeId, fileReferences, role);
        
        log.debug("[NoticeService] {} 파일 추가 완료. noticeId={}, 추가된파일={}개", 
                role, noticeId, fileReferences.size());
        
        return tempToFormalMap;
    }

    
    // ================== CategoryUsageChecker 구현 ==================
    
    @Override
    public boolean hasDataUsingCategory(Long categoryId) {
        long noticeCount = noticeRepository.countByCategoryId(categoryId);
        
        log.debug("[NoticeService] 카테고리 사용 확인. categoryId={}, 공지사항수={}", 
                categoryId, noticeCount);
        
        return noticeCount > 0;
    }
    
    @Override
    public String getDomainName() {
        return "공지사항";
    }
}