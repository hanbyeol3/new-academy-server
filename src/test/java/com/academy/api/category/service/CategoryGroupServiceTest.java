package com.academy.api.category.service;

import com.academy.api.category.domain.CategoryGroup;
import com.academy.api.category.dto.RequestCategoryGroupCreate;
import com.academy.api.category.dto.RequestCategoryGroupUpdate;
import com.academy.api.category.dto.ResponseCategoryGroup;
import com.academy.api.category.mapper.CategoryGroupMapper;
import com.academy.api.category.repository.CategoryGroupRepository;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryGroupServiceTest {

    @Mock
    private CategoryGroupRepository categoryGroupRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private CategoryGroupMapper categoryGroupMapper;

    @InjectMocks
    private CategoryGroupServiceImpl categoryGroupService;

    private CategoryGroup categoryGroup;
    private ResponseCategoryGroup responseCategoryGroup;

    @BeforeEach
    void setUp() {
        categoryGroup = CategoryGroup.builder()
                .name("교육과정")
                .description("교육과정 관련 카테고리 그룹")
                .createdBy(1L)
                .build();
        
        responseCategoryGroup = ResponseCategoryGroup.builder()
                .id(1L)
                .name("교육과정")
                .description("교육과정 관련 카테고리 그룹")
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("카테고리 그룹 목록 조회 - 성공")
    void getCategoryGroupList_Success() {
        // given
        List<CategoryGroup> categoryGroups = Arrays.asList(categoryGroup);
        List<ResponseCategoryGroup> responseList = Arrays.asList(responseCategoryGroup);

        given(categoryGroupRepository.findAllOrderByCreatedAtDesc()).willReturn(categoryGroups);
        given(categoryGroupMapper.toResponseList(categoryGroups)).willReturn(responseList);

        // when
        ResponseData<List<ResponseCategoryGroup>> result = categoryGroupService.getCategoryGroupList();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getName()).isEqualTo("교육과정");
        
        verify(categoryGroupRepository).findAllOrderByCreatedAtDesc();
        verify(categoryGroupMapper).toResponseList(categoryGroups);
    }

    @Test
    @DisplayName("카테고리 그룹 상세 조회 - 성공")
    void getCategoryGroup_Success() {
        // given
        Long groupId = 1L;
        given(categoryGroupRepository.findById(groupId)).willReturn(Optional.of(categoryGroup));
        given(categoryGroupMapper.toResponse(categoryGroup)).willReturn(responseCategoryGroup);

        // when
        ResponseData<ResponseCategoryGroup> result = categoryGroupService.getCategoryGroup(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData().getName()).isEqualTo("교육과정");
        
        verify(categoryGroupRepository).findById(groupId);
        verify(categoryGroupMapper).toResponse(categoryGroup);
    }

    @Test
    @DisplayName("카테고리 그룹 상세 조회 - 존재하지 않는 그룹")
    void getCategoryGroup_NotFound() {
        // given
        Long groupId = 999L;
        given(categoryGroupRepository.findById(groupId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryGroupService.getCategoryGroup(groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_GROUP_NOT_FOUND);
        
        verify(categoryGroupRepository).findById(groupId);
        verify(categoryGroupMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("카테고리 그룹 생성 - 성공")
    void createCategoryGroup_Success() {
        // given
        RequestCategoryGroupCreate request = new RequestCategoryGroupCreate();
        request.setName("새로운 그룹");
        request.setDescription("새로운 그룹 설명");
        request.setCreatedBy(1L);

        // Use reflection to set ID for saved entity simulation
        CategoryGroup savedCategoryGroup = CategoryGroup.builder()
                .name("새로운 그룹")
                .description("새로운 그룹 설명")
                .createdBy(1L)
                .build();
        // Set ID using reflection for test
        try {
            var idField = CategoryGroup.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedCategoryGroup, 1L);
        } catch (Exception e) {
            // Ignore reflection error in test
        }

        given(categoryGroupRepository.existsByName(request.getName())).willReturn(false);
        given(categoryGroupMapper.toEntity(request)).willReturn(categoryGroup);
        given(categoryGroupRepository.save(categoryGroup)).willReturn(savedCategoryGroup);

        // when
        ResponseData<Long> result = categoryGroupService.createCategoryGroup(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getMessage()).isEqualTo("카테고리 그룹이 생성되었습니다.");
        
        verify(categoryGroupRepository).existsByName(request.getName());
        verify(categoryGroupMapper).toEntity(request);
        verify(categoryGroupRepository).save(categoryGroup);
    }

    @Test
    @DisplayName("카테고리 그룹 생성 - 중복된 그룹명")
    void createCategoryGroup_NameAlreadyExists() {
        // given
        RequestCategoryGroupCreate request = new RequestCategoryGroupCreate();
        request.setName("기존 그룹");
        request.setDescription("설명");

        given(categoryGroupRepository.existsByName(request.getName())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryGroupService.createCategoryGroup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_GROUP_ALREADY_EXISTS);
        
        verify(categoryGroupRepository).existsByName(request.getName());
        verify(categoryGroupMapper, never()).toEntity(any());
        verify(categoryGroupRepository, never()).save(any());
    }

    @Test
    @DisplayName("카테고리 그룹 수정 - 성공")
    void updateCategoryGroup_Success() {
        // given
        Long groupId = 1L;
        RequestCategoryGroupUpdate request = new RequestCategoryGroupUpdate();
        request.setName("수정된 그룹명");
        request.setDescription("수정된 설명");
        request.setUpdatedBy(1L);

        given(categoryGroupRepository.findById(groupId)).willReturn(Optional.of(categoryGroup));
        given(categoryGroupRepository.existsByNameAndIdNot(request.getName(), groupId)).willReturn(false);

        // when
        Response result = categoryGroupService.updateCategoryGroup(groupId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("카테고리 그룹이 수정되었습니다.");
        
        verify(categoryGroupRepository).findById(groupId);
        verify(categoryGroupRepository).existsByNameAndIdNot(request.getName(), groupId);
        verify(categoryGroupMapper).updateEntity(categoryGroup, request);
    }

    @Test
    @DisplayName("카테고리 그룹 수정 - 중복된 그룹명")
    void updateCategoryGroup_NameAlreadyExists() {
        // given
        Long groupId = 1L;
        RequestCategoryGroupUpdate request = new RequestCategoryGroupUpdate();
        request.setName("중복된 그룹명");

        given(categoryGroupRepository.findById(groupId)).willReturn(Optional.of(categoryGroup));
        given(categoryGroupRepository.existsByNameAndIdNot(request.getName(), groupId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryGroupService.updateCategoryGroup(groupId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_GROUP_ALREADY_EXISTS);
        
        verify(categoryGroupRepository).findById(groupId);
        verify(categoryGroupRepository).existsByNameAndIdNot(request.getName(), groupId);
        verify(categoryGroupMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("카테고리 그룹 삭제 - 성공")
    void deleteCategoryGroup_Success() {
        // given
        Long groupId = 1L;
        
        given(categoryGroupRepository.existsById(groupId)).willReturn(true);
        given(categoryRepository.countByCategoryGroupId(groupId)).willReturn(0L);

        // when
        Response result = categoryGroupService.deleteCategoryGroup(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("카테고리 그룹이 삭제되었습니다.");
        
        verify(categoryGroupRepository).existsById(groupId);
        verify(categoryRepository).countByCategoryGroupId(groupId);
        verify(categoryGroupRepository).deleteById(groupId);
    }

    @Test
    @DisplayName("카테고리 그룹 삭제 - 하위 카테고리 존재")
    void deleteCategoryGroup_HasCategories() {
        // given
        Long groupId = 1L;
        
        given(categoryGroupRepository.existsById(groupId)).willReturn(true);
        given(categoryRepository.countByCategoryGroupId(groupId)).willReturn(3L);

        // when & then
        assertThatThrownBy(() -> categoryGroupService.deleteCategoryGroup(groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_GROUP_HAS_CATEGORIES);
        
        verify(categoryGroupRepository).existsById(groupId);
        verify(categoryRepository).countByCategoryGroupId(groupId);
        verify(categoryGroupRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("카테고리 그룹 삭제 - 존재하지 않는 그룹")
    void deleteCategoryGroup_NotFound() {
        // given
        Long groupId = 999L;
        
        given(categoryGroupRepository.existsById(groupId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryGroupService.deleteCategoryGroup(groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_GROUP_NOT_FOUND);
        
        verify(categoryGroupRepository).existsById(groupId);
        verify(categoryRepository, never()).countByCategoryGroupId(any());
        verify(categoryGroupRepository, never()).deleteById(any());
    }
}