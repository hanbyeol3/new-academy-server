package com.academy.api.facility.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.facility.domain.Facility;
import com.academy.api.facility.dto.RequestFacilityCreate;
import com.academy.api.facility.dto.RequestFacilityUpdate;
import com.academy.api.facility.dto.ResponseFacility;
import com.academy.api.facility.dto.ResponseFacilityListItem;
import com.academy.api.facility.mapper.FacilityMapper;
import com.academy.api.facility.repository.FacilityRepository;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.repository.UploadFileLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시설 서비스 구현체.
 * 
 * - 시설 안내 CRUD 비즈니스 로직 처리
 * - 커버 이미지 파일 연동 관리
 * - 공개/비공개 상태 관리
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
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;
    private final UploadFileLinkRepository uploadFileLinkRepository;

    /**
     * 시설 목록 조회 (관리자용).
     */
    @Override
    public ResponseList<ResponseFacilityListItem> getFacilityList(String title, Boolean isPublished, Pageable pageable) {
        log.info("[FacilityService] 시설 목록 조회 시작. title={}, isPublished={}, page={}, size={}", 
                title, isPublished, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Facility> facilityPage;
            
            if (title != null && isPublished != null) {
                facilityPage = facilityRepository.findByTitleContainingAndIsPublished(title, isPublished, pageable);
            } else if (title != null) {
                facilityPage = facilityRepository.findByTitleContaining(title, pageable);
            } else if (isPublished != null) {
                facilityPage = facilityRepository.findByIsPublished(isPublished, pageable);
            } else {
                facilityPage = facilityRepository.findAllFacilities(pageable);
            }

            ResponseList<ResponseFacilityListItem> response = facilityMapper.toResponseList(facilityPage);
            
            log.debug("[FacilityService] 시설 목록 조회 완료. 조회된 항목 수={}, 전체 항목 수={}", 
                    facilityPage.getNumberOfElements(), facilityPage.getTotalElements());
            
            return response;

        } catch (Exception e) {
            log.error("[FacilityService] 시설 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(
                    java.util.Collections.emptyList(),
                    0L,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
    }

    /**
     * 공개 시설 목록 조회 (공개 API용).
     */
    @Override
    public ResponseList<ResponseFacilityListItem> getPublicFacilityList(Pageable pageable) {
        log.info("[FacilityService] 공개 시설 목록 조회 시작. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Facility> facilityPage = facilityRepository.findPublishedFacilities(pageable);
            ResponseList<ResponseFacilityListItem> response = facilityMapper.toResponseList(facilityPage);
            
            log.debug("[FacilityService] 공개 시설 목록 조회 완료. 조회된 항목 수={}", facilityPage.getNumberOfElements());
            
            return response;

        } catch (Exception e) {
            log.error("[FacilityService] 공개 시설 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(
                    java.util.Collections.emptyList(),
                    0L,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
    }

    /**
     * 시설 상세 조회 (관리자용).
     */
    @Override
    public ResponseData<ResponseFacility> getFacility(Long id) {
        log.info("[FacilityService] 시설 상세 조회 시작. id={}", id);

        return facilityRepository.findById(id)
                .map(facility -> {
                    ResponseFacility response = facilityMapper.toResponse(facility);
                    log.debug("[FacilityService] 시설 상세 조회 완료. id={}, title={}", id, facility.getTitle());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[FacilityService] 시설을 찾을 수 없음. id={}", id);
                    return ResponseData.error("F404", "시설을 찾을 수 없습니다");
                });
    }

    /**
     * 공개 시설 상세 조회 (공개 API용).
     */
    @Override
    public ResponseData<ResponseFacility> getPublicFacility(Long id) {
        log.info("[FacilityService] 공개 시설 상세 조회 시작. id={}", id);

        return facilityRepository.findByIdAndPublished(id)
                .map(facility -> {
                    ResponseFacility response = facilityMapper.toResponse(facility);
                    log.debug("[FacilityService] 공개 시설 상세 조회 완료. id={}, title={}", id, facility.getTitle());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[FacilityService] 공개된 시설을 찾을 수 없음. id={}", id);
                    return ResponseData.error("F404", "시설을 찾을 수 없습니다");
                });
    }

    /**
     * 시설 등록.
     */
    @Override
    @Transactional
    public ResponseData<Long> createFacility(RequestFacilityCreate request, Long createdBy) {
        log.info("[FacilityService] 시설 등록 시작. title={}, coverImageFileId={}, createdBy={}", 
                request.getTitle(), request.getCoverImageFileId(), createdBy);

        try {
            // 시설 엔티티 생성 및 저장
            Facility facility = facilityMapper.toEntity(request, createdBy);
            Facility savedFacility = facilityRepository.save(facility);
            
            log.debug("[FacilityService] 시설 저장 완료. id={}", savedFacility.getId());

            // 커버 이미지 파일 연결
            linkSingleFile(savedFacility.getId(), request.getCoverImageFileId());

            log.debug("[FacilityService] 시설 등록 완료. id={}, title={}", savedFacility.getId(), savedFacility.getTitle());
            
            return ResponseData.ok("0000", "시설이 등록되었습니다", savedFacility.getId());

        } catch (Exception e) {
            log.error("[FacilityService] 시설 등록 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "시설 등록 중 오류가 발생했습니다");
        }
    }

    /**
     * 시설 수정.
     */
    @Override
    @Transactional
    public Response updateFacility(Long id, RequestFacilityUpdate request, Long updatedBy) {
        log.info("[FacilityService] 시설 수정 시작. id={}, title={}, updatedBy={}", 
                id, request.getTitle(), updatedBy);

        return facilityRepository.findById(id)
                .map(facility -> {
                    try {
                        // 시설 정보 업데이트
                        facilityMapper.updateEntity(facility, request, updatedBy);
                        Facility savedFacility = facilityRepository.save(facility);
                        
                        log.debug("[FacilityService] 시설 정보 업데이트 완료. id={}", id);

                        // 커버 이미지 변경 처리
                        if (request.getCoverImageFileId() != null) {
                            replaceSingleFile(id, request.getCoverImageFileId());
                        }

                        log.debug("[FacilityService] 시설 수정 완료. id={}, title={}", id, savedFacility.getTitle());
                        
                        return Response.ok("0000", "시설이 수정되었습니다");
                    } catch (Exception e) {
                        log.error("[FacilityService] 시설 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "시설 수정 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[FacilityService] 수정할 시설을 찾을 수 없음. id={}", id);
                    return Response.error("F404", "시설을 찾을 수 없습니다");
                });
    }

    /**
     * 시설 공개/비공개 상태 전환.
     */
    @Override
    @Transactional
    public Response toggleFacilityPublished(Long id, Long updatedBy) {
        log.info("[FacilityService] 시설 공개 상태 전환 시작. id={}, updatedBy={}", id, updatedBy);

        return facilityRepository.findById(id)
                .map(facility -> {
                    boolean beforeStatus = facility.isPublished();
                    facility.togglePublished();
                    facilityRepository.save(facility);
                    
                    String statusMessage = facility.isPublished() ? "공개" : "비공개";
                    log.debug("[FacilityService] 시설 공개 상태 전환 완료. id={}, {} → {}", 
                            id, beforeStatus ? "공개" : "비공개", statusMessage);
                    
                    return Response.ok("0000", "시설이 " + statusMessage + "로 변경되었습니다");
                })
                .orElseGet(() -> {
                    log.warn("[FacilityService] 상태 전환할 시설을 찾을 수 없음. id={}", id);
                    return Response.error("F404", "시설을 찾을 수 없습니다");
                });
    }

    /**
     * 시설 삭제.
     */
    @Override
    @Transactional
    public Response deleteFacility(Long id) {
        log.info("[FacilityService] 시설 삭제 시작. id={}", id);

        return facilityRepository.findById(id)
                .map(facility -> {
                    try {
                        // 연결된 파일 링크 먼저 삭제
                        uploadFileLinkRepository.deleteByOwnerTableAndOwnerId("facility", id);
                        log.debug("[FacilityService] 시설 연결 파일 해제 완료. facilityId={}", id);
                        
                        // 시설 삭제
                        facilityRepository.delete(facility);
                        
                        log.debug("[FacilityService] 시설 삭제 완료. id={}, title={}", id, facility.getTitle());
                        
                        return Response.ok("0000", "시설이 삭제되었습니다");
                    } catch (Exception e) {
                        log.error("[FacilityService] 시설 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "시설 삭제 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[FacilityService] 삭제할 시설을 찾을 수 없음. id={}", id);
                    return Response.error("F404", "시설을 찾을 수 없습니다");
                });
    }

    /**
     * 시설에 단일 커버 이미지 파일 연결.
     * 
     * @param facilityId 시설 ID
     * @param fileId 파일 ID (null인 경우 연결하지 않음)
     */
    private void linkSingleFile(Long facilityId, String fileId) {
        if (fileId != null && !fileId.trim().isEmpty()) {
            try {
                UploadFileLink fileLink = UploadFileLink.createFacilityCoverImage(fileId, facilityId);
                uploadFileLinkRepository.save(fileLink);
                log.debug("[FacilityService] 단일 파일 연결 완료. facilityId={}, fileId={}", facilityId, fileId);
            } catch (IllegalArgumentException e) {
                log.warn("[FacilityService] 유효하지 않은 파일 ID. facilityId={}, fileId={}, error={}", 
                        facilityId, fileId, e.getMessage());
                throw e;
            }
        }
    }

    /**
     * 시설의 기존 커버 이미지를 새 파일로 교체.
     * 
     * @param facilityId 시설 ID
     * @param newFileId 새 파일 ID (null인 경우 기존 파일만 해제)
     */
    private void replaceSingleFile(Long facilityId, String newFileId) {
        // 기존 파일 연결 해제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole("facility", facilityId, FileRole.COVER);
        log.debug("[FacilityService] 기존 커버 이미지 연결 해제 완료. facilityId={}", facilityId);
        
        // 새 파일 연결
        linkSingleFile(facilityId, newFileId);
    }
}