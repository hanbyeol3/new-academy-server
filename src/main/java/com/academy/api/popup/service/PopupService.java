package com.academy.api.popup.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 팝업 서비스 인터페이스.
 */
public interface PopupService {

    /**
     * 팝업 목록 조회 (관리자용 통합 검색).
     * 
     * @param keyword 검색 키워드 (제목)
     * @param type 팝업 타입 필터 (null이면 전체 타입)
     * @param isPublished 공개 여부 필터 (null이면 모든 상태)
     * @param sortType 정렬 방식 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 팝업 목록
     */
    ResponseList<ResponsePopupListItem> getPopupList(String keyword, Popup.PopupType type, Boolean isPublished, String sortType, Pageable pageable);

    /**
     * 팝업 상세 조회.
     * 
     * @param id 팝업 ID
     * @return 팝업 상세 정보
     */
    ResponseData<ResponsePopup> getPopup(Long id);

    /**
     * 팝업 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 팝업 ID
     */
    ResponseData<Long> createPopup(RequestPopupCreate request);

    /**
     * 팝업 수정.
     * 
     * @param id 수정할 팝업 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    Response updatePopup(Long id, RequestPopupUpdate request);

    /**
     * 팝업 삭제.
     * 
     * @param id 삭제할 팝업 ID
     * @return 삭제 결과
     */
    Response deletePopup(Long id);

    /**
     * 팝업 공개 상태 변경.
     * 
     * @param id 대상 팝업 ID
     * @param isPublished 공개 여부 (true: 공개, false: 비공개)
     * @return 변경 결과
     */
    Response updatePublishedStatus(Long id, Boolean isPublished);

    /**
     * 노출중인 팝업 목록 조회 (사용자용).
     * 
     * @return 노출중인 팝업 목록
     */
    ResponseList<ResponsePopupPublic> getActivePopups();
}