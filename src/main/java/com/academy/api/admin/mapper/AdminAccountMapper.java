package com.academy.api.admin.mapper;

import com.academy.api.admin.dto.request.RequestAdminAccountCreate;
import com.academy.api.admin.dto.response.ResponseAdminAccount;
import com.academy.api.admin.dto.response.ResponseAdminAccountDetail;
import com.academy.api.member.domain.Member;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 계정 Mapper.
 * 
 * Member 엔티티와 관리자 관련 DTO 간 변환을 담당합니다.
 */
@Component
public class AdminAccountMapper {

    /**
     * 생성 요청 DTO를 Member 엔티티로 변환.
     * 
     * @param request 생성 요청 DTO
     * @param passwordHash 해싱된 비밀번호
     * @param createdBy 생성자 ID
     * @return Member 엔티티
     */
    public Member toEntity(RequestAdminAccountCreate request, String passwordHash, Long createdBy) {
        return Member.builder()
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .memberName(request.getMemberName())
                .phoneNumber(request.getPhoneNumber())
                .emailAddress(request.getEmailAddress())
                .role(request.getRole())
                .build();
    }

    /**
     * Member 엔티티를 관리자 계정 응답 DTO로 변환.
     * 
     * @param member Member 엔티티
     * @return 관리자 계정 응답 DTO
     */
    public ResponseAdminAccount toResponse(Member member) {
        return ResponseAdminAccount.builder()
                .id(member.getId())
                .username(member.getUsername())
                .memberName(member.getMemberName())
                .phoneNumber(member.getPhoneNumber())
                .emailAddress(member.getEmailAddress())
                .isEmailVerified(member.getIsEmailVerified())
                .isPhoneVerified(member.getIsPhoneVerified())
                .role(member.getRole())
                .status(member.getStatus())
                .locked(member.getLocked())
                .memo(member.getMemo())
                .lastLoginAt(member.getLastLoginAt())
                .passwordChangedAt(member.getPasswordChangedAt())
                .suspendedAt(member.getSuspendedAt())
                .createdBy(member.getCreatedBy())
                .createdAt(member.getCreatedAt())
                .updatedBy(member.getUpdatedBy())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    /**
     * Member 엔티티를 관리자 계정 응답 DTO로 변환 (회원 이름 포함).
     * 
     * @param member Member 엔티티
     * @param createdByName 생성자 이름
     * @param updatedByName 수정자 이름
     * @return 관리자 계정 응답 DTO
     */
    public ResponseAdminAccount toResponse(Member member, String createdByName, String updatedByName) {
        ResponseAdminAccount response = toResponse(member);
        return ResponseAdminAccount.builder()
                .id(response.getId())
                .username(response.getUsername())
                .memberName(response.getMemberName())
                .phoneNumber(response.getPhoneNumber())
                .emailAddress(response.getEmailAddress())
                .isEmailVerified(response.getIsEmailVerified())
                .isPhoneVerified(response.getIsPhoneVerified())
                .role(response.getRole())
                .status(response.getStatus())
                .locked(response.getLocked())
                .memo(response.getMemo())
                .lastLoginAt(response.getLastLoginAt())
                .passwordChangedAt(response.getPasswordChangedAt())
                .suspendedAt(response.getSuspendedAt())
                .createdBy(response.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(response.getUpdatedAt())
                .build();
    }

    /**
     * Member 엔티티를 관리자 계정 상세 응답 DTO로 변환.
     * 
     * @param member Member 엔티티
     * @param createdByName 생성자 이름
     * @param updatedByName 수정자 이름
     * @return 관리자 계정 상세 응답 DTO
     */
    public ResponseAdminAccountDetail toDetailResponse(Member member, String createdByName, String updatedByName) {
        return ResponseAdminAccountDetail.builder()
                .id(member.getId())
                .username(member.getUsername())
                .memberName(member.getMemberName())
                .phoneNumber(member.getPhoneNumber())
                .emailAddress(member.getEmailAddress())
                .isEmailVerified(member.getIsEmailVerified())
                .isPhoneVerified(member.getIsPhoneVerified())
                .role(member.getRole())
                .status(member.getStatus())
                .locked(member.getLocked())
                .memo(member.getMemo())
                .lastLoginAt(member.getLastLoginAt())
                .passwordChangedAt(member.getPasswordChangedAt())
                .suspendedAt(member.getSuspendedAt())
                .createdBy(member.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(member.getCreatedAt())
                .updatedBy(member.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    /**
     * Member 엔티티 목록을 관리자 계정 응답 DTO 목록으로 변환.
     * 
     * @param members Member 엔티티 목록
     * @return 관리자 계정 응답 DTO 목록
     */
    public List<ResponseAdminAccount> toResponseList(List<Member> members) {
        return members.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Member 페이지를 관리자 계정 응답 목록으로 변환.
     * 
     * @param memberPage Member 페이지
     * @return 관리자 계정 응답 목록
     */
    public ResponseList<ResponseAdminAccount> toResponseList(Page<Member> memberPage) {
        List<ResponseAdminAccount> responses = toResponseList(memberPage.getContent());
        return ResponseList.ok(
                responses,
                memberPage.getTotalElements(),
                memberPage.getNumber(),
                memberPage.getSize()
        );
    }

    /**
     * Member 페이지를 관리자 계정 응답 목록으로 변환 (회원 이름 포함).
     * 
     * @param memberPage Member 페이지
     * @param memberNames 회원 이름 매핑 (회원 ID -> 이름)
     * @return 관리자 계정 응답 목록
     */
    public ResponseList<ResponseAdminAccount> toResponseListWithNames(Page<Member> memberPage, 
                                                                    java.util.Map<Long, String> memberNames) {
        List<ResponseAdminAccount> responses = memberPage.getContent().stream()
                .map(member -> {
                    String createdByName = memberNames.get(member.getCreatedBy());
                    String updatedByName = memberNames.get(member.getUpdatedBy());
                    return toResponse(member, createdByName, updatedByName);
                })
                .collect(Collectors.toList());

        return ResponseList.ok(
                responses,
                memberPage.getTotalElements(),
                memberPage.getNumber(),
                memberPage.getSize()
        );
    }

    /**
     * 통계 정보 생성.
     * 
     * @param totalLoginCount 총 로그인 횟수
     * @param successfulLoginCount 성공 로그인 횟수
     * @param failedLoginCount 실패 로그인 횟수
     * @param totalActionCount 총 액션 횟수
     * @param thisMonthActionCount 이번 달 액션 횟수
     * @return 통계 정보
     */
    public ResponseAdminAccountDetail.AdminAccountStatistics createStatistics(
            long totalLoginCount, long successfulLoginCount, long failedLoginCount,
            long totalActionCount, long thisMonthActionCount) {
        
        double successRate = totalLoginCount > 0 ? 
                (double) successfulLoginCount / totalLoginCount * 100 : 0.0;

        return ResponseAdminAccountDetail.AdminAccountStatistics.builder()
                .totalLoginCount(totalLoginCount)
                .successfulLoginCount(successfulLoginCount)
                .failedLoginCount(failedLoginCount)
                .loginSuccessRate(Math.round(successRate * 100.0) / 100.0) // 소수점 2자리
                .totalActionCount(totalActionCount)
                .thisMonthActionCount(thisMonthActionCount)
                .build();
    }
}