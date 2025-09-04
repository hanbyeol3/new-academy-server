package com.academy.api.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * JPA Timestamp 타입을 LocalDateTime으로 변환하는 컨버터.
 * 
 * - 데이터베이스의 TIMESTAMP 컬럼과 Java의 LocalDateTime 간 변환 처리
 * - @Converter(autoApply = true)로 전역 자동 적용 설정
 * - null 값 안전 처리 포함
 * 
 * 사용 목적:
 *  - 레거시 데이터베이스 스키마와의 호환성 유지
 *  - Java 8+ 시간 API (LocalDateTime) 활용
 *  - JPA 엔티티에서 타입 안전성 보장
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

	/**
	 * Java LocalDateTime을 데이터베이스 Timestamp로 변환.
	 * - JPA가 엔티티를 데이터베이스에 저장할 때 호출됨
	 * - null 값 안전 처리: LocalDateTime이 null이면 null 반환
	 * 
	 * @param localDateTime 변환할 LocalDateTime 객체 (null 허용)
	 * @return 변환된 Timestamp 객체 또는 null
	 */
	@Override
	public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
		// null 체크 후 Timestamp로 변환: NullPointerException 방지
		return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
	}

	/**
	 * 데이터베이스 Timestamp를 Java LocalDateTime으로 변환.
	 * - JPA가 데이터베이스에서 엔티티를 조회할 때 호출됨
	 * - null 값 안전 처리: Timestamp가 null이면 null 반환
	 * 
	 * @param timestamp 변환할 Timestamp 객체 (null 허용)
	 * @return 변환된 LocalDateTime 객체 또는 null
	 */
	@Override
	public LocalDateTime convertToEntityAttribute(Timestamp timestamp) {
		// null 체크 후 LocalDateTime으로 변환: NullPointerException 방지
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
