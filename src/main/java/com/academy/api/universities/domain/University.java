package com.academy.api.universities.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 대학교 정보 엔티티.
 * 
 * universities 테이블과 매핑되며 대학교 정보를 관리합니다.
 */
@Entity
@Table(name = "universities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 대학교 이름 */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** 대학교 이미지 경로 */
    @Column(name = "image_path", length = 255)
    private String imagePath;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private University(String name, String imagePath, Long createdBy) {
        this.name = name;
        this.imagePath = imagePath;
        this.createdBy = createdBy;
    }

    public void update(String name, String imagePath, Long updatedBy) {
        this.name = name;
        this.imagePath = imagePath;
        this.updatedBy = updatedBy;
    }
}