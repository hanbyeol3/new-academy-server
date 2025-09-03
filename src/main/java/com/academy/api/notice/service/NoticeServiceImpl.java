package com.academy.api.notice.service;

import com.academy.api.data.requests.notice.RequestNoticeCreate;
import com.academy.api.data.requests.notice.RequestNoticeUpdate;
import com.academy.api.data.responses.common.ResponsePage;
import com.academy.api.data.responses.notice.ResponseNotice;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.dto.NoticeSearchCond;
import com.academy.api.notice.repository.NoticeQueryRepository;
import com.academy.api.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeQueryRepository noticeQueryRepository;

    @Override
    public ResponsePage<ResponseNotice> list(NoticeSearchCond cond, Pageable pageable) {
        Page<ResponseNotice> page = noticeQueryRepository.search(cond, pageable);
        return ResponsePage.from(page);
    }

    @Override
    @Transactional
    public ResponseNotice get(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id: " + id));
        
        notice.incrementViewCount();
        
        return ResponseNotice.from(notice);
    }

    @Override
    @Transactional
    public Long create(RequestNoticeCreate request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .pinned(request.getPinned())
                .published(request.getPublished())
                .build();

        Notice saved = noticeRepository.save(notice);
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(Long id, RequestNoticeUpdate request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id: " + id));

        notice.update(request.getTitle(), request.getContent(), 
                     request.getPinned(), request.getPublished());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("공지사항을 찾을 수 없습니다. id: " + id);
        }
        noticeRepository.deleteById(id);
    }

}