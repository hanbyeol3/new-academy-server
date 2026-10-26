#!/bin/bash

# ===========================================
# 방문자 분석 시스템 API 테스트 스크립트
# 사용법: ./test_analytics_api.sh
# 전제: 서버 기동(local, :8080), jq 설치, testadmin 계정
# ===========================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api"

# 매 실행마다 새 방문자/세션으로 테스트 (중복 방지)
TS=$(date +%s)
SESSION_ID="test-session-${TS}-1"
VISITOR_ID="test-visitor-${TS}-1"
SESSION_ID_2="test-session-${TS}-2"
VISITOR_ID_2="test-visitor-${TS}-2"

TODAY=$(date +%Y-%m-%d)

PASS=0
FAIL=0

check() {
    # $1=테스트명, $2=응답, $3=성공판정 문자열
    if echo "$2" | grep -q "$3"; then
        echo -e "${GREEN}✓ $1${NC}"
        PASS=$((PASS+1))
    else
        echo -e "${RED}✗ $1${NC}"
        echo "$2" | head -c 500; echo
        FAIL=$((FAIL+1))
    fi
}

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}방문자 분석 API 테스트 시작 (${TODAY})${NC}"
echo -e "${GREEN}=========================================${NC}"

# ============================================
# 0. 헬스 체크 (인증 불필요 — permitAll 검증 겸용)
# ============================================
echo -e "\n${YELLOW}[0] 수집 API 헬스 체크 (익명)${NC}"
RESP=$(curl -s "${API_URL}/analytics/health")
check "헬스 체크 (익명 접근 = security permitAll 정상)" "$RESP" '"result":"Success"'

# ============================================
# 1. 관리자 로그인
# ============================================
echo -e "\n${YELLOW}[1] 관리자 로그인${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "testadmin", "password": "password123!"}')

JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken // empty' 2>/dev/null)
if [ -z "$JWT_TOKEN" ]; then
    JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
fi

if [ -n "$JWT_TOKEN" ]; then
    echo -e "${GREEN}✓ 로그인 성공${NC} (토큰: ${JWT_TOKEN:0:30}...)"
    PASS=$((PASS+1))
else
    echo -e "${RED}✗ 로그인 실패 — testadmin 비밀번호 확인 필요${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi
AUTH="Authorization: Bearer $JWT_TOKEN"

# ============================================
# 2. 데이터 수집 (공개 API — 토큰 없이 호출)
# ============================================
echo -e "\n${YELLOW}=========================================${NC}"
echo -e "${YELLOW}데이터 수집 API (익명 호출)${NC}"
echo -e "${YELLOW}=========================================${NC}"

# 2-1. 데스크톱 + 구글 검색 유입
echo -e "\n${YELLOW}[2-1] 페이지뷰: 데스크톱/구글 유입 → /${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/pageview" \
    -H "Content-Type: application/json" \
    -d '{
        "sessionId": "'$SESSION_ID'",
        "visitorId": "'$VISITOR_ID'",
        "pagePath": "/",
        "pageTitle": "홈",
        "referrer": "https://www.google.com/search?q=academy",
        "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "screenResolution": "1920x1080",
        "utmSource": "google",
        "utmMedium": "organic"
    }')
PAGEVIEW_ID_1=$(echo "$RESP" | jq -r '.data // empty')
check "페이지뷰 #1 (pageViewId=$PAGEVIEW_ID_1)" "$RESP" '"result":"Success"'

# 2-2. 같은 세션 두 번째 페이지 (이탈률 0% 만들기)
echo -e "\n${YELLOW}[2-2] 페이지뷰: 같은 세션 → /notice/list${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/pageview" \
    -H "Content-Type: application/json" \
    -d '{
        "sessionId": "'$SESSION_ID'",
        "visitorId": "'$VISITOR_ID'",
        "pagePath": "/notice/list",
        "pageTitle": "공지사항",
        "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }')
PAGEVIEW_ID_2=$(echo "$RESP" | jq -r '.data // empty')
check "페이지뷰 #2 (pageViewId=$PAGEVIEW_ID_2)" "$RESP" '"result":"Success"'

# 2-3. 모바일 + 네이버 유입 (다른 방문자)
echo -e "\n${YELLOW}[2-3] 페이지뷰: 모바일/네이버 유입 → /explanation${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/pageview" \
    -H "Content-Type: application/json" \
    -d '{
        "sessionId": "'$SESSION_ID_2'",
        "visitorId": "'$VISITOR_ID_2'",
        "pagePath": "/explanation",
        "pageTitle": "설명회 신청",
        "referrer": "https://m.naver.com",
        "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) Mobile/15E148"
    }')
check "페이지뷰 #3 (모바일)" "$RESP" '"result":"Success"'

# 2-4. 이벤트 (pageViewId 연결)
echo -e "\n${YELLOW}[2-4] 이벤트: 클릭 (pageViewId 연결)${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/event" \
    -H "Content-Type: application/json" \
    -d '{
        "sessionId": "'$SESSION_ID'",
        "pageViewId": '${PAGEVIEW_ID_2:-null}',
        "eventType": "click",
        "eventCategory": "button",
        "eventLabel": "공지사항 상세보기",
        "elementId": "btn-detail"
    }')
check "이벤트 (pageViewId 연결)" "$RESP" '"result":"Success"'

# 2-5. 이벤트 (pageViewId 없이 — NULL 허용 검증)
echo -e "\n${YELLOW}[2-5] 이벤트: pageViewId 없이 (NULL 허용 검증)${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/event" \
    -H "Content-Type: application/json" \
    -d '{
        "sessionId": "'$SESSION_ID'",
        "eventType": "custom",
        "eventCategory": "test",
        "eventLabel": "no-pageview-event"
    }')
check "이벤트 (pageViewId 없음 → NULL 저장)" "$RESP" '"result":"Success"'

# 2-6. 페이지 이탈
echo -e "\n${YELLOW}[2-6] 페이지 이탈: pageViewId=${PAGEVIEW_ID_1}${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/page-exit" \
    -H "Content-Type: application/json" \
    -d '{
        "pageViewId": '${PAGEVIEW_ID_1:-null}',
        "duration": 45,
        "scrollDepth": 75,
        "clickCount": 3,
        "isLastPage": false
    }')
check "페이지 이탈 업데이트" "$RESP" '"result":"Success"'

# 2-7. 배치 이벤트 (순수 배열)
echo -e "\n${YELLOW}[2-7] 배치 이벤트 (2개)${NC}"
RESP=$(curl -s -X POST "${API_URL}/analytics/events/batch" \
    -H "Content-Type: application/json" \
    -d '[
        {"sessionId": "'$SESSION_ID'", "pageViewId": '${PAGEVIEW_ID_2:-null}', "eventType": "scroll", "eventLabel": "50%"},
        {"sessionId": "'$SESSION_ID'", "pageViewId": '${PAGEVIEW_ID_2:-null}', "eventType": "click", "eventLabel": "menu"}
    ]')
check "배치 이벤트" "$RESP" '"result":"Success"'

# 2-8. 배치 초과 (101개 → 거부되어야 함)
echo -e "\n${YELLOW}[2-8] 배치 이벤트 101개 (거부 예상)${NC}"
BATCH="["
for i in {1..101}; do
    [ $i -ne 1 ] && BATCH+=","
    BATCH+='{"sessionId":"x","eventType":"test"}'
done
BATCH+="]"
RESP=$(curl -s -X POST "${API_URL}/analytics/events/batch" \
    -H "Content-Type: application/json" -d "$BATCH")
check "배치 100개 초과 거부" "$RESP" "LIMIT_EXCEEDED"

# ============================================
# 3. 오늘 날짜 수동 집계 (수집 데이터 → 통계 테이블)
# ============================================
echo -e "\n${YELLOW}=========================================${NC}"
echo -e "${YELLOW}오늘(${TODAY}) 데이터 집계 실행${NC}"
echo -e "${YELLOW}=========================================${NC}"

RESP=$(curl -s -X POST "${API_URL}/admin/analytics/aggregate?date=${TODAY}" -H "$AUTH")
check "수동 집계 실행" "$RESP" '"result":"Success"'

sleep 1

# ============================================
# 4. 관리자 통계 조회
# ============================================
echo -e "\n${YELLOW}=========================================${NC}"
echo -e "${YELLOW}관리자 통계 API${NC}"
echo -e "${YELLOW}=========================================${NC}"

# 4-1. 개요
echo -e "\n${YELLOW}[4-1] 개요 (24h — 방금 쌓은 데이터 포함)${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/overview?period=24h" -H "$AUTH")
check "개요 조회" "$RESP" '"result":"Success"'
echo "$RESP" | jq '.data | {visitors, newVisitors, returningVisitors, pageViews, totalSessions, bounceRate, visitorsChange}'

# 4-2. 시계열
echo -e "\n${YELLOW}[4-2] 시계열 (7d)${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/timeseries?period=7d" -H "$AUTH")
check "시계열 조회" "$RESP" '"result":"Success"'
echo "$RESP" | jq '[.items[] | {date, visitors, pageViews}]'

# 4-3. 페이지별 통계 (수정된 쿼리 검증 핵심!)
echo -e "\n${YELLOW}[4-3] 페이지별 통계 (24h)${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/pages?period=24h" -H "$AUTH")
check "페이지별 통계 (AIOOBE/GROUP BY 수정 검증)" "$RESP" '"result":"Success"'
echo "$RESP" | jq '[.items[] | {path, visitors, pageViews, entrances, bounceRate}]'

# 4-4. 유입경로별 통계 (수정된 쿼리 검증 핵심!)
echo -e "\n${YELLOW}[4-4] 유입경로별 통계 (24h)${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/referrers?period=24h" -H "$AUTH")
check "유입경로별 통계" "$RESP" '"result":"Success"'
echo "$RESP" | jq '[.items[] | {source, type, visitors, percentage, sessions}]'

# 4-5. 페이지 상세
echo -e "\n${YELLOW}[4-5] 페이지 상세 (/notice/list)${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/pages/detail?pagePath=%2Fnotice%2Flist&period=24h" -H "$AUTH")
check "페이지 상세 조회" "$RESP" '"result":"Success"'
echo "$RESP" | jq '.data | {path, totalPageViews, uniqueVisitors, avgTimeOnPage}'

# 4-6. 실시간
echo -e "\n${YELLOW}[4-6] 실시간 현황${NC}"
RESP=$(curl -s "${API_URL}/admin/analytics/realtime" -H "$AUTH")
check "실시간 현황" "$RESP" '"result":"Success"'
echo "$RESP" | jq '.data | {currentVisitors, last5Minutes, last30Minutes, lastHour}'

# 4-7. 재집계 (중복 누적 방지 검증 — 유입경로 방문자 수가 4-4와 같아야 함)
echo -e "\n${YELLOW}[4-7] 같은 날짜 재집계 → 유입경로 중복 누적 검증${NC}"
curl -s -X POST "${API_URL}/admin/analytics/aggregate?date=${TODAY}" -H "$AUTH" > /dev/null
sleep 1
RESP=$(curl -s "${API_URL}/admin/analytics/referrers?period=24h" -H "$AUTH")
check "재집계 후 유입경로 조회" "$RESP" '"result":"Success"'
echo "→ 아래 visitors가 [4-4]와 동일하면 중복 누적 버그 없음:"
echo "$RESP" | jq '[.items[] | {source, visitors}]'

# 4-8. 캐시 초기화
echo -e "\n${YELLOW}[4-8] 캐시 초기화${NC}"
RESP=$(curl -s -X DELETE "${API_URL}/admin/analytics/cache" -H "$AUTH")
check "캐시 초기화" "$RESP" '"result":"Success"'

# ============================================
# 결과 요약
# ============================================
echo -e "\n${GREEN}=========================================${NC}"
if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✅ 전체 통과: ${PASS}건${NC}"
else
    echo -e "${RED}❌ 실패 ${FAIL}건 / 통과 ${PASS}건${NC}"
fi
echo -e "${GREEN}=========================================${NC}"

echo -e "\n${YELLOW}수동 확인 사항:${NC}"
echo "1. DB 확인: SELECT * FROM analytics_daily WHERE date='${TODAY}';"
echo "2. NULL 이벤트: SELECT * FROM visitor_events WHERE page_view_id IS NULL ORDER BY id DESC LIMIT 3;"
echo "3. Redis 미기동 시 캐시 관련 로그 에러는 정상 (기능 영향 없음)"
