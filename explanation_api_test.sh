#!/bin/bash

# 설명회 API 종합 테스트 스크립트
# 포트: 8081
# 관리자 계정: testadmin / password123!

BASE_URL="http://localhost:8081"
JWT_TOKEN=""
EXPLANATION_ID=""
SCHEDULE_ID=""
RESERVATION_ID=""

echo "🚀 설명회 API 종합 테스트 시작"
echo "📋 Base URL: $BASE_URL"
echo ""

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 성공/실패 출력 함수
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_section() {
    echo -e "\n${CYAN}=== $1 ===${NC}"
}

# JSON 응답 검증 함수
check_response() {
    local response="$1"
    local expected_result="$2"
    
    if echo "$response" | jq -e '.result' >/dev/null 2>&1; then
        local result=$(echo "$response" | jq -r '.result')
        if [ "$result" = "$expected_result" ]; then
            return 0
        else
            print_error "예상 결과: $expected_result, 실제 결과: $result"
            return 1
        fi
    else
        print_error "Invalid JSON response: $response"
        return 1
    fi
}

# 1. JWT 토큰 발급
login_and_get_token() {
    print_section "JWT 토큰 발급"
    
    local response=$(curl -s -X POST "$BASE_URL/api/auth/sign-in" \
        -H "Content-Type: application/json" \
        -d '{"username": "testadmin", "password": "password123\\!"}')
    
    print_info "로그인 응답: $response"
    
    if echo "$response" | jq -e '.data.accessToken' >/dev/null 2>&1; then
        JWT_TOKEN=$(echo "$response" | jq -r '.data.accessToken')
        print_success "JWT 토큰 발급 성공"
        print_info "토큰: ${JWT_TOKEN:0:50}..."
    else
        print_error "JWT 토큰 발급 실패: $response"
        exit 1
    fi
}

# 2. 설명회 생성 API 테스트
test_create_explanation() {
    print_section "설명회 생성 API 테스트"
    
    # 2.1 정상 케이스 - 중등부 설명회 생성
    print_info "2.1 중등부 설명회 생성"
    local response=$(curl -s -X POST "$BASE_URL/api/admin/explanations" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "division": "MIDDLE",
            "title": "중등부 수학 설명회 - 기초부터 심화까지",
            "content": "중등부 학생들을 위한 체계적인 수학 교육 과정을 소개합니다.<br><strong>주요 특징:</strong><ul><li>개별 맞춤 교육</li><li>체계적인 단계별 학습</li><li>실시간 학습 관리</li></ul>",
            "isPublished": true,
            "initialSchedule": {
                "roundNo": 1,
                "startAt": "2026-02-01 14:00:00",
                "endAt": "2026-02-01 16:00:00",
                "location": "강의실 A",
                "applyStartAt": "2026-01-20 00:00:00",
                "applyEndAt": "2026-01-31 23:59:00",
                "status": "RESERVABLE",
                "capacity": 30
            }
        }')
    
    if check_response "$response" "Success"; then
        EXPLANATION_ID=$(echo "$response" | jq -r '.data')
        print_success "중등부 설명회 생성 성공, ID: $EXPLANATION_ID"
    else
        print_error "중등부 설명회 생성 실패: $response"
    fi
    
    # 2.2 고등부 설명회 생성 (비공개)
    print_info "2.2 고등부 설명회 생성 (비공개)"
    response=$(curl -s -X POST "$BASE_URL/api/admin/explanations" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "division": "HIGH",
            "title": "고등부 종합반 설명회",
            "content": "고등부 종합 교육과정 안내",
            "isPublished": false,
            "initialSchedule": {
                "roundNo": 1,
                "startAt": "2026-02-05 15:00:00",
                "endAt": "2026-02-05 17:00:00",
                "location": "대강당",
                "applyStartAt": "2026-01-25 09:00:00",
                "applyEndAt": "2026-02-04 18:00:00",
                "status": "RESERVABLE",
                "capacity": 50
            }
        }')
    
    if check_response "$response" "Success"; then
        print_success "고등부 설명회 생성 성공"
    else
        print_error "고등부 설명회 생성 실패: $response"
    fi
    
    # 2.3 에러 케이스 - 필수 필드 누락
    print_info "2.3 에러 케이스 - 필수 필드 누락"
    response=$(curl -s -X POST "$BASE_URL/api/admin/explanations" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "division": "MIDDLE",
            "content": "내용만 있음"
        }')
    
    if echo "$response" | jq -e '.success' >/dev/null 2>&1; then
        local success=$(echo "$response" | jq -r '.success')
        if [ "$success" = "false" ]; then
            print_success "필수 필드 누락 에러 테스트 성공"
        else
            print_error "필수 필드 누락 에러 테스트 실패"
        fi
    else
        print_warning "에러 응답 형식이 다름: $response"
    fi
}

# 3. 설명회 목록 조회 API 테스트 (필터링 상세 테스트)
test_list_explanations() {
    print_section "설명회 목록 조회 API 테스트 (필터링 포함)"
    
    # 3.1 전체 목록 조회
    print_info "3.1 전체 목록 조회"
    local response=$(curl -s -X GET "$BASE_URL/api/admin/explanations" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "전체 목록 조회 성공, 총 ${total}개"
    else
        print_error "전체 목록 조회 실패: $response"
    fi
    
    # 3.2 division 필터링 - MIDDLE
    print_info "3.2 division 필터링 - MIDDLE"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?division=MIDDLE" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "MIDDLE division 필터링 성공, ${total}개"
    else
        print_error "MIDDLE division 필터링 실패: $response"
    fi
    
    # 3.3 division 필터링 - HIGH
    print_info "3.3 division 필터링 - HIGH"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?division=HIGH" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "HIGH division 필터링 성공, ${total}개"
    else
        print_error "HIGH division 필터링 실패: $response"
    fi
    
    # 3.4 공개 상태 필터링 - isPublished=1
    print_info "3.4 공개 상태 필터링 - isPublished=1"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?isPublished=1" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "공개 설명회 필터링 성공, ${total}개"
    else
        print_error "공개 설명회 필터링 실패: $response"
    fi
    
    # 3.5 공개 상태 필터링 - isPublished=0
    print_info "3.5 공개 상태 필터링 - isPublished=0"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?isPublished=0" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "비공개 설명회 필터링 성공, ${total}개"
    else
        print_error "비공개 설명회 필터링 실패: $response"
    fi
    
    # 3.6 키워드 검색
    print_info "3.6 키워드 검색 - '수학'"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?q=수학" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "키워드 검색 성공, ${total}개"
    else
        print_error "키워드 검색 실패: $response"
    fi
    
    # 3.7 복합 조건 검색
    print_info "3.7 복합 조건 검색 - MIDDLE + 공개 + 키워드"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?division=MIDDLE&isPublished=1&q=수학&page=0&size=10" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        local page=$(echo "$response" | jq -r '.page')
        local size=$(echo "$response" | jq -r '.size')
        print_success "복합 조건 검색 성공, ${total}개 (페이지: ${page}, 크기: ${size})"
    else
        print_error "복합 조건 검색 실패: $response"
    fi
    
    # 3.8 페이징 처리
    print_info "3.8 페이징 처리 테스트"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations?page=1&size=5&sort=createdAt,desc" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local items_count=$(echo "$response" | jq -r '.items | length')
        print_success "페이징 처리 성공, 현재 페이지 아이템 수: ${items_count}"
    else
        print_error "페이징 처리 실패: $response"
    fi
}

# 4. 설명회 상세 조회 API 테스트
test_get_explanation() {
    print_section "설명회 상세 조회 API 테스트"
    
    # 4.1 정상 케이스 - 존재하는 설명회 조회
    print_info "4.1 존재하는 설명회 상세 조회 (ID: $EXPLANATION_ID)"
    local response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/$EXPLANATION_ID" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        # 스케줄 ID 추출
        SCHEDULE_ID=$(echo "$response" | jq -r '.data.schedules[0].scheduleId')
        local title=$(echo "$response" | jq -r '.data.title')
        local schedules_count=$(echo "$response" | jq -r '.data.schedules | length')
        print_success "설명회 상세 조회 성공"
        print_info "제목: $title"
        print_info "회차 수: $schedules_count"
        print_info "첫 번째 스케줄 ID: $SCHEDULE_ID"
    else
        print_error "설명회 상세 조회 실패: $response"
    fi
    
    # 4.2 에러 케이스 - 존재하지 않는 설명회 조회
    print_info "4.2 존재하지 않는 설명회 조회"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/999999" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if echo "$response" | jq -e '.success' >/dev/null 2>&1; then
        local success=$(echo "$response" | jq -r '.success')
        if [ "$success" = "false" ]; then
            print_success "존재하지 않는 설명회 에러 테스트 성공"
        else
            print_error "존재하지 않는 설명회 에러 테스트 실패"
        fi
    else
        print_warning "에러 응답 형식이 다름: $response"
    fi
}

# 5. 설명회 수정 API 테스트
test_update_explanation() {
    print_section "설명회 수정 API 테스트"
    
    # 5.1 정상 케이스 - 제목과 내용 수정
    print_info "5.1 설명회 수정 - 제목과 내용"
    local response=$(curl -s -X PUT "$BASE_URL/api/admin/explanations/$EXPLANATION_ID" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "title": "중등부 수학 설명회 - 수정된 제목",
            "content": "수정된 상세 내용입니다.<br><strong>변경사항:</strong><ul><li>새로운 커리큘럼 추가</li><li>강사진 업데이트</li></ul>",
            "isPublished": true
        }')
    
    if check_response "$response" "Success"; then
        print_success "설명회 수정 성공"
    else
        print_error "설명회 수정 실패: $response"
    fi
}

# 6. 설명회 공개/비공개 전환 테스트
test_toggle_published() {
    print_section "설명회 공개/비공개 전환 테스트"
    
    # 6.1 공개 → 비공개 전환
    print_info "6.1 공개 → 비공개 전환"
    local response=$(curl -s -X PATCH "$BASE_URL/api/admin/explanations/$EXPLANATION_ID/published" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        print_success "비공개 전환 성공"
    else
        print_error "비공개 전환 실패: $response"
    fi
    
    # 6.2 비공개 → 공개 전환
    print_info "6.2 비공개 → 공개 전환"
    response=$(curl -s -X PATCH "$BASE_URL/api/admin/explanations/$EXPLANATION_ID/published" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        print_success "공개 전환 성공"
    else
        print_error "공개 전환 실패: $response"
    fi
}

# 7. 회차 생성 API 테스트
test_create_schedule() {
    print_section "회차 생성 API 테스트"
    
    # 7.1 정상 케이스 - 2차 회차 생성
    print_info "7.1 2차 회차 생성"
    local response=$(curl -s -X POST "$BASE_URL/api/admin/explanations/$EXPLANATION_ID/schedules" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "roundNo": 2,
            "startAt": "2026-02-08 14:00:00",
            "endAt": "2026-02-08 16:00:00",
            "location": "강의실 B",
            "applyStartAt": "2026-01-28 00:00:00",
            "applyEndAt": "2026-02-07 23:59:00",
            "status": "RESERVABLE",
            "capacity": 25
        }')
    
    if check_response "$response" "Success"; then
        print_success "2차 회차 생성 성공"
    else
        print_error "2차 회차 생성 실패: $response"
    fi
    
    # 7.2 에러 케이스 - 시간 논리 오류 (endAt < startAt)
    print_info "7.2 에러 케이스 - 종료시간이 시작시간보다 이른 경우"
    response=$(curl -s -X POST "$BASE_URL/api/admin/explanations/$EXPLANATION_ID/schedules" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "roundNo": 3,
            "startAt": "2026-02-10 16:00:00",
            "endAt": "2026-02-10 14:00:00",
            "location": "강의실 C",
            "applyStartAt": "2026-02-01 00:00:00",
            "applyEndAt": "2026-02-09 23:59:00",
            "status": "RESERVABLE",
            "capacity": 20
        }')
    
    if echo "$response" | jq -e '.success' >/dev/null 2>&1; then
        local success=$(echo "$response" | jq -r '.success')
        if [ "$success" = "false" ]; then
            print_success "시간 논리 오류 에러 테스트 성공"
        else
            print_error "시간 논리 오류 에러 테스트 실패"
        fi
    else
        print_warning "에러 응답 형식이 다름: $response"
    fi
}

# 8. 예약 신청 API 테스트
test_create_reservation() {
    print_section "예약 신청 API 테스트"
    
    # 8.1 정상 케이스 - 완전한 정보로 예약 신청
    print_info "8.1 완전한 정보로 예약 신청 (스케줄 ID: $SCHEDULE_ID)"
    local response=$(curl -s -X POST "$BASE_URL/api/explanations/reservations" \
        -H "Content-Type: application/json" \
        -d "{
            \"scheduleId\": $SCHEDULE_ID,
            \"applicantName\": \"김학부모\",
            \"applicantPhone\": \"010-1234-5678\",
            \"studentName\": \"김학생\",
            \"studentPhone\": \"010-8765-4321\",
            \"gender\": \"M\",
            \"academicTrack\": \"SCIENCE\",
            \"schoolName\": \"테스트고등학교\",
            \"grade\": \"2\",
            \"memo\": \"수학에 관심이 많은 학생입니다. 심화 과정 문의드립니다.\",
            \"isMarketingAgree\": false
        }")
    
    if check_response "$response" "Success"; then
        RESERVATION_ID=$(echo "$response" | jq -r '.data')
        print_success "예약 신청 성공, ID: $RESERVATION_ID"
    else
        print_error "예약 신청 실패: $response"
    fi
    
    # 8.2 정상 케이스 - 최소 필수 정보만으로 예약 신청
    print_info "8.2 최소 필수 정보로 예약 신청"
    response=$(curl -s -X POST "$BASE_URL/api/explanations/reservations" \
        -H "Content-Type: application/json" \
        -d "{
            \"scheduleId\": $SCHEDULE_ID,
            \"applicantName\": \"박학부모\",
            \"applicantPhone\": \"010-2345-6789\"
        }")
    
    if check_response "$response" "Success"; then
        print_success "최소 정보 예약 신청 성공"
    else
        print_error "최소 정보 예약 신청 실패: $response"
    fi
    
    # 8.3 에러 케이스 - 중복 예약 시도
    print_info "8.3 에러 케이스 - 중복 예약 시도"
    response=$(curl -s -X POST "$BASE_URL/api/explanations/reservations" \
        -H "Content-Type: application/json" \
        -d "{
            \"scheduleId\": $SCHEDULE_ID,
            \"applicantName\": \"김학부모\",
            \"applicantPhone\": \"010-1234-5678\"
        }")
    
    if echo "$response" | jq -e '.success' >/dev/null 2>&1; then
        local success=$(echo "$response" | jq -r '.success')
        if [ "$success" = "false" ]; then
            print_success "중복 예약 에러 테스트 성공"
        else
            print_error "중복 예약 에러 테스트 실패"
        fi
    else
        print_warning "에러 응답 형식이 다름: $response"
    fi
}

# 9. 예약 조회 API 테스트
test_lookup_reservations() {
    print_section "예약 조회 API 테스트"
    
    # 9.1 전화번호로 예약 내역 조회
    print_info "9.1 전화번호로 예약 내역 조회"
    local response=$(curl -s -X GET "$BASE_URL/api/explanations/reservations/lookup?applicantPhone=010-1234-5678")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "예약 조회 성공, 총 ${total}개"
    else
        print_error "예약 조회 실패: $response"
    fi
    
    # 9.2 존재하지 않는 전화번호
    print_info "9.2 존재하지 않는 전화번호로 조회"
    response=$(curl -s -X GET "$BASE_URL/api/explanations/reservations/lookup?applicantPhone=010-9999-9999")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "존재하지 않는 전화번호 조회 성공, ${total}개"
    else
        print_error "존재하지 않는 전화번호 조회 실패: $response"
    fi
}

# 10. 관리자 예약 관리 API 테스트
test_admin_reservations() {
    print_section "관리자 예약 관리 API 테스트"
    
    # 10.1 전체 예약 목록 조회
    print_info "10.1 전체 예약 목록 조회"
    local response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "전체 예약 목록 조회 성공, 총 ${total}개"
    else
        print_error "전체 예약 목록 조회 실패: $response"
    fi
    
    # 10.2 설명회별 예약 필터링
    print_info "10.2 설명회별 예약 필터링"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations?explanationId=$EXPLANATION_ID" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "설명회별 예약 필터링 성공, ${total}개"
    else
        print_error "설명회별 예약 필터링 실패: $response"
    fi
    
    # 10.3 키워드 검색 (신청자명, 학생명)
    print_info "10.3 키워드 검색 - '김학'"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations?keyword=김학" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "키워드 검색 성공, ${total}개"
    else
        print_error "키워드 검색 실패: $response"
    fi
    
    # 10.4 예약 상태 필터링
    print_info "10.4 예약 상태 필터링 - CONFIRMED"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations?status=CONFIRMED" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "상태 필터링 성공, ${total}개"
    else
        print_error "상태 필터링 실패: $response"
    fi
    
    # 10.5 예약 상세 조회
    if [ -n "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
        print_info "10.5 예약 상세 조회 (ID: $RESERVATION_ID)"
        response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations/$RESERVATION_ID" \
            -H "Authorization: Bearer $JWT_TOKEN")
        
        if check_response "$response" "Success"; then
            local applicant_name=$(echo "$response" | jq -r '.data.applicantName')
            print_success "예약 상세 조회 성공, 신청자: $applicant_name"
        else
            print_error "예약 상세 조회 실패: $response"
        fi
    else
        print_warning "예약 ID가 없어 상세 조회를 건너뜁니다"
    fi
}

# 11. 예약 통계 조회 API 테스트
test_reservation_statistics() {
    print_section "예약 통계 조회 API 테스트"
    
    # 11.1 전체 통계
    print_info "11.1 전체 예약 통계 조회"
    local response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations/statistics" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        print_success "전체 통계 조회 성공"
    else
        print_error "전체 통계 조회 실패: $response"
    fi
    
    # 11.2 특정 설명회 통계
    print_info "11.2 특정 설명회 통계 조회 (ID: $EXPLANATION_ID)"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations/reservations/statistics?explanationId=$EXPLANATION_ID" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if check_response "$response" "Success"; then
        print_success "특정 설명회 통계 조회 성공"
    else
        print_error "특정 설명회 통계 조회 실패: $response"
    fi
}

# 12. 공개 API 테스트
test_public_apis() {
    print_section "공개 API 테스트"
    
    # 12.1 공개 설명회 목록 조회
    print_info "12.1 공개 설명회 목록 조회"
    local response=$(curl -s -X GET "$BASE_URL/api/explanations")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "공개 설명회 목록 조회 성공, 총 ${total}개"
    else
        print_error "공개 설명회 목록 조회 실패: $response"
    fi
    
    # 12.2 공개 설명회 상세 조회
    print_info "12.2 공개 설명회 상세 조회 (ID: $EXPLANATION_ID)"
    response=$(curl -s -X GET "$BASE_URL/api/explanations/$EXPLANATION_ID")
    
    if check_response "$response" "Success"; then
        print_success "공개 설명회 상세 조회 성공 (조회수 증가됨)"
    else
        print_error "공개 설명회 상세 조회 실패: $response"
    fi
    
    # 12.3 division 필터링
    print_info "12.3 공개 API division 필터링"
    response=$(curl -s -X GET "$BASE_URL/api/explanations?division=MIDDLE")
    
    if check_response "$response" "Success"; then
        local total=$(echo "$response" | jq -r '.total')
        print_success "공개 API division 필터링 성공, ${total}개"
    else
        print_error "공개 API division 필터링 실패: $response"
    fi
}

# 13. 에러 케이스 및 경계값 테스트
test_error_cases() {
    print_section "에러 케이스 및 경계값 테스트"
    
    # 13.1 인증 토큰 없이 관리자 API 접근
    print_info "13.1 인증 토큰 없이 관리자 API 접근"
    local response=$(curl -s -X GET "$BASE_URL/api/admin/explanations")
    
    if echo "$response" | grep -q "401\|Unauthorized"; then
        print_success "인증 토큰 없음 에러 테스트 성공"
    else
        print_warning "인증 에러 응답: $response"
    fi
    
    # 13.2 잘못된 토큰으로 접근
    print_info "13.2 잘못된 토큰으로 접근"
    response=$(curl -s -X GET "$BASE_URL/api/admin/explanations" \
        -H "Authorization: Bearer invalid_token")
    
    if echo "$response" | grep -q "401\|Unauthorized"; then
        print_success "잘못된 토큰 에러 테스트 성공"
    else
        print_warning "잘못된 토큰 에러 응답: $response"
    fi
    
    # 13.3 존재하지 않는 리소스 접근
    print_info "13.3 존재하지 않는 설명회 접근"
    response=$(curl -s -X GET "$BASE_URL/api/explanations/999999")
    
    if echo "$response" | grep -q "404\|Not Found"; then
        print_success "존재하지 않는 리소스 에러 테스트 성공"
    else
        print_warning "존재하지 않는 리소스 에러 응답: $response"
    fi
}

# 14. 예약 취소 테스트
test_cancel_reservation() {
    print_section "예약 취소 테스트"
    
    if [ -n "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
        # 14.1 사용자 예약 취소
        print_info "14.1 사용자 예약 취소 (ID: $RESERVATION_ID)"
        local response=$(curl -s -X POST "$BASE_URL/api/explanations/reservations/$RESERVATION_ID/cancel")
        
        if check_response "$response" "Success"; then
            print_success "사용자 예약 취소 성공"
        else
            print_error "사용자 예약 취소 실패: $response"
        fi
        
        # 14.2 이미 취소된 예약 재취소 (멱등성 테스트)
        print_info "14.2 이미 취소된 예약 재취소 (멱등성 테스트)"
        response=$(curl -s -X POST "$BASE_URL/api/explanations/reservations/$RESERVATION_ID/cancel")
        
        if check_response "$response" "Success"; then
            print_success "멱등성 테스트 성공 (재취소 허용)"
        else
            print_warning "멱등성 테스트 결과: $response"
        fi
    else
        print_warning "예약 ID가 없어 취소 테스트를 건너뜁니다"
    fi
}

# 메인 테스트 실행
main() {
    echo "🎯 테스트 대상: 설명회 API"
    echo "🏠 환경: $BASE_URL"
    echo ""
    
    login_and_get_token
    test_create_explanation
    test_list_explanations
    test_get_explanation
    test_update_explanation
    test_toggle_published
    test_create_schedule
    test_create_reservation
    test_lookup_reservations
    test_admin_reservations
    test_reservation_statistics
    test_public_apis
    test_error_cases
    test_cancel_reservation
    
    print_section "테스트 완료"
    print_success "🎉 모든 테스트가 완료되었습니다!"
    
    if [ -n "$EXPLANATION_ID" ]; then
        print_info "생성된 설명회 ID: $EXPLANATION_ID"
    fi
    if [ -n "$SCHEDULE_ID" ]; then
        print_info "스케줄 ID: $SCHEDULE_ID"
    fi
    if [ -n "$RESERVATION_ID" ]; then
        print_info "예약 ID: $RESERVATION_ID"
    fi
}

# jq 설치 확인
if ! command -v jq &> /dev/null; then
    print_error "jq가 설치되어 있지 않습니다. 'brew install jq'로 설치해주세요."
    exit 1
fi

# 스크립트 실행
main "$@"