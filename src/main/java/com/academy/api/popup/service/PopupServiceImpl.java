package com.academy.api.popup.service;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.service.FileService;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.dto.*;
import com.academy.api.popup.mapper.PopupMapper;
import com.academy.api.popup.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팝업 서비스 구현체.
 * 
 * - 팝업 CRUD 비즈니스 로직 처리
 * - IMAGE 타입: 임시파일 → 정식파일 이동 처리
 * - YOUTUBE 타입: URL 유효성 검증
 * - 노출 기간 및 공개상태 관리
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupServiceImpl implements PopupService {

    private final PopupRepository popupRepository;
    private final PopupMapper popupMapper;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    /**
     * 팝업 목록 조회 (관리자용 통합 검색).
     */
    @Override
    public ResponseList<ResponsePopupListItem> getPopupList(String keyword, Popup.PopupType type, Boolean isPublished, String sortType, Pageable pageable) {
        log.info("[PopupService] 팝업 목록 조회 시작. keyword={}, type={}, isPublished={}, sortType={}", 
                keyword, type, isPublished, sortType);

        try {
            // 정렬 타입 유효성 검증
            if (sortType != null && !isValidSortType(sortType)) {
                log.warn("[PopupService] 유효하지 않은 정렬 타입: {}", sortType);
                sortType = "SORT_ORDER_ASC"; // 기본값으로 fallback
            }

            // Repository 호출 (QueryDSL 동적 쿼리)
            Page<Popup> popupPage = popupRepository.searchPopupsForAdmin(keyword, type, isPublished, sortType, pageable);
            
            // 회원 이름을 포함한 DTO 변환
            List<ResponsePopupListItem> items = popupPage.getContent()
                    .stream()
                    .map(popup -> {
                        String createdByName = getMemberName(popup.getCreatedBy());
                        String updatedByName = getMemberName(popup.getUpdatedBy());
                        return ResponsePopupListItem.fromWithNames(popup, createdByName, updatedByName);
                    })
                    .toList();

            log.debug("[PopupService] 팝업 목록 조회 완료. 총 {}개, 현재 페이지 {}개", 
                     popupPage.getTotalElements(), popupPage.getNumberOfElements());
            
            return ResponseList.ok(items, popupPage.getTotalElements(), popupPage.getNumber(), popupPage.getSize());

        } catch (Exception e) {
            log.error("[PopupService] 팝업 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(List.of(), 0L, pageable.getPageNumber(), pageable.getPageSize());
        }
    }

    /**
     * 팝업 상세 조회.
     */
    @Override
    public ResponseData<ResponsePopup> getPopup(Long id) {
        log.info("[PopupService] 팝업 상세 조회 시작. id={}", id);

        return popupRepository.findById(id)
                .map(popup -> {
                    String createdByName = getMemberName(popup.getCreatedBy());
                    String updatedByName = getMemberName(popup.getUpdatedBy());
                    
                    ResponsePopup response = ResponsePopup.fromWithNames(popup, createdByName, updatedByName);
                    log.debug("[PopupService] 팝업 상세 조회 완료. id={}, title={}", id, popup.getTitle());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[PopupService] 팝업을 찾을 수 없음. id={}", id);
                    return ResponseData.error("P404", "팝업을 찾을 수 없습니다");
                });
    }

    /**
     * 팝업 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createPopup(RequestPopupCreate request) {
        log.info("[PopupService] 팝업 생성 시작. title={}, type={}", request.getTitle(), request.getType());

        try {
            // 엔티티 생성
            Popup popup = popupMapper.toEntity(request);
            Popup savedPopup = popupRepository.save(popup);
            
            // IMAGE 타입인 경우 파일 처리
            if (request.getType() == Popup.PopupType.IMAGE && request.getAttachmentFiles() != null && !request.getAttachmentFiles().isEmpty()) {
                processImageFiles(savedPopup.getId(), request.getAttachmentFiles());
            }
            
            log.debug("[PopupService] 팝업 생성 완료. id={}, title={}", savedPopup.getId(), savedPopup.getTitle());
            
            return ResponseData.ok("0000", "팝업이 생성되었습니다", savedPopup.getId());

        } catch (Exception e) {
            log.error("[PopupService] 팝업 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "팝업 생성 중 오류가 발생했습니다");
        }
    }

    /**
     * 팝업 수정.
     */
    @Override
    @Transactional
    public Response updatePopup(Long id, RequestPopupUpdate request) {
        log.info("[PopupService] 팝업 수정 시작. id={}, title={}", id, request.getTitle());

        return popupRepository.findById(id)
                .map(popup -> {
                    try {
                        // 엔티티 업데이트
                        popupMapper.updateEntity(popup, request);
                        popup.setUpdatedBy(SecurityUtils.getCurrentUserId());
                        
                        // IMAGE 타입으로 변경하거나 첨부파일이 있는 경우 파일 처리
                        if (request.getType() == Popup.PopupType.IMAGE && request.getAttachmentFiles() != null && !request.getAttachmentFiles().isEmpty()) {
                            processImageFilesForUpdate(id, request.getAttachmentFiles());
                        }
                        
                        popupRepository.save(popup);
                        
                        log.debug("[PopupService] 팝업 수정 완료. id={}, title={}", id, popup.getTitle());
                        
                        return Response.ok("0000", "팝업이 수정되었습니다");

                    } catch (Exception e) {
                        log.error("[PopupService] 팝업 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "팝업 수정 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[PopupService] 수정할 팝업을 찾을 수 없음. id={}", id);
                    return Response.error("P404", "팝업을 찾을 수 없습니다");
                });
    }

    /**
     * 팝업 삭제.
     */
    @Override
    @Transactional
    public Response deletePopup(Long id) {
        log.info("[PopupService] 팝업 삭제 시작. id={}", id);

        return popupRepository.findById(id)
                .map(popup -> {
                    try {
                        // 관련 파일 삭제 (IMAGE 타입인 경우)
                        if (popup.getType() == Popup.PopupType.IMAGE) {
                            deletePopupFiles(id);
                        }
                        
                        popupRepository.delete(popup);
                        
                        log.debug("[PopupService] 팝업 삭제 완료. id={}, title={}", id, popup.getTitle());
                        
                        return Response.ok("0000", "팝업이 삭제되었습니다");

                    } catch (Exception e) {
                        log.error("[PopupService] 팝업 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "팝업 삭제 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[PopupService] 삭제할 팝업을 찾을 수 없음. id={}", id);
                    return Response.error("P404", "팝업을 찾을 수 없습니다");
                });
    }

    /**
     * 팝업 공개 상태 변경.
     */
    @Override
    @Transactional
    public Response updatePublishedStatus(Long id, Boolean isPublished) {
        log.info("[PopupService] 공개 상태 변경 시작. id={}, isPublished={}", id, isPublished);
        
        return popupRepository.findById(id)
                .map(popup -> {
                    try {
                        popup.updatePublishedStatus(isPublished);
                        popup.setUpdatedBy(SecurityUtils.getCurrentUserId());
                        
                        log.debug("[PopupService] 공개 상태 변경 완료. id={}, 새상태={}", id, isPublished);
                        
                        String statusMessage = Boolean.TRUE.equals(isPublished) 
                            ? "팝업이 공개로 변경되었습니다" 
                            : "팝업이 비공개로 변경되었습니다";
                            
                        return Response.ok("0000", statusMessage);
                        
                    } catch (Exception e) {
                        log.error("[PopupService] 공개 상태 변경 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "공개 상태 변경 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[PopupService] 공개 상태를 변경할 팝업을 찾을 수 없음. id={}", id);
                    return Response.error("P404", "팝업을 찾을 수 없습니다");
                });
    }

    /**
     * 노출중인 팝업 목록 조회 (사용자용).
     */
    @Override
    public ResponseList<ResponsePopupPublic> getActivePopups() {
        log.info("[PopupService] 노출중인 팝업 목록 조회 시작");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Popup> activePopups = popupRepository.findActivePopupsWithConditions(now);
            
            List<ResponsePopupPublic> items = activePopups.stream()
                    .map(ResponsePopupPublic::from)
                    .toList();

            log.debug("[PopupService] 노출중인 팝업 목록 조회 완료. 노출중 팝업 수={}", items.size());
            
            return ResponseList.ok(items, (long) items.size(), 0, items.size());

        } catch (Exception e) {
            log.error("[PopupService] 노출중인 팝업 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(List.of(), 0L, 0, 0);
        }
    }

    /**
     * IMAGE 파일 처리 (임시파일 → 정식파일) - 생성용.
     */
    private void processImageFiles(Long popupId, List<RequestPopupCreate.AttachmentFileInfo> attachmentFiles) {
        log.debug("[PopupService] IMAGE 파일 처리 시작. popupId={}, 파일수={}", popupId, attachmentFiles.size());
        
        try {
            for (RequestPopupCreate.AttachmentFileInfo fileInfo : attachmentFiles) {
                if (fileInfo.getTempFileId() != null && fileInfo.getFileName() != null) {
                    fileService.promoteToFormalFile(fileInfo.getTempFileId(), fileInfo.getFileName());
                }
            }
            log.debug("[PopupService] IMAGE 파일 처리 완료. popupId={}", popupId);
            
        } catch (Exception e) {
            log.warn("[PopupService] IMAGE 파일 처리 중 오류: {}", e.getMessage());
            // 파일 처리 실패해도 팝업 생성은 진행
        }
    }

    /**
     * IMAGE 파일 처리 (임시파일 → 정식파일) - 수정용.
     */
    private void processImageFilesForUpdate(Long popupId, List<RequestPopupUpdate.AttachmentFileInfo> attachmentFiles) {
        log.debug("[PopupService] 수정용 IMAGE 파일 처리 시작. popupId={}, 파일수={}", popupId, attachmentFiles.size());
        
        try {
            for (RequestPopupUpdate.AttachmentFileInfo fileInfo : attachmentFiles) {
                if (fileInfo.getTempFileId() != null && fileInfo.getFileName() != null) {
                    fileService.promoteToFormalFile(fileInfo.getTempFileId(), fileInfo.getFileName());
                }
            }
            log.debug("[PopupService] 수정용 IMAGE 파일 처리 완료. popupId={}", popupId);
            
        } catch (Exception e) {
            log.warn("[PopupService] 수정용 IMAGE 파일 처리 중 오류: {}", e.getMessage());
            // 파일 처리 실패해도 팝업 수정은 진행
        }
    }

    /**
     * 팝업 관련 파일 삭제.
     * 
     * 현재 팝업과 파일 간 직접적인 연결 정보가 없어 개별 파일 삭제가 어려움.
     * 파일 시스템의 정리는 별도 배치 작업이나 파일 서비스의 정리 프로세스에서 처리됨.
     * 
     * 향후 개선사항:
     * - 팝업 엔티티에 imagePath 필드 추가
     * - 또는 팝업-파일 연결 테이블 생성
     * - 파일 메타데이터에서 참조 관계 추적
     */
    private void deletePopupFiles(Long popupId) {
        log.debug("[PopupService] 팝업 관련 파일 정리 시작. popupId={}", popupId);
        
        try {
            // 현재는 팝업과 파일 간 직접 연결 정보가 없어 개별 파일 삭제 불가
            // 임시파일 → 정식파일 변환 과정에서만 파일 서비스와 연동됨
            // 실제 파일 정리는 파일 서비스의 주기적 정리 작업에서 처리
            
            log.info("[PopupService] 팝업 삭제 완료. 관련 파일은 파일 서비스 정리 프로세스에서 처리됨. popupId={}", popupId);
            
        } catch (Exception e) {
            log.warn("[PopupService] 팝업 파일 정리 중 예외 발생: {}, popupId={}", e.getMessage(), popupId);
            // 파일 처리 실패해도 팝업 삭제는 진행 (의도된 동작)
        }
    }

    /**
     * 정렬 타입 유효성 검증.
     */
    private boolean isValidSortType(String sortType) {
        return sortType.equals("CREATED_ASC") || sortType.equals("CREATED_DESC") || 
               sortType.equals("SORT_ORDER_ASC") || sortType.equals("SORT_ORDER_DESC") ||
               sortType.equals("TITLE_ASC") || sortType.equals("TITLE_DESC");
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
}