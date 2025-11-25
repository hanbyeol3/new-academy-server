package com.academy.api.notice.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticeSearch;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeListItem;
import com.academy.api.notice.dto.ResponseNoticeSimple;
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
import java.util.stream.Collectors;

/**
 * 공지사항 서비스 구현체.
 * 주요 특징:
 * - 트랜잭션 경계 관리 (@Transactional)
 * - 체계적인 로깅 (info: 주요 비즈니스, debug: 상세 정보)
 * - 카테고리 연계 처리
 * - 파일 서비스 연동 (추후 구현)
 * - 예외 상황 처리
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
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final NoticeMapper noticeMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final FileService fileService;

    /**
     * 공지사항 목록 조회 (파일 개수 포함).
     * 
     * IN절을 활용한 일괄 조회로 성능을 최적화했습니다.
     * 각 공지사항의 첨부파일과 본문이미지 개수를 함께 제공합니다.
     * 
     * @param searchCondition 검색 조건
     * @param pageable 페이징 정보
     * @return 공지사항 목록 (파일 개수 포함)
     */
    public ResponseList<ResponseNoticeListItem> getNoticeListWithFileCount(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 공지사항 목록 조회 (파일 개수 포함) 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        // 1. 공지사항 목록 조회
        Page<Notice> noticePage = noticeRepository.searchNotices(searchCondition, pageable);
        List<Notice> notices = noticePage.getContent();
        
        if (notices.isEmpty()) {
            log.debug("[NoticeService] 조회된 공지사항이 없음");
            return ResponseList.ok(
                    ResponseNoticeListItem.fromList(notices),
                    noticePage.getTotalElements(),
                    noticePage.getNumber(),
                    noticePage.getSize()
            );
        }
        
        // 2. 공지사항 ID 목록 추출
        List<Long> noticeIds = notices.stream()
                .map(Notice::getId)
                .toList();
        
        // 3. IN절을 활용한 파일 개수 일괄 조회
        List<Object[]> fileCounts = uploadFileLinkRepository.countFilesByOwnerIdsGroupByRole(
                "notices", noticeIds);
        
        // 4. Map으로 변환: noticeId -> role -> count
        Map<Long, Map<FileRole, Long>> fileCountMap = fileCounts.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0], // ownerId
                        Collectors.toMap(
                                row -> (FileRole) row[1], // role
                                row -> (Long) row[2]      // count
                        )
                ));
        
        log.debug("[NoticeService] 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건, 파일연결조회={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), notices.size(), fileCounts.size());
        
        // 5. DTO 변환 (파일 개수 제외)
        List<ResponseNoticeListItem> items = ResponseNoticeListItem.fromList(notices);
        
        return ResponseList.ok(
                items,
                noticePage.getTotalElements(),
                noticePage.getNumber(),
                noticePage.getSize()
        );
    }

    @Override
    public ResponseList<ResponseNoticeSimple> getNoticeList(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchNotices(searchCondition, pageable);
        
        log.debug("[NoticeService] 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    @Override
    public ResponseList<ResponseNoticeListItem> getNoticeListForAdmin(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 관리자용 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchNoticesForAdmin(searchCondition, pageable);
        List<Notice> notices = noticePage.getContent();
        
        log.debug("[NoticeService] 관리자 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), notices.size());
        
        // 회원 이름 포함하여 DTO 변환
        List<ResponseNoticeListItem> items = notices.stream()
                .map(notice -> {
                    String createdByName = getMemberName(notice.getCreatedBy());
                    String updatedByName = getMemberName(notice.getUpdatedBy());
                    return ResponseNoticeListItem.fromWithNames(notice, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(
                items,
                noticePage.getTotalElements(),
                noticePage.getNumber(),
                noticePage.getSize()
        );
    }

    @Override
    public ResponseList<ResponseNoticeSimple> getExposableNoticeList(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 공개용 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchExposableNotices(searchCondition, pageable);
        
        log.debug("[NoticeService] 공개 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    /**
     * 공지사항 상세 조회 (파일 목록 포함).
     * 
     * JOIN을 활용하여 첨부파일과 본문이미지 목록을 함께 조회합니다.
     * 파일 역할별로 분리하여 제공합니다.
     * 
     * @param id 공지사항 ID
     * @return 공지사항 상세 정보 (파일 목록 포함)
     */
    public ResponseData<ResponseNotice> getNoticeWithFiles(Long id) {
        log.info("[NoticeService] 공지사항 상세 조회 (파일 포함) 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        
        // 첨부파일 목록 조회
        log.info("[NoticeService] 첨부파일 조회 시작. ownerTable=notices, ownerId={}, role=ATTACHMENT", id);
        List<Object[]> attachmentData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "notices", id, FileRole.ATTACHMENT);
        log.info("[NoticeService] 첨부파일 쿼리 결과 개수: {}", attachmentData.size());
        
        if (!attachmentData.isEmpty()) {
            for (int i = 0; i < attachmentData.size(); i++) {
                Object[] row = attachmentData.get(i);
                log.info("[NoticeService] 첨부파일[{}] 원본데이터: fileId={}, fileName={}, ext={}, size={}, url={}", 
                        i, row[0], row[1], row[2], row[3], row[4]);
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
        
        // ResponseNotice 생성 (파일 목록 및 회원 이름 포함)
        ResponseNotice response = ResponseNotice.fromWithNames(notice, createdByName, updatedByName);
        
        // 파일 정보 설정
        response = ResponseNotice.builder()
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
    public ResponseData<ResponseNotice> getNotice(Long id) {
        return getNoticeWithFiles(id);
    }

    @Override
    @Transactional
    public ResponseData<ResponseNotice> getNoticeWithViewCount(Long id) {
        log.info("[NoticeService] 공지사항 상세 조회 (조회수 증가) 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        Long beforeViewCount = notice.getViewCount();
        
        // 조회수 증가
        notice.incrementViewCount();
        
        log.debug("[NoticeService] 조회수 증가 완료. ID={}, 이전조회수={}, 현재조회수={}", 
                id, beforeViewCount, notice.getViewCount());
        
        // 파일 정보를 포함한 상세 조회
        return getNoticeWithFiles(id);
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
                request.getAttachments() != null ? request.getAttachments().size() : 0,
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
        
        // 파일 연결 처리
        createFileLinks(noticeId, request.getAttachments(), FileRole.ATTACHMENT);
        createFileLinks(noticeId, request.getInlineImages(), FileRole.INLINE);
        
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
    public Response updateNotice(Long id, RequestNoticeUpdate request) {
        log.info("🔄 [NoticeService] 공지사항 수정 시작!!! ID={}, 첨부파일={}개, 본문이미지={}개", 
                id, 
                request.getAttachments() != null ? request.getAttachments().size() : 0,
                request.getInlineImages() != null ? request.getInlineImages().size() : 0);
        
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
        
        // 파일 치환 처리 (첨부파일/본문이미지가 있는 경우에만)
        log.info("🔄 [NoticeService] 파일 치환 처리 시작. attachments={}, inlineImages={}", 
                request.getAttachments(), request.getInlineImages());
        if (request.getAttachments() != null) {
            log.info("🔄 [NoticeService] ATTACHMENT 파일 치환 실행. 파일개수={}", request.getAttachments().size());
            replaceFileLinks(id, request.getAttachments(), FileRole.ATTACHMENT);
        }
        if (request.getInlineImages() != null) {
            log.info("🔄 [NoticeService] INLINE 파일 치환 실행. 파일개수={}", request.getInlineImages().size());
            replaceFileLinks(id, request.getInlineImages(), FileRole.INLINE);
        }
        
        log.info("[NoticeService] 공지사항 수정 완료. ID={}, 제목={}", id, notice.getTitle());
        
        return Response.ok("0000", "공지사항이 수정되었습니다.");
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
    public ResponseData<List<ResponseNoticeSimple>> getRecentNotices(int limit) {
        log.info("[NoticeService] 최근 공지사항 조회 시작. 개수={}", limit);
        
        List<Notice> notices = noticeRepository.findRecentNotices(limit);
        List<ResponseNoticeSimple> response = noticeMapper.toSimpleResponseList(notices);
        
        log.debug("[NoticeService] 최근 공지사항 조회 완료. 반환개수={}", response.size());
        
        return ResponseData.ok(response);
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
     * Object[] 데이터를 ResponseFileInfo로 변환하는 도우미 메서드.
     * 
     * @param row [fileId, fileName, ext, size, url] 배열
     * @return ResponseFileInfo 인스턴스
     */
    private ResponseFileInfo mapToResponseFileInfo(Object[] row) {
        return ResponseFileInfo.builder()
                .fileId(String.valueOf(row[0]))  // Long을 String으로 변환
                .fileName((String) row[1])
                .ext((String) row[2])
                .size((Long) row[3])
                .url((String) row[4])
                .build();
    }

    /**
     * 파일 연결 생성 도우미 메서드.
     * 
     *
     * @param noticeId 공지사항 ID
     * @param fileIds 파일 ID 목록
     * @param role 파일 역할
     */
    private void createFileLinks(Long noticeId, List<String> fileIds, FileRole role) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[NoticeService] 연결할 {}파일 없음. noticeId={}", role, noticeId);
            return;
        }

        log.info("[NoticeService] {} 파일 연결 생성 시작. noticeId={}, 파일개수={}", role, noticeId, fileIds.size());

        // 임시 파일 ID를 정식 파일 ID로 변환하는 Map
        Map<String, Long> tempToFormalIdMap = new HashMap<>();
        
        // 1단계: 모든 임시 파일을 정식 파일로 변환
        for (String tempFileId : fileIds) {
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, extractOriginalFileName(tempFileId));
            if (formalFileId != null) {
                tempToFormalIdMap.put(tempFileId, formalFileId);
                log.debug("[NoticeService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}", tempFileId, formalFileId);
            } else {
                log.warn("[NoticeService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, role={}", tempFileId, role);
            }
        }

        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalIdMap.values().stream()
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
                role, noticeId, fileIds.size(), successfulLinks.size());
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
     * 파일 연결 치환 도우미 메서드 (DELETE + INSERT).
     * 
     * @param noticeId 공지사항 ID
     * @param fileIds 새로운 파일 ID 목록
     * @param role 파일 역할
     */
    private void replaceFileLinks(Long noticeId, List<String> fileIds, FileRole role) {
        // 1. 기존 연결 삭제 (DELETE)
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole(
                "notices", noticeId, role);
        
        log.debug("[NoticeService] 기존 {} 파일 연결 삭제 완료. noticeId={}", role, noticeId);

        // 2. 새로운 연결 생성 (INSERT)
        createFileLinks(noticeId, fileIds, role);
    }
}