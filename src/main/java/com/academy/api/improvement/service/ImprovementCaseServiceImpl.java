package com.academy.api.improvement.service;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.repository.UploadFileRepository;
import com.academy.api.improvement.domain.*;
import com.academy.api.improvement.dto.*;
import com.academy.api.improvement.mapper.ImprovementCaseMapper;
import com.academy.api.improvement.repository.ImprovementCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 성적 향상 사례 Service 구현체.
 * 
 * - 성적 향상 사례 CRUD 비즈니스 로직 처리
 * - 소프트 삭제(deletedAt) 관리
 * - 비밀글 비밀번호 검증 (BCrypt)
 * - 고정글/공개 상태 관리
 * - 파일 첨부 처리
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
public class ImprovementCaseServiceImpl implements ImprovementCaseService {
    
    private final ImprovementCaseRepository improvementCaseRepository;
    private final ImprovementCaseMapper improvementCaseMapper;
    private final UploadFileRepository uploadFileRepository;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // ==================== 공개 API ====================
    
    @Override
    public ResponseList<ResponseImprovementCasePublicList> getPublicCaseList(
            String keyword, String searchType, String division, String subject,
            String sortBy, Pageable pageable) {
        
        log.info("[ImprovementCaseService] 공개 사례 목록 조회. keyword={}, searchType={}, division={}, subject={}, sortBy={}",
                keyword, searchType, division, subject, sortBy);
        
        // Enum 변환
        Division divisionEnum = parseEnum(division, Division.class);
        Subject subjectEnum = parseEnum(subject, Subject.class);
        
        // 검색 실행
        Page<ImprovementCase> casePage = improvementCaseRepository.searchCasesForPublic(
                keyword, searchType, divisionEnum, subjectEnum, sortBy, pageable);
        
        log.debug("[ImprovementCaseService] 공개 사례 목록 조회 완료. 총 {}건, 현재 페이지 {}건",
                casePage.getTotalElements(), casePage.getNumberOfElements());
        
        return improvementCaseMapper.toPublicListResponse(casePage);
    }
    
    @Override
    public ResponseData<ResponseImprovementCaseDetail> getPublicCaseDetail(Long id) {
        log.info("[ImprovementCaseService] 공개 사례 상세 조회. ID={}", id);
        
        return improvementCaseRepository.findByIdAndPublishedAndNotDeleted(id)
                .<ResponseData<ResponseImprovementCaseDetail>>map(entity -> {
                    // 비밀글인 경우 내용 제한
                    if (entity.getIsSecret()) {
                        log.debug("[ImprovementCaseService] 비밀글 접근 시도. ID={}", id);
                        return ResponseData.error("S401", "비밀번호가 필요한 글입니다.");
                    }
                    
                    // 조회수 증가
                    entity.incrementViewCount();
                    
                    // 상세 응답 생성
                    ResponseImprovementCaseDetail response = buildDetailResponse(entity, true);
                    
                    log.debug("[ImprovementCaseService] 공개 사례 상세 조회 성공. ID={}, 조회수={}", id, entity.getViewCount());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 공개 사례 미존재. ID={}", id);
                    return ResponseData.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    public ResponseData<ResponseImprovementCaseDetail> getSecretCaseDetail(Long id, String password) {
        log.info("[ImprovementCaseService] 비밀글 상세 조회. ID={}", id);
        
        return improvementCaseRepository.findByIdAndPublishedAndNotDeleted(id)
                .<ResponseData<ResponseImprovementCaseDetail>>map(entity -> {
                    // 비밀번호 검증
                    if (!entity.getIsSecret()) {
                        log.debug("[ImprovementCaseService] 비밀글이 아닌 글에 비밀번호 검증 시도. ID={}", id);
                        return ResponseData.error("I400", "비밀글이 아닙니다.");
                    }
                    
                    if (!passwordEncoder.matches(password, entity.getPasswordHash())) {
                        log.warn("[ImprovementCaseService] 비밀번호 불일치. ID={}", id);
                        return ResponseData.error("I401", "비밀번호가 일치하지 않습니다.");
                    }
                    
                    // 조회수 증가
                    entity.incrementViewCount();
                    
                    // 상세 응답 생성
                    ResponseImprovementCaseDetail response = buildDetailResponse(entity, true);
                    
                    log.debug("[ImprovementCaseService] 비밀글 상세 조회 성공. ID={}", id);
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 비밀글 미존재. ID={}", id);
                    return ResponseData.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public ResponseData<Long> createPublicCase(RequestImprovementCaseCreate request, Long[] uploadFileIds) {
        log.info("[ImprovementCaseService] 공개 사례 생성. 제목={}, 작성자={}, 비밀글={}",
                request.getTitle(), request.getAuthorName(), request.getIsSecret());
        
        try {
            // 외부 작성자로 설정
            request.setWriterType(WriterType.EXTERNAL);
            
            // 엔티티 생성
            ImprovementCase entity = improvementCaseMapper.toEntity(request, null);
            ImprovementCase savedEntity = improvementCaseRepository.save(entity);
            
            // 파일 첨부 처리
            if (uploadFileIds != null && uploadFileIds.length > 0) {
                attachFiles(savedEntity, uploadFileIds);
            }
            
            log.info("[ImprovementCaseService] 공개 사례 생성 성공. ID={}", savedEntity.getId());
            return ResponseData.ok("0000", "성적 향상 사례가 등록되었습니다.", savedEntity.getId());
            
        } catch (Exception e) {
            log.error("[ImprovementCaseService] 공개 사례 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("I500", "사례 등록 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Transactional
    public Response updatePublicCase(Long id, RequestImprovementCaseUpdate request, String password, Long[] uploadFileIds) {
        log.info("[ImprovementCaseService] 공개 사례 수정. ID={}", id);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    // 외부 작성자 검증
                    if (entity.getWriterType() != WriterType.EXTERNAL) {
                        log.warn("[ImprovementCaseService] 관리자 작성 글 수정 시도. ID={}", id);
                        return Response.error("I403", "관리자가 작성한 글은 수정할 수 없습니다.");
                    }
                    
                    // 비밀번호 검증
                    if (entity.getIsSecret() && !passwordEncoder.matches(password, entity.getPasswordHash())) {
                        log.warn("[ImprovementCaseService] 수정 시 비밀번호 불일치. ID={}", id);
                        return Response.error("I401", "비밀번호가 일치하지 않습니다.");
                    }
                    
                    // 엔티티 업데이트
                    entity.update(
                            request.getTitle(),
                            request.getDivision(),
                            request.getSubject(),
                            request.getSubjectEnum(),
                            request.getPrevGrade(),
                            request.getPrevGradeType(),
                            request.getNextGrade(),
                            request.getNextGradeType(),
                            request.getContent(),
                            request.getIsPublished(),
                            false, // 외부 작성자는 고정글 설정 불가
                            null
                    );
                    
                    // 파일 첨부 처리
                    if (uploadFileIds != null) {
                        updateAttachedFiles(entity, uploadFileIds);
                    }
                    
                    log.info("[ImprovementCaseService] 공개 사례 수정 성공. ID={}", id);
                    return Response.ok("0000", "성적 향상 사례가 수정되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 수정할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public Response deletePublicCase(Long id, String authorName, String password) {
        log.info("[ImprovementCaseService] 공개 사례 삭제. ID={}, 작성자={}", id, authorName);
        
        return improvementCaseRepository.findByIdAndAuthorNameAndNotDeleted(id, authorName)
                .map(entity -> {
                    // 외부 작성자 검증
                    if (entity.getWriterType() != WriterType.EXTERNAL) {
                        log.warn("[ImprovementCaseService] 관리자 작성 글 삭제 시도. ID={}", id);
                        return Response.error("I403", "관리자가 작성한 글은 삭제할 수 없습니다.");
                    }
                    
                    // 비밀번호 검증
                    if (entity.getIsSecret() && !passwordEncoder.matches(password, entity.getPasswordHash())) {
                        log.warn("[ImprovementCaseService] 삭제 시 비밀번호 불일치. ID={}", id);
                        return Response.error("I401", "비밀번호가 일치하지 않습니다.");
                    }
                    
                    // 소프트 삭제 처리
                    entity.softDelete();
                    
                    log.info("[ImprovementCaseService] 공개 사례 삭제 성공. ID={}", id);
                    return Response.ok("0000", "성적 향상 사례가 삭제되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 삭제할 사례 미존재. ID={}, 작성자={}", id, authorName);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    // ==================== 관리자 API ====================
    
    @Override
    public ResponseList<ResponseImprovementCaseAdminList> getAdminCaseList(
            String keyword, String searchType, String writerType, String division,
            String subject, Boolean isPublished, Boolean isPinned,
            String sortBy, Pageable pageable) {
        
        log.info("[ImprovementCaseService] 관리자 사례 목록 조회. keyword={}, searchType={}, writerType={}, division={}, subject={}, isPublished={}, isPinned={}",
                keyword, searchType, writerType, division, subject, isPublished, isPinned);
        
        // Enum 변환
        WriterType writerTypeEnum = parseEnum(writerType, WriterType.class);
        Division divisionEnum = parseEnum(division, Division.class);
        Subject subjectEnum = parseEnum(subject, Subject.class);
        
        // 검색 실행
        Page<ImprovementCase> casePage = improvementCaseRepository.searchCasesForAdmin(
                keyword, searchType, writerTypeEnum, divisionEnum, subjectEnum,
                isPublished, isPinned, sortBy, pageable);
        
        log.debug("[ImprovementCaseService] 관리자 사례 목록 조회 완료. 총 {}건, 현재 페이지 {}건",
                casePage.getTotalElements(), casePage.getNumberOfElements());
        
        return improvementCaseMapper.toAdminListResponse(casePage);
    }
    
    @Override
    public ResponseData<ResponseImprovementCaseDetail> getAdminCaseDetail(Long id) {
        log.info("[ImprovementCaseService] 관리자 사례 상세 조회. ID={}", id);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    // 관리자는 조회수 증가 없음
                    
                    // 상세 응답 생성
                    ResponseImprovementCaseDetail response = buildDetailResponse(entity, false);
                    
                    log.debug("[ImprovementCaseService] 관리자 사례 상세 조회 성공. ID={}", id);
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 관리자 사례 미존재. ID={}", id);
                    return ResponseData.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public ResponseData<Long> createAdminCase(RequestImprovementCaseCreate request, Long[] uploadFileIds) {
        log.info("[ImprovementCaseService] 관리자 사례 생성. 제목={}, 작성자유형={}, 고정글={}",
                request.getTitle(), request.getWriterType(), request.getIsPinned());
        
        try {
            // 관리자 작성자 정보 설정
            Long createdBy = SecurityUtils.getCurrentUserId();
            
            // 엔티티 생성
            ImprovementCase entity = improvementCaseMapper.toEntity(request, createdBy);
            ImprovementCase savedEntity = improvementCaseRepository.save(entity);
            
            // 파일 첨부 처리
            if (uploadFileIds != null && uploadFileIds.length > 0) {
                attachFiles(savedEntity, uploadFileIds);
            }
            
            log.info("[ImprovementCaseService] 관리자 사례 생성 성공. ID={}", savedEntity.getId());
            return ResponseData.ok("0000", "성적 향상 사례가 등록되었습니다.", savedEntity.getId());
            
        } catch (Exception e) {
            log.error("[ImprovementCaseService] 관리자 사례 생성 실패: {}", e.getMessage(), e);
            return ResponseData.error("I500", "사례 등록 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Transactional
    public Response updateAdminCase(Long id, RequestImprovementCaseUpdate request, Long[] uploadFileIds) {
        log.info("[ImprovementCaseService] 관리자 사례 수정. ID={}", id);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    // 수정자 정보
                    Long updatedBy = SecurityUtils.getCurrentUserId();
                    
                    // 엔티티 업데이트
                    entity.update(
                            request.getTitle(),
                            request.getDivision(),
                            request.getSubject(),
                            request.getSubjectEnum(),
                            request.getPrevGrade(),
                            request.getPrevGradeType(),
                            request.getNextGrade(),
                            request.getNextGradeType(),
                            request.getContent(),
                            request.getIsPublished(),
                            request.getIsPinned(),
                            updatedBy
                    );
                    
                    // 파일 첨부 처리
                    if (uploadFileIds != null) {
                        updateAttachedFiles(entity, uploadFileIds);
                    }
                    
                    log.info("[ImprovementCaseService] 관리자 사례 수정 성공. ID={}", id);
                    return Response.ok("0000", "성적 향상 사례가 수정되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 수정할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public Response deleteAdminCase(Long id) {
        log.info("[ImprovementCaseService] 관리자 사례 삭제. ID={}", id);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    // 소프트 삭제 처리
                    entity.softDelete();
                    
                    log.info("[ImprovementCaseService] 관리자 사례 삭제 성공. ID={}", id);
                    return Response.ok("0000", "성적 향상 사례가 삭제되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 삭제할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public Response restoreAdminCase(Long id) {
        log.info("[ImprovementCaseService] 관리자 사례 복구. ID={}", id);
        
        return improvementCaseRepository.findById(id)
                .map(entity -> {
                    if (!entity.isDeleted()) {
                        log.warn("[ImprovementCaseService] 이미 활성 상태인 사례. ID={}", id);
                        return Response.error("I400", "이미 활성 상태인 사례입니다.");
                    }
                    
                    // 복구 처리
                    entity.restore();
                    
                    log.info("[ImprovementCaseService] 관리자 사례 복구 성공. ID={}", id);
                    return Response.ok("0000", "성적 향상 사례가 복구되었습니다.");
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 복구할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public Response updatePublishStatus(Long id, Boolean isPublished) {
        log.info("[ImprovementCaseService] 공개 상태 변경. ID={}, isPublished={}", id, isPublished);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    entity.updatePublished(isPublished);
                    
                    String message = isPublished ? "공개되었습니다." : "비공개되었습니다.";
                    log.info("[ImprovementCaseService] 공개 상태 변경 성공. ID={}, isPublished={}", id, isPublished);
                    return Response.ok("0000", "성적 향상 사례가 " + message);
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 상태 변경할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    @Override
    @Transactional
    public Response updatePinnedStatus(Long id, Boolean isPinned) {
        log.info("[ImprovementCaseService] 고정글 상태 변경. ID={}, isPinned={}", id, isPinned);
        
        return improvementCaseRepository.findByIdAndNotDeleted(id)
                .map(entity -> {
                    entity.updatePinned(isPinned);
                    
                    String message = isPinned ? "고정되었습니다." : "고정 해제되었습니다.";
                    log.info("[ImprovementCaseService] 고정글 상태 변경 성공. ID={}, isPinned={}", id, isPinned);
                    return Response.ok("0000", "성적 향상 사례가 " + message);
                })
                .orElseGet(() -> {
                    log.warn("[ImprovementCaseService] 상태 변경할 사례 미존재. ID={}", id);
                    return Response.error("I404", "성적 향상 사례를 찾을 수 없습니다.");
                });
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * 상세 응답 생성.
     */
    private ResponseImprovementCaseDetail buildDetailResponse(ImprovementCase entity, boolean isPublicApi) {
        ResponseImprovementCaseDetail response = improvementCaseMapper.toDetailResponse(entity);
        
        // 첨부파일 정보 설정
        List<ResponseFileInfo> attachments = getAttachments(entity.getId());
        response = ResponseImprovementCaseDetail.builder()
                .id(response.getId())
                .title(response.getTitle())
                .writerType(response.getWriterType())
                .authorName(response.getAuthorName())
                .division(response.getDivision())
                .divisionText(response.getDivisionText())
                .subject(response.getSubject())
                .subjectEnum(response.getSubjectEnum())
                .subjectText(response.getSubjectText())
                .prevGrade(response.getPrevGrade())
                .prevGradeType(response.getPrevGradeType())
                .prevGradeText(response.getPrevGradeText())
                .nextGrade(response.getNextGrade())
                .nextGradeType(response.getNextGradeType())
                .nextGradeText(response.getNextGradeText())
                .content(response.getContent())
                .viewCount(response.getViewCount())
                .isPublished(response.getIsPublished())
                .isPinned(response.getIsPinned())
                .isSecret(response.getIsSecret())
                .attachments(attachments)
                .navigation(getNavigation(entity.getId(), isPublicApi))
                .createdBy(response.getCreatedBy())
                .createdByName(response.getCreatedByName())
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(response.getUpdatedByName())
                .updatedAt(response.getUpdatedAt())
                .build();
        
        return response;
    }
    
    /**
     * 이전/다음글 정보 조회.
     */
    private ResponseImprovementCaseNavigation getNavigation(Long currentId, boolean isPublicApi) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        
        List<ImprovementCase> previousList;
        List<ImprovementCase> nextList;
        
        if (isPublicApi) {
            previousList = improvementCaseRepository.findPreviousPublicCase(currentId, pageRequest);
            nextList = improvementCaseRepository.findNextPublicCase(currentId, pageRequest);
        } else {
            previousList = improvementCaseRepository.findPreviousCase(currentId, pageRequest);
            nextList = improvementCaseRepository.findNextCase(currentId, pageRequest);
        }
        
        ResponseImprovementCaseNavigation.NavigationItem previous = null;
        if (!previousList.isEmpty()) {
            ImprovementCase prevCase = previousList.get(0);
            previous = ResponseImprovementCaseNavigation.NavigationItem.builder()
                    .id(prevCase.getId())
                    .title(prevCase.getTitle())
                    .isSecret(prevCase.getIsSecret())
                    .build();
        }
        
        ResponseImprovementCaseNavigation.NavigationItem next = null;
        if (!nextList.isEmpty()) {
            ImprovementCase nextCase = nextList.get(0);
            next = ResponseImprovementCaseNavigation.NavigationItem.builder()
                    .id(nextCase.getId())
                    .title(nextCase.getTitle())
                    .isSecret(nextCase.getIsSecret())
                    .build();
        }
        
        return ResponseImprovementCaseNavigation.builder()
                .previous(previous)
                .next(next)
                .build();
    }
    
    /**
     * 첨부파일 목록 조회.
     */
    private List<ResponseFileInfo> getAttachments(Long caseId) {
        List<UploadFileLink> fileLinks = uploadFileLinkRepository.findByOwnerTableAndOwnerId("improvement_cases", caseId);
        return fileLinks.stream()
                .map(ResponseFileInfo::from)
                .toList();
    }
    
    /**
     * 파일 첨부 처리.
     */
    private void attachFiles(ImprovementCase entity, Long[] uploadFileIds) {
        for (Long fileId : uploadFileIds) {
            UploadFileLink link = UploadFileLink.builder()
                    .fileId(fileId)
                    .ownerTable("improvement_cases")
                    .ownerId(entity.getId())
                    .role(FileRole.ATTACHMENT)
                    .createdBy(entity.getCreatedBy())
                    .build();
            uploadFileLinkRepository.save(link);
        }
    }
    
    /**
     * 첨부파일 업데이트.
     */
    private void updateAttachedFiles(ImprovementCase entity, Long[] uploadFileIds) {
        // 기존 파일 링크 삭제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerId("improvement_cases", entity.getId());
        
        // 새 파일 첨부
        if (uploadFileIds.length > 0) {
            attachFiles(entity, uploadFileIds);
        }
    }
    
    /**
     * 문자열을 Enum으로 변환.
     */
    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ImprovementCaseService] 유효하지 않은 {} 값: {}", enumClass.getSimpleName(), value);
            return null;
        }
    }
}