package com.academy.api.category.controller;

import com.academy.api.category.dto.*;
import com.academy.api.category.service.CategoryGroupService;
import com.academy.api.category.service.CategoryService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryAdminController.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryGroupService categoryGroupService;

    @MockBean
    private CategoryService categoryService;

    private ResponseCategoryGroup responseCategoryGroup;
    private ResponseCategory responseCategory;

    @BeforeEach
    void setUp() {
        responseCategoryGroup = ResponseCategoryGroup.builder()
                .id(1L)
                .name("교육과정")
                .description("교육과정 관련 카테고리 그룹")
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        responseCategory = ResponseCategory.builder()
                .id(1L)
                .categoryGroupId(1L)
                .categoryGroupName("교육과정")
                .name("프론트엔드")
                .slug("frontend")
                .description("프론트엔드 개발 카테고리")
                .sortOrder(1)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("카테고리 그룹 목록 조회 - 성공")
    void getCategoryGroupList_Success() throws Exception {
        // given
        List<ResponseCategoryGroup> categoryGroups = Arrays.asList(responseCategoryGroup);
        ResponseData<List<ResponseCategoryGroup>> responseData = ResponseData.ok(categoryGroups);

        given(categoryGroupService.getCategoryGroupList()).willReturn(responseData);

        // when & then
        mockMvc.perform(get("/api/admin/categories/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("교육과정"));
    }

    @Test
    @DisplayName("카테고리 그룹 상세 조회 - 성공")
    void getCategoryGroup_Success() throws Exception {
        // given
        Long groupId = 1L;
        ResponseData<ResponseCategoryGroup> responseData = ResponseData.ok(responseCategoryGroup);

        given(categoryGroupService.getCategoryGroup(groupId)).willReturn(responseData);

        // when & then
        mockMvc.perform(get("/api/admin/categories/groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.name").value("교육과정"));
    }

    @Test
    @DisplayName("카테고리 그룹 생성 - 성공")
    void createCategoryGroup_Success() throws Exception {
        // given
        RequestCategoryGroupCreate request = new RequestCategoryGroupCreate();
        request.setName("새로운 그룹");
        request.setDescription("새로운 그룹 설명");
        request.setCreatedBy(1L);

        ResponseData<Long> responseData = ResponseData.ok("0000", "카테고리 그룹이 생성되었습니다.", 1L);

        given(categoryGroupService.createCategoryGroup(any(RequestCategoryGroupCreate.class)))
                .willReturn(responseData);

        // when & then
        mockMvc.perform(post("/api/admin/categories/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.message").value("카테고리 그룹이 생성되었습니다."))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("카테고리 그룹 생성 - 유효성 검증 실패")
    void createCategoryGroup_ValidationError() throws Exception {
        // given
        RequestCategoryGroupCreate request = new RequestCategoryGroupCreate();
        // name을 비워두어 @NotBlank 검증 실패

        // when & then
        mockMvc.perform(post("/api/admin/categories/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 그룹 수정 - 성공")
    void updateCategoryGroup_Success() throws Exception {
        // given
        Long groupId = 1L;
        RequestCategoryGroupUpdate request = new RequestCategoryGroupUpdate();
        request.setName("수정된 그룹명");
        request.setDescription("수정된 설명");

        Response response = Response.ok("0000", "카테고리 그룹이 수정되었습니다.");

        given(categoryGroupService.updateCategoryGroup(eq(groupId), any(RequestCategoryGroupUpdate.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/categories/groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.message").value("카테고리 그룹이 수정되었습니다."));
    }

    @Test
    @DisplayName("카테고리 그룹 삭제 - 성공")
    void deleteCategoryGroup_Success() throws Exception {
        // given
        Long groupId = 1L;
        Response response = Response.ok("0000", "카테고리 그룹이 삭제되었습니다.");

        given(categoryGroupService.deleteCategoryGroup(groupId)).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/admin/categories/groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.message").value("카테고리 그룹이 삭제되었습니다."));
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 성공")
    void getCategoryList_Success() throws Exception {
        // given
        List<ResponseCategory> categories = Arrays.asList(responseCategory);
        ResponseData<List<ResponseCategory>> responseData = ResponseData.ok(categories);

        given(categoryService.getCategoryList()).willReturn(responseData);

        // when & then
        mockMvc.perform(get("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("프론트엔드"));
    }

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_Success() throws Exception {
        // given
        RequestCategoryCreate request = new RequestCategoryCreate();
        request.setCategoryGroupId(1L);
        request.setName("백엔드");
        request.setSlug("backend");
        request.setDescription("백엔드 개발 카테고리");
        request.setSortOrder(2);
        request.setCreatedBy(1L);

        ResponseData<Long> responseData = ResponseData.ok("0000", "카테고리가 생성되었습니다.", 2L);

        given(categoryService.createCategory(any(RequestCategoryCreate.class)))
                .willReturn(responseData);

        // when & then
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.message").value("카테고리가 생성되었습니다."))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("카테고리 생성 - 유효성 검증 실패")
    void createCategory_ValidationError() throws Exception {
        // given
        RequestCategoryCreate request = new RequestCategoryCreate();
        // 필수 필드들을 비워두어 유효성 검증 실패

        // when & then
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}