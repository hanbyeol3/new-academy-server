package com.academy.api.notice.service;

import com.academy.api.data.requests.notice.RequestNoticeCreate;
import com.academy.api.data.requests.notice.RequestNoticeUpdate;
import com.academy.api.data.responses.common.ResponsePage;
import com.academy.api.data.responses.notice.ResponseNotice;
import com.academy.api.notice.dto.NoticeSearchCond;
import org.springframework.data.domain.Pageable;

public interface NoticeService {

    ResponsePage<ResponseNotice> list(NoticeSearchCond cond, Pageable pageable);

    ResponseNotice get(Long id);

    Long create(RequestNoticeCreate request);

    void update(Long id, RequestNoticeUpdate request);

    void delete(Long id);

}