package com.academy.api.academy.mapper;

import com.academy.api.academy.domain.AcademyAbout;
import com.academy.api.academy.domain.AcademyAboutDetails;
import com.academy.api.academy.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import com.academy.api.data.responses.common.ResponseList;

import java.util.List;

/**
 * 학원 소개 정보 통합 매퍼.
 * 
 * AcademyAbout과 AcademyAboutDetails 모든 Entity ↔ DTO 변환을 담당합니다.
 */
@Component
public class AcademyAboutMapper {

    // ==================== AcademyAbout 관련 매핑 ====================

    /**
     * AcademyAbout Entity → Response DTO 변환.
     * 
     * @param entity 학원 소개 엔티티
     * @return 학원 소개 응답 DTO
     */
    public ResponseAcademyAbout toResponse(AcademyAbout entity) {
        if (entity == null) {
            return null;
        }

        return ResponseAcademyAbout.builder()
                .id(entity.getId())
                .mainTitle(entity.getMainTitle())
                .mainPointTitle(entity.getMainPointTitle())
                .mainDescription(entity.getMainDescription())
                .mainImagePath(entity.getMainImagePath())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Request DTO → AcademyAbout Entity 변환 (생성용).
     * 
     * @param request 수정 요청 DTO
     * @param createdBy 등록자 ID
     * @return 학원 소개 엔티티
     */
    public AcademyAbout toEntity(RequestAcademyAboutUpdate request, Long createdBy) {
        if (request == null) {
            return null;
        }

        return AcademyAbout.builder()
                .mainTitle(request.getMainTitle())
                .mainPointTitle(request.getMainPointTitle())
                .mainDescription(request.getMainDescription())
                .mainImagePath(request.getMainImagePath())
                .createdBy(createdBy)
                .build();
    }

    /**
     * Request DTO로 AcademyAbout Entity 업데이트.
     * 
     * @param entity 기존 엔티티
     * @param request 수정 요청 DTO
     * @param updatedBy 수정자 ID
     */
    public void updateEntity(AcademyAbout entity, RequestAcademyAboutUpdate request, Long updatedBy) {
        if (entity == null || request == null) {
            return;
        }

        entity.update(
                request.getMainTitle(),
                request.getMainPointTitle(), 
                request.getMainDescription(),
                request.getMainImagePath(),
                updatedBy
        );
    }

    /**
     * 기본 학원 소개 정보 생성.
     * 
     * @param createdBy 등록자 ID
     * @return 기본값으로 설정된 학원 소개 엔티티
     */
    public AcademyAbout createDefaultAcademyAbout(Long createdBy) {
        return AcademyAbout.builder()
                .mainTitle("학원 소개 타이틀을 입력하세요")
                .mainPointTitle("포인트 타이틀을 입력하세요")
                .mainDescription("학원 소개 내용을 입력하세요")
                .mainImagePath("")
                .createdBy(createdBy)
                .build();
    }

    // ==================== AcademyAboutDetails 관련 매핑 ====================

    /**
     * AcademyAboutDetails Entity → Response DTO 변환.
     * 
     * @param entity 학원 소개 상세 엔티티
     * @return 학원 소개 상세 응답 DTO
     */
    public ResponseAcademyAboutDetails toDetailsResponse(AcademyAboutDetails entity) {
        if (entity == null) {
            return null;
        }

        return ResponseAcademyAboutDetails.builder()
                .id(entity.getId())
                .aboutId(entity.getAbout() != null ? entity.getAbout().getId() : null)
                .detailTitle(entity.getDetailTitle())
                .detailDescription(entity.getDetailDescription())
                .sortOrder(entity.getSortOrder())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * AcademyAboutDetails Entity 목록 → Response DTO 목록 변환.
     * 
     * @param entities 엔티티 목록
     * @return 응답 DTO 목록
     */
    public List<ResponseAcademyAboutDetails> toDetailsResponseList(List<AcademyAboutDetails> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDetailsResponse)
                .toList();
    }

    /**
     * AcademyAboutDetails Entity 목록 → ResponseList 변환 (페이징용).
     * 
     * @param page 페이징된 엔티티
     * @return ResponseList
     */
    public ResponseList<ResponseAcademyAboutDetails> toDetailsResponseList(Page<AcademyAboutDetails> page) {
        List<ResponseAcademyAboutDetails> content = toDetailsResponseList(page.getContent());
        
        return ResponseList.ok(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * List → Page 변환 (페이징 처리용).
     * 
     * @param entities 엔티티 목록
     * @return ResponseList
     */
    public ResponseList<ResponseAcademyAboutDetails> toDetailsResponseList(List<AcademyAboutDetails> entities, String message) {
        List<ResponseAcademyAboutDetails> content = toDetailsResponseList(entities);
        
        return ResponseList.ok(content, (long) content.size(), 0, content.size());
    }

    /**
     * RequestDetailsCreate DTO → AcademyAboutDetails Entity 변환.
     * 
     * @param request 생성 요청 DTO
     * @param about 연관된 학원 소개 정보
     * @param createdBy 등록자 ID
     * @return 학원 소개 상세 엔티티
     */
    public AcademyAboutDetails toDetailsEntity(RequestAcademyAboutDetailsCreate request, AcademyAbout about, Long createdBy) {
        if (request == null) {
            return null;
        }

        return AcademyAboutDetails.builder()
                .about(about)
                .detailTitle(request.getDetailTitle())
                .detailDescription(request.getDetailDescription())
                .sortOrder(request.getSortOrder())
                .createdBy(createdBy)
                .build();
    }

    /**
     * Request DTO로 AcademyAboutDetails Entity 업데이트.
     * 
     * @param entity 기존 엔티티
     * @param request 수정 요청 DTO
     * @param updatedBy 수정자 ID
     */
    public void updateDetailsEntity(AcademyAboutDetails entity, RequestAcademyAboutDetailsUpdate request, Long updatedBy) {
        if (entity == null || request == null) {
            return;
        }

        entity.update(
                request.getDetailTitle(),
                request.getDetailDescription(),
                request.getSortOrder(),
                updatedBy
        );
    }
}