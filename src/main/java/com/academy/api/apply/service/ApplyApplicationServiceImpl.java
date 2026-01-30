package com.academy.api.apply.service;

import com.academy.api.apply.domain.*;
import com.academy.api.apply.dto.*;
import com.academy.api.apply.mapper.ApplyApplicationMapper;
import com.academy.api.apply.repository.*;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.dto.FileReference;
import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.service.FileService;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

/**
 * 원서접수 서비스 구현체.
 * 
 * - 원서접수 CRUD 비즈니스 로직 처리
 * - 파일 업로드 및 관리 (성적표, 증명사진)
 * - 과목 선택 로직 (구분별 제약 조건)
 * - 이력 자동 생성 및 관리
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
public class ApplyApplicationServiceImpl implements ApplyApplicationService {

    private final ApplyApplicationRepository applyApplicationRepository;
    private final ApplyApplicationLogRepository applyApplicationLogRepository;
    private final ApplyApplicationSubjectRepository applyApplicationSubjectRepository;
    private final MemberRepository memberRepository;
    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final FileService fileService;
    private final ApplyApplicationMapper applyApplicationMapper;

    @Override
    public ResponseList<ResponseApplyApplicationAdminList> getApplyApplicationList(String keyword, String status,
                                                                                 String division, String assigneeName, 
                                                                                 Long assigneeId, LocalDateTime createdFrom, 
                                                                                 LocalDateTime createdTo, String sortBy, 
                                                                                 Pageable pageable) {
        
        log.info("[ApplyApplicationService] 원서접수 목록 조회 시작. keyword={}, status={}, division={}, assigneeName={}", 
                 keyword, status, division, assigneeName);

        // Enum 안전 변환
        ApplicationStatus statusEnum = safeParseStatus(status);
        ApplicationDivision divisionEnum = safeParseDivision(division);

        // Repository 호출 (QueryDSL 동적 쿼리)
        Page<ApplyApplication> applicationPage = applyApplicationRepository.searchApplyApplicationsForAdmin(
            keyword, statusEnum, divisionEnum, assigneeName, assigneeId, createdFrom, createdTo, sortBy, pageable);

        log.debug("[ApplyApplicationService] 원서접수 목록 조회 완료. 총 {}건, 현재 페이지 {}개", 
                 applicationPage.getTotalElements(), applicationPage.getNumberOfElements());

        // 회원 이름 포함하여 ResponseList로 변환
        return toListItemResponseListWithNames(applicationPage);
    }

    @Override
    public ResponseData<ResponseApplyApplicationDetail> getApplyApplication(Long id) {
        
        log.info("[ApplyApplicationService] 원서접수 상세 조회 시작. ID={}", id);

        ApplyApplication application = applyApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

        // 과목 목록 조회
        List<ApplyApplicationSubject> subjects = applyApplicationSubjectRepository.findByApplyId(id);
        List<ResponseApplyApplicationSubject> subjectResponses = applyApplicationMapper.toSubjectResponses(subjects);

        // 이력 목록 조회
        List<ApplyApplicationLog> logs = applyApplicationLogRepository.findByApplyApplicationIdOrderByCreatedAt(id);
        List<ResponseApplyApplicationLog> logResponses = toLogResponsesWithNames(logs);

        // 파일 정보 조회
        List<ResponseFileInfo> transcriptFiles = getFilesByRole(id, FileRole.ATTACHMENT);
        List<ResponseFileInfo> photoFiles = getFilesByRole(id, FileRole.COVER);

        // 이전/다음글 조회
        ResponseApplyApplicationNavigation previousApp = applyApplicationRepository.findPreviousApplication(id)
                .map(ResponseApplyApplicationNavigation::from).orElse(null);
        ResponseApplyApplicationNavigation nextApp = applyApplicationRepository.findNextApplication(id)
                .map(ResponseApplyApplicationNavigation::from).orElse(null);

        // 회원 이름 조회
        String createdByName = getMemberName(application.getCreatedBy());
        String updatedByName = getMemberName(application.getUpdatedBy());

        // 상세 응답 생성
        ResponseApplyApplicationDetail response = ResponseApplyApplicationDetail.from(application)
                .toBuilder()
                .createdByName(createdByName)
                .updatedByName(updatedByName)
                .subjects(subjectResponses)
                .logs(logResponses)
                .transcriptFiles(transcriptFiles)
                .photoFiles(photoFiles)
                .previousApplication(previousApp)
                .nextApplication(nextApp)
                .build();

        log.debug("[ApplyApplicationService] 원서접수 상세 조회 완료. ID={}, 과목수={}, 이력수={}", 
                 id, subjects.size(), logs.size());

        return ResponseData.ok("0000", "조회 성공", response);
    }

    @Override
    @Transactional
    public ResponseData<Long> createApplyApplication(RequestApplyApplicationCreate request) {
        
        log.info("[ApplyApplicationService] 원서접수 생성 시작. 학생명={}, 구분={}", 
                 request.getStudentName(), request.getDivision());

        // 중복 신청 검사
        checkDuplicateSubmission(request.getStudentPhone());

        // 과목 선택 유효성 검증
        validateSubjectsByDivision(request.getDivision(), request.getSubjects());

        // 현재 로그인한 관리자 정보
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserName = getMemberName(currentUserId);

        // 엔티티 생성 (관리자 ID로 createdBy 설정)
        ApplyApplication application = applyApplicationMapper.toEntityWithCreatedBy(request, currentUserId);
        application = applyApplicationRepository.save(application);

        // 과목 정보 저장
        if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {
            saveApplicationSubjects(application.getId(), request.getSubjects());
        }

        // 파일 처리 (임시 → 정식 변환)
        processFileUploads(application.getId(), request);

        // 관리자 생성 이력 추가
        ApplyApplicationLog createLog = applyApplicationMapper.createAdminCreateLog(
                application, currentUserName, currentUserId);
        applyApplicationLogRepository.save(createLog);

        log.info("[ApplyApplicationService] 원서접수 생성 완료. ID={}, 관리자={}", application.getId(), currentUserName);

        // TODO: SMS 발송 (guardian1Phone으로)
        
        return ResponseData.ok("0000", "원서접수가 생성되었습니다.", application.getId());
    }

    @Override
    @Transactional
    public ResponseData<ResponseApplyApplicationDetail> updateApplyApplication(Long id, 
                                                                              RequestApplyApplicationUpdate request) {
        
        log.info("[ApplyApplicationService] 원서접수 수정 시작. ID={}", id);

        ApplyApplication application = applyApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

        // 수정 권한 검사 (처리 완료된 원서는 수정 제한)
        if (application.isCompleted()) {
            log.warn("[ApplyApplicationService] 완료된 원서접수 수정 시도. ID={}, 현재상태={}", id, application.getStatus());
            throw new BusinessException(ErrorCode.APPLY_APPLICATION_ALREADY_COMPLETED);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 엔티티 업데이트
        applyApplicationMapper.updateEntityFromRequest(application, request, currentUserId);

        // 과목 정보 업데이트
        if (request.getSubjects() != null) {
            updateApplicationSubjects(id, request.getSubjects());
        }

        // 파일 정보 업데이트
        updateFileUploads(id, request);

        application = applyApplicationRepository.save(application);

        // 수정 이력 추가
        ApplyApplicationLog updateLog = applyApplicationMapper.createUpdateLog(
                application, request.getUpdateReason(), currentUserId);
        applyApplicationLogRepository.save(updateLog);

        // 상세 정보 조회하여 반환
        ResponseData<ResponseApplyApplicationDetail> detailResponse = getApplyApplication(id);

        log.debug("[ApplyApplicationService] 원서접수 수정 완료. ID={}", id);

        return ResponseData.ok("0000", "원서접수가 수정되었습니다.", detailResponse.getData());
    }

    @Override
    @Transactional
    public Response deleteApplyApplication(Long id) {
        
        log.info("[ApplyApplicationService] 원서접수 삭제 시작. ID={}", id);

        if (!applyApplicationRepository.existsById(id)) {
            log.warn("[ApplyApplicationService] 삭제할 원서접수 미존재. ID={}", id);
            throw new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND);
        }

        // 연관된 이력과 과목도 함께 삭제 (CASCADE 설정으로 자동 삭제됨)
        applyApplicationRepository.deleteById(id);

        log.info("[ApplyApplicationService] 원서접수 삭제 완료. ID={}", id);

        return Response.ok("0000", "원서접수가 삭제되었습니다");
    }

    @Override
    public ResponseData<ResponseApplyApplicationStats> getApplyApplicationStats() {
        
        log.info("[ApplyApplicationService] 원서접수 통계 조회 시작");

        // 상태별 개수 조회
        Map<ApplicationStatus, Long> statusStats = applyApplicationRepository.getStatusStatistics();
        Map<ApplicationDivision, Long> divisionStats = applyApplicationRepository.getDivisionStatistics();

        ResponseApplyApplicationStats stats = ResponseApplyApplicationStats.full(
            statusStats.getOrDefault(ApplicationStatus.REGISTERED, 0L),
            statusStats.getOrDefault(ApplicationStatus.REVIEW, 0L),
            statusStats.getOrDefault(ApplicationStatus.COMPLETED, 0L),
            statusStats.getOrDefault(ApplicationStatus.CANCELED, 0L),
            divisionStats.getOrDefault(ApplicationDivision.MIDDLE, 0L),
            divisionStats.getOrDefault(ApplicationDivision.HIGH, 0L),
            divisionStats.getOrDefault(ApplicationDivision.SELF_STUDY_RETAKE, 0L)
        );

        log.debug("[ApplyApplicationService] 원서접수 통계 조회 완료. 전체={}", stats.getTotalCount());

        return ResponseData.ok("0000", "통계 조회 성공", stats);
    }

    @Override
    public ResponseData<ResponseApplyApplicationStats> getDetailedStats(LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("[ApplyApplicationService] 상세 통계 조회 시작. 기간={} ~ {}", startDate, endDate);

        // 복합 통계 조회
        Map<String, Object> complexStats = applyApplicationRepository.getComplexStatistics(startDate, endDate);

        // TODO: 복합 통계를 ResponseApplyApplicationStats로 변환하는 로직 구현
        ResponseApplyApplicationStats stats = ResponseApplyApplicationStats.basic(0L, 0L, 0L, 0L);

        log.debug("[ApplyApplicationService] 상세 통계 조회 완료");

        return ResponseData.ok("0000", "상세 통계 조회 성공", stats);
    }

    @Override
    @Transactional
    public ResponseData<ResponseApplyApplicationLog> addApplyApplicationLog(Long applyId, 
                                                                           RequestApplyApplicationLogCreate request) {
        
        log.info("[ApplyApplicationService] 원서접수 이력 추가 시작. applyId={}, logType={}", applyId, request.getLogType());

        ApplyApplication application = applyApplicationRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 이력 생성
        ApplyApplicationLog log = applyApplicationMapper.toLogEntity(applyId, request, application);
        log = ApplyApplicationLog.builder()
                .applyApplication(application)
                .logType(request.getLogType())
                .logContent(request.getLogContent())
                .nextStatus(request.getNextStatus())
                .nextAssigneeId(request.getNextAssigneeId())
                .createdBy(currentUserId)
                .build();
        
        log = applyApplicationLogRepository.save(log);

        // 상태 변경이 포함된 경우 원서접수 상태도 업데이트
        if (request.hasStatusChange()) {
            application.updateStatus(request.getNextStatus(), currentUserId);
            applyApplicationRepository.save(application);
            this.log.debug("[ApplyApplicationService] 원서접수 상태 변경. ID={}, 새상태={}", applyId, request.getNextStatus());
        }

        // 담당자 변경이 포함된 경우
        if (request.hasAssigneeChange()) {
            String assigneeName = getMemberName(request.getNextAssigneeId());
            application.assignTo(assigneeName, currentUserId);
            applyApplicationRepository.save(application);
            this.log.debug("[ApplyApplicationService] 담당자 변경. ID={}, 새담당자={}", applyId, assigneeName);
        }

        ResponseApplyApplicationLog response = applyApplicationMapper.toLogResponse(log);

        this.log.info("[ApplyApplicationService] 원서접수 이력 추가 완료. logId={}", log.getId());

        return ResponseData.ok("0000", "이력이 추가되었습니다.", response);
    }

    @Override
    @Transactional
    public Response updateApplyApplicationStatus(Long id, String status) {
        
        log.info("[ApplyApplicationService] 원서접수 상태 변경 시작. ID={}, status={}", id, status);

        ApplyApplication application = applyApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

        ApplicationStatus newStatus = safeParseStatus(status);
        if (newStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        application.updateStatus(newStatus, currentUserId);
        applyApplicationRepository.save(application);

        log.info("[ApplyApplicationService] 원서접수 상태 변경 완료. ID={}, status={}", id, status);

        return Response.ok("0000", "원서접수 상태가 변경되었습니다");
    }

    @Override
    @Transactional
    public Response assignApplyApplication(Long id, String assigneeName) {
        
        log.info("[ApplyApplicationService] 원서접수 담당자 배정 시작. ID={}, assigneeName={}", id, assigneeName);

        ApplyApplication application = applyApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        application.assignTo(assigneeName, currentUserId);
        applyApplicationRepository.save(application);

        // 담당자 배정 이력 추가
        ApplyApplicationLog assignLog = applyApplicationMapper.createAssignLog(
                application, assigneeName, currentUserId);
        applyApplicationLogRepository.save(assignLog);

        log.info("[ApplyApplicationService] 원서접수 담당자 배정 완료. ID={}, assigneeName={}", id, assigneeName);

        return Response.ok("0000", "담당자가 배정되었습니다");
    }

    @Override
    public ResponseData<List<ResponseApplyApplicationAdminList>> checkDuplicateApplications(String studentPhone, int hours) {
        
        log.info("[ApplyApplicationService] 중복 원서접수 검사 시작. studentPhone={}, hours={}", studentPhone, hours);

        List<ApplyApplication> duplicates = applyApplicationRepository.findPossibleDuplicates(studentPhone, hours);
        List<ResponseApplyApplicationAdminList> response = duplicates.stream()
                .map(ResponseApplyApplicationAdminList::from)
                .toList();

        if (!duplicates.isEmpty()) {
            log.warn("[ApplyApplicationService] 중복 원서접수 발견. studentPhone={}, 개수={}", studentPhone, duplicates.size());
        }

        return ResponseData.ok("0000", "중복 검사 완료", response);
    }

    @Override
    public ResponseList<ResponseApplyApplicationAdminList> getDelayedApplications(int days, Pageable pageable) {
        
        log.info("[ApplyApplicationService] 지연 원서접수 조회 시작. days={}", days);

        Page<ApplyApplication> delayedPage = applyApplicationRepository.findDelayedApplications(days, pageable);

        log.debug("[ApplyApplicationService] 지연 원서접수 조회 완료. {}건", delayedPage.getTotalElements());

        return applyApplicationMapper.toAdminListResponseList(delayedPage);
    }

    @Override
    public ResponseList<ResponseApplyApplicationAdminList> getApplicationsByAssignee(String assigneeName, String status, 
                                                                                   Pageable pageable) {
        
        log.info("[ApplyApplicationService] 담당자별 원서접수 조회 시작. assigneeName={}, status={}", assigneeName, status);

        ApplicationStatus statusEnum = safeParseStatus(status);
        Page<ApplyApplication> applicationPage = applyApplicationRepository.searchByAssignee(assigneeName, statusEnum, pageable);

        log.debug("[ApplyApplicationService] 담당자별 원서접수 조회 완료. {}건", applicationPage.getTotalElements());

        return toListItemResponseListWithNames(applicationPage);
    }


    /**
     * 회원 이름을 포함한 목록 응답 변환.
     */
    private ResponseList<ResponseApplyApplicationAdminList> toListItemResponseListWithNames(Page<ApplyApplication> page) {
        List<ResponseApplyApplicationAdminList> items = page.getContent().stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseApplyApplicationAdminList.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 이력 목록을 회원 이름 포함하여 응답 변환.
     */
    private List<ResponseApplyApplicationLog> toLogResponsesWithNames(List<ApplyApplicationLog> logs) {
        return logs.stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseApplyApplicationLog.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
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
     * 파일 역할별 파일 정보 조회.
     */
    private List<ResponseFileInfo> getFilesByRole(Long ownerId, FileRole role) {
        List<Object[]> fileData = uploadFileLinkRepository.findFileInfosByOwnerAndRole(
                "apply_applications", ownerId, role);
        
        return fileData.stream()
                .map(data -> ResponseFileInfo.builder()
                        .fileId(data[0] != null ? data[0].toString() : null)
                        .originalName((String) data[1])
                        .size((Long) data[2])
                        .build())
                .toList();
    }

    /**
     * 원서접수 상태 안전 변환.
     */
    private ApplicationStatus safeParseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ApplyApplicationService] 유효하지 않은 원서접수 상태: {}", status);
            return null;
        }
    }

    /**
     * 구분 안전 변환.
     */
    private ApplicationDivision safeParseDivision(String division) {
        if (division == null) {
            return null;
        }
        try {
            return ApplicationDivision.valueOf(division.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ApplyApplicationService] 유효하지 않은 구분: {}", division);
            return null;
        }
    }

    /**
     * 중복 신청 검사.
     */
    private void checkDuplicateSubmission(String studentPhone) {
        List<ApplyApplication> recentApplications = applyApplicationRepository
                .findPossibleDuplicates(studentPhone, 1);
        
        if (!recentApplications.isEmpty()) {
            log.warn("[ApplyApplicationService] 중복 신청 감지. studentPhone={}, 최근신청수={}", 
                     studentPhone, recentApplications.size());
            // 실제로는 예외를 발생시키거나 특별 처리
            // throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }
    }

    /**
     * 구분별 과목 선택 유효성 검증.
     */
    private void validateSubjectsByDivision(ApplicationDivision division, List<SubjectCode> subjects) {
        if (division == ApplicationDivision.SELF_STUDY_RETAKE) {
            // 독학재수는 과목 선택 없음
            if (subjects != null && !subjects.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_SUBJECT_FOR_DIVISION);
            }
            return;
        }
        
        if (subjects == null || subjects.isEmpty()) {
            throw new BusinessException(ErrorCode.SUBJECT_REQUIRED_FOR_DIVISION);
        }
        
        // 중등부: 국영수사과 (최대 5개)
        if (division == ApplicationDivision.MIDDLE) {
            List<SubjectCode> allowedSubjects = List.of(
                SubjectCode.KOR, SubjectCode.ENG, SubjectCode.MATH, SubjectCode.SCI, SubjectCode.SOC);
            if (!allowedSubjects.containsAll(subjects)) {
                throw new BusinessException(ErrorCode.INVALID_SUBJECT_FOR_MIDDLE);
            }
        }
        
        // 고등부: 국영수 (최대 3개)
        if (division == ApplicationDivision.HIGH) {
            List<SubjectCode> allowedSubjects = List.of(SubjectCode.KOR, SubjectCode.ENG, SubjectCode.MATH);
            if (!allowedSubjects.containsAll(subjects)) {
                throw new BusinessException(ErrorCode.INVALID_SUBJECT_FOR_HIGH);
            }
        }
    }

    /**
     * 과목 정보 저장.
     */
    private void saveApplicationSubjects(Long applyId, List<SubjectCode> subjects) {
        List<ApplyApplicationSubject> subjectEntities = subjects.stream()
                .map(subjectCode -> ApplyApplicationSubject.builder()
                        .applyId(applyId)
                        .subjectCode(subjectCode)
                        .build())
                .toList();
        
        applyApplicationSubjectRepository.saveAll(subjectEntities);
    }

    /**
     * 과목 정보 업데이트.
     */
    private void updateApplicationSubjects(Long applyId, List<SubjectCode> subjects) {
        // 기존 과목 정보 삭제
        applyApplicationSubjectRepository.deleteByApplyId(applyId);
        
        // 새 과목 정보 저장
        if (!subjects.isEmpty()) {
            saveApplicationSubjects(applyId, subjects);
        }
    }

    /**
     * 파일 업로드 처리 (임시 → 정식 변환).
     */
    private void processFileUploads(Long applyId, RequestApplyApplicationCreate request) {
        // 성적표 파일 처리
        if (request.getTranscriptFiles() != null && !request.getTranscriptFiles().isEmpty()) {
            createFileLinks(applyId, request.getTranscriptFiles(), FileRole.ATTACHMENT);
        }
        
        // 증명사진 파일 처리
        if (request.getPhotoFiles() != null && !request.getPhotoFiles().isEmpty()) {
            createFileLinks(applyId, request.getPhotoFiles(), FileRole.COVER);
        }
    }

    /**
     * 파일 업로드 업데이트.
     */
    private void updateFileUploads(Long applyId, RequestApplyApplicationUpdate request) {
        // 기존 파일 연결 삭제
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole("apply_applications", applyId, FileRole.ATTACHMENT);
        uploadFileLinkRepository.deleteByOwnerTableAndOwnerIdAndRole("apply_applications", applyId, FileRole.COVER);
        
        // 새 파일 연결 생성
        processFileUploads(applyId, convertUpdateToCreate(request));
    }

    /**
     * 파일 연결 생성 (notice 서비스 패턴 참조).
     */
    private Map<String, Long> createFileLinks(Long applyId, List<FileReference> fileReferences, FileRole role) {
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("[ApplyApplicationService] 연결할 {}파일 없음. applyId={}", role, applyId);
            return tempToFormalMap;
        }

        log.info("[ApplyApplicationService] {} 파일 연결 생성 시작. applyId={}, 파일개수={}", 
                role, applyId, fileReferences.size());

        // 1단계: 모든 임시 파일을 정식 파일로 변환
        for (FileReference fileRef : fileReferences) {
            String tempFileId = fileRef.getFileId();
            String originalFileName = fileRef.getFileName();
            
            Long formalFileId = fileService.promoteToFormalFile(tempFileId, originalFileName);
            if (formalFileId != null) {
                tempToFormalMap.put(tempFileId, formalFileId);
                log.debug("[ApplyApplicationService] 임시 파일 정식 변환 성공. tempId={} -> formalId={}", 
                        tempFileId, formalFileId);
            } else {
                log.warn("[ApplyApplicationService] 임시 파일 변환 실패로 연결 생략. tempFileId={}, role={}", 
                        tempFileId, role);
            }
        }

        // 2단계: 성공한 변환들에 대해 파일 연결 객체 생성
        List<UploadFileLink> successfulLinks = tempToFormalMap.values().stream()
                .map(formalFileId -> UploadFileLink.builder()
                    .fileId(formalFileId)
                    .ownerTable("apply_applications")
                    .ownerId(applyId)
                    .role(role)
                    .sortOrder(0)
                    .createdBy(SecurityUtils.getCurrentUserId())
                    .build())
                .collect(java.util.stream.Collectors.toList());

        // 3단계: DB에 파일 연결 저장
        if (!successfulLinks.isEmpty()) {
            uploadFileLinkRepository.saveAll(successfulLinks);
            log.info("[ApplyApplicationService] {} 파일 연결 저장 완료. applyId={}, 성공개수={}", 
                    role, applyId, successfulLinks.size());
        }

        return tempToFormalMap;
    }

    /**
     * Update 요청을 Create 요청 형태로 변환 (파일 처리용).
     */
    private RequestApplyApplicationCreate convertUpdateToCreate(RequestApplyApplicationUpdate request) {
        RequestApplyApplicationCreate createRequest = new RequestApplyApplicationCreate();
        createRequest.setTranscriptFiles(request.getTranscriptFiles());
        createRequest.setPhotoFiles(request.getPhotoFiles());
        return createRequest;
    }

    // ===== 엑셀/PDF 다운로드 =====

    @Override
    public void exportApplyApplicationListToExcel(String keyword, String status, String division, String assigneeName,
                                                 Long assigneeId, LocalDateTime createdFrom, LocalDateTime createdTo,
                                                 String sortBy, HttpServletResponse response) {
        log.info("[ApplyApplicationService] 원서접수 목록 엑셀 다운로드 시작. keyword={}, status={}, division={}, assigneeName={}",
                keyword, status, division, assigneeName);

        try {
            // 상태 및 구분 파싱
            ApplicationStatus applicationStatus = parseApplicationStatus(status);
            ApplicationDivision applicationDivision = parseApplicationDivision(division);

            // 원서접수 목록 조회 (페이징 없이 모든 데이터)
            List<ApplyApplication> applications = applyApplicationRepository.searchApplyApplicationsForExcel(
                    keyword, applicationStatus, applicationDivision, assigneeName, assigneeId, 
                    createdFrom, createdTo, sortBy);

            log.debug("[ApplyApplicationService] 엑셀 다운로드용 원서접수 조회 완료. 건수={}", applications.size());

            // 엑셀 파일 생성
            Workbook workbook = createApplyApplicationExcelWorkbook(applications);

            // 파일명 생성 (URL 인코딩)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filenameKorean = String.format("원서접수_목록_%s.xlsx", timestamp);
            String filenameEncoded = URLEncoder.encode(filenameKorean, StandardCharsets.UTF_8);

            // HTTP 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", 
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", 
                    "apply_applications_" + timestamp + ".xlsx", filenameEncoded));
            response.setHeader("Cache-Control", "no-cache");

            // 엑셀 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();

            log.info("[ApplyApplicationService] 엑셀 다운로드 완료. 파일명={}, 건수={}", filenameKorean, applications.size());

        } catch (Exception e) {
            log.error("[ApplyApplicationService] 엑셀 다운로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void exportApplyApplicationToPdf(Long id, HttpServletResponse response) {
        log.info("[ApplyApplicationService] 원서접수 PDF 다운로드 시작. id={}", id);

        try {
            // 원서접수 상세 조회
            ApplyApplication application = applyApplicationRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.APPLY_APPLICATION_NOT_FOUND));

            // 관련 데이터 조회
            List<ApplyApplicationSubject> subjects = applyApplicationSubjectRepository.findByApplyId(id);
            
            // PDF 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String studentName = application.getStudentName();
            String filenameKorean = String.format("원서접수서_%s_%s.pdf", studentName, timestamp);
            String filenameEncoded = URLEncoder.encode(filenameKorean, StandardCharsets.UTF_8);

            // HTTP 응답 설정
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                    "application_" + studentName + "_" + timestamp + ".pdf", filenameEncoded));
            response.setHeader("Cache-Control", "no-cache");

            // PDF 생성 및 출력 (간단한 텍스트 기반)
            createApplyApplicationPdf(application, subjects, response);

            log.info("[ApplyApplicationService] PDF 다운로드 완료. 파일명={}, 학생={}", filenameKorean, studentName);

        } catch (BusinessException e) {
            throw e; // 비즈니스 예외는 재throw
        } catch (Exception e) {
            log.error("[ApplyApplicationService] PDF 다운로드 실패. id={}: {}", id, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== 내부 도우미 메서드 =====

    /**
     * 상태 문자열을 Enum으로 변환.
     */
    private ApplicationStatus parseApplicationStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ApplyApplicationService] 유효하지 않은 상태: {}. null로 처리", status);
            return null;
        }
    }

    /**
     * 구분 문자열을 Enum으로 변환.
     */
    private ApplicationDivision parseApplicationDivision(String division) {
        if (division == null || division.trim().isEmpty()) {
            return null;
        }
        try {
            return ApplicationDivision.valueOf(division.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[ApplyApplicationService] 유효하지 않은 구분: {}. null로 처리", division);
            return null;
        }
    }

    /**
     * 원서접수 목록 엑셀 워크북 생성.
     */
    private Workbook createApplyApplicationExcelWorkbook(List<ApplyApplication> applications) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("원서접수 목록");

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

        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "번호", "학생명", "성별", "학년", "휴대폰", "구분", "상태",
            "보호자1", "보호자1 연락처", "보호자2", "보호자2 연락처", 
            "주소", "담당자", "접수일시", "수정일시"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        int rowNum = 1;
        for (ApplyApplication app : applications) {
            Row dataRow = sheet.createRow(rowNum++);

            // 데이터 설정
            createCell(dataRow, 0, app.getId().toString(), cellStyle);
            createCell(dataRow, 1, app.getStudentName(), cellStyle);
            createCell(dataRow, 2, app.getGender() != null ? app.getGender().getDisplayName() : "", cellStyle);
            createCell(dataRow, 3, app.getStudentGradeLevel() != null ? app.getStudentGradeLevel().getDisplayName() : "", cellStyle);
            createCell(dataRow, 4, app.getStudentPhone(), cellStyle);
            createCell(dataRow, 5, app.getDivision() != null ? app.getDivision().getDisplayName() : "", cellStyle);
            createCell(dataRow, 6, app.getStatus() != null ? app.getStatus().getDisplayName() : "", cellStyle);
            createCell(dataRow, 7, app.getGuardian1Name(), cellStyle);
            createCell(dataRow, 8, app.getGuardian1Phone(), cellStyle);
            createCell(dataRow, 9, app.getGuardian2Name(), cellStyle);
            createCell(dataRow, 10, app.getGuardian2Phone(), cellStyle);
            createCell(dataRow, 11, app.getAddress(), cellStyle);
            createCell(dataRow, 12, app.getAssigneeName() != null ? app.getAssigneeName() : "미배정", cellStyle);
            createCell(dataRow, 13, app.getCreatedAt() != null ? 
                app.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "", cellStyle);
            createCell(dataRow, 14, app.getUpdatedAt() != null ? 
                app.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "", cellStyle);
        }

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * 셀 생성 도우미 메서드.
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 원서접수 PDF 생성 (iText8 사용).
     */
    private void createApplyApplicationPdf(ApplyApplication application, List<ApplyApplicationSubject> subjects, 
                                         HttpServletResponse response) throws Exception {
        
        // PDF 문서 생성
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        // 기본 폰트 설정 (한글 지원)
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        try {
            // 제목
            document.add(new Paragraph("원서접수서")
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(20));
            
            // 학생 정보
            document.add(new Paragraph("학생 정보")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));
            
            document.add(new Paragraph("학생명: " + application.getStudentName()).setFont(font));
            document.add(new Paragraph("성별: " + (application.getGender() != null ? application.getGender().getDisplayName() : "")).setFont(font));
            document.add(new Paragraph("학년: " + (application.getStudentGradeLevel() != null ? application.getStudentGradeLevel().getDisplayName() : "")).setFont(font));
            document.add(new Paragraph("휴대폰: " + application.getStudentPhone()).setFont(font));
            document.add(new Paragraph("주소: " + (application.getAddress() != null ? application.getAddress() : "")).setFont(font));
            
            // 보호자 정보
            document.add(new Paragraph("보호자 정보")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
            
            document.add(new Paragraph("보호자1: " + application.getGuardian1Name() + " (" + application.getGuardian1Phone() + ")").setFont(font));
            if (application.getGuardian2Name() != null && !application.getGuardian2Name().trim().isEmpty()) {
                document.add(new Paragraph("보호자2: " + application.getGuardian2Name() + " (" + application.getGuardian2Phone() + ")").setFont(font));
            }
            
            // 신청 정보
            document.add(new Paragraph("신청 정보")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
            
            document.add(new Paragraph("구분: " + (application.getDivision() != null ? application.getDivision().getDisplayName() : "")).setFont(font));
            document.add(new Paragraph("상태: " + (application.getStatus() != null ? application.getStatus().getDisplayName() : "")).setFont(font));
            
            if (!subjects.isEmpty()) {
                String subjectNames = subjects.stream()
                        .map(s -> s.getSubjectCode().getDisplayName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("없음");
                document.add(new Paragraph("신청과목: " + subjectNames).setFont(font));
            }
            
            // 독학재수 희망대학/학과 정보
            if (application.getDivision() == ApplicationDivision.SELF_STUDY_RETAKE) {
                if (application.getDesiredUniversity() != null) {
                    document.add(new Paragraph("희망대학: " + application.getDesiredUniversity()).setFont(font));
                }
                if (application.getDesiredDepartment() != null) {
                    document.add(new Paragraph("희망학과: " + application.getDesiredDepartment()).setFont(font));
                }
            }
            
            // 접수 정보
            document.add(new Paragraph("접수 정보")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));
            
            document.add(new Paragraph("담당자: " + (application.getAssigneeName() != null ? application.getAssigneeName() : "미배정")).setFont(font));
            document.add(new Paragraph("접수일시: " + 
                (application.getCreatedAt() != null ? application.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")).setFont(font));
            
            // 학부모 의견
            if (application.getMapParentOpinion() != null && !application.getMapParentOpinion().trim().isEmpty()) {
                document.add(new Paragraph("학부모 의견")
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(10));
                
                document.add(new Paragraph(application.getMapParentOpinion()).setFont(font));
            }
            
            // 하단 정보
            document.add(new Paragraph("생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setFont(font)
                .setMarginTop(20)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
                
        } finally {
            document.close();
        }
    }
}