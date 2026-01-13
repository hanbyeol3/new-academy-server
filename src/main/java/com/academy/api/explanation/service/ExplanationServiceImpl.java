package com.academy.api.explanation.service;

import com.academy.api.explanation.domain.*;
import com.academy.api.explanation.dto.*;
import com.academy.api.explanation.mapper.ExplanationMapper;
import com.academy.api.explanation.repository.ExplanationRepository;
import com.academy.api.explanation.repository.ExplanationReservationRepository;
import com.academy.api.explanation.repository.ExplanationScheduleRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.service.FileService;
import com.academy.api.file.dto.FileReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static com.academy.api.explanation.domain.Gender.*;
import static com.academy.api.explanation.domain.AcademicTrack.*;
import static com.academy.api.explanation.domain.CanceledBy.*;

/**
 * 설명회 서비스 구현체.
 * 
 * - 설명회 CRUD 비즈니스 로직 처리
 * - 동시성 제어 및 트랜잭션 관리
 * - 예약 정원 관리 및 중복 방지
 * - 통일된 에러 처리 및 로깅
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
public class ExplanationServiceImpl implements ExplanationService {

    private final ExplanationRepository explanationRepository;
    private final ExplanationScheduleRepository scheduleRepository;
    private final ExplanationReservationRepository reservationRepository;
    private final ExplanationMapper explanationMapper;
    private final FileService fileService;
    private final UploadFileLinkRepository uploadFileLinkRepository;

    // ===== 설명회 관리 =====

    @Override
    @Transactional
    public ResponseData<Long> createExplanation(RequestExplanationCreate request, Long createdBy) {
        log.info("[ExplanationService] 설명회 생성 시작. division={}, title={}, createdBy={}", 
                request.getDivision(), request.getTitle(), createdBy);

        try {
            // 1. 설명회 생성
            Explanation explanation = explanationMapper.toEntity(request, createdBy);
            Explanation savedExplanation = explanationRepository.save(explanation);
            
            log.debug("[ExplanationService] 설명회 저장 완료. id={}", savedExplanation.getId());

            // 2. 초기 회차 생성
            ExplanationSchedule schedule = explanationMapper.toScheduleEntity(
                    request.getInitialSchedule(), 
                    savedExplanation.getId(), 
                    createdBy
            );
            ExplanationSchedule savedSchedule = scheduleRepository.save(schedule);

            log.debug("[ExplanationService] 초기 회차 저장 완료. scheduleId={}, roundNo={}", 
                    savedSchedule.getId(), savedSchedule.getRoundNo());

            // 3. 인라인 이미지 처리
            if (request.getInlineImages() != null && !request.getInlineImages().isEmpty()) {
                log.debug("[ExplanationService] 인라인 이미지 처리 시작. 이미지 수={}", request.getInlineImages().size());
                
                // 임시파일을 정식파일로 변환하고 링크 생성
                Map<String, Long> inlineTempMap = createFileLinkFromTempFiles(
                    request.getInlineImages(), savedExplanation.getId(), "EXPLANATION", createdBy);
                
                // content의 임시 URL을 정식 URL로 변환
                if (!inlineTempMap.isEmpty()) {
                    String convertedContent = fileService.convertTempUrlsInContent(request.getContent(), inlineTempMap);
                    if (!convertedContent.equals(request.getContent())) {
                        savedExplanation.updateContent(convertedContent);
                    }
                }
                
                log.debug("[ExplanationService] 인라인 이미지 처리 완료. 링크 수={}", inlineTempMap.size());
            }

            log.info("[ExplanationService] 설명회 생성 완료. explanationId={}, scheduleId={}", 
                    savedExplanation.getId(), savedSchedule.getId());

            return ResponseData.ok("0000", "설명회가 생성되었습니다.", savedExplanation.getId());

        } catch (Exception e) {
            log.error("[ExplanationService] 설명회 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseList<ResponseExplanationListItem> getExplanationListForAdmin(ExplanationDivision division,
                                                                               Boolean isPublished,
                                                                               String keyword,
                                                                               Pageable pageable) {
        log.info("[ExplanationService] 관리자용 설명회 목록 조회. division={}, isPublished={}, keyword={}, page={}", 
                division, isPublished, keyword, pageable.getPageNumber());

        Page<Explanation> explanationPage = explanationRepository.searchExplanationsForAdmin(
                division, isPublished, keyword, pageable);

        // 설명회 ID 목록 추출
        List<Long> explanationIds = explanationPage.getContent().stream()
                .map(Explanation::getId)
                .toList();

        // 회차 목록 조회 및 그룹핑
        Map<Long, List<ExplanationSchedule>> schedulesByExplanationId = getSchedulesByExplanationIds(explanationIds);

        log.debug("[ExplanationService] 관리자용 설명회 목록 조회 완료. 총 {}건, 현재 페이지 {}건", 
                explanationPage.getTotalElements(), explanationPage.getNumberOfElements());

        return explanationMapper.toListResponse(explanationPage, schedulesByExplanationId);
    }

    @Override
    public ResponseList<ResponseExplanationListItem> getPublishedExplanationList(ExplanationDivision division,
                                                                                String keyword,
                                                                                Pageable pageable) {
        log.info("[ExplanationService] 공개용 설명회 목록 조회. division={}, keyword={}, page={}", 
                division, keyword, pageable.getPageNumber());

        Page<Explanation> explanationPage = explanationRepository.searchPublishedExplanations(
                division, keyword, pageable);

        // 설명회 ID 목록 추출
        List<Long> explanationIds = explanationPage.getContent().stream()
                .map(Explanation::getId)
                .toList();

        // 회차 목록 조회 및 그룹핑
        Map<Long, List<ExplanationSchedule>> schedulesByExplanationId = getSchedulesByExplanationIds(explanationIds);

        log.debug("[ExplanationService] 공개용 설명회 목록 조회 완료. 총 {}건, 현재 페이지 {}건", 
                explanationPage.getTotalElements(), explanationPage.getNumberOfElements());

        return explanationMapper.toListResponse(explanationPage, schedulesByExplanationId);
    }

    @Override
    public ResponseData<ResponseExplanation> getExplanationForAdmin(Long id) {
        log.info("[ExplanationService] 관리자용 설명회 상세 조회. id={}", id);

        Explanation explanation = explanationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND));

        List<ExplanationSchedule> schedules = scheduleRepository
                .findByExplanationIdOrderByStartAtAsc(id);

        // 인라인 이미지 조회
        List<ResponseFileInfo> inlineImages = getInlineImagesByExplanationId(id);

        ResponseExplanation response = explanationMapper.toResponse(explanation, schedules, inlineImages);
        
        log.debug("[ExplanationService] 관리자용 설명회 상세 조회 완료. id={}, 회차수={}, 인라인이미지수={}", 
                id, schedules.size(), inlineImages.size());

        return ResponseData.ok("0000", "조회 성공", response);
    }

    @Override
    @Transactional
    public ResponseData<ResponseExplanation> getPublishedExplanation(Long id) {
        log.info("[ExplanationService] 공개용 설명회 상세 조회 (조회수 증가). id={}", id);

        Explanation explanation = explanationRepository.findByIdAndPublished(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND));

        // 조회수 증가
        explanationRepository.incrementViewCount(id);
        
        List<ExplanationSchedule> schedules = scheduleRepository
                .findByExplanationIdOrderByStartAtAsc(id);

        // 인라인 이미지 조회
        List<ResponseFileInfo> inlineImages = getInlineImagesByExplanationId(id);

        ResponseExplanation response = explanationMapper.toResponse(explanation, schedules, inlineImages);

        log.debug("[ExplanationService] 공개용 설명회 상세 조회 완료. id={}, 조회수 증가, 회차수={}, 인라인이미지수={}", 
                id, schedules.size(), inlineImages.size());

        return ResponseData.ok("0000", "조회 성공", response);
    }

    @Override
    @Transactional
    public Response updateExplanation(Long id, RequestExplanationUpdate request, Long updatedBy) {
        log.info("[ExplanationService] 설명회 수정. id={}, title={}, updatedBy={}", 
                id, request.getTitle(), updatedBy);

        Explanation explanation = explanationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND));

        explanation.update(request.getTitle(), request.getContent(), 
                          request.getIsPublished(), updatedBy);

        // 인라인 이미지 처리
        if (request.getNewInlineImages() != null && !request.getNewInlineImages().isEmpty()) {
            log.debug("[ExplanationService] 새로운 인라인 이미지 추가. 이미지 수={}", request.getNewInlineImages().size());
            addFileLinks(request.getNewInlineImages(), id, "EXPLANATION", updatedBy);
        }

        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            log.debug("[ExplanationService] 기존 인라인 이미지 삭제. 삭제 수={}", request.getDeleteInlineImageFileIds().size());
            deleteSelectedFileLinks(id, request.getDeleteInlineImageFileIds());
        }

        // 4. content에서 삭제된 이미지 URL 제거 (FAQ와 동일한 로직)
        String finalContent = explanation.getContent();
        
        if (request.getDeleteInlineImageFileIds() != null && !request.getDeleteInlineImageFileIds().isEmpty()) {
            finalContent = fileService.removeDeletedImageUrlsFromContent(finalContent, request.getDeleteInlineImageFileIds());
            log.info("[ExplanationService] 삭제된 이미지 URL 제거 완료. ID={}, 삭제된이미지={}개", 
                    id, request.getDeleteInlineImageFileIds().size());
        }
        
        // 4-2. 모든 temp URL을 정식 URL로 변환 (기존 + 신규 포함)
        String convertedContent = fileService.convertAllTempUrlsInContent(finalContent);
        
        // 4-3. Content가 변경된 경우 업데이트
        if (!convertedContent.equals(explanation.getContent())) {
            // 엔티티 다시 조회하여 최신 상태 확보
            Explanation currentExplanation = explanationRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND));
            
            // 도메인 메서드를 사용해서 content 업데이트
            currentExplanation.updateContent(convertedContent);
            
            log.debug("[ExplanationService] content URL 변환 완료. ID={}, 원본길이={}, 변환후길이={}", 
                    id, explanation.getContent().length(), convertedContent.length());
        }

        explanationRepository.save(explanation);

        log.debug("[ExplanationService] 설명회 수정 완료. id={}", id);

        return Response.ok("0000", "설명회가 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response deleteExplanation(Long id) {
        log.info("[ExplanationService] 설명회 삭제. id={}", id);

        if (!explanationRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND);
        }

        // CASCADE 삭제로 회차 및 예약도 함께 삭제됨
        explanationRepository.deleteById(id);

        log.debug("[ExplanationService] 설명회 삭제 완료. id={}", id);

        return Response.ok("0000", "설명회가 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response toggleExplanationPublishStatus(Long id, Long updatedBy) {
        log.info("[ExplanationService] 설명회 공개/비공개 전환. id={}, updatedBy={}", id, updatedBy);

        Explanation explanation = explanationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND));

        Boolean newStatus = !explanation.getIsPublished();
        explanation.updatePublishStatus(newStatus, updatedBy);

        explanationRepository.save(explanation);

        log.debug("[ExplanationService] 설명회 공개 상태 변경 완료. id={}, isPublished={}", 
                id, newStatus);

        return Response.ok("0000", 
                newStatus ? "설명회가 공개되었습니다." : "설명회가 비공개로 변경되었습니다.");
    }

    // ===== 회차 관리 =====

    @Override
    @Transactional
    public ResponseData<Long> createExplanationSchedule(Long explanationId, 
                                                       RequestExplanationScheduleCreate request, 
                                                       Long createdBy) {
        log.info("[ExplanationService] 회차 생성. explanationId={}, roundNo={}, createdBy={}", 
                explanationId, request.getRoundNo(), createdBy);

        // 설명회 존재 확인
        if (!explanationRepository.existsById(explanationId)) {
            throw new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND);
        }

        // 회차 번호 중복 확인
        if (scheduleRepository.existsByExplanationIdAndRoundNo(explanationId, request.getRoundNo())) {
            log.warn("[ExplanationService] 중복된 회차 번호. explanationId={}, roundNo={}", 
                    explanationId, request.getRoundNo());
            return ResponseData.error("E001", "이미 존재하는 회차 번호입니다.");
        }

        ExplanationSchedule schedule = explanationMapper.toScheduleEntity(request, explanationId, createdBy);
        ExplanationSchedule savedSchedule = scheduleRepository.save(schedule);

        log.debug("[ExplanationService] 회차 생성 완료. scheduleId={}", savedSchedule.getId());

        return ResponseData.ok("0000", "회차가 생성되었습니다.", savedSchedule.getId());
    }

    @Override
    @Transactional
    public Response updateExplanationSchedule(Long explanationId, Long scheduleId,
                                             RequestExplanationScheduleUpdate request, Long updatedBy) {
        log.info("[ExplanationService] 회차 수정. explanationId={}, scheduleId={}, updatedBy={}", 
                explanationId, scheduleId, updatedBy);

        // 설명회와 회차 매칭 확인
        ExplanationSchedule schedule = scheduleRepository
                .findByIdAndExplanationId(scheduleId, explanationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_MISMATCH));

        // 정원 축소 검증 (현재 예약 인원보다 적게 설정 불가)
        if (request.getCapacity() != null && 
            schedule.getReservedCount() > request.getCapacity()) {
            log.warn("[ExplanationService] 정원 축소 불가. 현재예약={}, 요청정원={}", 
                    schedule.getReservedCount(), request.getCapacity());
            throw new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_CAPACITY_VIOLATION);
        }

        schedule.update(
                request.getStartAt(),
                request.getEndAt(),
                request.getLocation(),
                request.getApplyStartAt(),
                request.getApplyEndAt(),
                request.getStatus(),
                request.getCapacity(),
                updatedBy
        );

        scheduleRepository.save(schedule);

        log.debug("[ExplanationService] 회차 수정 완료. scheduleId={}", scheduleId);

        return Response.ok("0000", "회차가 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response deleteExplanationSchedule(Long explanationId, Long scheduleId) {
        log.info("[ExplanationService] 회차 삭제. explanationId={}, scheduleId={}", 
                explanationId, scheduleId);

        // 설명회와 회차 매칭 확인
        ExplanationSchedule schedule = scheduleRepository
                .findByIdAndExplanationId(scheduleId, explanationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_MISMATCH));

        // CASCADE 삭제로 예약도 함께 삭제됨
        scheduleRepository.delete(schedule);

        log.debug("[ExplanationService] 회차 삭제 완료. scheduleId={}", scheduleId);

        return Response.ok("0000", "회차가 삭제되었습니다.");
    }

    // ===== 예약 관리 =====

    @Override
    @Transactional
    public ResponseData<Long> createReservation(RequestExplanationReservationCreate request, String clientIp) {
        log.info("[ExplanationService] 예약 신청. scheduleId={}, applicantName={}, applicantPhone={}", 
                request.getScheduleId(), request.getApplicantName(), request.getApplicantPhone());

        return processReservationCreate(request, clientIp, false);
    }

    @Override
    @Transactional
    public ResponseData<Long> createReservationByAdmin(RequestExplanationReservationCreate request, String clientIp) {
        log.info("[ExplanationService] 관리자 예약 신청. scheduleId={}, applicantName={}, applicantPhone={}", 
                request.getScheduleId(), request.getApplicantName(), request.getApplicantPhone());

        return processReservationCreate(request, clientIp, true);
    }

    /**
     * 예약 생성 공통 로직.
     * 
     * @param request 예약 요청
     * @param clientIp 클라이언트 IP
     * @param isAdminRequest 관리자 요청 여부
     * @return 예약 결과
     */
    private ResponseData<Long> processReservationCreate(RequestExplanationReservationCreate request, 
                                                       String clientIp, boolean isAdminRequest) {
        
        // 1. 회차 조회 및 락 획득 (동시성 제어)
        ExplanationSchedule schedule = scheduleRepository
                .findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_NOT_FOUND));

        // 2. 예약 가능 여부 검증 (관리자는 일부 검증 생략 가능)
        if (!isAdminRequest) {
            if (!schedule.isReservable()) {
                log.warn("[ExplanationService] 예약 불가능한 회차. scheduleId={}", request.getScheduleId());
                throw new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_NOT_RESERVABLE);
            }
        }

        // 3. 중복 예약 확인
        if (reservationRepository.findConfirmedReservation(
                request.getScheduleId(), request.getApplicantPhone()).isPresent()) {
            log.warn("[ExplanationService] 중복 예약 시도. scheduleId={}, phone={}", 
                    request.getScheduleId(), request.getApplicantPhone());
            throw new BusinessException(ErrorCode.EXPLANATION_RESERVATION_DUPLICATE);
        }

        // 4. 정원 확인
        if (schedule.getCapacity() != null && 
            schedule.getReservedCount() >= schedule.getCapacity()) {
            log.warn("[ExplanationService] 정원 마감. scheduleId={}, capacity={}, reserved={}", 
                    request.getScheduleId(), schedule.getCapacity(), schedule.getReservedCount());
            throw new BusinessException(ErrorCode.EXPLANATION_SCHEDULE_FULL);
        }

        // 5. 예약 생성
        ExplanationReservation reservation = explanationMapper.toReservationEntity(request, clientIp);
        ExplanationReservation savedReservation = reservationRepository.save(reservation);

        // 6. 예약 인원수 증가 (캐시 갱신)
        schedule.incrementReservedCount();
        scheduleRepository.save(schedule);

        log.debug("[ExplanationService] 예약 생성 완료. reservationId={}, scheduleId={}, reservedCount={}", 
                savedReservation.getId(), schedule.getId(), schedule.getReservedCount());

        return ResponseData.ok("0000", "예약이 완료되었습니다.", savedReservation.getId());
    }

    // ===== 유틸리티 메서드 =====

    /**
     * 설명회 ID 목록에 해당하는 회차 목록을 조회하고 그룹핑.
     * 
     * @param explanationIds 설명회 ID 목록
     * @return 설명회별 회차 목록 Map
     */
    private Map<Long, List<ExplanationSchedule>> getSchedulesByExplanationIds(List<Long> explanationIds) {
        if (explanationIds.isEmpty()) {
            return Map.of();
        }

        List<ExplanationSchedule> allSchedules = scheduleRepository
                .findByExplanationIdInOrderByExplanationIdAndStartAtAsc(explanationIds);

        return allSchedules.stream()
                .collect(Collectors.groupingBy(ExplanationSchedule::getExplanationId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseList<ResponseExplanationReservation> getReservationListForAdmin(
            Long explanationId, Long scheduleId, String keyword, String status, 
            String startDate, String endDate, Pageable pageable) {
        
        log.info("[ExplanationService] 관리자용 예약 목록 조회 시작. explanationId={}, scheduleId={}, keyword={}, status={}", 
                explanationId, scheduleId, keyword, status);

        // 상태 enum 변환
        ReservationStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[ExplanationService] 유효하지 않은 예약 상태: {}", status);
            }
        }

        // 날짜 범위 파싱
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            }
        } catch (DateTimeParseException e) {
            log.warn("[ExplanationService] 날짜 파싱 오류: startDate={}, endDate={}", startDate, endDate);
        }

        Page<ExplanationReservation> reservationPage = reservationRepository.searchReservationsForAdmin(
                explanationId, scheduleId, keyword, statusEnum, startDateTime, endDateTime, pageable);

        log.debug("[ExplanationService] 관리자용 예약 목록 조회 완료. 총 {}건", reservationPage.getTotalElements());
        
        return explanationMapper.toReservationListResponse(reservationPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseExplanationReservation> getReservation(Long reservationId) {
        log.info("[ExplanationService] 예약 상세 조회 시작. reservationId={}", reservationId);

        ExplanationReservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            log.warn("[ExplanationService] 예약을 찾을 수 없음. reservationId={}", reservationId);
            return ResponseData.error("R404", "예약을 찾을 수 없습니다.");
        }

        ResponseExplanationReservation response = explanationMapper.toReservationResponse(reservation);
        log.debug("[ExplanationService] 예약 상세 조회 완료. reservationId={}", reservationId);

        return ResponseData.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseList<ResponseExplanationReservation> lookupReservationsByPhone(
            String applicantPhone, String keyword, Pageable pageable) {
        
        log.info("[ExplanationService] 전화번호 기반 예약 조회 시작. applicantPhone={}, keyword={}", 
                applicantPhone, keyword);

        if (applicantPhone == null || applicantPhone.trim().isEmpty()) {
            log.warn("[ExplanationService] 전화번호가 제공되지 않음");
            return ResponseList.error("R400", "전화번호를 입력해주세요.");
        }

        Page<ExplanationReservation> reservationPage = reservationRepository.findByApplicantPhoneAndKeyword(
                applicantPhone.trim(), keyword, pageable);

        log.debug("[ExplanationService] 전화번호 기반 예약 조회 완료. 총 {}건", reservationPage.getTotalElements());
        
        return explanationMapper.toReservationListResponse(reservationPage);
    }

    @Override
    @Transactional
    public Response cancelReservationByUser(Long reservationId) {
        log.info("[ExplanationService] 사용자 예약 취소 시작. reservationId={}", reservationId);

        ExplanationReservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            log.warn("[ExplanationService] 예약을 찾을 수 없음. reservationId={}", reservationId);
            return Response.error("R404", "예약을 찾을 수 없습니다.");
        }

        // 이미 취소된 예약인지 확인 (멱등 처리)
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            log.info("[ExplanationService] 이미 취소된 예약. reservationId={}", reservationId);
            return Response.ok("0000", "이미 취소된 예약입니다.");
        }

        // 예약 취소 처리
        reservation.cancel(CanceledBy.USER);

        // 회차 예약 인원수 감소 (락 사용)
        ExplanationSchedule schedule = scheduleRepository.findByIdForUpdate(reservation.getScheduleId())
                .orElse(null);
        
        if (schedule != null) {
            schedule.decrementReservedCount();
            scheduleRepository.save(schedule);
            log.debug("[ExplanationService] 회차 예약 인원수 감소. scheduleId={}, 현재 예약수={}", 
                    schedule.getId(), schedule.getReservedCount());
        }

        log.info("[ExplanationService] 사용자 예약 취소 완료. reservationId={}", reservationId);
        return Response.ok("0000", "예약이 취소되었습니다.");
    }

    @Override
    @Transactional
    public Response cancelReservationByAdmin(Long reservationId, String reason) {
        log.info("[ExplanationService] 관리자 예약 취소 시작. reservationId={}, reason={}", reservationId, reason);

        ExplanationReservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            log.warn("[ExplanationService] 예약을 찾을 수 없음. reservationId={}", reservationId);
            return Response.error("R404", "예약을 찾을 수 없습니다.");
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            log.info("[ExplanationService] 이미 취소된 예약. reservationId={}", reservationId);
            return Response.ok("0000", "이미 취소된 예약입니다.");
        }

        // 관리자 취소 처리 (사유 포함)
        reservation.cancel(CanceledBy.MANAGER);
        if (reason != null && !reason.trim().isEmpty()) {
            reservation.updateMemo(reservation.getMemo() + " [관리자 취소 사유: " + reason.trim() + "]");
        }

        // 회차 예약 인원수 감소
        ExplanationSchedule schedule = scheduleRepository.findByIdForUpdate(reservation.getScheduleId())
                .orElse(null);
        
        if (schedule != null) {
            schedule.decrementReservedCount();
            scheduleRepository.save(schedule);
            log.debug("[ExplanationService] 회차 예약 인원수 감소. scheduleId={}, 현재 예약수={}", 
                    schedule.getId(), schedule.getReservedCount());
        }

        log.info("[ExplanationService] 관리자 예약 취소 완료. reservationId={}, reason={}", reservationId, reason);
        return Response.ok("0000", "예약이 취소되었습니다.");
    }

    @Override
    @Transactional
    public Response updateReservationMemo(Long reservationId, String memo) {
        log.info("[ExplanationService] 예약 메모 수정 시작. reservationId={}", reservationId);

        ExplanationReservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            log.warn("[ExplanationService] 예약을 찾을 수 없음. reservationId={}", reservationId);
            return Response.error("R404", "예약을 찾을 수 없습니다.");
        }

        reservation.updateMemo(memo);

        log.info("[ExplanationService] 예약 메모 수정 완료. reservationId={}", reservationId);
        return Response.ok("0000", "메모가 수정되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseData<Map<String, Object>> getReservationStatistics(Long explanationId) {
        log.info("[ExplanationService] 예약 통계 조회 시작. explanationId={}", explanationId);

        // 설명회 존재 확인
        if (explanationId != null) {
            Explanation explanation = explanationRepository.findById(explanationId).orElse(null);
            if (explanation == null) {
                log.warn("[ExplanationService] 설명회를 찾을 수 없음. explanationId={}", explanationId);
                return ResponseData.error("E404", "설명회를 찾을 수 없습니다.");
            }
        }

        // 전체 통계 조회
        long totalReservations = reservationRepository.countByExplanationId(explanationId);
        long confirmedReservations = reservationRepository.countByExplanationIdAndStatus(
                explanationId, ReservationStatus.CONFIRMED);
        long canceledReservations = reservationRepository.countByExplanationIdAndStatus(
                explanationId, ReservationStatus.CANCELED);

        // 일별 예약 통계 (최근 7일)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        List<Map<String, Object>> dailyStats = reservationRepository.getDailyReservationStats(
                explanationId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        // 회차별 통계
        List<Map<String, Object>> scheduleStats = reservationRepository.getReservationStatsBySchedule(explanationId);

        Map<String, Object> statistics = Map.of(
                "totalReservations", totalReservations,
                "confirmedReservations", confirmedReservations,
                "canceledReservations", canceledReservations,
                "dailyStats", dailyStats,
                "scheduleStats", scheduleStats
        );

        log.debug("[ExplanationService] 예약 통계 조회 완료. 총 예약수={}, 확정={}, 취소={}", 
                totalReservations, confirmedReservations, canceledReservations);

        return ResponseData.ok(statistics);
    }

    @Override
    public void exportReservationListToExcel(Long explanationId, Long scheduleId, String keyword, String status,
                                            String startDate, String endDate, HttpServletResponse response) {
        log.info("[ExplanationService] 예약 목록 엑셀 다운로드 시작. explanationId={}, scheduleId={}, keyword={}, status={}",
                explanationId, scheduleId, keyword, status);

        try {
            // 상태 및 날짜 파싱
            ReservationStatus reservationStatus = parseReservationStatus(status);
            LocalDateTime startDateTime = parseDateTime(startDate);
            LocalDateTime endDateTime = parseDateTime(endDate);

            // 예약 목록 조회 (페이징 없이 모든 데이터)
            List<ExplanationReservation> reservations = reservationRepository.findReservationsForExport(
                    explanationId, scheduleId, reservationStatus, keyword);

            log.debug("[ExplanationService] 엑셀 다운로드용 예약 조회 완료. 건수={}", reservations.size());

            // 엑셀 파일 생성
            Workbook workbook = createReservationExcelWorkbook(reservations);

            // 파일명 생성 (URL 인코딩)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filenameKorean = String.format("설명회_예약목록_%s.xlsx", timestamp);
            String filenameEncoded = URLEncoder.encode(filenameKorean, StandardCharsets.UTF_8);

            // HTTP 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", 
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", 
                    "explanation_reservations_" + timestamp + ".xlsx", filenameEncoded));
            response.setHeader("Cache-Control", "no-cache");

            // 엑셀 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();

            log.info("[ExplanationService] 엑셀 다운로드 완료. 파일명={}, 건수={}", filenameKorean, reservations.size());

        } catch (Exception e) {
            log.error("[ExplanationService] 엑셀 다운로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 예약 상태 문자열을 Enum으로 변환.
     */
    private ReservationStatus parseReservationStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ExplanationService] 유효하지 않은 예약 상태: {}. null로 처리", status);
            return null;
        }
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환.
     */
    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // yyyy-MM-dd 형식을 yyyy-MM-dd 00:00:00으로 변환
            LocalDate date = LocalDate.parse(dateString.trim());
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            log.warn("[ExplanationService] 유효하지 않은 날짜 형식: {}. null로 처리", dateString);
            return null;
        }
    }

    /**
     * 예약 목록 엑셀 워크북 생성.
     */
    private Workbook createReservationExcelWorkbook(List<ExplanationReservation> reservations) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("예약 목록");

        // 헤더 스타일 생성
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        // 일반 셀 스타일 생성
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        // 날짜 스타일 생성
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(cellStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "예약ID", "신청자명", "신청자 전화번호", "학생명", "학생 전화번호", 
                "성별", "계열", "학교명", "학년", "예약상태", 
                "메모", "마케팅수신동의", "예약생성일시", "취소일시", "취소주체"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        // 데이터 행 생성
        int rowIndex = 1;
        for (ExplanationReservation reservation : reservations) {
            Row row = sheet.createRow(rowIndex++);

            // 예약 ID
            row.createCell(0).setCellValue(reservation.getId());

            // 신청자 정보
            row.createCell(1).setCellValue(reservation.getApplicantName());
            row.createCell(2).setCellValue(reservation.getApplicantPhone());

            // 학생 정보
            row.createCell(3).setCellValue(reservation.getStudentName() != null ? reservation.getStudentName() : "");
            row.createCell(4).setCellValue(reservation.getStudentPhone() != null ? reservation.getStudentPhone() : "");

            // 성별
            String genderValue = "";
            if (reservation.getGender() != null) {
                genderValue = switch (reservation.getGender()) {
                    case M -> "남성";
                    case F -> "여성";
                };
            }
            row.createCell(5).setCellValue(genderValue);

            // 계열
            String trackValue = "";
            if (reservation.getAcademicTrack() != null) {
                trackValue = switch (reservation.getAcademicTrack()) {
                    case LIBERAL_ARTS -> "문과";
                    case SCIENCE -> "이과";
                    case UNDECIDED -> "미정";
                };
            }
            row.createCell(6).setCellValue(trackValue);

            // 학교/학년
            row.createCell(7).setCellValue(reservation.getSchoolName() != null ? reservation.getSchoolName() : "");
            row.createCell(8).setCellValue(reservation.getGrade() != null ? reservation.getGrade() : "");

            // 예약 상태
            String statusValue = switch (reservation.getStatus()) {
                case CONFIRMED -> "확정";
                case CANCELED -> "취소";
            };
            row.createCell(9).setCellValue(statusValue);

            // 메모 및 기타
            row.createCell(10).setCellValue(reservation.getMemo() != null ? reservation.getMemo() : "");
            row.createCell(11).setCellValue(reservation.getIsMarketingAgree() ? "동의" : "비동의");

            // 예약 생성일시
            Cell createdAtCell = row.createCell(12);
            createdAtCell.setCellValue(reservation.getCreatedAt());
            createdAtCell.setCellStyle(dateStyle);

            // 취소 정보
            if (reservation.getCanceledAt() != null) {
                Cell canceledAtCell = row.createCell(13);
                canceledAtCell.setCellValue(reservation.getCanceledAt());
                canceledAtCell.setCellStyle(dateStyle);
            } else {
                row.createCell(13).setCellValue("");
            }

            String canceledByValue = "";
            if (reservation.getCanceledBy() != null) {
                canceledByValue = switch (reservation.getCanceledBy()) {
                    case USER -> "사용자";
                    case MANAGER -> "관리자";
                    case SYSTEM -> "시스템";
                };
            }
            row.createCell(14).setCellValue(canceledByValue);

            // 모든 셀에 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellStyle() != dateStyle) {
                    cell.setCellStyle(cellStyle);
                }
            }
        }

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    // ===== 인라인 이미지 처리 도우미 메서드 =====

    /**
     * 임시파일을 정식파일로 변환하고 파일링크 생성.
     * 
     * @param inlineImages 인라인 이미지 정보 목록
     * @param entityId 설명회 ID
     * @param entityType 엔티티 타입
     * @param createdBy 생성자 ID
     * @return 임시파일ID -> 정식파일ID 매핑 Map
     */
    private Map<String, Long> createFileLinkFromTempFiles(
            List<RequestExplanationCreate.InlineImageInfo> inlineImages, 
            Long entityId, String entityType, Long createdBy) {
        
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        for (RequestExplanationCreate.InlineImageInfo imageInfo : inlineImages) {
            try {
                // 임시파일을 정식파일로 변환
                Long fileId = fileService.promoteToFormalFile(imageInfo.getTempFileId(), imageInfo.getFileName());
                
                // 파일링크 생성
                UploadFileLink fileLink = UploadFileLink.builder()
                        .fileId(fileId)
                        .ownerTable("explanations")
                        .ownerId(entityId)
                        .role(FileRole.INLINE)
                        .createdBy(createdBy)
                        .build();
                
                uploadFileLinkRepository.save(fileLink);
                tempToFormalMap.put(imageInfo.getTempFileId(), fileId);
                
                log.debug("[ExplanationService] 인라인 이미지 파일링크 생성. tempFileId={}, formalFileId={}", 
                        imageInfo.getTempFileId(), fileId);
                        
            } catch (Exception e) {
                log.error("[ExplanationService] 인라인 이미지 처리 실패. tempFileId={}: {}", 
                        imageInfo.getTempFileId(), e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
        
        return tempToFormalMap;
    }

    /**
     * 새로운 파일링크 추가.
     * 
     * @param newFiles 새로 추가할 파일 목록
     * @param entityId 설명회 ID
     * @param entityType 엔티티 타입
     * @param createdBy 생성자 ID
     */
    private void addFileLinks(List<FileReference> newFiles, Long entityId, String entityType, Long createdBy) {
        for (FileReference fileRef : newFiles) {
            try {
                // 임시파일을 정식파일로 변환
                Long fileId = fileService.promoteToFormalFile(fileRef.getTempFileId(), fileRef.getFileName());
                
                // 파일링크 생성
                UploadFileLink fileLink = UploadFileLink.builder()
                        .fileId(fileId)
                        .ownerTable("explanations")
                        .ownerId(entityId)
                        .role(FileRole.INLINE)
                        .createdBy(createdBy)
                        .build();
                
                uploadFileLinkRepository.save(fileLink);
                
                log.debug("[ExplanationService] 새로운 인라인 이미지 파일링크 생성. tempFileId={}, formalFileId={}", 
                        fileRef.getTempFileId(), fileId);
                        
            } catch (Exception e) {
                log.error("[ExplanationService] 새로운 인라인 이미지 처리 실패. tempFileId={}: {}", 
                        fileRef.getTempFileId(), e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 선택된 파일링크 삭제.
     * 
     * @param explanationId 설명회 ID
     * @param fileIds 삭제할 파일 ID 목록
     */
    private void deleteSelectedFileLinks(Long explanationId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("[ExplanationService] 삭제할 인라인 이미지 없음. explanationId={}", explanationId);
            return;
        }
        
        log.info("🗑️ [ExplanationService] 인라인 이미지 선택 삭제 실행. explanationId={}, 삭제파일={}개", 
                explanationId, fileIds.size());
        
        // 파일링크 삭제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRoleAndFileIdIn(
                "explanations", explanationId, FileRole.INLINE, fileIds);
        
        log.debug("[ExplanationService] 인라인 이미지 선택 삭제 완료. explanationId={}, 삭제된파일IDs={}", 
                explanationId, fileIds);
    }

    /**
     * 설명회 ID로 인라인 이미지 목록 조회.
     * 
     * @param explanationId 설명회 ID
     * @return 인라인 이미지 정보 목록
     */
    private List<ResponseFileInfo> getInlineImagesByExplanationId(Long explanationId) {
        List<UploadFileLink> fileLinks = uploadFileLinkRepository
                .findByOwnerTableAndOwnerIdAndRole("explanations", explanationId, FileRole.INLINE);
        
        return fileLinks.stream()
                .map(this::mapToResponseFileInfo)
                .toList();
    }

    /**
     * UploadFileLink를 ResponseFileInfo로 변환.
     * 
     * @param fileLink 파일링크
     * @return 파일 정보 DTO
     */
    private ResponseFileInfo mapToResponseFileInfo(UploadFileLink fileLink) {
        try {
            var fileInfoResponse = fileService.getFileInfo(fileLink.getFileId().toString());
            if (fileInfoResponse.getData() != null) {
                var fileData = fileInfoResponse.getData();
                return ResponseFileInfo.builder()
                        .fileId(fileLink.getFileId().toString())
                        .fileName(fileData.getOriginalFileName())
                        .originalName(fileData.getOriginalFileName())
                        .size(fileData.getFileSize())
                        .url(fileData.getDownloadUrl())
                        .build();
            }
        } catch (Exception e) {
            log.warn("[ExplanationService] 파일 정보 조회 실패. fileId={}: {}", 
                    fileLink.getFileId(), e.getMessage());
        }
        
        // 실패한 경우 기본 정보만 반환
        return ResponseFileInfo.builder()
                .fileId(fileLink.getFileId().toString())
                .fileName("파일 정보 없음")
                .size(0L)
                .url("")
                .build();
    }

}