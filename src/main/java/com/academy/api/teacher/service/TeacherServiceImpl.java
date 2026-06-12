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
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.teacher.domain.Teacher;
import com.academy.api.teacher.domain.TeacherCareer;
import com.academy.api.teacher.domain.TeacherSubject;
import com.academy.api.teacher.dto.*;
import com.academy.api.teacher.mapper.TeacherMapper;
import com.academy.api.teacher.repository.TeacherRepository;
import com.academy.api.teacher.repository.TeacherCareerRepository;
import com.academy.api.teacher.repository.TeacherSubjectRepository;
import com.academy.api.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final TeacherCareerRepository teacherCareerRepository;
    private final CategoryRepository categoryRepository;
    private final TeacherMapper teacherMapper;
    private final FileService fileService;

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
    public ResponseList<ResponseTeacherListItem> getTeacherList(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable) {
        log.info("[TeacherService] 강사 목록 조회 시작. keyword={}, categoryId={}, isPublished={}, sortType={}, 페이지={}", 
                keyword, categoryId, isPublished, sortType, pageable);

        // sortType 유효성 검사 및 기본값 설정
        String effectiveSortType = sortType;
        if (sortType != null && !sortType.trim().isEmpty()) {
            // 유효한 sortType 값인지 확인
            if (!isValidSortType(sortType.trim())) {
                log.warn("[TeacherService] 유효하지 않은 정렬 타입: {}. 기본값 사용", sortType);
                effectiveSortType = "CREATED_DESC";
            }
        } else {
            effectiveSortType = "CREATED_DESC";
        }

        // ✅ 단일 경로: QueryDSL 통합 처리
        Page<Teacher> teacherPage = teacherRepository.searchTeachersForAdmin(keyword, categoryId, isPublished, effectiveSortType, pageable);
        
        log.debug("[TeacherService] 강사 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                teacherPage.getTotalElements(), teacherPage.getNumber(), teacherPage.getNumberOfElements());

        ResponseList<ResponseTeacherListItem> result = teacherMapper.toListItemResponseList(teacherPage);
        
        log.debug("[TeacherService] 강사 목록 조회 완료. keyword={}, categoryId={}, isPublished={}, 결과수={}", 
                keyword, categoryId, isPublished, result.getItems().size());
        return result;
    }

    /**
     * 공개 강사 목록 조회 (공개용).
     */
    @Override
    public ResponseList<ResponseTeacherListItem> getPublishedTeacherList(String keyword, Long categoryId, Pageable pageable) {
        log.info("[TeacherService] 공개 강사 목록 조회 시작. keyword={}, categoryId={}, page={}, size={}", 
                keyword, categoryId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Teacher> teacherPage;
        
        // keyword와 categoryId 조합에 따른 분기 처리
        if (categoryId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 키워드 + 카테고리 검색
                teacherPage = teacherRepository.findPublishedByCategoryAndKeyword(categoryId, keyword.trim(), pageable);
                log.debug("[TeacherService] 공개 강사 카테고리+키워드 검색 완료. categoryId={}, keyword={}, 검색결과수={}", 
                        categoryId, keyword, teacherPage.getTotalElements());
            } else {
                // 카테고리만 검색
                teacherPage = teacherRepository.findPublishedByCategory(categoryId, pageable);
                log.debug("[TeacherService] 공개 강사 카테고리 검색 완료. categoryId={}, 검색결과수={}", 
                        categoryId, teacherPage.getTotalElements());
            }
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 키워드만 검색
                teacherPage = teacherRepository.findPublishedByTeacherNameContainingWithSubjects(keyword.trim(), pageable);
                log.debug("[TeacherService] 공개 강사 키워드 검색 완료. keyword={}, 검색결과수={}", keyword, teacherPage.getTotalElements());
            } else {
                // 전체 조회
                teacherPage = teacherRepository.findPublishedWithSubjects(pageable);
                log.debug("[TeacherService] 공개 강사 전체 목록 조회 완료. 총강사수={}", teacherPage.getTotalElements());
            }
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
        log.info("[TeacherService] 강사 생성 시작. teacherName={}, categoryId={}", 
                request.getTeacherName(), 
                request.getCategoryId());

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
                    savedTeacher.getRoleName(),
                    imagePath, // 이미지 경로 설정
                    savedTeacher.getIntroText(),
                    savedTeacher.getMemo(),
                    savedTeacher.getIsPublished(),
                    savedTeacher.getIsComingSoon(),
                    savedTeacher.getIsMain(),
                    savedTeacher.getMainSortOrder(),
                    savedTeacher.getUpdatedBy()
                );
                log.debug("[TeacherService] 이미지 경로 설정 완료. imagePath={}, teacherId={}", 
                        imagePath, savedTeacher.getId());
            } else {
                log.warn("[TeacherService] 파일 승격 실패. tempFileId={}", request.getImageTempFileId());
            }
        }

        // 4. 과목 연결 처리 (단일 과목)
        if (request.getCategoryId() != null) {
            assignTeacherToSubject(savedTeacher, request.getCategoryId());
            log.debug("[TeacherService] 과목 연결 완료. categoryId={}", request.getCategoryId());
        }

        // 5. 경력 정보 처리
        if (request.getCareers() != null && !request.getCareers().isEmpty()) {
            createTeacherCareers(savedTeacher, request.getCareers());
            log.debug("[TeacherService] 경력 정보 저장 완료. 경력수={}", request.getCareers().size());
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
                getValueOrDefault(request.getRoleName(), teacher.getRoleName()),
                newImagePath, // 처리된 이미지 경로
                getValueOrDefault(request.getIntroText(), teacher.getIntroText()),
                getValueOrDefault(request.getMemo(), teacher.getMemo()),
                getValueOrDefault(request.getIsPublished(), teacher.getIsPublished()),
                getValueOrDefault(request.getIsComingSoon(), teacher.getIsComingSoon()),
                getValueOrDefault(request.getIsMain(), teacher.getIsMain()),
                getValueOrDefault(request.getMainSortOrder(), teacher.getMainSortOrder()),
                SecurityUtils.getCurrentUserId()
        );
        log.debug("[TeacherService] 기본 정보 업데이트 완료");

        // 4. 과목 관계 업데이트 (단일 과목, 순서 유지)
        if (request.getCategoryId() != null) {
            updateTeacherSubject(teacher, request.getCategoryId());
            log.debug("[TeacherService] 과목 관계 업데이트 완료. categoryId={}", request.getCategoryId());
        }

        // 5. 경력 정보 업데이트
        if (request.getCareers() != null) {
            updateTeacherCareers(teacher, request.getCareers());
            log.debug("[TeacherService] 경력 정보 업데이트 완료. 경력수={}", request.getCareers().size());
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
                teacher.getRoleName(),
                teacher.getImagePath(),
                teacher.getIntroText(),
                teacher.getMemo(),
                isPublished,
                teacher.getIsComingSoon(),
                teacher.getIsMain(),
                teacher.getMainSortOrder(),
                SecurityUtils.getCurrentUserId()
        );

        String statusText = isPublished ? "공개" : "비공개";
        log.info("[TeacherService] 강사 공개상태 변경 완료. id={}, status={}", id, statusText);
        
        return Response.ok("0000", "강사 " + statusText + " 상태로 변경되었습니다.");
    }

    // ================== 내부 도우미 메서드 ==================

    /**
     * 강사에게 단일 과목 할당 (생성 시).
     * 
     * @param teacher 강사 엔티티
     * @param categoryId 과목 카테고리 ID
     */
    private void assignTeacherToSubject(Teacher teacher, Long categoryId) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 과목 카테고리를 찾을 수 없음. categoryId={}", categoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
        
        // 2. 해당 과목의 최대 sort_order 조회
        Integer maxSortOrder = teacherSubjectRepository.findMaxSortOrderByCategoryId(categoryId);
        
        // 3. 새로운 과목 연결 생성 (자동으로 마지막 순서로 추가)
        TeacherSubject subject = TeacherSubject.builder()
                .teacher(teacher)
                .category(category)
                .sortOrder(maxSortOrder != null ? maxSortOrder + 1 : 0)
                .build();
        
        teacherSubjectRepository.save(subject);
        log.debug("[TeacherService] 강사-과목 연결 생성 완료. teacherId={}, categoryId={}, sortOrder={}", 
                teacher.getId(), categoryId, subject.getSortOrder());
    }
    
    /**
     * 강사의 과목 변경 (수정 시, 순서 유지).
     * 
     * @param teacher 강사 엔티티
     * @param newCategoryId 새로운 과목 카테고리 ID
     */
    private void updateTeacherSubject(Teacher teacher, Long newCategoryId) {
        // 1. 기존 과목 연결 조회
        Optional<TeacherSubject> existingSubject = teacherSubjectRepository.findByTeacherId(teacher.getId());
        
        if (existingSubject.isEmpty()) {
            // 과목 연결이 없는 경우 새로 생성
            log.debug("[TeacherService] 기존 과목 연결 없음. 새로 생성. teacherId={}", teacher.getId());
            assignTeacherToSubject(teacher, newCategoryId);
            return;
        }
        
        TeacherSubject subject = existingSubject.get();
        
        // 2. 같은 과목이면 순서 유지, 변경 없음
        if (subject.getCategory().getId().equals(newCategoryId)) {
            log.debug("[TeacherService] 동일한 과목. 변경 없음. teacherId={}, categoryId={}", 
                    teacher.getId(), newCategoryId);
            return;
        }
        
        // 3. 다른 과목으로 변경하는 경우
        Category newCategory = categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 새 과목 카테고리를 찾을 수 없음. categoryId={}", newCategoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
        
        // 새 과목에서의 최대 순서 조회
        Integer maxSortOrder = teacherSubjectRepository.findMaxSortOrderByCategoryId(newCategoryId);
        
        // 과목 변경 및 새 과목에서는 마지막 순서로 추가
        subject.changeCategory(newCategory);
        subject.changeSortOrder(maxSortOrder != null ? maxSortOrder + 1 : 0);
        
        teacherSubjectRepository.save(subject);
        log.debug("[TeacherService] 강사 과목 변경 완료. teacherId={}, oldCategory={}, newCategory={}, sortOrder={}", 
                teacher.getId(), subject.getCategory().getId(), newCategoryId, subject.getSortOrder());
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

    /**
     * 정렬 타입 유효성 검사 도우미 메서드.
     */
    private boolean isValidSortType(String sortType) {
        return sortType.equals("CREATED_DESC") || 
               sortType.equals("CREATED_ASC") || 
               sortType.equals("NAME_ASC") || 
               sortType.equals("NAME_DESC") ||
               sortType.equals("ORDER_ASC") ||
               sortType.equals("ORDER_DESC");
    }

    /**
     * 강사 경력 생성.
     * 
     * @param teacher 강사 엔티티
     * @param careerItems 경력 항목 리스트
     */
    private void createTeacherCareers(Teacher teacher, List<com.academy.api.teacher.dto.CareerItem> careerItems) {
        int sortOrder = 0;
        for (com.academy.api.teacher.dto.CareerItem item : careerItems) {
            if (item.getText() != null && !item.getText().trim().isEmpty()) {
                TeacherCareer career = TeacherCareer.builder()
                        .teacher(teacher)
                        .careerText(item.getText())
                        .isHighlight(item.getHighlight() != null ? item.getHighlight() : false)
                        .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : sortOrder++)
                        .build();
                
                teacherCareerRepository.save(career);
                log.debug("[TeacherService] 경력 항목 저장. text={}, highlight={}, sortOrder={}", 
                        item.getText(), item.getHighlight(), career.getSortOrder());
            }
        }
    }

    /**
     * 강사 경력 업데이트 (삭제 후 재생성).
     * 
     * @param teacher 강사 엔티티
     * @param careerItems 경력 항목 리스트
     */
    private void updateTeacherCareers(Teacher teacher, List<com.academy.api.teacher.dto.CareerItem> careerItems) {
        // 1. 기존 경력을 엔티티 컬렉션에서 모두 제거 (orphanRemoval이 자동으로 DELETE 실행)
        teacher.getCareers().clear();
        
        // 2. flush를 통해 DELETE 쿼리 즉시 실행
        teacherRepository.flush();
        log.debug("[TeacherService] 기존 경력 삭제 완료. teacherId={}", teacher.getId());
        
        // 3. 새 경력 추가
        if (careerItems != null && !careerItems.isEmpty()) {
            for (int i = 0; i < careerItems.size(); i++) {
                com.academy.api.teacher.dto.CareerItem item = careerItems.get(i);
                
                TeacherCareer career = TeacherCareer.builder()
                        .teacher(teacher)
                        .careerText(item.getText())
                        .isHighlight(item.getHighlight() != null ? item.getHighlight() : false)
                        .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : i)
                        .build();
                
                teacher.getCareers().add(career);
            }
            log.debug("[TeacherService] 새 경력 {} 개 추가 완료", careerItems.size());
        }
    }

    /**
     * 카테고리별 강사 목록 조회 (공개용).
     * 과목 그룹(ID=4)의 모든 카테고리별로 공개된 강사 목록을 그룹화하여 반환.
     * 각 과목 내에서는 sort_order 순서대로 강사를 정렬하여 반환.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseData<List<ResponseTeacherByCategory>> getTeachersByCategory() {
        log.info("[TeacherService] 카테고리별 강사 목록 조회 시작");
        
        // 1. 과목 카테고리 그룹(ID=4)의 모든 카테고리 조회 (sortOrder 순)
        Long subjectGroupId = 4L; // 과목 그룹 ID
        List<Category> categories = categoryRepository.findByCategoryGroupIdOrderBySortOrder(subjectGroupId);
        log.debug("[TeacherService] 과목 카테고리 조회 완료. 카테고리 수={}", categories.size());
        
        // 2. ResponseTeacherByCategory 리스트 생성
        List<ResponseTeacherByCategory> responseList = new ArrayList<>();
        
        for (Category category : categories) {
            // 3. 각 카테고리별로 강사-과목 연결 조회 (sort_order 순서대로)
            List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findPublishedByCategoryIdOrderBySortOrder(category.getId());
            
            // 4. TeacherSubject를 TeacherSimple로 변환 (sortOrder 포함)
            List<TeacherSimple> teacherSimpleList = teacherSubjects.stream()
                    .map(ts -> {
                        TeacherSimple simple = teacherMapper.toTeacherSimple(ts.getTeacher());
                        // sortOrder를 직접 설정 (Builder 패턴이므로 새로 생성)
                        return TeacherSimple.builder()
                                .id(simple.getId())
                                .name(simple.getName())
                                .roleName(simple.getRoleName())
                                .comingSoon(simple.getComingSoon())
                                .image(simple.getImage())
                                .careers(simple.getCareers())
                                .sortOrder(ts.getSortOrder())
                                .build();
                    })
                    .collect(Collectors.toList());
            
            // 5. ResponseTeacherByCategory 생성
            ResponseTeacherByCategory categoryResponse = ResponseTeacherByCategory.builder()
                    .categoryId(category.getId())
                    .slug(category.getSlug())
                    .name(category.getName())
                    .description(category.getDescription())
                    .teachers(teacherSimpleList)
                    .build();
            responseList.add(categoryResponse);
            
            log.debug("[TeacherService] 카테고리별 매핑 완료. category={}, 강사수={}", 
                    category.getName(), teacherSimpleList.size());
        }
        
        log.info("[TeacherService] 카테고리별 강사 목록 조회 완료. 카테고리 수={}", categories.size());
        
        return ResponseData.ok("0000", "카테고리별 강사 목록 조회 성공", responseList);
    }

    /**
     * 카테고리별 강사 순서 변경.
     * 특정 과목 카테고리 내에서 강사들의 표시 순서를 변경합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param request 정렬된 강사 ID 목록
     * @return 순서 변경 결과
     */
    @Override
    @Transactional
    public Response updateCategoryTeacherOrder(Long categoryId, com.academy.api.category.dto.RequestTeacherOrderUpdate request) {
        log.info("[TeacherService] 카테고리별 강사 순서 변경 시작. categoryId={}, teacherCount={}", 
                categoryId, request.getTeacherIds().size());

        // 1. 카테고리 존재 여부 확인
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("[TeacherService] 존재하지 않는 카테고리. categoryId={}", categoryId);
            return Response.error("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.");
        }

        // 2. 해당 카테고리의 모든 강사-과목 연결 조회
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findByCategoryIdOrderBySortOrder(categoryId);
        
        if (teacherSubjects.isEmpty()) {
            log.warn("[TeacherService] 해당 카테고리에 강사가 없음. categoryId={}", categoryId);
            return Response.error("NO_TEACHERS_IN_CATEGORY", "해당 과목에 등록된 강사가 없습니다.");
        }

        // 3. 요청된 강사 ID 목록과 실제 강사 ID 목록 비교
        Set<Long> requestedTeacherIds = new HashSet<>(request.getTeacherIds());
        Set<Long> actualTeacherIds = teacherSubjects.stream()
                .map(ts -> ts.getTeacher().getId())
                .collect(Collectors.toSet());

        // 3-1. 요청된 강사 ID 중 해당 카테고리에 없는 강사가 있는지 확인
        Set<Long> invalidTeacherIds = new HashSet<>(requestedTeacherIds);
        invalidTeacherIds.removeAll(actualTeacherIds);
        if (!invalidTeacherIds.isEmpty()) {
            log.warn("[TeacherService] 해당 카테고리에 속하지 않은 강사 ID 포함. invalidIds={}", invalidTeacherIds);
            return Response.error("INVALID_TEACHER_IDS", "해당 과목에 속하지 않은 강사가 포함되어 있습니다.");
        }

        // 3-2. 실제 강사 중 요청에 누락된 강사가 있는지 확인
        Set<Long> missingTeacherIds = new HashSet<>(actualTeacherIds);
        missingTeacherIds.removeAll(requestedTeacherIds);
        if (!missingTeacherIds.isEmpty()) {
            log.warn("[TeacherService] 요청에 누락된 강사 ID 존재. missingIds={}", missingTeacherIds);
            return Response.error("MISSING_TEACHER_IDS", "일부 강사가 누락되었습니다. 모든 강사를 포함해야 합니다.");
        }

        // 4. 순서 업데이트
        Map<Long, TeacherSubject> teacherSubjectMap = teacherSubjects.stream()
                .collect(Collectors.toMap(ts -> ts.getTeacher().getId(), ts -> ts));

        for (int i = 0; i < request.getTeacherIds().size(); i++) {
            Long teacherId = request.getTeacherIds().get(i);
            TeacherSubject teacherSubject = teacherSubjectMap.get(teacherId);
            if (teacherSubject != null) {
                teacherSubject.changeSortOrder(i);
                log.debug("[TeacherService] 강사 순서 업데이트. teacherId={}, newOrder={}", teacherId, i);
            }
        }

        // 5. 변경사항 저장
        teacherSubjectRepository.saveAll(teacherSubjects);

        log.info("[TeacherService] 카테고리별 강사 순서 변경 완료. categoryId={}, updatedCount={}", 
                categoryId, teacherSubjects.size());
        
        return Response.ok("0000", "강사 순서가 성공적으로 변경되었습니다.");
    }

    /**
     * 메인 강사 여부 변경.
     */
    @Override
    @Transactional
    public Response updateMainStatus(Long id, Boolean isMain) {
        log.info("[TeacherService] 메인 강사 여부 변경 시작. id={}, isMain={}", id, isMain);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TeacherService] 강사를 찾을 수 없음. id={}", id);
                    return new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                });

        // 메인 강사 설정/해제
        teacher.updateMainStatus(isMain);

        String statusText = isMain ? "메인 강사로 설정" : "메인 강사 해제";
        log.info("[TeacherService] {} 완료. id={}, mainSortOrder={}", statusText, id, teacher.getMainSortOrder());
        
        return Response.ok("0000", statusText + "되었습니다.");
    }

    /**
     * 메인 강사 순서 일괄 변경.
     */
    @Override
    @Transactional
    public Response updateMainTeacherOrder(RequestMainTeacherOrder request) {
        log.info("[TeacherService] 메인 강사 순서 일괄 변경 시작. orderCount={}", request.getOrders().size());

        // 1. 요청된 강사 ID 추출
        List<Long> teacherIds = request.getOrders().stream()
                .map(RequestMainTeacherOrder.TeacherOrder::getTeacherId)
                .toList();

        // 2. 메인 강사인지 확인
        List<Teacher> mainTeachers = teacherRepository.findMainTeachersByIds(teacherIds);
        
        if (mainTeachers.size() != teacherIds.size()) {
            log.warn("[TeacherService] 일부 강사가 메인 강사가 아님. requestedCount={}, mainCount={}", 
                    teacherIds.size(), mainTeachers.size());
            return Response.error("NOT_MAIN_TEACHER", "메인 강사가 아닌 강사가 포함되어 있습니다.");
        }

        // 3. 순서 업데이트
        Map<Long, Teacher> teacherMap = mainTeachers.stream()
                .collect(Collectors.toMap(Teacher::getId, t -> t));

        for (RequestMainTeacherOrder.TeacherOrder order : request.getOrders()) {
            Teacher teacher = teacherMap.get(order.getTeacherId());
            if (teacher != null) {
                teacher.updateMainSortOrder(order.getMainSortOrder());
                log.debug("[TeacherService] 메인 강사 순서 업데이트. teacherId={}, newOrder={}", 
                        order.getTeacherId(), order.getMainSortOrder());
            }
        }

        log.info("[TeacherService] 메인 강사 순서 일괄 변경 완료. updatedCount={}", mainTeachers.size());
        
        return Response.ok("0000", "메인 강사 순서가 변경되었습니다.");
    }

    /**
     * 메인 강사 목록 조회.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseList<ResponseTeacherListItem> getMainTeacherList() {
        log.info("[TeacherService] 메인 강사 목록 조회 시작");

        List<Teacher> mainTeachers = teacherRepository.findMainTeachers();
        
        List<ResponseTeacherListItem> items = mainTeachers.stream()
                .map(teacherMapper::toListItemResponse)
                .toList();

        log.info("[TeacherService] 메인 강사 목록 조회 완료. count={}", items.size());
        
        return ResponseList.ok(items, (long) items.size(), 0, items.size());
    }
}