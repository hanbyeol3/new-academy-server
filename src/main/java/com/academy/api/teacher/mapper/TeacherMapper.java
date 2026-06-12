package com.academy.api.teacher.mapper;

import com.academy.api.category.domain.Category;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.teacher.domain.Teacher;
import com.academy.api.teacher.domain.TeacherCareer;
import com.academy.api.teacher.domain.TeacherSubject;
import com.academy.api.teacher.dto.CareerItem;
import com.academy.api.teacher.dto.RequestTeacherCreate;
import com.academy.api.teacher.dto.RequestTeacherUpdate;
import com.academy.api.teacher.dto.ResponseTeacher;
import com.academy.api.teacher.dto.ResponseTeacherByCategory;
import com.academy.api.teacher.dto.ResponseTeacherListItem;
import com.academy.api.teacher.dto.TeacherSimple;
import com.academy.api.file.domain.FileRole;
import com.academy.api.file.domain.UploadFileLink;
import com.academy.api.file.repository.UploadFileLinkRepository;
import com.academy.api.file.repository.UploadFileRepository;
import com.academy.api.file.dto.UploadFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 강사 엔티티 ↔ DTO 매핑 유틸리티.
 * 
 * - Entity와 DTO 간 변환 처리
 * - 복합 객체 매핑 (강사 + 과목 정보)
 * - 페이징 처리된 데이터 변환
 */
@Component
@RequiredArgsConstructor
public class TeacherMapper {

    private final UploadFileLinkRepository uploadFileLinkRepository;
    private final UploadFileRepository uploadFileRepository;

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     * 
     * @param request 생성 요청 DTO
     * @return Teacher 엔티티 (과목 정보 제외)
     */
    public Teacher toEntity(RequestTeacherCreate request) {
        return Teacher.builder()
                .teacherName(request.getTeacherName())
                .roleName(request.getRoleName())
                .introText(request.getIntroText())
                .memo(request.getMemo())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .isComingSoon(request.getIsComingSoon() != null ? request.getIsComingSoon() : false)
                .isMain(false)  // 생성 시 기본값
                .mainSortOrder(null)  // 생성 시 null (메인이 아니므로)
                .createdBy(SecurityUtils.getCurrentUserId())
                .build();
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     * 
     * @param teacher Teacher 엔티티
     * @return 상세 응답 DTO (과목 정보 포함)
     */
    public ResponseTeacher toResponse(Teacher teacher) {
        // 단일 과목 정보 매핑
        ResponseTeacher.CategoryInfo categoryInfo = null;
        Integer sortOrder = null;
        
        if (!teacher.getSubjects().isEmpty()) {
            TeacherSubject teacherSubject = teacher.getSubjects().get(0);  // 단일 과목만 존재
            Category category = teacherSubject.getCategory();
            
            categoryInfo = ResponseTeacher.CategoryInfo.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .categorySlug(category.getSlug())
                    .categoryDescription(category.getDescription())
                    .build();
            
            sortOrder = teacherSubject.getSortOrder();
        }

        List<CareerItem> careers = teacher.getCareers().stream()
                .map(career -> CareerItem.builder()
                        .id(career.getId())
                        .text(career.getCareerText())
                        .highlight(career.getIsHighlight())
                        .sortOrder(career.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return ResponseTeacher.builder()
                .id(teacher.getId())
                .teacherName(teacher.getTeacherName())
                .role(teacher.getRoleName())
                .comingSoon(teacher.getIsComingSoon())
                .careers(careers)
                .image(getImageFromPath(teacher.getImagePath()))
                .introText(teacher.getIntroText())
                .memo(teacher.getMemo())
                .isPublished(teacher.getIsPublished())
                .isMain(teacher.getIsMain())
                .mainSortOrder(teacher.getMainSortOrder())
                .category(categoryInfo)
                .sortOrder(sortOrder)
                .createdBy(teacher.getCreatedBy())
                .createdAt(teacher.getCreatedAt())
                .updatedBy(teacher.getUpdatedBy())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티를 목록용 응답 DTO로 변환.
     * 
     * @param teacher Teacher 엔티티
     * @return 목록용 응답 DTO (과목 정보 포함)
     */
    public ResponseTeacherListItem toListItemResponse(Teacher teacher) {
        // 단일 과목 정보 매핑
        ResponseTeacherListItem.CategoryInfo categoryInfo = null;
        Integer sortOrder = null;
        
        if (!teacher.getSubjects().isEmpty()) {
            TeacherSubject teacherSubject = teacher.getSubjects().get(0);  // 단일 과목만 존재
            Category category = teacherSubject.getCategory();
            
            categoryInfo = ResponseTeacherListItem.CategoryInfo.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .categorySlug(category.getSlug())
                    .categoryGroupId(category.getCategoryGroup().getId())
                    .categoryGroupName(category.getCategoryGroup().getName())
                    .build();
            
            sortOrder = teacherSubject.getSortOrder();
        }

        List<CareerItem> careers = teacher.getCareers().stream()
                .map(career -> CareerItem.builder()
                        .id(career.getId())
                        .text(career.getCareerText())
                        .highlight(career.getIsHighlight())
                        .sortOrder(career.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return ResponseTeacherListItem.builder()
                .id(teacher.getId())
                .teacherName(teacher.getTeacherName())
                .role(teacher.getRoleName())
                .comingSoon(teacher.getIsComingSoon())
                .careers(careers)
                .image(getImageFromPath(teacher.getImagePath()))
                .introText(teacher.getIntroText())
                .isPublished(teacher.getIsPublished())
                .isMain(teacher.getIsMain())
                .mainSortOrder(teacher.getMainSortOrder())
                .category(categoryInfo)
                .sortOrder(sortOrder)
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 리스트를 상세 응답 DTO 리스트로 변환.
     * 
     * @param teachers Teacher 엔티티 리스트
     * @return 상세 응답 DTO 리스트
     */
    public List<ResponseTeacher> toResponseList(List<Teacher> teachers) {
        return teachers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 리스트를 목록용 응답 DTO 리스트로 변환.
     * 
     * @param teachers Teacher 엔티티 리스트
     * @return 목록용 응답 DTO 리스트
     */
    public List<ResponseTeacherListItem> toListItemResponseList(List<Teacher> teachers) {
        return teachers.stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 페이지를 상세 응답 리스트로 변환.
     * 
     * @param page Teacher 페이지
     * @return 상세 응답 리스트
     */
    public ResponseList<ResponseTeacher> toResponseList(Page<Teacher> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티 페이지를 목록용 응답 리스트로 변환.
     * 
     * @param page Teacher 페이지
     * @return 목록용 응답 리스트
     */
    public ResponseList<ResponseTeacherListItem> toListItemResponseList(Page<Teacher> page) {
        return ResponseList.from(page.map(this::toListItemResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     * 
     * @param teacher 수정할 엔티티
     * @param request 수정 요청 데이터
     */
    public void updateEntity(Teacher teacher, RequestTeacherUpdate request) {
        teacher.update(
                getValueOrDefault(request.getTeacherName(), teacher.getTeacherName()),
                getValueOrDefault(request.getRoleName(), teacher.getRoleName()),
                teacher.getImagePath(), // 이미지는 별도 처리 (파일 서비스에서)
                getValueOrDefault(request.getIntroText(), teacher.getIntroText()),
                getValueOrDefault(request.getMemo(), teacher.getMemo()),
                getValueOrDefault(request.getIsPublished(), teacher.getIsPublished()),
                getValueOrDefault(request.getIsComingSoon(), teacher.getIsComingSoon()),
                teacher.getIsMain(),  // 메인 관련 필드는 유지
                teacher.getMainSortOrder(),  // 메인 관련 필드는 유지
                SecurityUtils.getCurrentUserId() // 수정자 ID
        );
    }

    /**
     * TeacherSubject 엔티티 생성.
     * 
     * @param teacher 강사 엔티티
     * @param category 과목 카테고리 엔티티
     * @return TeacherSubject 엔티티
     */
    public TeacherSubject createTeacherSubject(Teacher teacher, Category category) {
        return TeacherSubject.builder()
                .teacher(teacher)
                .category(category)
                .build();
    }

    /**
     * 부분 업데이트를 위한 null 체크 도우미 메서드.
     * 
     * @param newValue 새로운 값
     * @param defaultValue 기본값 (기존값)
     * @return null이 아닌 경우 새로운 값, null인 경우 기본값
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }

    /**
     * 강사를 간소화된 DTO로 변환.
     * 
     * @param teacher Teacher 엔티티
     * @return 간소화된 강사 정보 DTO
     */
    public TeacherSimple toTeacherSimple(Teacher teacher) {
        List<CareerItem> careers = teacher.getCareers().stream()
                .map(career -> CareerItem.builder()
                        .id(career.getId())
                        .text(career.getCareerText())
                        .highlight(career.getIsHighlight())
                        .sortOrder(career.getSortOrder())
                        .build())
                .collect(Collectors.toList());
        
        return TeacherSimple.builder()
                .id(teacher.getId())
                .name(teacher.getTeacherName())
                .roleName(teacher.getRoleName())
                .comingSoon(teacher.getIsComingSoon())
                .image(getImageFromPath(teacher.getImagePath()))
                .careers(careers)
                .build();
    }
    
    /**
     * 카테고리와 강사 목록을 ResponseTeacherByCategory로 변환.
     * 
     * @param category 카테고리 엔티티
     * @param teachers 해당 카테고리의 강사 목록
     * @return 카테고리별 강사 응답 DTO
     */
    public ResponseTeacherByCategory toCategoryResponse(Category category, List<Teacher> teachers) {
        List<TeacherSimple> teacherSimpleList = teachers.stream()
                .map(this::toTeacherSimple)
                .collect(Collectors.toList());
        
        return ResponseTeacherByCategory.builder()
                .categoryId(category.getId())
                .slug(category.getSlug())
                .name(category.getName())
                .description(category.getDescription())
                .teachers(teacherSimpleList)
                .build();
    }
    
    /**
     * 강사의 이미지 파일 조회 (imagePath 기반).
     * 
     * @param imagePath 이미지 경로
     * @return 이미지 파일 정보, 없으면 null
     */
    private UploadFileDto getImageFromPath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }
        
        // imagePath가 "formal/123" 형식인 경우 파일 ID 추출
        if (imagePath.startsWith("formal/")) {
            try {
                Long fileId = Long.parseLong(imagePath.substring("formal/".length()));
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
            } catch (NumberFormatException e) {
                // 파일 ID 파싱 실패 시 null 반환
                return null;
            }
        }
        
        // 다른 형식의 경로는 현재 지원하지 않음
        return null;
    }
}