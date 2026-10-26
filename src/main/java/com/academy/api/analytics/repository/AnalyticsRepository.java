package com.academy.api.analytics.repository;

import com.academy.api.analytics.repository.custom.AnalyticsRepositoryCustom;

/**
 * 분석 통계용 Repository.
 * QueryDSL 커스텀 메서드들을 제공합니다.
 */
public interface AnalyticsRepository extends AnalyticsRepositoryCustom {
    // 모든 메서드는 AnalyticsRepositoryCustom와 AnalyticsRepositoryImpl에 정의됨
}