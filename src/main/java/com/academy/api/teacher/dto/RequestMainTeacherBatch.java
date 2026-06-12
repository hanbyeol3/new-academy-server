package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 메인 강사 일괄 처리 요청 DTO.
 * 
 * 메인 강사 추가/제거/순서 변경/소개글 수정을 일괄 처리합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메인 강사 일괄 처리 요청")
public class RequestMainTeacherBatch {
    
    @Schema(description = "메인으로 추가할 강사 ID 목록", example = "[1, 3, 5]")
    private List<Long> addTeacherIds = new ArrayList<>();
    
    @Schema(description = "메인에서 제거할 강사 ID 목록", example = "[2, 4]")
    private List<Long> removeTeacherIds = new ArrayList<>();
    
    @Valid
    @Schema(description = "메인 강사 정보 업데이트 목록 (순서 및 소개글)")
    private List<MainTeacherUpdate> updates = new ArrayList<>();
    
    /**
     * 메인 강사 정보 업데이트.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "메인 강사 정보 업데이트")
    public static class MainTeacherUpdate {
        
        @NotNull(message = "강사 ID를 입력해주세요")
        @Schema(description = "강사 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long teacherId;
        
        @NotNull(message = "메인 노출 순서를 입력해주세요")
        @Min(value = 1, message = "메인 노출 순서는 1 이상이어야 합니다")
        @Schema(description = "메인 노출 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer mainSortOrder;
        
        @Size(max = 255, message = "소개글은 255자 이하여야 합니다")
        @Schema(description = "한 줄 소개 (null이면 기존 값 유지)", example = "수학의 즐거움을 알려드립니다")
        private String introText;
    }
    
    /**
     * 추가/제거 ID가 중복되지 않는지 검증.
     */
    @AssertTrue(message = "추가할 강사와 제거할 강사에 중복된 ID가 있습니다")
    public boolean isValidTeacherIds() {
        if (addTeacherIds == null || removeTeacherIds == null) {
            return true;
        }
        
        Set<Long> addSet = new HashSet<>(addTeacherIds);
        Set<Long> removeSet = new HashSet<>(removeTeacherIds);
        
        // 두 집합에 교집합이 없어야 함
        return Collections.disjoint(addSet, removeSet);
    }
    
    /**
     * updates에 중복된 teacherId가 없는지 검증.
     */
    @AssertTrue(message = "업데이트 목록에 중복된 강사 ID가 있습니다")
    public boolean isUniqueUpdateIds() {
        if (updates == null || updates.isEmpty()) {
            return true;
        }
        
        Set<Long> teacherIds = new HashSet<>();
        for (MainTeacherUpdate update : updates) {
            if (update.getTeacherId() != null) {
                if (!teacherIds.add(update.getTeacherId())) {
                    return false;  // 중복 발견
                }
            }
        }
        return true;
    }
    
    /**
     * mainSortOrder가 연속적인지 검증.
     */
    @AssertTrue(message = "메인 노출 순서는 1부터 연속적이어야 합니다")
    public boolean isSequentialSortOrder() {
        if (updates == null || updates.isEmpty()) {
            return true;
        }
        
        List<Integer> sortOrders = new ArrayList<>();
        for (MainTeacherUpdate update : updates) {
            if (update.getMainSortOrder() != null) {
                sortOrders.add(update.getMainSortOrder());
            }
        }
        
        if (sortOrders.isEmpty()) {
            return true;
        }
        
        sortOrders.sort(Integer::compareTo);
        
        // 1부터 시작하는지 확인
        if (sortOrders.get(0) != 1) {
            return false;
        }
        
        // 연속적인지 확인
        for (int i = 1; i < sortOrders.size(); i++) {
            if (sortOrders.get(i) != sortOrders.get(i - 1) + 1) {
                return false;
            }
        }
        
        return true;
    }
}