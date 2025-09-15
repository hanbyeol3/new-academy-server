package com.academy.api.notice.mapper;

import com.academy.api.file.dto.UploadFileDto;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.model.ResponseNotice;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공지사항 도메인 Mapper.
 * Entity와 DTO 간의 변환을 담당합니다.
 */
@Component
public class NoticeMapper {

    /**
     * Notice 엔티티를 응답 DTO로 변환 (첨부파일 없음).
     */
    public ResponseNotice toResponse(Notice notice) {
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.getPinned())
                .published(notice.getPublished())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .attachedFiles(Collections.emptyList())
                .build();
    }

    /**
     * Notice 엔티티를 응답 DTO로 변환 (첨부파일 포함).
     */
    public ResponseNotice toResponse(Notice notice, List<UploadFileDto> attachedFiles) {
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.getPinned())
                .published(notice.getPublished())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .attachedFiles(attachedFiles != null ? attachedFiles : Collections.emptyList())
                .build();
    }

    /**
     * Notice 엔티티 목록을 응답 DTO 목록으로 변환.
     */
    public List<ResponseNotice> toResponseList(List<Notice> notices) {
        return notices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}