package com.academy.api.faq.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import com.academy.api.faq.domain.Faq;
import com.academy.api.faq.domain.FaqSearchType;
import com.academy.api.file.dto.FileReference;
import com.academy.api.faq.dto.RequestFaqCreate;
import com.academy.api.faq.dto.RequestFaqPublishedUpdate;
import com.academy.api.faq.dto.RequestFaqUpdate;
import com.academy.api.faq.dto.ResponseFaq;
import com.academy.api.faq.dto.ResponseFaqListItem;
import com.academy.api.faq.mapper.FaqMapper;
import com.academy.api.faq.repository.FaqRepository;
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
 * FAQ 서비스 구현체.
 * 
 * 주요 특징:
 * - 트랜잭션 경계 관리 (@Transactional)
 * - 체계적인 로깅 (info: 주요 비즈니스, debug: 상세 정보)
 * - 카테고리 연계 처리
 * - 파일 서비스 연동 및 content URL 자동 변환 (INLINE 이미지만)
 * - 임시 파일을 정식 파일로 승격 처리
 * - 검색 기능 (질문/답변/작성자/전체)
 * - 예외 상황 처리
 * 
 * Content URL 변환 기능:
 * - FAQ 생성/수정 시 본문 이미지의 임시 URL을 정식 URL로 자동 변환
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
public class FaqServiceImpl implements FaqService, CategoryUsageChecker {

    private final FaqRepository faqRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final FaqMapper faqMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final FileService fileService;

    @Override
    public ResponseList<ResponseFaqListItem> getFaqList(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable) {
        log.info("[FaqService] FAQ 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}, 페이지={}", 
                keyword, searchType, categoryId, isPublished, sortBy, pageable);

        // searchType enum 변환
        FaqSearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = FaqSearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[FaqService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = FaqSearchType.ALL;
            }
        }
        
        Page<Faq> faqPage = faqRepository.searchFaqs(keyword, effectiveSearchType, categoryId, isPublished, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        
        log.debug("[FaqService] FAQ 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                faqPage.getTotalElements(), faqPage.getNumber(), faqPage.getContent().size());
        
        return faqMapper.toListItemResponseList(faqPage);
    }

    @Override
    public ResponseList<ResponseFaqListItem> getFaqListForAdmin(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable) {
        log.info("[FaqService] 관리자용 FAQ 목록 조회 시작. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}, 페이지={}", 
                keyword, searchType, categoryId, isPublished, sortBy, pageable);

        // searchType enum 변환
        FaqSearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = FaqSearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[FaqService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = FaqSearchType.ALL;
            }
        }
        
        // ✅ 단일 경로: QueryDSL 통합 처리 (searchType 포함)
        Page<Faq> faqPage = faqRepository.searchFaqsForAdmin(keyword, effectiveSearchType, categoryId, isPublished, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        List<Faq> faqs = faqPage.getContent();
        
        log.debug("[FaqService] 관리자 FAQ 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                faqPage.getTotalElements(), faqPage.getNumber(), faqs.size());
        
        // 회원 이름 포함하여 DTO 변환
        List<ResponseFaqListItem> items = faqs.stream()
                .map(faq -> {
                    String createdByName = getMemberName(faq.getCreatedBy());
                    String updatedByName = getMemberName(faq.getUpdatedBy());
                    return ResponseFaqListItem.fromWithNames(faq, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(
                items,
                faqPage.getTotalElements(),
                faqPage.getNumber(),
                faqPage.getSize()
        );
    }

    @Override
    public ResponseList<ResponseFaqListItem> getPublishedFaqList(String keyword, String searchType, Long categoryId, String sortBy, Pageable pageable) {
        log.info("[FaqService] 공개용 FAQ 목록 조회 시작. keyword={}, searchType={}, categoryId={}, sortBy={}, 페이지={}", 
                keyword, searchType, categoryId, sortBy, pageable);

        // searchType enum 변환
        FaqSearchType effectiveSearchType = null;
        if (searchType != null) {
            try {
                effectiveSearchType = FaqSearchType.valueOf(searchType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[FaqService] 유효하지 않은 searchType, 기본값 적용. searchType={}", searchType);
                effectiveSearchType = FaqSearchType.ALL;
            }
        }
        
        Page<Faq> faqPage = faqRepository.searchPublishedFaqs(keyword, effectiveSearchType, categoryId, sortBy != null ? sortBy : "CREATED_DESC", pageable);
        
        log.debug("[FaqService] 공개 FAQ 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                faqPage.getTotalElements(), faqPage.getNumber(), faqPage.getContent().size());
        
        return faqMapper.toListItemResponseList(faqPage);
    }

    /**
     * FAQ 상세 조회 (파일 목록 포함).
     * 
     * JOIN을 활용하여 본문이미지 목록을 함께 조회합니다.
     * 
     * @param id FAQ ID
     * @return FAQ 상세 정보 (파일 목록 포함)
     */
    public ResponseData<ResponseFaq> getFaqWithFiles(Long id) {
        log.info("[FaqService] FAQ 상세 조회 (파일 포함) 시작. ID={}", id);
        
        Faq faq = findFaqById(id);
        
        // 본문 이미지 목록 조회  
        log.info("[FaqService] 본문이미지 조회 시작. ownerTable=faq, ownerId={}, role=INLINE", id);
        List<Object[]> inlineImageData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "faq", id, FileRole.INLINE);
        log.info("[FaqService] 본문이미지 쿼리 결과 개수: {}", inlineImageData.size());
        
        List<ResponseFileInfo> inlineImages = inlineImageData.stream()
                .map(this::mapToResponseFileInfo)
                .toList();
        
        log.info("[FaqService] FAQ 조회 완료. ID={}, 질문제목={}, 본문이미지={}개", 
                id, faq.getTitle(), inlineImages.size());
        
        // 회원 이름 조회
        String createdByName = getMemberName(faq.getCreatedBy());
        String updatedByName = getMemberName(faq.getUpdatedBy());
        
        // ResponseFaq 생성 (파일 목록 및 회원 이름 포함)
        ResponseFaq response = ResponseFaq.fromWithNames(faq, createdByName, updatedByName);
        
        // 파일 정보 설정
        response = ResponseFaq.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .isPublished(response.getIsPublished())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .inlineImages(inlineImages)
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
    public ResponseData<ResponseFaq> getFaq(Long id) {
        return getFaqWithFiles(id);
    }

    /**
     * FAQ 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 FAQ ID
     */
    @Override
    @Transactional
    public ResponseData<Long> createFaq(RequestFaqCreate request) {
        log.info("[FaqService] FAQ 생성 시작. 질문제목={}, 카테고리ID={}, 본문이미지={}개", 
                request.getTitle(), request.getCategoryId(), 
                request.getInlineImages() != null ? request.getInlineImages().size() : 0);
        
        // 카테고리 조회 (있는 경우만)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[FaqService] 카테고리 조회 완료. ID={}, 카테고리명={}", 
                    request.getCategoryId(), category.getName());
        }
        
        // FAQ 생성
        Faq faq = faqMapper.toEntity(request, category);
        Faq savedFaq = faqRepository.save(faq);
        Long faqId = savedFaq.getId();
        
        // 파일 연결 처리 및 content URL 변환
        Map<String, Long> inlineTempMap = createFileLinkFromTempFiles(faqId, request.getInlineImages(), FileRole.INLINE);
        
        // content에서 임시 URL을 정식 URL로 변환 (본문 이미지만 해당)
        if (!inlineTempMap.isEmpty()) {
            String updatedContent = fileService.convertTempUrlsInContent(savedFaq.getContent(), inlineTempMap);
            if (!updatedContent.equals(savedFaq.getContent())) {
                // content가 변경된 경우 DB 업데이트
                savedFaq = faqRepository.findById(faqId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));
                
                // 도메인 메서드를 사용해서 content 업데이트
                savedFaq.updateContent(updatedContent);
                faqRepository.save(savedFaq);
                log.info("[FaqService] content 내 임시 URL 변환 완료. ID={}", faqId);
            }
        }
        
        log.info("[FaqService] FAQ 생성 완료. ID={}, 질문제목={}", savedFaq.getId(), savedFaq.getTitle());
        
        return ResponseData.ok("0000", "FAQ가 생성되었습니다.", savedFaq.getId());
    }

    /**
     * FAQ 수정 (파일 치환 포함).
     * 
     * 제공해주신 치환 정책을 적용합니다:
     * 1. 기존 파일 연결 삭제 (DELETE)
     * 2. 새로운 파일 연결 생성 (INSERT)
     * 
     * @param id FAQ ID
     * @param request 수정 요청 정보
     * @return 응답 정보
     */
    @Override
    @Transactional
    public ResponseData<ResponseFaq> updateFaq(Long id, RequestFaqUpdate request) {
        log.info("🔄 [FaqService] FAQ 수정 시작!!! ID={}, " +
                "신규본문이미지={}개, 삭제본문이미지={}개", 
                id,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0,
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0);
        
        Faq faq = findFaqById(id);
        
        // 카테고리 변경 처리
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[FaqService] 카테고리 변경. 기존={}, 신규={}", 
                    faq.getCategory() != null ? faq.getCategory().getName() : "없음", 
                    category.getName());
        }
        
        // 엔티티 업데이트
        faqMapper.updateEntity(faq, request, category);
        
        // 선택적 파일 처리 (삭제 → 추가 순서)
        log.info("🔄 [FaqService] 선택적 파일 처리 시작. " +
                "삭제 본문이미지={}개, 신규 본문이미지={}개", 
                request.getDeleteInlineImageFileIds() != null ? request.getDeleteInlineImageFileIds().size() : 0,
                request.getNewInlineImages() != null ? request.getNewInlineImages().size() : 0);
        
        // 1. 선택된 파일 삭제
        deleteSelectedFileLinks(id, request.getDeleteInlineImageFileIds(), FileRole.INLINE);
        
        // 2. 새 파일 추가
        Map<String, Long> newInlineTempMap = addFileLinks(id, request.getNewInlineImages(), FileRole.INLINE);
        
        // 3. 파일 처리 결과 로깅
        log.info("[FaqService] 파일 처리 결과. ID={}, 새이미지={}개", 
                id, newInlineTempMap.size());
        
        // 4. Content URL 완전 처리
        String finalContent = faq.getContent();
        
        // 4-1. 삭제된 이미지 URL 제거
        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            finalContent = fileService.removeDeletedImageUrlsFromContent(finalContent, request.getDeleteInlineImageFileIds());
            log.info("[FaqService] 삭제된 이미지 URL 제거 완료. ID={}, 삭제된이미지={}개", 
                    id, request.getDeleteInlineImageFileIds().size());
        }
        
        // 4-2. 모든 temp URL을 정식 URL로 변환 (기존 + 신규 포함)
        String convertedContent = fileService.convertAllTempUrlsInContent(finalContent);
        
        // 4-3. Content가 변경된 경우 업데이트
        if (!convertedContent.equals(faq.getContent())) {
            // 엔티티 다시 조회하여 최신 상태 확보
            Faq currentFaq = faqRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));
            
            // 도메인 메서드를 사용해서 content 업데이트
            currentFaq.updateContent(convertedContent);
            faqRepository.save(currentFaq);
            log.info("[FaqService] Content URL 완전 변환 완료. ID={}, 최종content길이={}", 
                    id, convertedContent.length());
        }
        
        log.info("[FaqService] FAQ 수정 완료. ID={}, 질문제목={}", id, faq.getTitle());
        
        // 5. 완전한 FAQ 정보 반환 (파일 정보 포함)
        ResponseFaq updatedFaq = getFaqWithFiles(id).getData();
        
        return ResponseData.ok("0000", "FAQ가 수정되었습니다.", updatedFaq);
    }

    /**
     * FAQ 삭제.
     * 
     * @param id 삭제할 FAQ ID
     * @return 삭제 결과
     */
    @Override
    @Transactional
    public Response deleteFaq(Long id) {
        log.info("[FaqService] FAQ 삭제 시작. ID={}", id);
        
        Faq faq = findFaqById(id);
        String title = faq.getTitle();
        
        faqRepository.delete(faq);
        
        log.info("[FaqService] FAQ 삭제 완료. ID={}, 질문제목={}", id, title);
        
        return Response.ok("0000", "FAQ가 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response togglePublished(Long id, Boolean isPublished) {
        log.info("[FaqService] 공개 상태 변경 시작. ID={}, 공개여부={}", id, isPublished);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        int updatedCount = faqRepository.updatePublishedStatus(id, isPublished, currentUserId);
        if (updatedCount == 0) {
            log.warn("[FaqService] 공개 상태 변경 실패 - FAQ를 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.FAQ_NOT_FOUND);
        }
        
        log.info("[FaqService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, isPublished);
        
        String message = isPublished ? "FAQ가 공개되었습니다." : "FAQ가 비공개되었습니다.";
        return Response.ok("0000", message);
    }

    @Override
    @Transactional
    public Response updateFaqPublished(Long id, RequestFaqPublishedUpdate request) {
        log.info("[FaqService] 공개 상태 변경 시작. ID={}, 공개여부={}", 
                id, request.getIsPublished());
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        int updatedCount = faqRepository.updatePublishedStatus(id, request.getIsPublished(), currentUserId);
        if (updatedCount == 0) {
            log.warn("[FaqService] 공개 상태 변경 실패 - FAQ를 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.FAQ_NOT_FOUND);
        }
        
        log.info("[FaqService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, request.getIsPublished());
        
        String message = request.getIsPublished() ? "FAQ가 공개되었습니다." : "FAQ가 비공개되었습니다.";
        return Response.ok("0000", message);
    }


    @Override
    public ResponseData<List<Object[]>> getFaqStatsByCategory() {
        log.info("[FaqService] 카테고리별 FAQ 통계 조회 시작");
        
        List<Object[]> stats = faqRepository.getFaqStatsByCategory();
        
        log.debug("[FaqService] 카테고리별 통계 조회 완료. 카테고리수={}", stats.size());
        
        return ResponseData.ok(stats);
    }

    /**
     * FAQ 조회 도우미 메서드.
     */
    private Faq findFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[FaqService] FAQ를 찾을 수 없음. ID={}", id);
                    return new BusinessException(ErrorCode.FAQ_NOT_FOUND);
                });
    }

    /**
     * 카테고리 조회 도우미 메서드.
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[FaqService] 카테고리를 찾을 수 없음. ID={}", categoryId);
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
     * 임시 파일을 정식 파일로 변환하고 UploadFileLink를 생성하여 FAQ와 연결합니다.
     * Content URL 변환을 위한 임시-정식 파일 ID 매핑을 반환합니다.
     * 
     * @param faqId FAQ ID
     * @param fileReferences 파일 참조 목록 (파일ID + 원본명)
     * @param role 파일 역할 (INLINE만 지원)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinks(Long faqId, List<FileReference> fileReferences, FileRole role) {
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[FaqService] 연결할 {}파일 없음. faqId={}", role, faqId);
            return tempToFormalMap;
        }

        log.info("[FaqService] {} 파일 연결 생성 시작. faqId={}, 파일개수={}", role, faqId, fileReferences.size());

        // 1단계: 모든 임시 파일을 정식 파일로 변환 (원본명 포함)
        for (FileReference fileRef : fileReferences) {
            String tempFileId = fileRef.getFileId();
            String originalFileName = fileRef.getFileName();
            
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, originalFileName);
            if (formalFileId != null) {
                tempToFormalMap.put(tempFileId, formalFileId);
                log.debug("[FaqService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, originalName={}", 
                        tempFileId, formalFileId, originalFileName);
            } else {
                log.warn("[FaqService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, originalName={}, role={}", 
                        tempFileId, originalFileName, role);
            }
        }

        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성 (INLINE만 지원)
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> UploadFileLink.createFaqInlineImage(formalFileId, faqId))
                .toList();

        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[FaqService] {} 파일 연결 생성 완료. faqId={}, 요청={}개, 성공={}개", 
                role, faqId, fileReferences.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * 임시파일 정보를 기반으로 파일 연결 생성 (새로운 방식).
     * 
     * @param faqId FAQ ID
     * @param tempFileInfos 임시파일 정보 목록 (tempFileId + fileName)
     * @param role 파일 역할 (INLINE만 지원)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> createFileLinkFromTempFiles(Long faqId, List<?> tempFileInfos, FileRole role) {
        log.info("🔥 [FaqService] createFileLinkFromTempFiles 호출됨!!! faqId={}, role={}, tempFileInfos={}", 
                faqId, role, tempFileInfos);
        
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (tempFileInfos == null || tempFileInfos.isEmpty()) {
            log.info("⚠️ [FaqService] 연결할 {}파일 없음. faqId={}", role, faqId);
            return tempToFormalMap;
        }
        
        log.info("🚀 [FaqService] {} 파일 연결 생성 시작. faqId={}, 파일개수={}", role, faqId, tempFileInfos.size());
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환
        for (Object tempFileInfo : tempFileInfos) {
            String tempFileId = null;
            String fileName = null;
            
            // 타입에 따라 처리 (InlineImageInfo만 지원)
            if (tempFileInfo instanceof RequestFaqCreate.InlineImageInfo) {
                RequestFaqCreate.InlineImageInfo info = (RequestFaqCreate.InlineImageInfo) tempFileInfo;
                tempFileId = info.getTempFileId();
                fileName = info.getFileName();
            }
            
            if (tempFileId != null) {
                Long formalFileId = fileService.promoteToFormalFile(tempFileId, fileName);
                if (formalFileId != null) {
                    tempToFormalMap.put(tempFileId, formalFileId);
                    log.debug("[FaqService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}, fileName={}", 
                            tempFileId, formalFileId, fileName);
                } else {
                    log.warn("[FaqService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, fileName={}, role={}", 
                            tempFileId, fileName, role);
                }
            }
        }
        
        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성 (INLINE만 지원)
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> UploadFileLink.createFaqInlineImage(formalFileId, faqId))
                .toList();
                
        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
        }
        
        log.info("[FaqService] {} 파일 연결 생성 완료. faqId={}, 요청={}개, 성공={}개", 
                role, faqId, tempFileInfos.size(), successfulLinks.size());
                
        return tempToFormalMap;
    }

    /**
     * 선택된 파일 연결 삭제.
     * 
     * @param faqId FAQ ID
     * @param fileIds 삭제할 파일 ID 목록
     * @param role 파일 역할
     */
    private void deleteSelectedFileLinks(Long faqId, List<Long> fileIds, FileRole role) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[FaqService] 삭제할 {} 파일 없음. faqId={}", role, faqId);
            return;
        }
        
        log.info("🗑️ [FaqService] {} 파일 선택 삭제 실행. faqId={}, 삭제파일={}개", 
                role, faqId, fileIds.size());
        
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
                "faq", faqId, role, fileIds);
        
        log.debug("[FaqService] {} 파일 선택 삭제 완료. faqId={}, 삭제된파일IDs={}", 
                role, faqId, fileIds);
    }

    /**
     * 새 파일 추가 (기존 파일은 유지).
     * 
     * 기존 파일들은 그대로 유지하고 새로운 파일만 추가합니다.
     * 내부적으로 createFileLinks를 호출하여 임시-정식 파일 ID 매핑을 반환합니다.
     * 
     * @param faqId FAQ ID
     * @param fileReferences 새로 추가할 파일 참조 목록 (파일ID + 원본명)
     * @param role 파일 역할 (INLINE만 지원)
     * @return 임시 파일 ID → 정식 파일 ID 매핑 (content URL 변환용)
     */
    private Map<String, Long> addFileLinks(Long faqId, List<FileReference> fileReferences, FileRole role) {
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[FaqService] 추가할 {} 파일 없음. faqId={}", role, faqId);
            return new HashMap<>();
        }
        
        log.info("➕ [FaqService] {} 파일 추가 실행. faqId={}, 추가파일={}개", 
                role, faqId, fileReferences.size());
        
        // 기존 createFileLinks 메서드 재사용하여 임시-정식 ID 매핑 반환
        Map<String, Long> tempToFormalMap = createFileLinks(faqId, fileReferences, role);
        
        log.debug("[FaqService] {} 파일 추가 완료. faqId={}, 추가된파일={}개", 
                role, faqId, fileReferences.size());
        
        return tempToFormalMap;
    }

    // ================== CategoryUsageChecker 구현 ==================
    
    @Override
    public boolean hasDataUsingCategory(Long categoryId) {
        long faqCount = faqRepository.countByCategoryId(categoryId);
        
        log.debug("[FaqService] 카테고리 사용 확인. categoryId={}, FAQ수={}", 
                categoryId, faqCount);
        
        return faqCount > 0;
    }
    
    @Override
    public String getDomainName() {
        return "FAQ";
    }
}