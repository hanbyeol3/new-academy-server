package com.academy.api.academy.service;

import com.academy.api.academy.domain.AcademyInfo;
import com.academy.api.academy.dto.RequestAcademyInfoUpdate;
import com.academy.api.academy.dto.ResponseAcademyInfo;
import com.academy.api.academy.mapper.AcademyInfoMapper;
import com.academy.api.academy.repository.AcademyInfoRepository;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학원 정보 서비스 구현체.
 * 
 * - 단일 설정 테이블 특성 반영
 * - 없으면 기본값으로 생성 후 반환
 * - UPDATE 방식으로 관리
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
public class AcademyInfoServiceImpl implements AcademyInfoService {

    private final AcademyInfoRepository academyInfoRepository;
    private final AcademyInfoMapper academyInfoMapper;

    /**
     * 학원 정보 조회.
     * 
     * 단일 설정 테이블 특성상 데이터가 없으면 기본값으로 생성합니다.
     * 
     * @return 학원 정보
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseAcademyInfo> getAcademyInfo() {
        log.info("[AcademyInfoService] 학원 정보 조회 시작");

        try {
            AcademyInfo academyInfo = academyInfoRepository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyInfoService] 학원 정보가 존재하지 않아 기본값 생성");
                        return createDefaultAcademyInfo();
                    });

            ResponseAcademyInfo response = academyInfoMapper.toResponse(academyInfo);
            
            log.debug("[AcademyInfoService] 학원 정보 조회 완료. id={}, academyName={}", 
                    academyInfo.getId(), academyInfo.getAcademyName());
            
            return ResponseData.ok(response);

        } catch (Exception e) {
            log.error("[AcademyInfoService] 학원 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "학원 정보 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * 학원 정보 수정.
     * 
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    @Override
    @Transactional
    public Response updateAcademyInfo(RequestAcademyInfoUpdate request) {
        log.info("[AcademyInfoService] 학원 정보 수정 시작. academyName={}", 
                request.getAcademyName());

        try {
            // 기존 학원 정보 조회 (없으면 기본값 생성)
            AcademyInfo academyInfo = academyInfoRepository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyInfoService] 학원 정보가 존재하지 않아 기본값으로 생성 후 수정");
                        return createDefaultAcademyInfo();
                    });

            // 엔티티 업데이트
            academyInfoMapper.updateEntity(academyInfo, request);

            // 저장
            AcademyInfo savedAcademyInfo = academyInfoRepository.save(academyInfo);

            log.debug("[AcademyInfoService] 학원 정보 수정 완료. id={}, academyName={}", 
                    savedAcademyInfo.getId(), savedAcademyInfo.getAcademyName());

            return Response.ok("0000", "학원 정보가 수정되었습니다");

        } catch (Exception e) {
            log.error("[AcademyInfoService] 학원 정보 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "학원 정보 수정 중 오류가 발생했습니다");
        }
    }

    /**
     * 기본 학원 정보 생성.
     * 
     * @return 기본값으로 생성된 학원 정보 엔티티
     */
    private AcademyInfo createDefaultAcademyInfo() {
        log.info("[AcademyInfoService] 기본 학원 정보 생성 및 저장");

        AcademyInfo defaultInfo = academyInfoMapper.createDefaultAcademyInfo();
        AcademyInfo savedInfo = academyInfoRepository.save(defaultInfo);

        log.debug("[AcademyInfoService] 기본 학원 정보 생성 완료. id={}", savedInfo.getId());
        return savedInfo;
    }
}