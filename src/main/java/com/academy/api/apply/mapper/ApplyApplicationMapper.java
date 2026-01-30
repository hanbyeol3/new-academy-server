package com.academy.api.apply.mapper;

import com.academy.api.apply.domain.*;
import com.academy.api.apply.dto.*;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 원서접수 매퍼.
 * 
 * Entity ↔ DTO 변환을 담당합니다.
 * 표준 Response 형태 변환 및 파일 처리 지원.
 */
@Component
public class ApplyApplicationMapper {

    /**
     * Create 요청을 엔티티로 변환.
     */
    public ApplyApplication toEntity(RequestApplyApplicationCreate request) {
        return ApplyApplication.builder()
                .status(ApplicationStatus.REGISTERED) // 기본 상태
                .division(request.getDivision())
                .studentName(request.getStudentName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .studentPhone(request.getStudentPhone())
                .schoolName(request.getSchoolName())
                .schoolGrade(request.getSchoolGrade())
                .studentGradeLevel(request.getStudentGradeLevel())
                .email(request.getEmail())
                .postalCode(request.getPostalCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .parentOpinion(request.getParentOpinion())
                .mapParentOpinion(request.getMapParentOpinion())
                .desiredUniversity(request.getDesiredUniversity())
                .desiredDepartment(request.getDesiredDepartment())
                .guardian1Name(request.getGuardian1Name())
                .guardian1Phone(request.getGuardian1Phone())
                .guardian1Relation(request.getGuardian1Relation())
                .guardian2Name(request.getGuardian2Name())
                .guardian2Phone(request.getGuardian2Phone())
                .guardian2relation(request.getGuardian2Relation())
                .assigneeName(request.getAssigneeName())
                .build();
    }

    /**
     * Create 요청을 엔티티로 변환 (생성자 정보 포함).
     */
    public ApplyApplication toEntityWithCreatedBy(RequestApplyApplicationCreate request, Long createdBy) {
        return ApplyApplication.builder()
                .status(ApplicationStatus.REGISTERED)
                .division(request.getDivision())
                .studentName(request.getStudentName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .studentPhone(request.getStudentPhone())
                .schoolName(request.getSchoolName())
                .schoolGrade(request.getSchoolGrade())
                .studentGradeLevel(request.getStudentGradeLevel())
                .email(request.getEmail())
                .postalCode(request.getPostalCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .parentOpinion(request.getParentOpinion())
                .mapParentOpinion(request.getMapParentOpinion())
                .desiredUniversity(request.getDesiredUniversity())
                .desiredDepartment(request.getDesiredDepartment())
                .guardian1Name(request.getGuardian1Name())
                .guardian1Phone(request.getGuardian1Phone())
                .guardian1Relation(request.getGuardian1Relation())
                .guardian2Name(request.getGuardian2Name())
                .guardian2Phone(request.getGuardian2Phone())
                .guardian2relation(request.getGuardian2Relation())
                .assigneeName(request.getAssigneeName())
                .createdBy(createdBy)
                .build();
    }

    /**
     * Update 요청으로 엔티티 업데이트.
     */
    public void updateEntityFromRequest(ApplyApplication entity, RequestApplyApplicationUpdate request, Long updatedBy) {
        entity.update(
                request.getStudentName(),
                request.getGender(),
                request.getBirthDate(),
                request.getStudentPhone(),
                request.getSchoolName(),
                request.getSchoolGrade(),
                request.getStudentGradeLevel(),
                request.getEmail(),
                request.getPostalCode(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getLatitude(),
                request.getLongitude(),
                request.getParentOpinion(),
                request.getMapParentOpinion(),
                request.getDesiredUniversity(),
                request.getDesiredDepartment(),
                request.getGuardian1Name(),
                request.getGuardian1Phone(),
                request.getGuardian1Relation(),
                request.getGuardian2Name(),
                request.getGuardian2Phone(),
                request.getGuardian2Relation(),
                updatedBy
        );
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponseApplyApplicationDetail toDetailResponse(ApplyApplication entity) {
        return ResponseApplyApplicationDetail.from(entity);
    }

    /**
     * 엔티티를 관리자 목록 응답 DTO로 변환.
     */
    public ResponseApplyApplicationAdminList toAdminListResponse(ApplyApplication entity) {
        return ResponseApplyApplicationAdminList.from(entity);
    }

    /**
     * 엔티티 목록을 관리자 목록 ResponseList로 변환.
     */
    public ResponseList<ResponseApplyApplicationAdminList> toAdminListResponseList(Page<ApplyApplication> page) {
        List<ResponseApplyApplicationAdminList> items = page.getContent().stream()
                .map(this::toAdminListResponse)
                .toList();
        
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 회원 이름을 포함한 관리자 목록 ResponseList로 변환.
     */
    public ResponseList<ResponseApplyApplicationAdminList> toAdminListResponseListWithNames(Page<ApplyApplication> page, 
                                                                                          List<String> createdByNames, 
                                                                                          List<String> updatedByNames) {
        List<ResponseApplyApplicationAdminList> items = page.getContent().stream()
                .map(ResponseApplyApplicationAdminList::from)
                .toList();
        
        // 회원 이름 설정 로직은 서비스에서 처리
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 이력 생성 요청을 엔티티로 변환.
     */
    public ApplyApplicationLog toLogEntity(Long applyId, RequestApplyApplicationLogCreate request, 
                                          ApplyApplication applyApplication) {
        return ApplyApplicationLog.builder()
                .applyApplication(applyApplication)
                .logType(request.getLogType())
                .logContent(request.getLogContent())
                .nextStatus(request.getNextStatus())
                .nextAssigneeId(request.getNextAssigneeId())
                .build();
    }

    /**
     * 이력 엔티티를 응답 DTO로 변환.
     */
    public ResponseApplyApplicationLog toLogResponse(ApplyApplicationLog entity) {
        return ResponseApplyApplicationLog.from(entity);
    }

    /**
     * 이력 엔티티 목록을 응답 DTO 목록으로 변환.
     */
    public List<ResponseApplyApplicationLog> toLogResponses(List<ApplyApplicationLog> entities) {
        return entities.stream()
                .map(this::toLogResponse)
                .toList();
    }

    /**
     * 과목 엔티티를 응답 DTO로 변환.
     */
    public ResponseApplyApplicationSubject toSubjectResponse(ApplyApplicationSubject entity) {
        return ResponseApplyApplicationSubject.from(entity);
    }

    /**
     * 과목 엔티티 목록을 응답 DTO 목록으로 변환.
     */
    public List<ResponseApplyApplicationSubject> toSubjectResponses(List<ApplyApplicationSubject> entities) {
        return entities.stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    /**
     * 관리자 생성 이력 생성.
     */
    public ApplyApplicationLog createAdminCreateLog(ApplyApplication applyApplication, 
                                                   String adminName, Long createdBy) {
        return ApplyApplicationLog.builder()
                .applyApplication(applyApplication)
                .logType(ApplicationLogType.CREATE)
                .logContent("관리자 " + adminName + "이(가) 원서접수를 생성하였습니다.")
                .createdBy(createdBy)
                .build();
    }

    /**
     * 시스템 생성 이력 생성 (외부 접수).
     */
    public ApplyApplicationLog createSystemCreateLog(ApplyApplication applyApplication) {
        return ApplyApplicationLog.builder()
                .applyApplication(applyApplication)
                .logType(ApplicationLogType.CREATE)
                .logContent("온라인을 통해 원서접수가 생성되었습니다.")
                .build();
    }

    /**
     * 수정 이력 생성.
     */
    public ApplyApplicationLog createUpdateLog(ApplyApplication applyApplication, 
                                              String updateReason, Long updatedBy) {
        return ApplyApplicationLog.builder()
                .applyApplication(applyApplication)
                .logType(ApplicationLogType.UPDATE)
                .logContent("정보 수정: " + updateReason)
                .createdBy(updatedBy)
                .build();
    }

    /**
     * 담당자 배정 이력 생성.
     */
    public ApplyApplicationLog createAssignLog(ApplyApplication applyApplication, 
                                              String assigneeName, Long createdBy) {
        return ApplyApplicationLog.builder()
                .applyApplication(applyApplication)
                .logType(ApplicationLogType.UPDATE)
                .logContent("담당자가 [" + assigneeName + "]로 배정되었습니다.")
                .createdBy(createdBy)
                .build();
    }
}