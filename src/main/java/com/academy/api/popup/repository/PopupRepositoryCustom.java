package com.academy.api.popup.repository;

import com.academy.api.popup.domain.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팝업 Repository Custom 인터페이스 (QueryDSL 동적 쿼리).
 */
public interface PopupRepositoryCustom {

    /**
     * 관리자용 팝업 검색 (QueryDSL 동적 쿼리).
     * 
     * @param keyword 제목 검색 키워드
     * @param type 팝업 타입 필터
     * @param isPublished 공개 여부 필터
     * @param sortType 정렬 방식
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Popup> searchPopupsForAdmin(String keyword, Popup.PopupType type, Boolean isPublished, String sortType, Pageable pageable);

    /**
     * 노출중인 팝업 조회 (QueryDSL 복잡한 조건).
     * 
     * @param now 현재 시각
     * @return 노출중인 팝업 목록 (정렬순서별)
     */
    List<Popup> findActivePopupsWithConditions(LocalDateTime now);

    /**
     * 특정 정렬순서 범위의 팝업 조회.
     * 
     * @param minOrder 최소 정렬순서
     * @param maxOrder 최대 정렬순서
     * @return 정렬순서 범위 내 팝업
     */
    List<Popup> findPopupsBySortOrderRange(Integer minOrder, Integer maxOrder);
}