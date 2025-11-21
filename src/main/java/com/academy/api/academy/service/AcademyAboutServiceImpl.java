package com.academy.api.academy.service;

import com.academy.api.academy.domain.AcademyAbout;
import com.academy.api.academy.domain.AcademyAboutDetails;
import com.academy.api.academy.dto.*;
import com.academy.api.academy.mapper.AcademyAboutMapper;
import com.academy.api.academy.repository.AcademyAboutRepository;
import com.academy.api.academy.repository.AcademyAboutDetailsRepository;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학원 소개 정보 통합 서비스 구현체.
 * 
 * - AcademyAbout과 AcademyAboutDetails 통합 관리
 * - File 서비스 연동으로 이미지 처리
 * - 단일 설정 테이블과 다중 CRUD 테이블 혼합 처리
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
public class AcademyAboutServiceImpl implements AcademyAboutService {

    private final AcademyAboutRepository academyAboutRepository;
    private final AcademyAboutDetailsRepository academyAboutDetailsRepository;
    private final AcademyAboutMapper academyAboutMapper;

    // ==================== AcademyAbout 관련 구현 ====================

    /**
     * 학원 소개 정보 조회 (관리자용).
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseAcademyAbout> getAcademyAbout() {
        log.info("[AcademyAboutService] 학원 소개 정보 조회 시작 (관리자용)");

        try {
            AcademyAbout academyAbout = academyAboutRepository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyAboutService] 학원 소개 정보가 존재하지 않아 기본값 생성");
                        return createDefaultAcademyAbout();
                    });

            ResponseAcademyAbout response = academyAboutMapper.toResponse(academyAbout);
            
            log.debug("[AcademyAboutService] 학원 소개 정보 조회 완료. id={}, mainTitle={}", 
                    academyAbout.getId(), academyAbout.getMainTitle());
            
            return ResponseData.ok(response);

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "학원 소개 정보 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 정보 수정.
     */
    @Override
    @Transactional
    public Response updateAcademyAbout(RequestAcademyAboutUpdate request, Long updatedBy) {
        log.info("[AcademyAboutService] 학원 소개 정보 수정 시작. mainTitle={}, updatedBy={}", 
                request.getMainTitle(), updatedBy);

        try {
            // 기존 학원 소개 정보 조회 (없으면 기본값 생성)
            AcademyAbout academyAbout = academyAboutRepository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyAboutService] 학원 소개 정보가 존재하지 않아 기본값으로 생성 후 수정");
                        return createDefaultAcademyAbout();
                    });

            // 엔티티 업데이트
            academyAboutMapper.updateEntity(academyAbout, request, updatedBy);

            // 저장
            AcademyAbout savedAcademyAbout = academyAboutRepository.save(academyAbout);

            log.debug("[AcademyAboutService] 학원 소개 정보 수정 완료. id={}, mainTitle={}", 
                    savedAcademyAbout.getId(), savedAcademyAbout.getMainTitle());

            return Response.ok("0000", "학원 소개 정보가 수정되었습니다");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 정보 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "학원 소개 정보 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 정보 조회 (공개용).
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseAcademyAbout> getPublicAcademyAbout() {
        log.info("[AcademyAboutService] 학원 소개 정보 조회 시작 (공개용)");

        try {
            AcademyAbout academyAbout = academyAboutRepository.findFirstRow()
                    .orElse(null);

            if (academyAbout == null) {
                log.warn("[AcademyAboutService] 공개용 학원 소개 정보가 존재하지 않습니다");
                return ResponseData.error("N404", "학원 소개 정보를 찾을 수 없습니다");
            }

            ResponseAcademyAbout response = academyAboutMapper.toResponse(academyAbout);
            
            log.debug("[AcademyAboutService] 공개용 학원 소개 정보 조회 완료. id={}", academyAbout.getId());
            
            return ResponseData.ok(response);

        } catch (Exception e) {
            log.error("[AcademyAboutService] 공개용 학원 소개 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "학원 소개 정보 조회 중 오류가 발생했습니다");
        }
    }

    // ==================== AcademyAboutDetails 관련 구현 ====================

    /**
     * 학원 소개 상세 목록 조회 (관리자용).
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseList<ResponseAcademyAboutDetails> getDetailsList() {
        log.info("[AcademyAboutService] 학원 소개 상세 목록 조회 시작 (관리자용)");

        try {
            List<AcademyAboutDetails> detailsList = academyAboutDetailsRepository.findAllOrderBySortOrderAscIdAsc();
            
            log.debug("[AcademyAboutService] 학원 소개 상세 목록 조회 완료. 총 {}건", detailsList.size());
            
            return academyAboutMapper.toDetailsResponseList(detailsList, "상세 목록 조회 성공");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 상세 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.error("E500", "상세 목록 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 상세 정보 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createDetails(RequestAcademyAboutDetailsCreate request, Long createdBy) {
        log.info("[AcademyAboutService] 학원 소개 상세 정보 생성 시작. detailTitle={}, createdBy={}", 
                request.getDetailTitle(), createdBy);

        try {
            // 기본 학원 소개 정보 조회/생성
            AcademyAbout academyAbout = academyAboutRepository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyAboutService] 기본 학원 소개 정보가 없어 생성");
                        return createDefaultAcademyAbout();
                    });

            // 정렬 순서 설정 (요청값이 없으면 마지막 순서로 설정)
            if (request.getSortOrder() == null || request.getSortOrder() <= 0) {
                Integer maxSortOrder = academyAboutDetailsRepository.findMaxSortOrderByAbout(academyAbout);
                request.setSortOrder(maxSortOrder + 1);
                log.debug("[AcademyAboutService] 정렬 순서 자동 설정: {}", request.getSortOrder());
            }

            // 엔티티 생성 및 저장
            AcademyAboutDetails detailsEntity = academyAboutMapper.toDetailsEntity(request, academyAbout, createdBy);
            AcademyAboutDetails savedDetails = academyAboutDetailsRepository.save(detailsEntity);

            log.debug("[AcademyAboutService] 학원 소개 상세 정보 생성 완료. id={}, detailTitle={}", 
                    savedDetails.getId(), savedDetails.getDetailTitle());

            return ResponseData.ok("0000", "상세 정보가 생성되었습니다", savedDetails.getId());

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 상세 정보 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "상세 정보 생성 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 상세 정보 수정.
     */
    @Override
    @Transactional
    public Response updateDetails(Long id, RequestAcademyAboutDetailsUpdate request, Long updatedBy) {
        log.info("[AcademyAboutService] 학원 소개 상세 정보 수정 시작. id={}, detailTitle={}, updatedBy={}", 
                id, request.getDetailTitle(), updatedBy);

        try {
            // 기존 상세 정보 조회
            AcademyAboutDetails details = academyAboutDetailsRepository.findByIdWithAbout(id)
                    .orElse(null);

            if (details == null) {
                log.warn("[AcademyAboutService] 수정할 상세 정보가 존재하지 않습니다. id={}", id);
                return Response.error("N404", "상세 정보를 찾을 수 없습니다");
            }

            // 엔티티 업데이트
            academyAboutMapper.updateDetailsEntity(details, request, updatedBy);

            // 저장
            AcademyAboutDetails savedDetails = academyAboutDetailsRepository.save(details);

            log.debug("[AcademyAboutService] 학원 소개 상세 정보 수정 완료. id={}, detailTitle={}", 
                    savedDetails.getId(), savedDetails.getDetailTitle());

            return Response.ok("0000", "상세 정보가 수정되었습니다");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 상세 정보 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "상세 정보 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 상세 정보 삭제.
     */
    @Override
    @Transactional
    public Response deleteDetails(Long id) {
        log.info("[AcademyAboutService] 학원 소개 상세 정보 삭제 시작. id={}", id);

        try {
            // 기존 상세 정보 조회
            if (!academyAboutDetailsRepository.existsById(id)) {
                log.warn("[AcademyAboutService] 삭제할 상세 정보가 존재하지 않습니다. id={}", id);
                return Response.error("N404", "상세 정보를 찾을 수 없습니다");
            }

            // 삭제
            academyAboutDetailsRepository.deleteById(id);

            log.debug("[AcademyAboutService] 학원 소개 상세 정보 삭제 완료. id={}", id);

            return Response.ok("0000", "상세 정보가 삭제되었습니다");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 상세 정보 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "상세 정보 삭제 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 상세 정보 순서 변경.
     */
    @Override
    @Transactional
    public Response updateDetailsOrder(RequestDetailsOrderUpdate request, Long updatedBy) {
        log.info("[AcademyAboutService] 학원 소개 상세 정보 순서 변경 시작. 변경 항목 수={}, updatedBy={}", 
                request.getItems().size(), updatedBy);

        try {
            // 일괄 순서 업데이트
            for (RequestDetailsOrderUpdate.OrderItem item : request.getItems()) {
                academyAboutDetailsRepository.updateSortOrder(item.getId(), item.getSortOrder(), updatedBy);
                log.debug("[AcademyAboutService] 순서 업데이트: id={}, sortOrder={}", 
                        item.getId(), item.getSortOrder());
            }

            log.debug("[AcademyAboutService] 학원 소개 상세 정보 순서 변경 완료");

            return Response.ok("0000", "순서가 변경되었습니다");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 학원 소개 상세 정보 순서 변경 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "순서 변경 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 소개 상세 목록 조회 (공개용).
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseList<ResponseAcademyAboutDetails> getPublicDetailsList() {
        log.info("[AcademyAboutService] 학원 소개 상세 목록 조회 시작 (공개용)");

        try {
            List<AcademyAboutDetails> detailsList = academyAboutDetailsRepository.findAllOrderBySortOrderAscIdAsc();
            
            log.debug("[AcademyAboutService] 공개용 학원 소개 상세 목록 조회 완료. 총 {}건", detailsList.size());
            
            return academyAboutMapper.toDetailsResponseList(detailsList, "상세 목록 조회 성공");

        } catch (Exception e) {
            log.error("[AcademyAboutService] 공개용 학원 소개 상세 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.error("E500", "상세 목록 조회 중 오류가 발생했습니다");
        }
    }

    // ==================== 내부 헬퍼 메서드 ====================

    /**
     * 기본 학원 소개 정보 생성.
     * 
     * @return 기본값으로 생성된 학원 소개 엔티티
     */
    private AcademyAbout createDefaultAcademyAbout() {
        log.info("[AcademyAboutService] 기본 학원 소개 정보 생성 및 저장");

        AcademyAbout defaultAbout = academyAboutMapper.createDefaultAcademyAbout(1L); // 시스템 생성자 ID: 1
        AcademyAbout savedAbout = academyAboutRepository.save(defaultAbout);

        log.debug("[AcademyAboutService] 기본 학원 소개 정보 생성 완료. id={}", savedAbout.getId());
        return savedAbout;
    }
}