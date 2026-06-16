package com.academy.api.holiday.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공휴일 엔티티.
 * 
 * holidays 테이블과 매핑되며 한국천문연구원 특일 정보를 저장합니다.
 */
@Entity
@Table(name = "holidays", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"holiday_date", "source"}),
    indexes = @Index(name = "idx_holidays_date", columnList = "holiday_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Holiday {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 공휴일 날짜 */
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;
    
    /** 공휴일명 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /** 공휴일 여부 */
    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = true;
    
    /** 출처 */
    @Column(name = "source", nullable = false, length = 50)
    private String source = "KASI";
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    private Holiday(LocalDate holidayDate, String name, Boolean isHoliday, String source) {
        this.holidayDate = holidayDate;
        this.name = name;
        this.isHoliday = isHoliday != null ? isHoliday : true;
        this.source = source != null ? source : "KASI";
    }
    
    /**
     * 공휴일 정보 업데이트.
     */
    public void update(String name, Boolean isHoliday) {
        this.name = name;
        this.isHoliday = isHoliday;
    }
    
    /**
     * 공휴일 정보 생성 (팩토리 메서드).
     */
    public static Holiday create(LocalDate date, String name) {
        return Holiday.builder()
                .holidayDate(date)
                .name(name)
                .isHoliday(true)
                .source("KASI")
                .build();
    }
    
    /**
     * 한국천문연구원 API 데이터로부터 생성.
     */
    public static Holiday fromKasiData(LocalDate date, String name, Boolean isHoliday) {
        return Holiday.builder()
                .holidayDate(date)
                .name(name)
                .isHoliday(isHoliday)
                .source("KASI")
                .build();
    }
}