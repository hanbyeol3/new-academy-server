package com.academy.api.teacher.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.category.service.CategoryUsageChecker;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.file.service.FileService;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.teacher.domain.Teacher;
import com.academy.api.teacher.domain.TeacherSubject;
import com.academy.api.teacher.dto.RequestTeacherCreate;
import com.academy.api.teacher.dto.RequestTeacherUpdate;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherListItem;
import com.academy.api.teacher.mapper.TeacherMapper;
import com.academy.api.teacher.repository.TeacherRepository;
import com.academy.api.teacher.repository.TeacherSubjectRepository;
import com.academy.api.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 강사 서비스 구현체.
 * 
 * - 강사 CRUD 비즈니스 로직 처리
 * - CategoryUsageChecker 구현: 과목 삭제 시 강사 연결 확인
 * - 강사 1명 : 과목 N개 (1:다) 관계 관리
 * - 파일 업로드 및 임시파일 → 정식파일 변환
 * - 검색 및 필터링 기능
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
public class TeacherServiceImpl implements TeacherService, CategoryUsageChecker {

    private final TeacherRepository teacherRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final CategoryRepository categoryRepository;
    private final TeacherMapper teacherMapper;
    private final FileService fileService;
    private final UploadFileLinkRepository uploadFileLinkRepository;

    // ================== CategoryUsageChecker 구현 ==================
    
    @Override
    public boolean hasDataUsingCategory(Long categoryId) {
        long teacherCount = teacherRepository.countTeachersBySubjectCategoryId(categoryId);
        
        log.debug("[TeacherService] 카테고리 사용 확인. categoryId={}, 연결된강사수={}", 
                categoryId, teacherCount);
        
        return teacherCount > 0;
    }
    
    @Override
    public String getDomainName() {
        return "강사";
    }

    // ================== CRUD 구현 ==================

    /**
     * 강사 목록 조회 (관리자용 - 통합 검색).
     */
    @Override
    public ResponseList<ResponseTeacherListItem> getTeacherList(String keyword, Long categoryId, Boolean isPublished, Pageable pageable) {
        log.info("[TeacherService] 강사 목록 조회 시작. keyword={}, categoryId={}, isPublished={}, page={}, size={}", 
                keyword, categoryId, isPublished, pageable.getPageNumber(), pageable.getPageSize());

        Page<Teacher> teacherPage;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategoryId = categoryId != null;
        
        // 과목별 필터링 + 키워드 + 공개 상태 조합
        if (hasCategoryId) {
            if (hasKeyword && isPublished != null) {
                // 과목 + 키워드 + 공개 상태 (복합 조건이므로 단순화)
                // TODO: 추후 Repository 메서드 추가 시 최적화
                teacherPage = teacherRepository.findBySubjectCategoryIdAndIsPublishedWithSubjects(categoryId, isPublished, pageable);
                log.debug("[TeacherService] 과목+필터 검색 완료 (키워드는 후처리). categoryId={}, isPublished={}, 결과수={}", 
                        categoryId, isPublished, teacherPage.getTotalElements());
            } else if (hasKeyword) {
                // 과목 + 키워드 (복합 조건이므로 단순화)  
                // TODO: 추후 Repository 메서드 추가 시 최적화
                teacherPage = teacherRepository.findBySubjectCategoryIdWithSubjects(categoryId, pageable);
                log.debug("[TeacherService] 과목 검색 완료 (키워드는 후처리). categoryId={}, 결과수={}", categoryId, teacherPage.getTotalElements());
            } else if (isPublished != null) {
                // 과목 + 공개 상태
                teacherPage = teacherRepository.findBySubjectCategoryIdAndIsPublishedWithSubjects(categoryId, isPublished, pageable);
                log.debug("[TeacherService] 과목+필터 검색 완료. categoryId={}, isPublished={}, 결과수={}", 
                        categoryId, isPublished, teacherPage.getTotalElements());
            } else {
                // 과목만
                teacherPage = teacherRepository.findBySubjectCategoryIdWithSubjects(categoryId, pageable);
                log.debug("[TeacherService] 과목 검색 완료. categoryId={}, 결과수={}", categoryId, teacherPage.getTotalElements());
            }
        } else {
            // 기존 로직 (과목 필터링 없음)
            if (hasKeyword && isPublished != null) {
                // 키워드 + 필터 조합
                teacherPage = teacherRepository.findByTeacherNameContainingAndIsPublishedWithSubjects(keyword.trim(), isPublished, pageable);
                log.debug("[TeacherService] 키워드+필터 검색 완료. keyword={}, isPublished={}, 결과수={}", 
                        keyword, isPublished, teacherPage.getTotalElements());
            } else if (hasKeyword) {
                // 키워드만
                teacherPage = teacherRepository.findByTeacherNameContainingWithSubjects(keyword.trim(), pageable);
                log.debug("[TeacherService] 키워드 검색 완료. keyword={}, 결과수={}", keyword, teacherPage.getTotalElements());
            } else if (isPublished != null) {
                if (isPublished) {
                    // 공개만
                    teacherPage = teacherRepository.findPublishedWithSubjects(pageable);
                } else {
                    // 비공개만
                    teacherPage = teacherRepository.findUnpublishedWithSubjects(pageable);
                }
                log.debug("[TeacherService] 공개상태 필터링 완료. isPublished={}, 결과수={}", 
                        isPublished, teacherPage.getTotalElements());
            } else {
                // 전체 조회
                teacherPage = teacherRepository.findAllWithSubjects(pageable);
                log.debug("[TeacherService] 전체 목록 조회 완료. 총강사수={}", teacherPage.getTotalElements());
            }
        }
        
        ResponseList<ResponseTeacherListItem> result = teacherMapper.toListItemResponseList(teacherPage);
        
        log.debug("[TeacherService] 강사 목록 조회 완료. keyword={}, categoryId={}, isPublished={}, 결과수={}", 
                keyword, categoryId, isPublished, result.getItems().size());
        return result;
    }

    /**
     * 공개 강사 목록 조회 (공개용).
     */
    @Override
    public ResponseList<ResponseTeacherListItem> getPublishedTeacherList(String keyword, Pageable pageable) {
        log.info("[TeacherService] 공개 강사 목록 조회 시작. keyword={}, page={}, size={}", 
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        Page<Teacher> teacherPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            teacherPage = teacherRepository.findPublishedByTeacherNameContainingWithSubjects(keyword.trim(), pageable);
            log.debug("[TeacherService] 공개 강사 키워드 검색 완료. keyword={}, 검색결과수={}", keyword, teacherPage.getTotalElements());
        } else {
            teacherPage = teacherRepository.findPublishedWithSubjects(pageable);
            log.debug("[TeacherService] 공개 강사 전체 목록 조회 완료. 총강사수={}", teacherPage.getTotalElements());
        }

        ResponseList<ResponseTeacherListItem> result = teacherMapper.toListItemResponseList(teacherPage);
        
        log.debug("[TeacherService] 공개 강사 목록 조회 완료. 결과수={}", result.getItems().size());
        return result;
    }

    /**
     * 강사 상세 조회.
     */
    @Override
    public ResponseData<ResponseTeacher> getTeacher(Long id) {
        log.info("[TeacherService] 강사 상세 조회 시작. id={}", id);

        Teacher teacher = teacherRepository.findByIdWithSubjects(id)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 강사를 찾을 수 없음. id={}", id);
                    return new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                });

        ResponseTeacher response = teacherMapper.toResponse(teacher);
        
        log.debug("[TeacherService] 강사 상세 조회 완료. id={}, teacherName={}, 과목수={}", 
                id, teacher.getTeacherName(), teacher.getSubjects().size());
        
        return ResponseData.ok("0000", "강사 조회 성공", response);
    }

    /**
     * 강사 생성.
     */
    @Override
    @Transactional
    public ResponseData<Long> createTeacher(RequestTeacherCreate request) {
        log.info("[TeacherService] 강사 생성 시작. teacherName={}, 과목수={}", 
                request.getTeacherName(), 
                request.getSubjectCategoryIds() != null ? request.getSubjectCategoryIds().size() : 0);

        // 1. 강사명 중복 검사
        if (teacherRepository.existsByTeacherName(request.getTeacherName())) {
            log.warn("[TeacherService] 강사명 중복. teacherName={}", request.getTeacherName());
            throw new BusinessException(ErrorCode.TEACHER_NAME_ALREADY_EXISTS);
        }

        // 2. 강사 엔티티 생성
        Teacher teacher = teacherMapper.toEntity(request);
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.debug("[TeacherService] 강사 엔티티 저장 완료. id={}", savedTeacher.getId());

        // 3. 이미지 파일 처리 (임시파일 → 정식파일 → imagePath 설정)
        if (request.getImageTempFileId() != null) {
            log.debug("[TeacherService] 이미지 파일 처리 시작. tempFileId={}", request.getImageTempFileId());
            
            Long formalFileId = fileService.promoteToFormalFile(
                    request.getImageTempFileId(),
                    request.getImageFileName()
            );
            
            log.debug("[TeacherService] 파일 승격 결과. formalFileId={}", formalFileId);
            
            if (formalFileId != null) {
                // imagePath 필드에 직접 설정
                String imagePath = "formal/" + formalFileId;
                savedTeacher.update(
                    savedTeacher.getTeacherName(),
                    savedTeacher.getCareer(),
                    imagePath, // 이미지 경로 설정
                    savedTeacher.getIntroText(),
                    savedTeacher.getMemo(),
                    savedTeacher.getIsPublished(),
                    savedTeacher.getUpdatedBy()
                );
                log.debug("[TeacherService] 이미지 경로 설정 완료. imagePath={}, teacherId={}", 
                        imagePath, savedTeacher.getId());
            } else {
                log.warn("[TeacherService] 파일 승격 실패. tempFileId={}", request.getImageTempFileId());
            }
        }

        // 4. 과목 연결 처리
        if (request.getSubjectCategoryIds() != null && !request.getSubjectCategoryIds().isEmpty()) {
            createTeacherSubjects(savedTeacher, request.getSubjectCategoryIds());
            log.debug("[TeacherService] 과목 연결 완료. 과목수={}", request.getSubjectCategoryIds().size());
        }

        log.info("[TeacherService] 강사 생성 완료. id={}, teacherName={}", 
                savedTeacher.getId(), savedTeacher.getTeacherName());
        
        return ResponseData.ok("0000", "강사가 생성되었습니다.", savedTeacher.getId());
    }

    /**
     * 강사 수정.
     */
    @Override
    @Transactional
    public ResponseData<ResponseTeacher> updateTeacher(Long id, RequestTeacherUpdate request) {
        log.info("[TeacherService] 강사 수정 시작. id={}, teacherName={}", id, request.getTeacherName());

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 수정할 강사를 찾을 수 없음. id={}", id);
                    return new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                });

        // 1. 강사명 중복 검사 (자신 제외)
        if (request.getTeacherName() != null && 
            teacherRepository.existsByTeacherNameAndIdNot(request.getTeacherName(), id)) {
            log.warn("[TeacherService] 강사명 중복. teacherName={}", request.getTeacherName());
            throw new BusinessException(ErrorCode.TEACHER_NAME_ALREADY_EXISTS);
        }

        // 2. 이미지 파일 처리
        String newImagePath = handleImageFile(teacher, request);

        // 3. 기본 정보 업데이트 (이미지 경로 포함)
        teacher.update(
                getValueOrDefault(request.getTeacherName(), teacher.getTeacherName()),
                getValueOrDefault(request.getCareer(), teacher.getCareer()),
                newImagePath, // 처리된 이미지 경로
                getValueOrDefault(request.getIntroText(), teacher.getIntroText()),
                getValueOrDefault(request.getMemo(), teacher.getMemo()),
                getValueOrDefault(request.getIsPublished(), teacher.getIsPublished()),
                SecurityUtils.getCurrentUserId()
        );
        log.debug("[TeacherService] 기본 정보 업데이트 완료");

        // 4. 과목 관계 업데이트
        if (request.getSubjectCategoryIds() != null) {
            updateTeacherSubjects(teacher, request.getSubjectCategoryIds());
            log.debug("[TeacherService] 과목 관계 업데이트 완료. 과목수={}", request.getSubjectCategoryIds().size());
        }

        Teacher updatedTeacher = teacherRepository.findByIdWithSubjects(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        ResponseTeacher response = teacherMapper.toResponse(updatedTeacher);
        
        log.info("[TeacherService] 강사 수정 완료. id={}, teacherName={}", id, updatedTeacher.getTeacherName());
        
        return ResponseData.ok("0000", "강사 정보가 수정되었습니다.", response);
    }

    /**
     * 강사 삭제.
     */
    @Override
    @Transactional
    public Response deleteTeacher(Long id) {
        log.info("[TeacherService] 강사 삭제 시작. id={}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 삭제할 강사를 찾을 수 없음. id={}", id);
                    return new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                });

        // 1. 과목 관계 삭제
        teacherSubjectRepository.deleteByTeacherId(id);
        log.debug("[TeacherService] 과목 관계 삭제 완료");

        // 2. 이미지 파일 삭제
        if (teacher.getImagePath() != null) {
            try {
                fileService.deleteFile(teacher.getImagePath());
                log.debug("[TeacherService] 이미지 파일 삭제 완료. imagePath={}", teacher.getImagePath());
            } catch (Exception e) {
                log.warn("[TeacherService] 이미지 파일 삭제 실패. imagePath={}, error={}", 
                        teacher.getImagePath(), e.getMessage());
            }
        }

        // 3. 강사 엔티티 삭제
        teacherRepository.delete(teacher);
        
        log.info("[TeacherService] 강사 삭제 완료. id={}, teacherName={}", id, teacher.getTeacherName());
        
        return Response.ok("0000", "강사가 삭제되었습니다.");
    }

    /**
     * 강사 공개/비공개 상태 변경.
     */
    @Override
    @Transactional
    public Response updatePublishedStatus(Long id, Boolean isPublished) {
        log.info("[TeacherService] 강사 공개상태 변경 시작. id={}, isPublished={}", id, isPublished);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 강사를 찾을 수 없음. id={}", id);
                    return new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                });

        teacher.update(
                teacher.getTeacherName(),
                teacher.getCareer(),
                teacher.getImagePath(),
                teacher.getIntroText(),
                teacher.getMemo(),
                isPublished,
                teacher.getUpdatedBy()
        );

        String statusText = isPublished ? "공개" : "비공개";
        log.info("[TeacherService] 강사 공개상태 변경 완료. id={}, status={}", id, statusText);
        
        return Response.ok("0000", "강사 " + statusText + " 상태로 변경되었습니다.");
    }

    /**
     * 과목별 강사 조회 (공개용).
     */
    @Override
    public ResponseList<ResponseTeacherListItem> getPublishedTeachersBySubject(Long categoryId, Pageable pageable) {
        log.info("[TeacherService] 과목별 강사 조회 시작 (공개). categoryId={}, page={}, size={}", 
                categoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Teacher> teacherPage = teacherRepository.findPublishedBySubjectCategoryIdWithSubjects(categoryId, pageable);
        
        ResponseList<ResponseTeacherListItem> result = teacherMapper.toListItemResponseList(teacherPage);
        
        log.debug("[TeacherService] 과목별 강사 조회 완료 (공개). categoryId={}, 결과수={}", 
                categoryId, result.getItems().size());
        
        return result;
    }

    // ================== 내부 도우미 메서드 ==================

    /**
     * 강사-과목 관계 생성.
     */
    private void createTeacherSubjects(Teacher teacher, List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        
        if (categories.size() != categoryIds.size()) {
            log.warn("[TeacherService] 일부 카테고리를 찾을 수 없음. 요청={}, 조회={}", 
                    categoryIds.size(), categories.size());
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<TeacherSubject> teacherSubjects = categories.stream()
                .map(category -> teacherMapper.createTeacherSubject(teacher, category))
                .collect(Collectors.toList());

        teacherSubjectRepository.saveAll(teacherSubjects);
    }

    /**
     * 강사-과목 관계 업데이트.
     */
    private void updateTeacherSubjects(Teacher teacher, List<Long> categoryIds) {
        // 1. 기존 관계 삭제
        teacherSubjectRepository.deleteByTeacherId(teacher.getId());

        // 2. 새로운 관계 생성
        if (!categoryIds.isEmpty()) {
            createTeacherSubjects(teacher, categoryIds);
        }
    }

    /**
     * 이미지 파일 처리.
     * 
     * @param teacher 강사 엔티티
     * @param request 수정 요청
     * @return 새로운 imagePath (삭제시 null, 변경없으면 기존값, 새로 업로드시 새 경로)
     */
    private String handleImageFile(Teacher teacher, RequestTeacherUpdate request) {
        if (request.getDeleteImage() != null && request.getDeleteImage()) {
            // 이미지 삭제
            if (teacher.getImagePath() != null) {
                try {
                    // imagePath에서 파일 ID 추출하여 삭제
                    if (teacher.getImagePath().startsWith("formal/")) {
                        Long fileId = Long.parseLong(teacher.getImagePath().substring("formal/".length()));
                        fileService.deleteFile("formal/" + fileId);
                        log.debug("[TeacherService] 기존 이미지 파일 삭제 완료. fileId={}", fileId);
                    }
                } catch (Exception e) {
                    log.warn("[TeacherService] 기존 이미지 파일 삭제 실패: {}", e.getMessage());
                }
            }
            return null; // imagePath를 null로 설정
        } else if (request.getImageTempFileId() != null) {
            // 새 이미지 업로드
            try {
                Long formalFileId = fileService.promoteToFormalFile(
                        request.getImageTempFileId(),
                        request.getImageFileName()
                );
                
                if (formalFileId != null) {
                    String newImagePath = "formal/" + formalFileId;
                    
                    // 기존 이미지 파일 삭제
                    if (teacher.getImagePath() != null) {
                        try {
                            if (teacher.getImagePath().startsWith("formal/")) {
                                Long oldFileId = Long.parseLong(teacher.getImagePath().substring("formal/".length()));
                                fileService.deleteFile("formal/" + oldFileId);
                                log.debug("[TeacherService] 기존 이미지 파일 교체 삭제 완료. oldFileId={}", oldFileId);
                            }
                        } catch (Exception e) {
                            log.warn("[TeacherService] 기존 이미지 파일 교체 삭제 실패: {}", e.getMessage());
                        }
                    }
                    
                    log.debug("[TeacherService] 새 이미지 파일 처리 완료. newImagePath={}", newImagePath);
                    return newImagePath;
                } else {
                    log.warn("[TeacherService] 파일 승격 실패. tempFileId={}", request.getImageTempFileId());
                    return teacher.getImagePath(); // 기존 경로 유지
                }
            } catch (Exception e) {
                log.error("[TeacherService] 이미지 파일 처리 실패: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        
        // 이미지 변경 없음
        return teacher.getImagePath();
    }

    /**
     * 부분 업데이트를 위한 null 체크 도우미 메서드.
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }
}