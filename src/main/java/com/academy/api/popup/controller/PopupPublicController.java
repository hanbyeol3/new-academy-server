package com.academy.api.popup.controller;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.popup.dto.ResponsePopupPublic;
import com.academy.api.popup.service.PopupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 팝업 공개 API 컨트롤러.
 *
 * 사용자에게 노출되는 팝업 조회 기능을 제공합니다.
 * 인증이 필요하지 않으며 누구나 접근 가능합니다.
 */
@Tag(name = "Popup (Public)", description = "사용자용 팝업 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
public class PopupPublicController {

    private final PopupService popupService;

    /**
     * 노출중인 팝업 목록 조회.
     *
     * @return 현재 노출 중인 팝업 목록
     */
    @Operation(
        summary = "노출중인 팝업 목록 조회",
        description = """
                현재 노출 중인 팝업 목록을 조회합니다.
                
                노출 조건:
                - 공개 상태 (isPublished = true)
                - 노출 기간 내:
                  * ALWAYS: 항상 노출
                  * PERIOD: 현재 시간이 exposureStartAt ~ exposureEndAt 범위 내
                
                정렬:
                - 정렬순서 (sortOrder) 오름차순
                - 동일 순서 시 생성일 내림차순
                
                반환 정보:
                - 팝업 기본 정보 (ID, 제목, 타입)
                - 위치 및 크기 정보
                - 노출 기간 정보
                - 링크 URL (PC/모바일)
                - 다시 보지 않기 설정
                
                민감정보 제외:
                - 등록자/수정자 정보 제외
                - 관리자 전용 필드 제외
                
                캐싱:
                - 결과가 자주 변경되지 않으므로 캐싱 고려 가능
                - 노출 기간 변경 시점에 캐시 무효화 필요
                
                프론트엔드 활용:
                - 페이지 로드 시 자동 호출
                - 정렬순서에 따른 레이어 순서 적용
                - 다시 보지 않기 쿠키 처리
                - PC/모바일 분기 처리
                
                성능 최적화:
                - 노출 조건이 복잡하므로 인덱스 활용
                - 결과 수가 적으므로 페이징 불필요
                
                사용 예시:
                - GET /api/popups/active
                
                응답 예시:
                {
                  "success": true,
                  "data": {
                    "items": [
                      {
                        "id": 1,
                        "title": "신년 이벤트",
                        "type": "IMAGE",
                        "widthPx": 400,
                        "heightPx": 300,
                        "positionTopPx": 100,
                        "positionLeftPx": 100,
                        "pcLinkUrl": "https://example.com/pc",
                        "mobileLinkUrl": "https://example.com/mobile",
                        "dismissForDays": 7,
                        "sortOrder": 1000
                      }
                    ],
                    "total": 1
                  }
                }
                """
    )
    @GetMapping("/active")
    public ResponseList<ResponsePopupPublic> getActivePopups() {
        log.info("[PopupPublicController] 노출중인 팝업 목록 조회 요청");
        
        return popupService.getActivePopups();
    }
}