package com.academy.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의.
 * 
 * 애플리케이션에서 발생할 수 있는 모든 에러 코드를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "HANDLE_ACCESS_DENIED", "접근 권한이 없습니다."),

    // 인증/인가 에러
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "인증이 필요합니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_ACCOUNT_SUSPENDED(HttpStatus.LOCKED, "AUTH_ACCOUNT_SUSPENDED", "정지된 계정입니다. 관리자에게 문의하세요."),
    AUTH_ACCOUNT_DELETED(HttpStatus.LOCKED, "AUTH_ACCOUNT_DELETED", "삭제된 계정입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_NOT_FOUND", "Refresh Token을 찾을 수 없습니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_EXPIRED", "Refresh Token이 만료되었습니다."),

    // 회원 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    MEMBER_USERNAME_DUPLICATE(HttpStatus.CONFLICT, "MEMBER_USERNAME_DUPLICATE", "이미 사용 중인 사용자명입니다."),
    MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER_EMAIL_DUPLICATE", "이미 사용 중인 이메일 주소입니다."),
    MEMBER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER_PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다."),
    MEMBER_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_SAME_PASSWORD", "현재 비밀번호와 동일합니다. 다른 비밀번호를 입력해주세요."),

    // 갤러리 관련 에러
    GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "GALLERY_NOT_FOUND", "갤러리 항목을 찾을 수 없습니다."),
    IMAGE_SOURCE_REQUIRED(HttpStatus.BAD_REQUEST, "IMAGE_SOURCE_REQUIRED", "이미지 파일 ID 또는 이미지 URL이 필요합니다."),
    IMAGE_SOURCE_CONFLICT(HttpStatus.BAD_REQUEST, "IMAGE_SOURCE_CONFLICT", "이미지 파일 ID와 이미지 URL을 동시에 지정할 수 없습니다."),

    // 학사일정 관련 에러
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "학사일정을 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "시작 일자는 종료 일자보다 늦을 수 없습니다."),

    // 카테고리 관련 에러
    CATEGORY_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_GROUP_NOT_FOUND", "카테고리 그룹을 찾을 수 없습니다."),
    CATEGORY_GROUP_ALREADY_EXISTS(HttpStatus.CONFLICT, "CATEGORY_GROUP_ALREADY_EXISTS", "이미 존재하는 카테고리 그룹명입니다."),
    CATEGORY_GROUP_HAS_CATEGORIES(HttpStatus.BAD_REQUEST, "CATEGORY_GROUP_HAS_CATEGORIES", "하위 카테고리가 존재하여 삭제할 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."),
    CATEGORY_SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "CATEGORY_SLUG_ALREADY_EXISTS", "같은 그룹 내에서 이미 사용 중인 슬러그입니다."),
    CATEGORY_HAS_RELATED_DATA(HttpStatus.BAD_REQUEST, "CATEGORY_HAS_RELATED_DATA", "카테고리에 연결된 데이터가 있어 삭제할 수 없습니다."),
    INVALID_SLUG_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_SLUG_FORMAT", "슬러그는 영문, 숫자, 하이픈만 사용 가능합니다."),

    // 공지사항 관련 에러
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_NOT_FOUND", "공지사항을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}