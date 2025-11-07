package com.academy.api.notice.service;

import com.academy.api.category.domain.Category;
import com.academy.api.category.repository.CategoryRepository;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticeSearch;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeSimple;
import com.academy.api.notice.mapper.NoticeMapper;
import com.academy.api.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공지사항 서비스 구현체.
 * 
 * 공지사항 도메인의 모든 비즈니스 로직을 구현합니다.
 * CLAUDE.md 표준에 따라 설계되었습니다.
 * 
 * 주요 특징:
 * - 트랜잭션 경계 관리 (@Transactional)
 * - 체계적인 로깅 (info: 주요 비즈니스, debug: 상세 정보)
 * - 카테고리 연계 처리
 * - 파일 서비스 연동 (추후 구현)
 * - 예외 상황 처리
 * 
 * 로깅 레벨 원칙:
 * - info: 주요 비즈니스 로직 시작점과 완료
 * - debug: 처리 단계별 상세 정보
 * - warn: 예상 가능한 예외 상황
 * - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final CategoryRepository categoryRepository;
    private final NoticeMapper noticeMapper;

    @Override
    public ResponseList<ResponseNoticeSimple> getNoticeList(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchNotices(searchCondition, pageable);
        
        log.debug("[NoticeService] 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    @Override
    public ResponseList<ResponseNoticeSimple> getNoticeListForAdmin(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 관리자용 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchNoticesForAdmin(searchCondition, pageable);
        
        log.debug("[NoticeService] 관리자 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    @Override
    public ResponseList<ResponseNoticeSimple> getExposableNoticeList(RequestNoticeSearch searchCondition, Pageable pageable) {
        log.info("[NoticeService] 공개용 공지사항 목록 조회 시작. 검색조건={}, 페이지={}", searchCondition, pageable);
        
        Page<Notice> noticePage = noticeRepository.searchExposableNotices(searchCondition, pageable);
        
        log.debug("[NoticeService] 공개 공지사항 검색 결과. 전체={}건, 현재페이지={}, 실제반환={}건", 
                noticePage.getTotalElements(), noticePage.getNumber(), noticePage.getContent().size());
        
        return noticeMapper.toSimpleResponseList(noticePage);
    }

    @Override
    public ResponseData<ResponseNotice> getNotice(Long id) {
        log.info("[NoticeService] 공지사항 상세 조회 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        
        log.debug("[NoticeService] 공지사항 조회 완료. ID={}, 제목={}, 조회수={}", 
                id, notice.getTitle(), notice.getViewCount());
        
        ResponseNotice response = noticeMapper.toResponse(notice);
        return ResponseData.ok(response);
    }

    @Override
    @Transactional
    public ResponseData<ResponseNotice> getNoticeWithViewCount(Long id) {
        log.info("[NoticeService] 공지사항 상세 조회 (조회수 증가) 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        Long beforeViewCount = notice.getViewCount();
        
        // 조회수 증가
        notice.incrementViewCount();
        
        log.debug("[NoticeService] 조회수 증가 완료. ID={}, 이전조회수={}, 현재조회수={}", 
                id, beforeViewCount, notice.getViewCount());
        
        ResponseNotice response = noticeMapper.toResponse(notice);
        return ResponseData.ok(response);
    }

    @Override
    @Transactional
    public ResponseData<Long> createNotice(RequestNoticeCreate request) {
        log.info("[NoticeService] 공지사항 생성 시작. 제목={}, 카테고리ID={}", request.getTitle(), request.getCategoryId());
        
        // 카테고리 조회 (있는 경우만)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[NoticeService] 카테고리 조회 완료. ID={}, 카테고리명={}", 
                    request.getCategoryId(), category.getName());
        }
        
        // 공지사항 생성
        Notice notice = noticeMapper.toEntity(request, category);
        Notice savedNotice = noticeRepository.save(notice);
        
        log.info("[NoticeService] 공지사항 생성 완료. ID={}, 제목={}", savedNotice.getId(), savedNotice.getTitle());
        
        return ResponseData.ok("0000", "공지사항이 생성되었습니다.", savedNotice.getId());
    }

    @Override
    @Transactional
    public Response updateNotice(Long id, RequestNoticeUpdate request) {
        log.info("[NoticeService] 공지사항 수정 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        
        // 카테고리 변경 처리
        Category category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryById(request.getCategoryId());
            log.debug("[NoticeService] 카테고리 변경. 기존={}, 신규={}", 
                    notice.getCategory() != null ? notice.getCategory().getName() : "없음", 
                    category.getName());
        }
        
        // 엔티티 업데이트
        noticeMapper.updateEntity(notice, request, category);
        
        log.info("[NoticeService] 공지사항 수정 완료. ID={}, 제목={}", id, notice.getTitle());
        
        return Response.ok("0000", "공지사항이 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response deleteNotice(Long id) {
        log.info("[NoticeService] 공지사항 삭제 시작. ID={}", id);
        
        Notice notice = findNoticeById(id);
        String title = notice.getTitle();
        
        noticeRepository.delete(notice);
        
        log.info("[NoticeService] 공지사항 삭제 완료. ID={}, 제목={}", id, title);
        
        return Response.ok("0000", "공지사항이 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response incrementViewCount(Long id) {
        log.info("[NoticeService] 조회수 증가 시작. ID={}", id);
        
        int updatedCount = noticeRepository.incrementViewCount(id);
        if (updatedCount == 0) {
            log.warn("[NoticeService] 조회수 증가 실패 - 공지사항을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        
        log.debug("[NoticeService] 조회수 증가 완료. ID={}", id);
        
        return Response.ok("0000", "조회수가 증가되었습니다.");
    }

    @Override
    @Transactional
    public Response toggleImportant(Long id, Boolean isImportant) {
        log.info("[NoticeService] 중요 공지 상태 변경 시작. ID={}, 중요공지={}", id, isImportant);
        
        int updatedCount = noticeRepository.updateImportantStatus(id, isImportant);
        if (updatedCount == 0) {
            log.warn("[NoticeService] 중요 공지 상태 변경 실패 - 공지사항을 찾을 수 없음. ID={}", id);
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        
        log.info("[NoticeService] 중요 공지 상태 변경 완료. ID={}, 중요공지={}", id, isImportant);
        
        String message = isImportant ? "중요 공지로 설정되었습니다." : "중요 공지가 해제되었습니다.";
        return Response.ok("0000", message);
    }

    @Override
    @Transactional
    public Response togglePublished(Long id, Boolean isPublished) {
        log.info("[NoticeService] 공개 상태 변경 시작. ID={}, 공개여부={}", id, isPublished);
        
        // 비공개 → 공개 변경 시 특별 처리를 위해 엔티티를 조회
        if (isPublished) {
            Notice notice = findNoticeById(id);
            notice.togglePublished();
            log.debug("[NoticeService] 공개 상태 변경 (특별 처리 포함). ID={}, 노출타입={}", 
                    id, notice.getExposureType());
        } else {
            int updatedCount = noticeRepository.updatePublishedStatus(id, isPublished);
            if (updatedCount == 0) {
                log.warn("[NoticeService] 공개 상태 변경 실패 - 공지사항을 찾을 수 없음. ID={}", id);
                throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
            }
        }
        
        log.info("[NoticeService] 공개 상태 변경 완료. ID={}, 공개여부={}", id, isPublished);
        
        String message = isPublished ? "공지사항이 공개되었습니다." : "공지사항이 비공개되었습니다.";
        return Response.ok("0000", message);
    }

    @Override
    public ResponseData<List<ResponseNoticeSimple>> getRecentNotices(int limit) {
        log.info("[NoticeService] 최근 공지사항 조회 시작. 개수={}", limit);
        
        List<Notice> notices = noticeRepository.findRecentNotices(limit);
        List<ResponseNoticeSimple> response = noticeMapper.toSimpleResponseList(notices);
        
        log.debug("[NoticeService] 최근 공지사항 조회 완료. 반환개수={}", response.size());
        
        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<List<Object[]>> getNoticeStatsByCategory() {
        log.info("[NoticeService] 카테고리별 공지사항 통계 조회 시작");
        
        List<Object[]> stats = noticeRepository.getNoticeStatsByCategory();
        
        log.debug("[NoticeService] 카테고리별 통계 조회 완료. 카테고리수={}", stats.size());
        
        return ResponseData.ok(stats);
    }

    /**
     * 공지사항 조회 도우미 메서드.
     */
    private Notice findNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[NoticeService] 공지사항을 찾을 수 없음. ID={}", id);
                    return new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
                });
    }

    /**
     * 카테고리 조회 도우미 메서드.
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[NoticeService] 카테고리를 찾을 수 없음. ID={}", categoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }
}