package com.academy.api.gallery.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.gallery.repository.GalleryRepository;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.domain.Gallery;
import com.academy.api.gallery.domain.GallerySearchType;
import com.academy.api.file.dto.FileReference;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryPublishedUpdate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.dto.ResponseGalleryDetail;
import com.academy.api.gallery.dto.ResponseGalleryAdminList;
import com.academy.api.gallery.dto.ResponseGalleryNavigation;
import com.academy.api.gallery.dto.ResponseGalleryPublicList;
import com.academy.api.gallery.mapper.GalleryMapper;
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
 * 갤러리 서비스 구현체.
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
 * - 갤러리 생성/수정 시 본문 이미지의 임시 URL을 정식 URL로 자동 변환
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
public class GalleryServiceImpl implements GalleryService, CategoryUsageChecker {

    private final GalleryRepository galleryRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final GalleryMapper galleryMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final FileService fileService;

	/**
	 * [관리자] 갤러리 목록 조회 (모든 상태 포함).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID (null이면 전체 카테고리)
	 * @param isPublished 공개 상태 (null이면 모든 상태)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Override
    public ResponseList<ResponseGalleryAdminList> getGalleryListForAdmin(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable) {
        log.info("[GalleryService] 관리자용 갤러리 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}, 페이지={}",
                keyword, searchType, categoryId, isPublished, sortBy, pageable);

        // searchType enum 변환
        GallerySearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = GallerySearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[GalleryService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = GallerySearchType.ALL;
            }
        }
        
        // ✅ 단일 경로: QueryDSL 통합 처리 (searchType 포함)
        Page<Gallery> galleryPage = galleryRepository.searchGalleriesForAdmin(keyword, effectiveSearchType, categoryId, isPublished, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        List<Gallery> galleries = galleryPage.getContent();
        
        log.debug("[GalleryService] 관리자 갤러리 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건",
                galleryPage.getTotalElements(), galleryPage.getNumber(), galleries.size());
        
        // 회원 이름 및 커버 이미지 정보 포함하여 DTO 변환
        List<ResponseGalleryAdminList> items = galleries.stream()
                .map(gallery -> {
                    String createdByName = getMemberName(gallery.getCreatedBy());
                    String updatedByName = getMemberName(gallery.getUpdatedBy());
                    
                    // 커버 이미지 정보 조회
                    ResponseFileInfo coverImage = getCoverImage(gallery.getId());
                    
                    return ResponseGalleryAdminList.fromWithNames(gallery, createdByName, updatedByName, coverImage);
                })
                .toList();
        
        return ResponseList.ok(
                items,
                galleryPage.getTotalElements(),
                galleryPage.getNumber(),
                galleryPage.getSize()
        );
    }

	/**
	 * [공개] 갤러리 목록 조회 (노출 가능한 것만).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID (null이면 전체 카테고리)
	 * @param isPublished 공개 상태 (null이면 모든 상태)
	 * @param sortBy 정렬 기준 (null이면 기본 정렬)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Override
    public ResponseList<ResponseGalleryPublicList> getGalleryListForPublic(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable) {
        log.info("[GalleryService] 공개용 갤러리 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}, 페이지={}",
                keyword, searchType, categoryId, isPublished, sortBy, pageable);

        // searchType enum 변환
        GallerySearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = GallerySearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[GalleryService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = GallerySearchType.ALL;
            }
        }
        
        Page<Gallery> galleryPage = galleryRepository.searchGalleriesForPublic(keyword, effectiveSearchType, categoryId, isPublished, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        
        log.debug("[GalleryService] 공개 갤러리 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건",
                galleryPage.getTotalElements(), galleryPage.getNumber(), galleryPage.getContent().size());
        
        return galleryMapper.toSimpleResponseList(galleryPage);
    }

    /**
     * 갤러리 상세 조회 (파일 목록 포함).
     * 
     * JOIN을 활용하여 첨부파일과 본문이미지 목록을 함께 조회합니다.
     * 파일 역할별로 분리하여 제공합니다.
     * 
     * @param id 갤러리 ID
     * @return 갤러리 상세 정보 (파일 목록 포함)
     */
    public ResponseData<ResponseGalleryDetail> getGalleryWithFiles(Long id) {
        log.info("[GalleryService] 갤러리 상세 조회 (파일 포함) 시작. ID={}", id);
        
        Gallery gallery = findGalleryById(id);
        
        // 커버이미지 조회
        ResponseFileInfo coverImage = getCoverImage(id);
        
        // 본문 이미지 목록 조회  
        log.debug("[GalleryService] 본문이미지 조회 시작. ownerTable=gallery, ownerId={}, role=INLINE", id);
        List<Object[]> inlineImageData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "gallery", id, FileRole.INLINE);
        log.info("[GalleryService] 본문이미지 쿼리 결과 개수: {}", inlineImageData.size());
        
        List<ResponseFileInfo> inlineImages = inlineImageData.stream()
                .map(this::mapToResponseFileInfo)
                .toList();
        
        log.info("[GalleryService] 갤러리 조회 완료. ID={}, 제목={}, 조회수={}, 커버이미지={}, 본문이미지={}개",
                id, gallery.getTitle(), gallery.getViewCount(), coverImage != null, inlineImages.size());
        
        // 회원 이름 조회
        String createdByName = getMemberName(gallery.getCreatedBy());
        String updatedByName = getMemberName(gallery.getUpdatedBy());
        
        // 이전글/다음글 조회
        ResponseGalleryNavigation navigation = getGalleryNavigation(id);
        
        // ResponseGallery 생성 (파일 목록 및 회원 이름 포함)
        ResponseGalleryDetail response = ResponseGalleryDetail.fromWithNames(gallery, createdByName, updatedByName);
        
        // 파일 정보 및 네비게이션 정보 설정
        response = ResponseGalleryDetail.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .isPublished(response.getIsPublished())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .viewCount(response.getViewCount())
                .coverImage(coverImage)
                .inlineImages(inlineImages)
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

	/**
	 * [공개] 갤러리 상세 조회 (조회수 증가)
	 *
	 * @param id 갤러리 ID
	 * @return 갤러리 상세 정보
	 */
    @Override
    public ResponseData<ResponseGalleryDetail> getGalleryForAdmin(Long id) {
        return getGalleryWithFiles(id);
    }

    @Override
    @Transactional
    public ResponseData<ResponseGalleryDetail> getGalleryForPublic(Long id) {
        log.info("[GalleryService] 갤러리 상세 조회 (조회수 증가) 시작. ID={}", id);
        
        Gallery gallery = findGalleryById(id);
        Long beforeViewCount = gallery.getViewCount();
        
        // 조회수 증가
        Long currentUserId = SecurityUtils.getCurrentUserId();
        gallery.incrementViewCount(currentUserId);
        
        log.debug("[GalleryService] 조회수 증가 완료. ID={}, 이전조회수={}, 현재조회수={}",
                id, beforeViewCount, gallery.getViewCount());
        
        // 파일 정보를 포함한 상세 조회
        return getGalleryWithFiles(id);
    }


    /**
     * 갤러리 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 갤러리 ID
     */
    @Override
    @Transactional
    public ResponseData<Long> createGallery(RequestGalleryCreate request) {
        log.info("[GalleryService] 갤러리 생성 시작. 제목={}, 카테고리ID={}, coverImageTempFileId={}, 본문이미지={}개",
                request.getTitle(), request.getCategoryId(),
		        request.getCoverImageTempFileId(),
                request.getInlineImages() != null ? request.getInlineImages().size() : 0);
        
        // 카테고리 조회 (있는 경우만)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[GalleryService] 카테고리 조회 완료. ID={}, 카테고리명={}",
                    request.getCategoryId(), category.getName());
        }
        
        // 갤러리 생성
        Gallery gallery = galleryMapper.toEntity(request, category);
        Gallery savedGallery = galleryRepository.save(gallery);
        Long galleryId = savedGallery.getId();
        
        // 커버이미지 처리
        if (request.getCoverImageTempFileId() != null) {
            createCoverImageLink(galleryId, request.getCoverImageTempFileId(), request.getCoverImageFileName());
        }
        
        // 본문이미지 연결 처리 및 content URL 변환
        Map<String, Long> inlineTempMap = createFileLinkFromTempFiles(galleryId, request.getInlineImages(), FileRole.INLINE);
        
        // content에서 임시 URL을 정식 URL로 변환 (본문 이미지만 해당)
        if (!inlineTempMap.isEmpty()) {
            String updatedContent = fileService.convertTempUrlsInContent(savedGallery.getContent(), inlineTempMap);
            if (!updatedContent.equals(savedGallery.getContent())) {
                // content가 변경된 경우 DB 업데이트
                savedGallery = galleryRepository.findById(galleryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.GALLERY_NOT_FOUND));
                
                // 도메인 메서드를 사용해서 content 업데이트
                savedGallery.updateContent(updatedContent);
                galleryRepository.save(savedGallery);
                log.info("[GalleryService] content 내 임시 URL 변환 완료. ID={}", galleryId);
            }
        }
        
        log.info("[GalleryService] 갤러리 생성 완료. ID={}, 제목={}", savedGallery.getId(), savedGallery.getTitle());
        
        return ResponseData.ok("0000", "갤러리이 생성되었습니다.", savedGallery.getId());
    }

    /**
     * 갤러리 수정 (파일 치환 포함).
     * 
     * 제공해주신 치환 정책을 적용합니다:
     * 1. 기존 파일 연결 삭제 (DELETE)
     * 2. 새로운 파일 연결 생성 (INSERT)
     * 
     * @param id 갤러리 ID
     * @param request 수정 요청 정보
     * @return 응답 정보
     */
    @Override
    @Transactional
    public ResponseData<ResponseGalleryDetail> updateGallery(Long id, RequestGalleryUpdate request) {
        log.info("[GalleryService] 갤러리 수정 시작. ID={}, " +
                "신규본문이미지={}개, 삭제본문이미지={}개, 커버이미지삭제={}", 
                id,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0,
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0,
                request.getDeleteCoverImage());
        
        Gallery gallery = findGalleryById(id);
        
        // 카테고리 변경 처리
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[GalleryService] 카테고리 변경. 기존={}, 신규={}",
                    gallery.getCategory() != null ? gallery.getCategory().getName() : "없음",
                    category.getName());
        }
        
        // 엔티티 업데이트
        galleryMapper.updateEntity(gallery, request, category);
        
        // 커버이미지 처리
        handleCoverImageUpdate(id, request);
        
        // 본문이미지 선택적 처리 (삭제 → 추가 순서)
        log.debug("[GalleryService] 본문이미지 처리 시작. " +
                "삭제본문이미지={}개, 신규본문이미지={}개", 
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0);
        
        // 1. 선택된 본문이미지 삭제
        deleteSelectedFileLinks(id, request.getDeleteInlineImageFileIds(), FileRole.INLINE);
        
        // 2. 새 본문이미지 추가
        Map<String, Long> newInlineTempMap = createFileLinks(id, request.getNewInlineImages(), FileRole.INLINE);
        
        // 3. 파일 처리 결과 로깅
        log.info("[GalleryService] 본문이미지 처리 결과. ID={}, 새이미지={}개", id, newInlineTempMap.size());
        
        // 5. Content URL 완전 처리
        String finalContent = gallery.getContent();
        
        // 5-1. 삭제된 이미지 URL 제거
        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            finalContent = fileService.removeDeletedImageUrlsFromContent(finalContent, request.getDeleteInlineImageFileIds());
            log.info("[GalleryService] 삭제된 이미지 URL 제거 완료. ID={}, 삭제된이미지={}개",
                    id, request.getDeleteInlineImageFileIds().size());
        }
        
        // 5-2. 모든 temp URL을 정식 URL로 변환 (기존 + 신규 포함)
        String convertedContent = fileService.convertAllTempUrlsInContent(finalContent);
        
        // 5-3. Content가 변경된 경우 업데이트
        if (!convertedContent.equals(gallery.getContent())) {
            // 엔티티 다시 조회하여 최신 상태 확보
            Gallery currentGallery = galleryRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GALLERY_NOT_FOUND));
            
            // 도메인 메서드를 사용해서 content 업데이트
            currentGallery.updateContent(convertedContent);
            galleryRepository.save(currentGallery);
            log.info("[GalleryService] Content URL 완전 변환 완료. ID={}, 최종content길이={}",
                    id, convertedContent.length());
        }
        
        log.info("[GalleryService] 갤러리 수정 완료. ID={}, 제목={}", id, gallery.getTitle());
        
        // 6. 완전한 갤러리 정보 반환 (파일 정보 포함)
        ResponseGalleryDetail updatedGallery = getGalleryWithFiles(id).getData();
        
        return ResponseData.ok("0000", "갤러리이 수정되었습니다.", updatedGallery);
    }

    /**
     * 갤러리 삭제.
     * 
     * @param id 삭제할 갤러리 ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public Response deleteGallery(Long id) {
        log.info("[GalleryService] 갤러리 삭제 시작. ID={}", id);
        
        Gallery gallery = findGalleryById(id);
        String title = gallery.getTitle();
        
        galleryRepository.delete(gallery);
        
        log.info("[GalleryService] 갤러리 삭제 완료. ID={}, 제목={}", id, title);
        
        return Response.ok("0000", "갤러리이 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response incrementViewCount(Long id) {
        log.info("[GalleryService] 조회수 증가 시작. ID={}", id);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        int updatedCount = galleryRepository.incrementViewCount(id, currentUserId);
        if (updatedCount == 0) {
            log.warn("[GalleryService] 조회수 증가 실패 - 갤러리을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.GALLERY_NOT_FOUND);
        }

        log.debug("[GalleryService] 조회수 증가 완료. ID={}, updatedBy={}", id, currentUserId);
        
        return Response.ok("0000", "조회수가 증가되었습니다.");
    }

    @Override
    @Transactional
    public Response updateGalleryPublished(Long id, RequestGalleryPublishedUpdate request) {
        log.info("[GalleryService] 공개 상태 변경 시작. ID={}, 공개여부={}",
                id, request.getIsPublished());
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Gallery gallery = findGalleryById(id);
        
        if (request.getIsPublished()) {
            // 공개로 변경
            gallery.setPublished(true);

            
            // Repository를 통해 updatedBy 필드 업데이트
            galleryRepository.updatePublishedStatus(id, true, currentUserId);
            
            log.info("[GalleryService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, true);

            // 응답 메시지 분기
            String message = "갤러리가 공개로 변경되었습니다.";
                
            return Response.ok("0000", message);
        } else {
            // 비공개로 변경 (makePermanent는 무시)
            int updatedCount = galleryRepository.updatePublishedStatus(id, false, currentUserId);
            if (updatedCount == 0) {
                log.warn("[GalleryService] 공개 상태 변경 실패 - 갤러리을 찾을 수 없음. ID={}", id);
                throw new BusinessException(ErrorCode.GALLERY_NOT_FOUND);
            }
            
            log.info("[GalleryService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, false);
            
            return Response.ok("0000", "갤러리이 비공개로 변경되었습니다.");
        }
    }

    /**
     * 갤러리 조회 도우미 메서드.
     */
    private Gallery findGalleryById(Long id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[GalleryService] 갤러리을 찾을 수 없음. ID={}", id);
                    return new BusinessException(ErrorCode.GALLERY_NOT_FOUND);
                });
    }

    /**
     * 카테고리 조회 도우미 메서드.
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[GalleryService] 카테고리를 찾을 수 없음. ID={}", categoryId);
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
    private ResponseGalleryNavigation getGalleryNavigation(Long currentId) {
        log.debug("[GalleryService] 네비게이션 정보 조회 시작. currentId={}", currentId);
        
        // 이전글 조회
        Gallery previousGallery = galleryRepository.findPreviousGallery(currentId);
        ResponseGalleryNavigation.NavigationItem previous = null;
        if (previousGallery != null) {
            previous = ResponseGalleryNavigation.NavigationItem.builder()
                    .id(previousGallery.getId())
                    .title(previousGallery.getTitle())
                    .createdAt(previousGallery.getCreatedAt())
                    .build();
            log.debug("[GalleryService] 이전글 조회 완료. previousId={}, title={}",
                    previousGallery.getId(), previousGallery.getTitle());
        }
        
        // 다음글 조회
        Gallery nextGallery = galleryRepository.findNextGallery(currentId);
        ResponseGalleryNavigation.NavigationItem next = null;
        if (nextGallery != null) {
            next = ResponseGalleryNavigation.NavigationItem.builder()
                    .id(nextGallery.getId())
                    .title(nextGallery.getTitle())
                    .createdAt(nextGallery.getCreatedAt())
                    .build();
            log.debug("[GalleryService] 다음글 조회 완료. nextId={}, title={}",
                    nextGallery.getId(), nextGallery.getTitle());
        }
        
        ResponseGalleryNavigation navigation = ResponseGalleryNavigation.of(previous, next);
        log.debug("[GalleryService] 네비게이션 정보 조회 완료. hasPrevious={}, hasNext={}",
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
        String originalName = (String) row[2];
        log.debug("[GalleryService] mapToResponseFileInfo - fileId={}, fileName={}, originalName={}, ext={}, size={}, url={}", 
                  row[0], row[1], originalName, row[3], row[4], row[5]);
        
        return ResponseFileInfo.builder()
                .fileId(String.valueOf(row[0]))  // Long을 String으로 변환
                .fileName((String) row[1])
                .originalName(originalName)   // 원본 파일명 추가
                .ext((String) row[3])
                .size((Long) row[4])
                .url((String) row[5])
                .build();
    }

    /**
     * 본문이미지 연결 생성 및 임시 파일을 정식 파일로 승격.
     * 
     * 임시 파일을 정식 파일로 변환하고 UploadFileLink를 생성하여 갤러리와 연결합니다.
     * Content URL 변환을 위한 임시-정식 파일 ID 매핑을 반환합니다.
     * 
     * @param galleryId 갤러리 ID
     * @param fileReferences 파일 참조 목록 (파일ID + 원본명)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinks(Long galleryId, List<FileReference> fileReferences, FileRole role) {
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[GalleryService] 연결할 본문이미지 없음. galleryId={}", galleryId);
            return tempToFormalMap;
        }

        log.info("[GalleryService] 본문이미지 연결 생성 시작. galleryId={}, 파일개수={}", galleryId, fileReferences.size());
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환 (원본명 포함)
        for (FileReference fileRef : fileReferences) {
            String tempFileId = fileRef.getFileId();
            String originalFileName = fileRef.getFileName();
            
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, originalFileName);
            if (formalFileId != null) {
                tempToFormalMap.put(tempFileId, formalFileId);
                log.debug("[GalleryService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, originalName={}",
                        tempFileId, formalFileId, originalFileName);
            } else {
                log.warn("[GalleryService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, originalName={}",
                        tempFileId, originalFileName);
            }
        }

        // 2단계: 성공한 변환들에 대해 본문이미지 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> UploadFileLink.createGalleryInlineImage(formalFileId, galleryId))
                .toList();

        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[GalleryService] 본문이미지 연결 생성 완료. galleryId={}, 요청={}개, 성공={}개",
                galleryId, fileReferences.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * 임시파일 정보를 기반으로 파일 연결 생성 (새로운 방식).
     * 
     * @param galleryId 갤러리 ID
     * @param tempFileInfos 임시파일 정보 목록 (tempFileId + fileName)
     * @param role 파일 역할 (INLINE만 사용, 갤러리는 첨부파일 없음)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinkFromTempFiles(Long galleryId, List<?> tempFileInfos, FileRole role) {
        log.info("[GalleryService] 파일 연결 생성 시작. galleryId={}, role={}, 파일개수={}",
                galleryId, role, tempFileInfos != null ? tempFileInfos.size() : 0);
        
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (tempFileInfos == null || tempFileInfos.isEmpty()) {
            log.debug("[GalleryService] 연결할 {}파일 없음. galleryId={}", role, galleryId);
            return tempToFormalMap;
        }
        
        log.debug("[GalleryService] {} 파일 연결 생성 시작. galleryId={}, 파일개수={}", role, galleryId, tempFileInfos.size());
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환
        for (Object tempFileInfo : tempFileInfos) {
            String tempFileId = null;
            String fileName = null;
            
            // InlineImageInfo 타입만 처리 (갤러리는 본문이미지만 사용)
            if (tempFileInfo instanceof RequestGalleryCreate.InlineImageInfo) {
                RequestGalleryCreate.InlineImageInfo info = (RequestGalleryCreate.InlineImageInfo) tempFileInfo;
                tempFileId = info.getTempFileId();
                fileName = info.getFileName();
            }
            
            if (tempFileId != null) {
                Long formalFileId = fileService.promoteToFormalFile(tempFileId, fileName);
                if (formalFileId != null) {
                    tempToFormalMap.put(tempFileId, formalFileId);
                    log.debug("[GalleryService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, fileName={}",
                            tempFileId, formalFileId, fileName);
                } else {
                    log.warn("[GalleryService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, fileName={}, role={}",
                            tempFileId, fileName, role);
                }
            }
        }
        
        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> {
                    if (role == FileRole.COVER) {
                        return UploadFileLink.createGalleryCover(formalFileId, galleryId);
                    } else {
                        return UploadFileLink.createGalleryInlineImage(formalFileId, galleryId);
                    }
                })
                .toList();
                
        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[GalleryService] {} 파일 연결 생성 완료. galleryId={}, 요청={}개, 성공={}개",
                role, galleryId, tempFileInfos.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }


    /**
     * 선택된 파일 연결 삭제.
     * 
     * @param galleryId 갤러리 ID
     * @param fileIds 삭제할 파일 ID 목록
     * @param role 파일 역할
     */
    private void deleteSelectedFileLinks(Long galleryId, List<Long> fileIds, FileRole role) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[GalleryService] 삭제할 {} 파일 없음. galleryId={}", role, galleryId);
            return;
        }
        
        log.info("[GalleryService] {} 파일 선택 삭제 실행. galleryId={}, 삭제파일={}개",
                role, galleryId, fileIds.size());
        
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
                "gallery", galleryId, role, fileIds);
        
        log.debug("[GalleryService] {} 파일 선택 삭제 완료. galleryId={}, 삭제된파일IDs={}",
                role, galleryId, fileIds);
    }


    
    // ================== CategoryUsageChecker 구현 ==================
    
    @Override
    public boolean hasDataUsingCategory(Long categoryId) {
        long galleryCount = galleryRepository.countByCategoryId(categoryId);
        
        log.debug("[GalleryService] 카테고리 사용 확인. categoryId={}, 갤러리수={}",
                categoryId, galleryCount);
        
        return galleryCount > 0;
    }
    
    @Override
    public String getDomainName() {
        return "갤러리";
    }
    
    /**
     * 커버이미지 연결 생성.
     * 
     * @param galleryId 갤러리 ID
     * @param tempFileId 임시 파일 ID
     * @param fileName 원본 파일명
     */
    private void createCoverImageLink(Long galleryId, String tempFileId, String fileName) {
        if (tempFileId == null) {
            log.debug("[GalleryService] 커버이미지 임시파일ID가 null. galleryId={}", galleryId);
            return;
        }
        
        log.info("[GalleryService] 커버이미지 연결 생성 시작. galleryId={}, tempFileId={}, fileName={}",
                galleryId, tempFileId, fileName);
        
        // 임시 파일을 정식 파일로 변환
        Long formalFileId = fileService.promoteToFormalFile(tempFileId, fileName);
        if (formalFileId != null) {
            // 커버이미지 연결 생성
            UploadFileLink coverLink = UploadFileLink.createGalleryCover(formalFileId, galleryId);
            uploadFileLinkRepository.save(coverLink);
            
            log.info("[GalleryService] 커버이미지 연결 생성 완료. galleryId={}, tempId={} -> formalId={}",
                    galleryId, tempFileId, formalFileId);
        } else {
            log.warn("[GalleryService] 커버이미지 임시 파일 변환 실패. tempFileId={}, fileName={}",
                    tempFileId, fileName);
        }
    }
    
    /**
     * 커버이미지 조회.
     * 
     * @param galleryId 갤러리 ID
     * @return 커버이미지 정보 (없으면 null)
     */
    private ResponseFileInfo getCoverImage(Long galleryId) {
        log.debug("[GalleryService] 커버이미지 조회 시작. galleryId={}", galleryId);
        
        List<Object[]> coverData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "gallery", galleryId, FileRole.COVER);
        
        if (!coverData.isEmpty()) {
            ResponseFileInfo coverImage = mapToResponseFileInfo(coverData.get(0));
            log.debug("[GalleryService] 커버이미지 조회 완료. galleryId={}, fileId={}", 
                     galleryId, coverImage.getFileId());
            return coverImage;
        }
        
        log.debug("[GalleryService] 커버이미지 없음. galleryId={}", galleryId);
        return null;
    }
    
    /**
     * 커버이미지 처리 (수정 시).
     * 
     * @param galleryId 갤러리 ID
     * @param request 수정 요청
     */
    private void handleCoverImageUpdate(Long galleryId, RequestGalleryUpdate request) {
        log.debug("[GalleryService] 커버이미지 수정 처리 시작. galleryId={}, deleteCover={}, newTempFileId={}",
                galleryId, request.getDeleteCoverImage(), request.getCoverImageTempFileId());
        
        // 커버이미지 삭제 처리
        if (Boolean.TRUE.equals(request.getDeleteCoverImage())) {
            uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole(
                    "gallery", galleryId, FileRole.COVER);
            log.info("[GalleryService] 기존 커버이미지 삭제 완료. galleryId={}", galleryId);
        }
        
        // 새 커버이미지 추가 처리
        if (request.getCoverImageTempFileId() != null) {
            // 기존 커버이미지가 있다면 먼저 삭제 (하나만 유지)
            if (!Boolean.TRUE.equals(request.getDeleteCoverImage())) {
                uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole(
                        "gallery", galleryId, FileRole.COVER);
                log.debug("[GalleryService] 기존 커버이미지 교체를 위한 삭제. galleryId={}", galleryId);
            }
            
            createCoverImageLink(galleryId, request.getCoverImageTempFileId(), request.getCoverImageFileName());
        }
    }
}