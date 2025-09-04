package com.academy.api.notice.controller;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.model.ResponseNotice;
import com.academy.api.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 관련 REST API를 제공하는 컨트롤러.
 * 
 * - RESTful API 설계 원칙 준수 (GET/POST/PUT/DELETE)
 * - 통일된 응답 포맷 사용 (Response, ResponseData, ResponseList)
 * - OpenAPI(Swagger) 문서화 완비
 * - Bean Validation을 통한 입력 데이터 검증
 * 
 * API 엔드포인트:
 *  - GET /api/notices: 목록 조회 (검색 및 페이지네이션)
 *  - GET /api/notices/{id}: 단건 조회 (조회수 자동 증가)
 *  - POST /api/notices: 신규 생성
 *  - PUT /api/notices/{id}: 기존 수정
 *  - DELETE /api/notices/{id}: 삭제
 * 
 * 보안:
 *  - BasicAuth 인증 필요 (SecurityRequirement 설정)
 *  - Spring Security 설정에 따른 인가 처리
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notices", description = "공지사항 API")
@SecurityRequirement(name = "basicAuth")
public class NoticeController {

	/** 공지사항 비즈니스 로직 처리를 위한 서비스 계층 의존성 */
	private final NoticeService noticeService;

	/**
	 * 공지사항 목록 조회 API.
	 * - 검색 조건에 따른 필터링 지원
	 * - 페이지네이션을 통한 대용량 데이터 효율적 처리
	 * - 정렬 옵션 지원 (생성일, 조회수 등)
	 * 
	 * @param cond 검색 조건 (제목, 내용, 발행상태, 고정여부 등)
	 * @param pageable 페이지네이션 정보 (기본 10건씩 조회)
	 * @return 검색된 공지사항 목록과 페이지 정보를 포함한 ResponseList
	 */
	@GetMapping
	@Operation(summary = "공지사항 목록 조회", description = "검색 조건과 페이지네이션을 적용하여 공지사항 목록을 조회합니다.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "목록 조회 성공") })
	public ResponseList<ResponseNotice> list(
			@Parameter(description = "검색 조건 (제목, 내용, 발행상태 등)") ResponseNotice.Criteria cond,
			@Parameter(description = "페이지네이션 정보 (page, size, sort)") @PageableDefault(size = 10) Pageable pageable
	) {
		// 서비스 계층 호출: 비즈니스 로직 위임
		return noticeService.list(cond, pageable);
	}

	/**
	 * 공지사항 단건 조회 API.
	 * - ID를 통한 특정 공지사항 조회
	 * - 조회 시 자동으로 조회수 증가 처리
	 * - 존재하지 않는 경우 404 에러 응답
	 * 
	 * @param id 조회할 공지사항의 고유 식별자
	 * @return 조회된 공지사항 정보 또는 에러 응답
	 */
	@GetMapping("/{id}")
	@Operation(summary = "공지사항 단건 조회", description = "ID로 특정 공지사항을 조회하고 조회수를 자동으로 증가시킵니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음")
	})
	public ResponseData<ResponseNotice> get(
			@Parameter(description = "조회할 공지사항 ID", example = "1") @PathVariable Long id
	) {
		// 서비스 계층 호출: 조회 및 조회수 증가 로직 포함
		return noticeService.get(id);
	}

	/**
	 * 새로운 공지사항 생성 API.
	 * - Bean Validation을 통한 입력 데이터 검증
	 * - 생성 성공 시 HTTP 201 Created 응답
	 * - 생성된 공지사항의 ID를 응답에 포함
	 * 
	 * @param request 공지사항 생성에 필요한 데이터 (제목, 내용, 발행여부 등)
	 * @return 생성된 공지사항의 ID 또는 검증 실패 에러
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성하고 생성된 ID를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성 성공"),
			@ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패")
	})
	public ResponseData<Long> create(
			@Parameter(description = "공지사항 생성 요청 데이터") @Valid @RequestBody RequestNoticeCreate request
	) {
		// 서비스 계층 호출: 엔티티 생성 및 영속화 로직
		return noticeService.create(request);
	}

	/**
	 * 기존 공지사항 수정 API.
	 * - ID로 수정 대상 공지사항 식별
	 * - Bean Validation을 통한 입력 데이터 검증
	 * - 존재하지 않는 경우 404 에러 응답
	 * 
	 * @param id 수정할 공지사항의 고유 식별자
	 * @param request 수정할 데이터 (제목, 내용, 발행여부 등)
	 * @return 수정 성공/실패 응답
	 */
	@PutMapping("/{id}")
	@Operation(summary = "공지사항 수정", description = "기존 공지사항의 정보를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음"),
			@ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패")
	})
	public Response update(
			@Parameter(description = "수정할 공지사항 ID", example = "1") @PathVariable Long id,
			@Parameter(description = "공지사항 수정 요청 데이터") @Valid @RequestBody RequestNoticeUpdate request
	) {
		// 서비스 계층 호출: 엔티티 조회 및 업데이트 로직
		return noticeService.update(id, request);
	}

	/**
	 * 공지사항 삭제 API.
	 * - ID로 삭제 대상 공지사항 식별
	 * - 물리적 삭제 수행 (소프트 삭제 아님)
	 * - 존재하지 않는 경우 404 에러 응답
	 * 
	 * @param id 삭제할 공지사항의 고유 식별자
	 * @return 삭제 성공/실패 응답
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "공지사항 삭제", description = "지정된 ID의 공지사항을 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음")
	})
	public Response delete(
			@Parameter(description = "삭제할 공지사항 ID", example = "1") @PathVariable Long id
	) {
		// 서비스 계층 호출: 엔티티 존재 확인 및 삭제 로직
		return noticeService.delete(id);
	}
}