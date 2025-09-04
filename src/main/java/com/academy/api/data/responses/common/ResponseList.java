package com.academy.api.data.responses.common;

import com.academy.api.data.responses.ResponseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 목록/페이지 데이터를 포함하는 API 응답 클래스.
 * 
 * - 목록 조회, 페이지네이션 결과를 반환할 때 사용
 * - Spring Data의 Page<T> 객체와 쉽게 호환되도록 설계
 * - 정적 팩토리 메서드를 통해 일관된 목록 응답 생성
 * 
 * 구성 요소:
 *  - items: 다음 데이터 목록
 *  - total: 전체 데이터 개수
 *  - page: 현재 페이지 번호 (0-based)
 *  - size: 페이지 크기
 * 
 * 사용 예시:
 *  - ResponseList.from(page): Spring Data Page로부터 변환
 *  - ResponseList.ok(items, total, page, size): 직접 생성
 *  - ResponseList.error("E001", "검색 실패"): 에러 응답
 * 
 * @param <T> 목록에 포함될 데이터의 타입 (예: ResponseNotice, User)
 */
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class ResponseList<T> extends ResponseBase {

	/**
	 * 생성자
	 *
	 * @param result       응답 결과
	 * @param code         응답 코드
	 * @param message      응답 메시지
	 * @param needLogin    응답 메시지
	 * @param accessDenied 응답 메시지
	 * @param items        데이터 리스트
	 * @param total        전체 개수
	 * @param page         현재 페이지 번호
	 * @param size         페이지 크기
	 */
	public ResponseList(ResponseResult result, String code, String message, boolean needLogin, boolean accessDenied, 
						List<T> items, long total, int page, int size) {
		super(result, code, message, needLogin, accessDenied);
		this.items = items;
		this.total = total;
		this.page = page;
		this.size = size;
	}

	/**
	 * 생성자 (에러 처리)
	 *
	 * @param code    응답 코드
	 * @param message 응답 메시지
	 */
	public ResponseList(String code, String message) {
		super(ResponseResult.Error, code, message, false, false);
	}

	/**
	 * 생성자 (에러 처리)
	 *
	 * @param code         응답 코드
	 * @param message      응답 메시지
	 * @param needLogin    로그인 필요여부
	 * @param accessDenied 접근 권한 없음 여부
	 */
	public ResponseList(String code, String message, boolean needLogin, boolean accessDenied) {
		super(ResponseResult.Error, code, message, needLogin, accessDenied);
	}

	/**
	 * 데이터 목록
	 */
	/** 
	 * 현재 페이지에 포함된 데이터 목록.
	 * - 빈 리스트일 수 있지만 null이어서는 안 됨
	 * - 제네릭 타입 T로 타입 안전성 보장
	 */
	@Schema(description = "데이터 목록")
	private List<T> items;

	/** 
	 * 검색 조건에 맞는 전체 데이터 개수.
	 * - 현재 페이지의 items.size()가 아닌 전체 개수
	 * - 클라이언트의 페이지네이션 UI 계산에 사용
	 */
	@Schema(description = "전체 개수")
	private long total;

	/** 
	 * 현재 페이지 번호 (0-based).
	 * - Spring Data Pageable과 동일한 방식 (0부터 시작)
	 * - UI에서는 +1하여 표시하는 것이 일반적
	 */
	@Schema(description = "현재 페이지 번호")
	private int page;

	/** 
	 * 한 페이지에 표시할 데이터 개수.
	 * - Pageable.getPageSize()와 동일한 값
	 * - 실제 items.size()와는 다를 수 있음 (마지막 페이지의 경우)
	 */
	@Schema(description = "페이지 크기")
	private int size;

	// ───────────────────────────────────────────────────────────────────
	// 정적 팩토리 메서드 (일관된 목록 응답 생성을 위한 유틸리티)
	// ───────────────────────────────────────────────────────────────────
	
	/**
	 * 성공 목록 응답 생성.
	 * - 성공 상태에 목록 데이터와 페이지네이션 정보를 포함
	 * - 목록 조회 결과를 직접 생성할 때 사용
	 * 
	 * @param <T>   데이터의 타입
	 * @param items 데이터 목록 (빈 리스트 가능, null 불가)
	 * @param total 전체 데이터 개수
	 * @param page  현재 페이지 번호 (0-based)
	 * @param size  페이지 크기
	 * @return 성공 ResponseList 인스턴스
	 */
	public static <T> ResponseList<T> ok(List<T> items, long total, int page, int size) {
		return new ResponseList<>(ResponseResult.Success, "", "", false, false, items, total, page, size);
	}

	/**
	 * Spring Data Page로부터 변환하여 응답 생성.
	 * - 가장 일반적으로 사용되는 팩토리 메서드
	 * - Spring Data JPA의 Page 객체를 ResponseList로 쉽게 변환
	 * 
	 * @param <T>  데이터의 타입
	 * @param page Spring Data의 Page 객체 (페이지네이션 정보 포함)
	 * @return Spring Data Page로부터 변환된 ResponseList 인스턴스
	 */
	public static <T> ResponseList<T> from(Page<T> page) {
		return ok(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
	}

	/**
	 * 에러 목록 응답 생성.
	 * - 실패 상태에 에러 코드와 메시지만 포함 (데이터는 비어있음)
	 * - 목록 조회 실패, 검색 오류 등에서 사용
	 * 
	 * @param <T>     데이터의 타입 (실제로는 사용되지 않지만 타입 정의 필요)
	 * @param code    에러 코드 (예: "E001", "SEARCH_ERROR")
	 * @param message 에러 메시지 (사용자에게 표시될 내용)
	 * @return 에러 ResponseList 인스턴스
	 */
	public static <T> ResponseList<T> error(String code, String message) {
		return new ResponseList<>(code, message);
	}
}