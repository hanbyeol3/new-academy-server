package com.academy.api.notice.service;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.model.ResponseNotice;
import org.springframework.data.domain.Pageable;

/**
 * 공지사항 비즈니스 로직을 처리하는 서비스 인터페이스.
 * 
 * - 공지사항의 CRUD 작업을 담당
 * - 컨트롤러와 리포지토리 계층 사이의 비즈니스 로직 추상화
 * - 통일된 응답 포맷(Response, ResponseData, ResponseList) 사용
 * 
 * 모든 메서드는 다음 원칙을 따름:
 *  1) 목록 조회 → ResponseList<T>
 *  2) 단건 조회 → ResponseData<T> 
 *  3) 생성 → ResponseData<Long> (생성된 ID 반환)
 *  4) 수정/삭제 → Response (단순 성공/실패)
 */
public interface NoticeService {

	/** 
	 * 공지사항 목록 조회 (페이지네이션 포함).
	 * - 검색 조건에 따른 필터링 및 정렬 지원
	 * - 결과는 ResponseList 형태로 페이지 정보와 함께 반환
	 * 
	 * @param cond 검색 조건 (제목, 내용, 발행상태 등)
	 * @param pageable 페이지네이션 정보 (페이지 번호, 크기, 정렬)
	 * @return 검색된 공지사항 목록과 페이지 정보를 포함한 ResponseList
	 */
	ResponseList<ResponseNotice> list(ResponseNotice.Criteria cond, Pageable pageable);

	/** 
	 * 공지사항 단건 조회.
	 * - ID로 특정 공지사항을 조회하며 조회수 자동 증가
	 * - 존재하지 않는 경우 에러 응답 반환
	 * 
	 * @param id 조회할 공지사항 ID
	 * @return 조회된 공지사항 정보 또는 에러 응답
	 */
	ResponseData<ResponseNotice> get(Long id);

	/** 
	 * 새로운 공지사항 생성.
	 * - 입력 데이터 검증 후 공지사항 엔티티 생성
	 * - 생성된 공지사항의 ID를 응답으로 반환
	 * 
	 * @param request 공지사항 생성 요청 데이터
	 * @return 생성된 공지사항의 ID 또는 에러 응답
	 */
	ResponseData<Long> create(RequestNoticeCreate request);

	/** 
	 * 기존 공지사항 수정.
	 * - ID로 공지사항을 찾아 요청 데이터로 업데이트
	 * - 존재하지 않는 경우 에러 응답 반환
	 * 
	 * @param id 수정할 공지사항 ID
	 * @param request 공지사항 수정 요청 데이터
	 * @return 수정 성공/실패 응답
	 */
	Response update(Long id, RequestNoticeUpdate request);

	/** 
	 * 공지사항 삭제.
	 * - ID로 공지사항을 찾아 삭제 처리
	 * - 존재하지 않는 경우 에러 응답 반환
	 * 
	 * @param id 삭제할 공지사항 ID
	 * @return 삭제 성공/실패 응답
	 */
	Response delete(Long id);
}