package com.academy.api.apply.controller;

import com.academy.api.apply.dto.RequestApplyApplicationCreate;
import com.academy.api.apply.service.ApplyApplicationService;
import com.academy.api.data.responses.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 원서접수 공개 컨트롤러.
 */
@Tag(name = "Apply Application (Public)", description = "원서접수 등록 API")
@Slf4j
@RestController
@RequestMapping("/api/apply-applications")
@RequiredArgsConstructor
public class ApplyApplicationPublicController {

    private final ApplyApplicationService applyApplicationService;

    @Operation(
        summary = "원서접수 등록",
        description = """
                새로운 원서접수를 등록합니다.
                
                필수 입력 사항:
                - 구분 (중등부/고등부/독학재수)
                - 학생정보 (이름, 성별, 학년, 휴대폰, 주소)
                - 보호자정보 (최소 1명 이상)
                
                선택 입력 사항:
                - 신청과목 (구분에 따라 제한)
                - 성적표 파일 (fileRole: "Attachment")
                - 증명사진 파일 (fileRole: "Cover")
                - 희망대학/학과 (독학재수만)
                
                과목 선택 규칙:
                - 중등부: 국어, 영어, 수학, 사회, 과학
                - 고등부: 국어, 영어, 수학
                - 독학재수: 과목 선택 없음 (null)
                
                파일 업로드:
                - 증명사진: fileRole = "Cover"
                - 성적표: fileRole = "Attachment"
                - 임시 파일 → 정식 파일로 자동 승격
                
                접수 완료 후:
                - 상태가 REGISTERED로 자동 설정
                - guardian1_phone으로 접수 확인 문자 발송 (TODO)
                
                주의사항:
                - 중복 원서접수 검증 수행
                - 모든 필수 정보 입력 필수
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createApplyApplication(
            @Parameter(description = "원서접수 등록 요청")
            @RequestBody @Valid RequestApplyApplicationCreate request) {
        
        log.info("공개 원서접수 등록 요청. 학생명={}, 구분={}, 과목수={}", 
                request.getStudentName(), request.getDivision(), 
                request.getSubjects() != null ? request.getSubjects().size() : 0);
        
        ResponseData<Long> result = applyApplicationService.createApplyApplication(request);
        
        // TODO: 원서접수 완료 후 guardian1_phone으로 문자 발송
        log.info("원서접수 등록 완료. ID={}, 학생명={} (문자발송 TODO)", 
                result.getData(), request.getStudentName());
        
        return result;
    }
}