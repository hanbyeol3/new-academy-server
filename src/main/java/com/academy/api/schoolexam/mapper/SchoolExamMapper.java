package com.academy.api.schoolexam.mapper;

import com.academy.api.data.responses.ResponseList;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.dto.ResponseSchoolExamAdminList;
import com.academy.api.schoolexam.dto.ResponseSchoolExamDetail;
import com.academy.api.schoolexam.dto.ResponseSchoolExamPublicList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 학교별 시험분석 Mapper.
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class SchoolExamMapper {

    private final MemberRepository memberRepository;

    /**
     * Entity를 상세 응답 DTO로 변환.
     */
    public ResponseSchoolExamDetail toDetailResponse(SchoolExam entity) {
        if (entity == null) {
            return null;
        }

        return ResponseSchoolExamDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .schoolLevel(entity.getSchoolLevel())
                .isPublished(entity.getIsPublished())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .viewCount(entity.getViewCount())
                .createdBy(entity.getCreatedBy())
                .createdByName(getMemberName(entity.getCreatedBy()))
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(getMemberName(entity.getUpdatedBy()))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity를 관리자 목록 응답 DTO로 변환.
     */
    public ResponseSchoolExamAdminList toAdminListResponse(SchoolExam entity) {
        if (entity == null) {
            return null;
        }

        return ResponseSchoolExamAdminList.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .schoolLevel(entity.getSchoolLevel())
                .isPublished(entity.getIsPublished())
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .viewCount(entity.getViewCount())
                .createdBy(entity.getCreatedBy())
                .createdByName(getMemberName(entity.getCreatedBy()))
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(getMemberName(entity.getUpdatedBy()))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity를 공개 목록 응답 DTO로 변환.
     */
    public ResponseSchoolExamPublicList toPublicListResponse(SchoolExam entity) {
        if (entity == null) {
            return null;
        }

        return ResponseSchoolExamPublicList.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .schoolLevel(entity.getSchoolLevel())
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .viewCount(entity.getViewCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Page를 관리자 목록 ResponseList로 변환.
     */
    public ResponseList<ResponseSchoolExamAdminList> toAdminResponseList(Page<SchoolExam> page) {
        return ResponseList.from(page.map(this::toAdminListResponse));
    }

    /**
     * Page를 공개 목록 ResponseList로 변환.
     */
    public ResponseList<ResponseSchoolExamPublicList> toPublicResponseList(Page<SchoolExam> page) {
        return ResponseList.from(page.map(this::toPublicListResponse));
    }

    /**
     * 회원 이름 조회.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }

        Optional<Member> member = memberRepository.findById(memberId);
        return member.map(Member::getMemberName).orElse("알수없음");
    }
}