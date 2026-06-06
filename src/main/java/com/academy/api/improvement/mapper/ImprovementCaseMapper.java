package com.academy.api.improvement.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.improvement.domain.ImprovementCase;
import com.academy.api.improvement.dto.*;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 성적 향상 사례 Mapper.
 * 
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ImprovementCaseMapper {
    
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 생성 요청 DTO를 엔티티로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param createdBy 생성자 ID (관리자 작성시)
     * @return 엔티티
     */
    public ImprovementCase toEntity(RequestImprovementCaseCreate request, Long createdBy) {
        String passwordHash = null;
        if (request.getIsSecret() && request.getPassword() != null) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }
        
        return ImprovementCase.builder()
                .title(request.getTitle())
                .writerType(request.getWriterType())
                .authorName(request.getAuthorName())
                .phoneNumber(request.getPhoneNumber())
                .division(request.getDivision())
                .subject(request.getSubject())  // 수정: getSubject() 사용
                .gradeType(request.getGradeType())
                .prevResult(request.getPrevResult()) // 수정: getPrevResult() 사용
                .nextResult(request.getNextResult()) // 수정: getNextResult() 사용
                .content(request.getContent())
                .isPublished(request.getIsPublished())
                .isPinned(request.getIsPinned())
                .isSecret(request.getIsSecret())
                .passwordHash(passwordHash)
                .createdBy(createdBy)
                .build();
    }
    
    /**
     * 엔티티를 상세 응답 DTO로 변환.
     * 
     * @param entity 엔티티
     * @return 상세 응답 DTO
     */
    public ResponseImprovementCaseDetail toDetailResponse(ImprovementCase entity) {
        String createdByName = getMemberName(entity.getCreatedBy());
        String updatedByName = getMemberName(entity.getUpdatedBy());
        
        return ResponseImprovementCaseDetail.fromWithNames(entity, createdByName, updatedByName);
    }
    
    /**
     * 엔티티 목록을 공개용 목록 응답으로 변환.
     * 
     * @param page 페이지 결과
     * @return 목록 응답
     */
    public ResponseList<ResponseImprovementCasePublicList> toPublicListResponse(Page<ImprovementCase> page) {
        List<ResponseImprovementCasePublicList> items = page.getContent().stream()
                .map(ResponseImprovementCasePublicList::from)
                .toList();
        
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }
    
    /**
     * 엔티티 목록을 관리자용 목록 응답으로 변환.
     * 
     * @param page 페이지 결과
     * @return 목록 응답
     */
    public ResponseList<ResponseImprovementCaseAdminList> toAdminListResponse(Page<ImprovementCase> page) {
        List<ResponseImprovementCaseAdminList> items = page.getContent().stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseImprovementCaseAdminList.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.ok(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }
    
    /**
     * 회원 ID로 회원명 조회.
     * 
     * @param memberId 회원 ID
     * @return 회원명
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("알 수 없음");
    }
}