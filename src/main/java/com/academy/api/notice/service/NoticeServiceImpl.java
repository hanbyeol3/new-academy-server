package com.academy.api.notice.service;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.model.ResponseNotice;
import com.academy.api.notice.repository.NoticeQueryRepository;
import com.academy.api.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지사항 서비스 구현체.
 * 
 * - 공지사항 CRUD 비즈니스 로직 처리
 * - 대용량 데이터 조회 시 성능 최적화
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

	private final NoticeRepository noticeRepository;
	private final NoticeQueryRepository noticeQueryRepository;

	/** 
	 * 공지사항 목록 조회 및 페이지네이션 처리.
	 * - 검색 조건에 따른 동적 쿼리 생성
	 * - 성능 최적화를 위한 인덱스 활용 쿼리
	 */
	@Override
	public ResponseList<ResponseNotice> list(ResponseNotice.Criteria cond, Pageable pageable) {
		// 입력 파라미터 로깅: 요청 내용 추적 및 디버깅 용도
		log.info("[NoticeService] 목록 조회 시작. 조건={}, 페이지네이션={}", cond, pageable);
		
		// 리포지토리 계층에서 페이지 결과 조회
		Page<ResponseNotice> page = noticeQueryRepository.search(cond, pageable);
		
		// Spring Data Page를 ResponseList로 변환: API 일관성 유지
		ResponseList<ResponseNotice> result = ResponseList.from(page);
		
		// 처리 결과 요약 로깅: 성능 모니터링 및 결과 검증
		log.debug("[NoticeService] 목록 조회 완료. 전체={}건, 현재페이지={}, 페이지크기={}, 실제반환={}건", 
				result.getTotal(), result.getPage(), result.getSize(), result.getItems().size());
		
		return result;
	}

	/** 
	 * 공지사항 단건 조회 및 조회수 증가 처리.
	 * - 조회와 동시에 조회수 자동 증가 (비즈니스 로직)
	 * - 엔티티 조회 실패 시 의미 있는 에러 메시지 반환
	 */
	@Override
	@Transactional  // 조회수 증가 업데이트를 위한 쓰기 트랜잭션 필요
	public ResponseData<ResponseNotice> get(Long id) {
		// 입력 파라미터 로깅
		log.info("[NoticeService] 단건 조회 시작. ID={}", id);
		
		return noticeRepository.findById(id)
				.map(notice -> {
					// 조회수 증가: 비즈니스 로직 처리
					notice.incrementViewCount();
					
					// 조회수 증가 완료 로깅
					log.debug("[NoticeService] 조회수 증가 완료. ID={}, 새조회수={}", id, notice.getViewCount());
					
					// 엔티티 → 응답 DTO 변환 후 성공 응답 생성
					return ResponseData.ok(ResponseNotice.from(notice));
				})
				.orElseGet(() -> {
					// 엔티티 미존재 시 경고 로깅 및 에러 응답
					log.warn("[NoticeService] 공지사항 미존재. ID={}", id);
					return ResponseData.error("N404", "공지사항을 찾을 수 없습니다. ID: " + id);
				});
	}

	/** 
	 * 새로운 공지사항 생성 처리.
	 * - 요청 데이터를 Notice 엔티티로 변환 후 영속화
	 * - 생성된 엔티티의 ID를 반환하여 클라이언트가 후속 작업 가능
	 */
	@Override
	@Transactional  // 데이터 생성을 위한 쓰기 트랜잭션
	public ResponseData<Long> create(RequestNoticeCreate request) {
		// 입력 파라미터 로깅 (민감 정보 제외하고 기본 정보만)
		log.info("[NoticeService] 공지사항 생성 시작. 제목=[{}], 발행여부={}, 고정여부={}", 
				request.getTitle(), request.getPublished(), request.getPinned());
		
		// 요청 DTO → 엔티티 변환: 비즈니스 로직 캡핑
		Notice notice = Notice.builder()
				.title(request.getTitle())
				.content(request.getContent())
				.pinned(request.getPinned())
				.published(request.getPublished())
				.build();

		// 엔티티 영속화: JPA 리포지토리를 통한 데이터베이스 저장
		Notice saved = noticeRepository.save(notice);
		
		// 생성 결과 로깅
		log.debug("[NoticeService] 공지사항 생성 완료. 생성ID={}, 제목=[{}]", saved.getId(), saved.getTitle());
		
		// 생성된 ID를 포함한 성공 응답 반환
		return ResponseData.ok(saved.getId());
	}

	/** 
	 * 기존 공지사항 수정 처리.
	 * - ID로 엔티티를 찾아 요청 데이터로 업데이트
	 * - 엔티티의 비즈니스 로직 메서드 활용
	 */
	@Override
	@Transactional  // 데이터 수정을 위한 쓰기 트랜잭션
	public Response update(Long id, RequestNoticeUpdate request) {
		// 입력 파라미터 로깅
		log.info("[NoticeService] 공지사항 수정 시작. ID={}, 제목=[{}], 발행여부={}", 
				id, request.getTitle(), request.getPublished());
		
		return noticeRepository.findById(id)
				.map(notice -> {
					// 엔티티 비즈니스 로직 메서드 호출: 더티 체킹 자동 처리
					notice.update(request.getTitle(), request.getContent(),
							request.getPinned(), request.getPublished());
					
					// 수정 성공 로깅
					log.debug("[NoticeService] 공지사항 수정 완료. ID={}, 수정시간={}", id, notice.getUpdatedAt());
					
					// 성공 응답 반환
					return Response.ok("0000", "공지사항이 수정되었습니다.");
				})
				.orElseGet(() -> {
					// 엔티티 미존재 시 경고 로깅 및 에러 응답
					log.warn("[NoticeService] 수정 대상 공지사항 미존재. ID={}", id);
					return Response.error("N404", "공지사항을 찾을 수 없습니다. ID: " + id);
				});
	}

	/** 
	 * 공지사항 삭제 처리.
	 * - ID 존재 여부 선확인 후 삭제 실행
	 * - 삭제 불가능한 상황에서의 예외 처리
	 */
	@Override
	@Transactional  // 데이터 삭제를 위한 쓰기 트랜잭션
	public Response delete(Long id) {
		// 입력 파라미터 로깅
		log.info("[NoticeService] 공지사항 삭제 시작. ID={}", id);
		
		// 엔티티 존재 여부 확인: 뛰어난 성능을 위해 exists 사용
		if (!noticeRepository.existsById(id)) {
			// 엔티티 미존재 시 경고 로깅 및 에러 응답
			log.warn("[NoticeService] 삭제 대상 공지사항 미존재. ID={}", id);
			return Response.error("N404", "공지사항을 찾을 수 없습니다. ID: " + id);
		}
		
		// 엔티티 삭제 실행
		noticeRepository.deleteById(id);
		
		// 삭제 성공 로깅
		log.debug("[NoticeService] 공지사항 삭제 완료. ID={}", id);
		
		// 기본 성공 응답 반환
		return Response.ok();
	}
}