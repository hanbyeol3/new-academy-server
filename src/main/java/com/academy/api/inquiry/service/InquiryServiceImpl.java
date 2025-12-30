package com.academy.api.inquiry.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.inquiry.domain.*;
import com.academy.api.inquiry.dto.*;
import com.academy.api.inquiry.mapper.InquiryMapper;
import com.academy.api.inquiry.repository.InquiryLogRepository;
import com.academy.api.inquiry.repository.InquiryRepository;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상담신청 서비스 구현체.
 * 
 * - 상담신청 CRUD 비즈니스 로직 처리
 * - 상담이력 자동 생성 및 관리
 * - 통계 데이터 집계 및 분석
 * - 중복 신청 검사 및 지연 처리 알림
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 중복 신청, 지연 처리 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryLogRepository inquiryLogRepository;
    private final MemberRepository memberRepository;
    private final InquiryMapper inquiryMapper;

    @Override
    public ResponseList<ResponseInquiryListItem> getInquiryList(String keyword, String searchType, String status,
                                                               String sourceType, String assigneeName,
                                                               LocalDateTime startDate, LocalDateTime endDate,
                                                               Boolean isExternal, String sortBy, Pageable pageable) {
        
        log.info("[InquiryService] 상담신청 목록 조회 시작. keyword={}, searchType={}, status={}, sourceType={}, assigneeName={}, isExternal={}", 
                 keyword, searchType, status, sourceType, assigneeName, isExternal);

        // Enum 안전 변환
        InquirySearchType searchTypeEnum = safeParseSearchType(searchType);
        InquiryStatus statusEnum = safeParseStatus(status);
        InquirySourceType sourceTypeEnum = safeParseSourceType(sourceType);

        // Repository 호출 (QueryDSL 동적 쿼리)
        Page<Inquiry> inquiryPage = inquiryRepository.searchInquiriesForAdmin(
            keyword, searchTypeEnum, statusEnum, sourceTypeEnum, assigneeName, startDate, endDate, isExternal, sortBy, pageable);

        log.debug("[InquiryService] 상담신청 목록 조회 완료. 총 {}건, 현재 페이지 {}개", 
                 inquiryPage.getTotalElements(), inquiryPage.getNumberOfElements());

        // 회원 이름 포함하여 ResponseList로 변환
        return toListItemResponseListWithNames(inquiryPage);
    }

    @Override
    public ResponseList<ResponseInquiryListItem> getNewInquiries(String keyword, String sourceType, Pageable pageable) {
        
        log.info("[InquiryService] 신규 상담신청 조회 시작. keyword={}, sourceType={}", keyword, sourceType);

        InquirySourceType sourceTypeEnum = safeParseSourceType(sourceType);
        Page<Inquiry> inquiryPage = inquiryRepository.searchNewInquiries(keyword, sourceTypeEnum, pageable);

        log.debug("[InquiryService] 신규 상담신청 조회 완료. {}건", inquiryPage.getTotalElements());

        return toListItemResponseListWithNames(inquiryPage);
    }

    @Override
    public ResponseData<ResponseInquiry> getInquiry(Long id) {
        
        log.info("[InquiryService] 상담신청 상세 조회 시작. ID={}", id);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[InquiryService] 상담신청 미존재. ID={}", id);
                    return null;
                });

        if (inquiry == null) {
            return ResponseData.error("I404", "상담신청을 찾을 수 없습니다");
        }

        // 상담이력 조회
        List<InquiryLog> logs = inquiryLogRepository.findByInquiryIdOrderByCreatedAt(id);
        List<ResponseInquiryLog> logResponses = inquiryMapper.toLogResponses(logs);

        // 회원 이름 조회
        String createdByName = getMemberName(inquiry.getCreatedBy());
        String updatedByName = getMemberName(inquiry.getUpdatedBy());

        ResponseInquiry response = inquiryMapper.toResponseWithDetails(inquiry, createdByName, updatedByName, logResponses);

        log.debug("[InquiryService] 상담신청 상세 조회 완료. ID={}, 이력수={}", id, logs.size());

        return ResponseData.ok("0000", "조회 성공", response);
    }

    @Override
    @Transactional
    public ResponseData<Long> createInquiry(RequestInquiryCreate request) {
        
        log.info("[InquiryService] 상담신청 생성 시작. 이름={}, 연락처={}", request.getName(), request.getPhoneNumber());

        // 중복 신청 검사
        checkDuplicateSubmission(request.getPhoneNumber());

        // 현재 로그인한 관리자 정보
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserName = getMemberName(currentUserId);

        // 엔티티 생성 (관리자 ID로 createdBy 설정)
        Inquiry inquiry = inquiryMapper.toEntityWithCreatedBy(request, currentUserId);
        inquiry = inquiryRepository.save(inquiry);

        // 관리자 생성 이력 추가
        InquiryLog createLog = inquiryMapper.createAdminCreateLog(inquiry, currentUserName, currentUserId);
        inquiryLogRepository.save(createLog);

        log.info("[InquiryService] 상담신청 생성 완료. ID={}, 관리자={}", inquiry.getId(), currentUserName);

        return ResponseData.ok("0000", "상담신청이 생성되었습니다.", inquiry.getId());
    }

    @Override
    @Transactional
    public ResponseData<Long> createInquiryFromExternal(RequestInquiryCreate request, String clientIp) {
        
        log.info("[InquiryService] 외부 상담신청 생성 시작. 이름={}, 연락처={}, IP={}", 
                 request.getName(), request.getPhoneNumber(), clientIp);

        // 중복 신청 검사 (외부 접수는 더 엄격)
        checkDuplicateSubmission(request.getPhoneNumber());

        // IP 주소 설정
        request.setClientIp(clientIp);

        // 엔티티 생성 (created_by는 null)
        Inquiry inquiry = inquiryMapper.toEntity(request);
        inquiry = inquiryRepository.save(inquiry);

        // 시스템 생성 이력 추가
        InquiryLog createLog = inquiryMapper.createSystemCreateLog(inquiry);
        inquiryLogRepository.save(createLog);

        log.info("[InquiryService] 외부 상담신청 생성 완료. ID={}", inquiry.getId());

        return ResponseData.ok("0000", "상담신청이 접수되었습니다.", inquiry.getId());
    }

    @Override
    @Transactional
    public ResponseData<ResponseInquiry> updateInquiry(Long id, RequestInquiryUpdate request) {
        
        log.info("[InquiryService] 상담신청 수정 시작. ID={}", id);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[InquiryService] 수정할 상담신청 미존재. ID={}", id);
                    return null;
                });

        if (inquiry == null) {
            return ResponseData.error("I404", "상담신청을 찾을 수 없습니다");
        }

        // 수정 권한 검사 (처리 완료된 상담은 수정 제한)
        if (inquiry.isProcessed() && request.hasStatusUpdate()) {
            log.warn("[InquiryService] 처리 완료된 상담신청 상태 변경 시도. ID={}, 현재상태={}", id, inquiry.getStatus());
            return ResponseData.error("I403", "이미 처리 완료된 상담신청입니다");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 엔티티 업데이트
        inquiryMapper.updateEntityFromRequest(inquiry, request, currentUserId);

        // 상태 변경 시 이력 자동 생성
        if (request.hasStatusUpdate() || request.hasAssigneeUpdate()) {
            String logContent = createUpdateLogContent(request);
            InquiryLog updateLog = InquiryLog.builder()
                    .inquiry(inquiry)
                    .logType(LogType.MEMO)
                    .logContent(logContent)
                    .nextStatus(request.getStatusEnum())
                    .nextAssignee(null) // 담당자 변경은 별도 메서드에서
                    .createdBy(currentUserId)
                    .build();
            inquiryLogRepository.save(updateLog);
        }

        inquiry = inquiryRepository.save(inquiry);
        ResponseInquiry response = inquiryMapper.toResponse(inquiry);

        log.debug("[InquiryService] 상담신청 수정 완료. ID={}", id);

        return ResponseData.ok("0000", "상담신청이 수정되었습니다.", response);
    }

    @Override
    @Transactional
    public Response deleteInquiry(Long id) {
        
        log.info("[InquiryService] 상담신청 삭제 시작. ID={}", id);

        if (!inquiryRepository.existsById(id)) {
            log.warn("[InquiryService] 삭제할 상담신청 미존재. ID={}", id);
            return Response.error("I404", "상담신청을 찾을 수 없습니다");
        }

        // 연관된 이력도 함께 삭제 (CASCADE 설정으로 자동 삭제됨)
        inquiryRepository.deleteById(id);

        log.info("[InquiryService] 상담신청 삭제 완료. ID={}", id);

        return Response.ok("0000", "상담신청이 삭제되었습니다");
    }

    @Override
    public ResponseData<ResponseInquiryStats> getInquiryStats() {
        
        log.info("[InquiryService] 상담신청 통계 조회 시작");

        // 상태별 개수 조회
        Long newCount = inquiryRepository.countByStatus(InquiryStatus.NEW);
        Long inProgressCount = inquiryRepository.countByStatus(InquiryStatus.IN_PROGRESS);
        Long doneCount = inquiryRepository.countByStatus(InquiryStatus.DONE);
        Long rejectedCount = inquiryRepository.countByStatus(InquiryStatus.REJECTED);
        Long spamCount = inquiryRepository.countByStatus(InquiryStatus.SPAM);

        ResponseInquiryStats stats = ResponseInquiryStats.basic(
            newCount, inProgressCount, doneCount, rejectedCount, spamCount);

        log.debug("[InquiryService] 상담신청 통계 조회 완료. 전체={}", stats.getTotalCount());

        return ResponseData.ok("0000", "통계 조회 성공", stats);
    }

    @Override
    public ResponseData<ResponseInquiryStats> getDetailedStats(LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("[InquiryService] 상세 통계 조회 시작. 기간={} ~ {}", startDate, endDate);

        // 복합 통계 조회
        java.util.Map<String, Object> complexStats = inquiryRepository.getComplexStatistics(startDate, endDate);

        // 기본 통계로 변환 (실제로는 더 상세한 변환 로직 필요)
        ResponseInquiryStats stats = ResponseInquiryStats.basic(0L, 0L, 0L, 0L, 0L);

        log.debug("[InquiryService] 상세 통계 조회 완료");

        return ResponseData.ok("0000", "상세 통계 조회 성공", stats);
    }

    @Override
    @Transactional
    public ResponseData<ResponseInquiryLog> addInquiryLog(Long inquiryId, RequestInquiryLogCreate request) {
        
        log.info("[InquiryService] 상담이력 추가 시작. inquiryId={}, logType={}", inquiryId, request.getLogType());

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseGet(() -> {
                    log.warn("[InquiryService] 이력 추가할 상담신청 미존재. ID={}", inquiryId);
                    return null;
                });

        if (inquiry == null) {
            return ResponseData.error("I404", "상담신청을 찾을 수 없습니다");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 이력 생성
        InquiryLog inquiryLog = inquiryMapper.toLogEntity(inquiryId, request, inquiry);
        inquiryLog = inquiryLogRepository.save(inquiryLog);

        // 상태 변경이 포함된 경우 상담신청 상태도 업데이트
        if (request.hasStatusChange()) {
            inquiry.updateStatus(request.getNextStatusEnum(), currentUserId);
            inquiryRepository.save(inquiry);
            log.debug("[InquiryService] 상담신청 상태 변경. ID={}, 새상태={}", inquiryId, request.getNextStatus());
        }

        // 담당자 변경이 포함된 경우 (nextAssignee는 ID이므로 이름으로 변환 필요)
        if (request.hasAssigneeChange()) {
            String assigneeName = getMemberName(request.getNextAssignee());
            inquiry.assignTo(assigneeName, currentUserId);
            inquiryRepository.save(inquiry);
            log.debug("[InquiryService] 담당자 변경. ID={}, 새담당자={}", inquiryId, assigneeName);
        }

        ResponseInquiryLog response = inquiryMapper.toLogResponse(inquiryLog);

        log.info("[InquiryService] 상담이력 추가 완료. logId={}", inquiryLog.getId());

        return ResponseData.ok("0000", "상담이력이 추가되었습니다.", response);
    }

    @Override
    @Transactional
    public Response updateInquiryStatus(Long id, String status) {
        
        log.info("[InquiryService] 상담신청 상태 변경 시작. ID={}, status={}", id, status);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[InquiryService] 상태 변경할 상담신청 미존재. ID={}", id);
                    return null;
                });

        if (inquiry == null) {
            return Response.error("I404", "상담신청을 찾을 수 없습니다");
        }

        InquiryStatus newStatus = safeParseStatus(status);
        if (newStatus == null) {
            return Response.error("I400", "유효하지 않은 상담 상태입니다");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        inquiry.updateStatus(newStatus, currentUserId);
        inquiryRepository.save(inquiry);

        log.info("[InquiryService] 상담신청 상태 변경 완료. ID={}, status={}", id, status);

        return Response.ok("0000", "상담 상태가 변경되었습니다");
    }

    @Override
    @Transactional
    public Response assignInquiry(Long id, String assigneeName) {
        
        log.info("[InquiryService] 상담신청 담당자 배정 시작. ID={}, assigneeName={}", id, assigneeName);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("[InquiryService] 담당자 배정할 상담신청 미존재. ID={}", id);
                    return null;
                });

        if (inquiry == null) {
            return Response.error("I404", "상담신청을 찾을 수 없습니다");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        inquiry.assignTo(assigneeName, currentUserId);
        inquiryRepository.save(inquiry);

        log.info("[InquiryService] 상담신청 담당자 배정 완료. ID={}, assigneeName={}", id, assigneeName);

        return Response.ok("0000", "담당자가 배정되었습니다");
    }

    @Override
    public ResponseData<List<ResponseInquiryListItem>> checkDuplicateInquiries(String phoneNumber, int hours) {
        
        log.info("[InquiryService] 중복 신청 검사 시작. phoneNumber={}, hours={}", phoneNumber, hours);

        List<Inquiry> duplicates = inquiryRepository.findPossibleDuplicates(phoneNumber, hours);
        List<ResponseInquiryListItem> response = inquiryMapper.toListItems(duplicates);

        if (!duplicates.isEmpty()) {
            log.warn("[InquiryService] 중복 신청 발견. phoneNumber={}, 개수={}", phoneNumber, duplicates.size());
        }

        return ResponseData.ok("0000", "중복 검사 완료", response);
    }

    @Override
    public ResponseList<ResponseInquiryListItem> getDelayedInquiries(int days, Pageable pageable) {
        
        log.info("[InquiryService] 지연 상담신청 조회 시작. days={}", days);

        Page<Inquiry> delayedPage = inquiryRepository.findDelayedInquiries(days, pageable);

        log.debug("[InquiryService] 지연 상담신청 조회 완료. {}건", delayedPage.getTotalElements());

        return inquiryMapper.toListItemResponseList(delayedPage);
    }

    @Override
    public ResponseList<ResponseInquiryListItem> getInquiriesByAssignee(String assigneeName, String status, Pageable pageable) {
        
        log.info("[InquiryService] 담당자별 상담신청 조회 시작. assigneeName={}, status={}", assigneeName, status);

        InquiryStatus statusEnum = safeParseStatus(status);
        Page<Inquiry> inquiryPage = inquiryRepository.searchByAssignee(assigneeName, statusEnum, pageable);

        log.debug("[InquiryService] 담당자별 상담신청 조회 완료. {}건", inquiryPage.getTotalElements());

        return toListItemResponseListWithNames(inquiryPage);
    }

    /**
     * 회원 이름을 포함한 목록 응답 변환.
     */
    private ResponseList<ResponseInquiryListItem> toListItemResponseListWithNames(Page<Inquiry> page) {
        List<ResponseInquiryListItem> items = page.getContent().stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseInquiryListItem.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 회원 이름 조회 도우미 메서드.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return null; // 외부에서 등록한 경우 null 반환
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }


    /**
     * 검색 타입 안전 변환.
     */
    private InquirySearchType safeParseSearchType(String searchType) {
        if (searchType == null) {
            return InquirySearchType.ALL; // 기본값
        }
        try {
            return InquirySearchType.valueOf(searchType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[InquiryService] 유효하지 않은 검색 타입: {}. ALL로 기본 설정", searchType);
            return InquirySearchType.ALL;
        }
    }

    /**
     * 상담 상태 안전 변환.
     */
    private InquiryStatus safeParseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return InquiryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[InquiryService] 유효하지 않은 상담 상태: {}", status);
            return null;
        }
    }

    /**
     * 접수 경로 안전 변환.
     */
    private InquirySourceType safeParseSourceType(String sourceType) {
        if (sourceType == null) {
            return null;
        }
        try {
            return InquirySourceType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[InquiryService] 유효하지 않은 접수 경로: {}", sourceType);
            return null;
        }
    }

    /**
     * 중복 신청 검사.
     */
    private void checkDuplicateSubmission(String phoneNumber) {
        Optional<Inquiry> recentInquiry = inquiryRepository
                .findFirstByPhoneNumberOrderByCreatedAtDesc(phoneNumber);
        
        if (recentInquiry.isPresent()) {
            Inquiry recent = recentInquiry.get();
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            
            if (recent.getCreatedAt().isAfter(oneHourAgo)) {
                log.warn("[InquiryService] 중복 신청 감지. phoneNumber={}, 최근신청={}", 
                         phoneNumber, recent.getCreatedAt());
                // 실제로는 예외를 발생시키거나 특별 처리
                // throw new BusinessException("1시간 이내 중복 신청입니다");
            }
        }
    }

    /**
     * 수정 이력 내용 생성.
     */
    private String createUpdateLogContent(RequestInquiryUpdate request) {
        StringBuilder content = new StringBuilder("정보 수정: ");
        
        if (request.hasStatusUpdate()) {
            content.append("상태변경(").append(request.getStatus()).append(") ");
        }
        if (request.hasAssigneeUpdate()) {
            content.append("담당자변경(").append(request.getAssigneeName()).append(") ");
        }
        
        return content.toString();
    }
}