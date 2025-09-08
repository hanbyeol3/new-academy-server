package com.academy.api.notice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices", indexes = {
        @Index(name = "idx_notices_pinned_desc", columnList = "pinned DESC"),
        @Index(name = "idx_notices_created_at_desc", columnList = "createdAt DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = true;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /** 첨부 파일 그룹 아이디 */
    @Column(name = "file_group_key", length = 36)
    private String fileGroupKey;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String title, String content, Boolean pinned, Boolean published) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (pinned != null) {
            this.pinned = pinned;
        }
        if (published != null) {
            this.published = published;
        }
    }

    public void updateFileGroupKey(String fileGroupKey) {
        this.fileGroupKey = fileGroupKey;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

}