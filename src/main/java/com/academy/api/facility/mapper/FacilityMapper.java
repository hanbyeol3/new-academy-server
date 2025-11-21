package com.academy.api.facility.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.facility.domain.Facility;
import com.academy.api.facility.dto.RequestFacilityCreate;
import com.academy.api.facility.dto.RequestFacilityUpdate;
import com.academy.api.facility.dto.ResponseFacility;
import com.academy.api.facility.dto.ResponseFacilityListItem;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.repository.UploadFileRepository;
import com.academy.api.file.dto.UploadFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 시설 매퍼.
 * 
 * Entity ↔ DTO 변환 및 파일 연동을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class FacilityMapper {

    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final UploadFileRepository uploadFileRepository;

    /**
     * Request DTO → Entity 변환 (생성용).
     * 
     * @param request 시설 생성 요청 DTO
     * @param createdBy 등록자 ID
     * @return 시설 엔티티
     */
    public Facility toEntity(RequestFacilityCreate request, Long createdBy) {
        if (request == null) {
            return null;
        }

        return Facility.builder()
                .title(request.getTitle())
                .isPublished(request.getIsPublished())
                .createdBy(createdBy)
                .build();
    }

    /**
     * Entity → 상세 Response DTO 변환.
     * 
     * @param entity 시설 엔티티
     * @return 시설 상세 응답 DTO
     */
    public ResponseFacility toResponse(Facility entity) {
        if (entity == null) {
            return null;
        }

        return ResponseFacility.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .isPublished(entity.getIsPublished())
                .coverImage(getLinkedCoverImage(entity.getId()))
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity → 목록 Response DTO 변환.
     * 
     * @param entity 시설 엔티티
     * @return 시설 목록 응답 DTO
     */
    public ResponseFacilityListItem toListItem(Facility entity) {
        if (entity == null) {
            return null;
        }

        return ResponseFacilityListItem.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .isPublished(entity.getIsPublished())
                .coverImage(getLinkedCoverImage(entity.getId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Page<Entity> → ResponseList<DTO> 변환.
     * 
     * @param facilityPage 시설 페이지
     * @return 시설 목록 응답
     */
    public ResponseList<ResponseFacilityListItem> toResponseList(Page<Facility> facilityPage) {
        List<ResponseFacilityListItem> items = facilityPage.getContent()
                .stream()
                .map(this::toListItem)
                .toList();

        return ResponseList.ok(
                items, 
                facilityPage.getTotalElements(), 
                facilityPage.getNumber(), 
                facilityPage.getSize()
        );
    }

    /**
     * Request DTO로 Entity 업데이트.
     * 
     * @param entity 기존 엔티티
     * @param request 수정 요청 DTO
     * @param updatedBy 수정자 ID
     */
    public void updateEntity(Facility entity, RequestFacilityUpdate request, Long updatedBy) {
        if (entity == null || request == null) {
            return;
        }

        entity.update(
                request.getTitle(),
                request.getIsPublished(),
                updatedBy
        );
    }

    /**
     * 시설의 커버 이미지 조회 (단일 파일 최적화).
     * 
     * @param facilityId 시설 ID
     * @return 커버 이미지 파일 정보, 없으면 null
     */
    private UploadFileDto getLinkedCoverImage(Long facilityId) {
        List<UploadFileLink> links = uploadFileLinkRepository.findByOwnerTableAndOwnerIdAndRole("facility", facilityId, FileRole.COVER);
        
        if (links.isEmpty()) {
            return null;
        }
        
        // 단일 커버 이미지이므로 첫 번째 링크만 사용
        Long fileId = links.get(0).getFileId();
        return uploadFileRepository.findById(fileId)
                .map(file -> UploadFileDto.builder()
                        .id(file.getId().toString())
                        .groupKey(null)
                        .fileName(file.getFileName())
                        .ext(file.getExt())
                        .size(file.getSize())
                        .regDate(file.getCreatedAt())
                        .downloadUrl("/api/public/files/download/" + file.getId())
                        .build())
                .orElse(null);
    }

    /**
     * 연결된 파일 정보 조회 (범용).
     * 
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 ID
     * @param role 파일 역할
     * @return 파일 정보 DTO, 없으면 null
     */
    private UploadFileDto getLinkedFile(String ownerTable, Long ownerId, FileRole role) {
        List<UploadFileLink> links = uploadFileLinkRepository.findByOwnerTableAndOwnerIdAndRole(ownerTable, ownerId, role);
        
        if (links.isEmpty()) {
            return null;
        }
        
        // 첫 번째 링크의 파일 정보 조회
        Long fileId = links.get(0).getFileId();
        return uploadFileRepository.findById(fileId)
                .map(file -> UploadFileDto.builder()
                        .id(file.getId().toString())
                        .groupKey(null) // groupKey는 UploadFile 엔티티에 없음
                        .fileName(file.getFileName())
                        .ext(file.getExt())
                        .size(file.getSize())
                        .regDate(file.getCreatedAt())
                        .downloadUrl("/api/public/files/download/" + file.getId())
                        .build())
                .orElse(null);
    }
}