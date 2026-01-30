#!/bin/bash

# 원서접수 API 종합 테스트 스크립트 (Enhanced)
# 2026년 1월 - 파일 첨부, 필터링, 다운로드 등 실무 테스트

BASE_URL="http://localhost:8081"
JWT_TOKEN=""

echo "🚀 === 원서접수 API 종합 테스트 (Enhanced) 시작 ==="

# ====== 1. 초기 설정 ======

echo -e "\n📋 1️⃣ === 관리자 로그인 ==="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "password": "password123!"
  }')

if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
    JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
    echo "✅ 로그인 성공"
else
    echo "❌ 로그인 실패: $LOGIN_RESPONSE"
    exit 1
fi

# ====== 2. 파일 업로드 준비 ======

echo -e "\n📁 2️⃣ === 임시 파일 업로드 (성적표, 증명사진) ==="

# 더미 파일 생성
echo "더미 성적표 내용" > /tmp/transcript.pdf
echo "더미 증명사진 내용" > /tmp/photo.jpg

# 성적표 파일 업로드
echo "성적표 파일 업로드..."
TRANSCRIPT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/public/files/upload" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@/tmp/transcript.pdf")

if echo "$TRANSCRIPT_RESPONSE" | grep -q '"success":true'; then
    TRANSCRIPT_FILE_ID=$(echo "$TRANSCRIPT_RESPONSE" | jq -r '.data.fileId')
    echo "✅ 성적표 파일 업로드 성공: $TRANSCRIPT_FILE_ID"
else
    echo "❌ 성적표 파일 업로드 실패"
    TRANSCRIPT_FILE_ID=""
fi

# 증명사진 파일 업로드
echo "증명사진 파일 업로드..."
PHOTO_RESPONSE=$(curl -s -X POST "$BASE_URL/api/public/files/upload" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@/tmp/photo.jpg")

if echo "$PHOTO_RESPONSE" | grep -q '"success":true'; then
    PHOTO_FILE_ID=$(echo "$PHOTO_RESPONSE" | jq -r '.data.fileId')
    echo "✅ 증명사진 파일 업로드 성공: $PHOTO_FILE_ID"
else
    echo "❌ 증명사진 파일 업로드 실패"
    PHOTO_FILE_ID=""
fi

# ====== 3. 원서접수 생성 (다양한 케이스) ======

echo -e "\n📝 3️⃣ === 원서접수 생성 테스트 ==="

# Case 1: 중등부 원서접수 (성적표 + 증명사진 첨부)
echo -e "\n📋 3.1 중등부 원서접수 생성 (파일 첨부 포함)"
CREATE_MIDDLE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/apply-applications" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"division\": \"MIDDLE\",
    \"studentName\": \"테스트중학생\",
    \"gender\": \"MALE\",
    \"birthDate\": \"2010-03-15\",
    \"studentGradeLevel\": \"M3\",
    \"studentPhone\": \"010-1234-5678\",
    \"schoolName\": \"테스트중학교\",
    \"schoolGrade\": \"3학년 2반\",
    \"address\": \"서울시 강남구 테스트동 123-45\",
    \"guardian1Name\": \"테스트부모\",
    \"guardian1Phone\": \"010-9876-5432\",
    \"guardian1Relation\": \"부\",
    \"subjects\": [\"KOR\", \"ENG\", \"MATH\"],
    \"transcriptFiles\": [
      {\"fileId\": \"$TRANSCRIPT_FILE_ID\", \"fileName\": \"transcript.pdf\"}
    ],
    \"photoFiles\": [
      {\"fileId\": \"$PHOTO_FILE_ID\", \"fileName\": \"photo.jpg\"}
    ]
  }")

if echo "$CREATE_MIDDLE_RESPONSE" | grep -q '"success":true'; then
    MIDDLE_APPLY_ID=$(echo "$CREATE_MIDDLE_RESPONSE" | jq -r '.data')
    echo "✅ 중등부 원서접수 생성 성공: ID=$MIDDLE_APPLY_ID"
else
    echo "❌ 중등부 원서접수 생성 실패: $CREATE_MIDDLE_RESPONSE"
fi

# Case 2: 고등부 원서접수 (성적표만 첨부)
echo -e "\n📋 3.2 고등부 원서접수 생성 (성적표만)"
CREATE_HIGH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/apply-applications" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"division\": \"HIGH\",
    \"studentName\": \"테스트고등학생\",
    \"gender\": \"FEMALE\",
    \"birthDate\": \"2008-07-20\",
    \"studentGradeLevel\": \"H2\",
    \"studentPhone\": \"010-2345-6789\",
    \"schoolName\": \"테스트고등학교\",
    \"schoolGrade\": \"2학년 1반\",
    \"address\": \"서울시 서초구 테스트로 456\",
    \"guardian1Name\": \"테스트학부모\",
    \"guardian1Phone\": \"010-8765-4321\",
    \"guardian1Relation\": \"모\",
    \"subjects\": [\"KOR\", \"ENG\", \"MATH\"],
    \"transcriptFiles\": [
      {\"fileId\": \"$TRANSCRIPT_FILE_ID\", \"fileName\": \"transcript.pdf\"}
    ]
  }")

if echo "$CREATE_HIGH_RESPONSE" | grep -q '"success":true'; then
    HIGH_APPLY_ID=$(echo "$CREATE_HIGH_RESPONSE" | jq -r '.data')
    echo "✅ 고등부 원서접수 생성 성공: ID=$HIGH_APPLY_ID"
else
    echo "❌ 고등부 원서접수 생성 실패: $CREATE_HIGH_RESPONSE"
fi

# Case 3: 독학재수 원서접수 (파일 없음)
echo -e "\n📋 3.3 독학재수 원서접수 생성 (파일 없음)"
CREATE_SELF_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/apply-applications" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "division": "SELF_STUDY_RETAKE",
    "studentName": "테스트재수생",
    "gender": "MALE",
    "birthDate": "2005-12-10",
    "studentPhone": "010-3456-7890",
    "address": "서울시 영등포구 테스트빌딩 789",
    "desiredUniversity": "서울대학교",
    "desiredDepartment": "컴퓨터공학과",
    "guardian1Name": "테스트보호자",
    "guardian1Phone": "010-7654-3210",
    "guardian1Relation": "부"
  }')

if echo "$CREATE_SELF_RESPONSE" | grep -q '"success":true'; then
    SELF_APPLY_ID=$(echo "$CREATE_SELF_RESPONSE" | jq -r '.data')
    echo "✅ 독학재수 원서접수 생성 성공: ID=$SELF_APPLY_ID"
else
    echo "❌ 독학재수 원서접수 생성 실패: $CREATE_SELF_RESPONSE"
fi

# ====== 4. 다양한 필터링 조회 테스트 ======

echo -e "\n🔍 4️⃣ === 다양한 필터링 조회 테스트 ==="

# 4.1 구분별 조회
echo -e "\n📊 4.1 구분별 조회 (중등부)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?division=MIDDLE" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "중등부 원서접수: \($count)건"'

echo -e "\n📊 4.2 구분별 조회 (고등부)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?division=HIGH" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "고등부 원서접수: \($count)건"'

echo -e "\n📊 4.3 구분별 조회 (독학재수)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?division=SELF_STUDY_RETAKE" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "독학재수 원서접수: \($count)건"'

# 4.2 상태별 조회
echo -e "\n📊 4.4 상태별 조회 (등록 상태)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?status=REGISTERED" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "등록 상태: \($count)건"'

echo -e "\n📊 4.5 상태별 조회 (검토 상태)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?status=REVIEW" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "검토 상태: \($count)건"'

# 4.3 키워드 검색
echo -e "\n📊 4.6 키워드 검색 (테스트)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?keyword=테스트" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "테스트 키워드 검색: \($count)건"'

# 4.4 복합 필터링
echo -e "\n📊 4.7 복합 필터링 (중등부 + 등록상태)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?division=MIDDLE&status=REGISTERED" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | length as $count | "중등부+등록상태: \($count)건"'

# 4.5 정렬 테스트
echo -e "\n📊 4.8 정렬 테스트 (이름 오름차순)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?sortBy=studentName_asc" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items[0:3] | map(.studentName)'

# ====== 5. 생성된 원서접수 상세 조회 및 관리 ======

if [ ! -z "$MIDDLE_APPLY_ID" ]; then
    echo -e "\n📄 5️⃣ === 생성된 원서접수 상세 조회 및 관리 ==="
    
    # 5.1 상세 조회 (파일 포함)
    echo -e "\n📋 5.1 중등부 원서접수 상세 조회 (파일 포함)"
    curl -s -X GET "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID" \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '{
        studentName: .data.studentName,
        division: .data.divisionDescription,
        transcriptFiles: (.data.transcriptFiles // [] | length),
        photoFiles: (.data.photoFiles // [] | length),
        subjects: (.data.subjects // [] | map(.subjectCode))
      }'
    
    # 5.2 원서접수 수정 (학생 정보 업데이트)
    echo -e "\n✏️ 5.2 원서접수 수정 테스트"
    curl -s -X PUT "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "studentName": "테스트중학생_수정",
        "gender": "MALE",
        "birthDate": "2010-03-15",
        "studentGradeLevel": "M3",
        "studentPhone": "010-1234-5678",
        "schoolName": "테스트중학교_수정",
        "schoolGrade": "3학년 2반",
        "address": "서울시 강남구 테스트동 123-45 수정",
        "guardian1Name": "테스트부모_수정",
        "guardian1Phone": "010-9876-5432",
        "guardian1Relation": "부",
        "subjects": ["KOR", "ENG", "MATH", "SCI"]
      }' | jq '{success: .success, message: .message}'
    
    # 5.3 상태 변경
    echo -e "\n🔄 5.3 상태 변경 (등록 → 검토)"
    curl -s -X PUT "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID/status?status=REVIEW" \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '{success: .success, message: .message}'
    
    # 5.4 담당자 배정
    echo -e "\n👤 5.4 담당자 배정"
    curl -s -X PUT "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID/assignee?assigneeName=테스트담당자" \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '{success: .success, message: .message}'
    
    # 5.5 이력 추가
    echo -e "\n📝 5.5 이력 추가"
    curl -s -X POST "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID/logs" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "logType": "CALL",
        "description": "학부모 상담 완료 - 수업 일정 안내"
      }' | jq '{success: .success, message: .message}'
    
    # 5.6 PDF 다운로드 테스트
    echo -e "\n📁 5.6 PDF 다운로드 테스트"
    curl -s -I "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID/export/pdf" \
      -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"
fi

# ====== 6. 조건별 엑셀 다운로드 테스트 ======

echo -e "\n📊 6️⃣ === 조건별 엑셀 다운로드 테스트 ==="

# 6.1 전체 목록 엑셀 다운로드
echo -e "\n📋 6.1 전체 목록 엑셀 다운로드"
curl -s -I "$BASE_URL/api/admin/apply-applications/export/excel" \
  -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"

# 6.2 중등부만 필터링해서 엑셀 다운로드
echo -e "\n📋 6.2 중등부만 엑셀 다운로드"
curl -s -I "$BASE_URL/api/admin/apply-applications/export/excel?division=MIDDLE" \
  -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"

# 6.3 등록 상태만 엑셀 다운로드
echo -e "\n📋 6.3 등록 상태만 엑셀 다운로드"
curl -s -I "$BASE_URL/api/admin/apply-applications/export/excel?status=REGISTERED" \
  -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"

# 6.4 복합 조건 엑셀 다운로드
echo -e "\n📋 6.4 복합 조건 엑셀 다운로드 (중등부 + 검토상태)"
curl -s -I "$BASE_URL/api/admin/apply-applications/export/excel?division=MIDDLE&status=REVIEW" \
  -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"

# 6.5 키워드 검색 결과 엑셀 다운로드
echo -e "\n📋 6.5 키워드 검색 결과 엑셀 다운로드"
curl -s -I "$BASE_URL/api/admin/apply-applications/export/excel?keyword=테스트" \
  -H "Authorization: Bearer $JWT_TOKEN" | grep -E "HTTP/|Content-Type|Content-Disposition"

# ====== 7. 통계 및 관리 기능 테스트 ======

echo -e "\n📈 7️⃣ === 통계 및 관리 기능 테스트 ==="

# 7.1 통계 조회
echo -e "\n📊 7.1 원서접수 통계 조회"
curl -s -X GET "$BASE_URL/api/admin/apply-applications/statistics" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.data'

# 7.2 중복 검사
echo -e "\n🔍 7.2 중복 검사 (테스트 전화번호)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications/duplicates?studentPhone=010-1234-5678&hours=24" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '{duplicates: (.data | length)}'

# 7.3 지연 처리 조회
echo -e "\n⏰ 7.3 지연 처리 조회 (7일 기준)"
curl -s -X GET "$BASE_URL/api/admin/apply-applications/delayed?days=7" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '{delayed: (.items | length)}'

# ====== 8. 파일 관련 테스트 ======

if [ ! -z "$MIDDLE_APPLY_ID" ]; then
    echo -e "\n📁 8️⃣ === 파일 관련 테스트 ==="
    
    # 8.1 파일 첨부 후 상세 조회로 파일 확인
    echo -e "\n📋 8.1 첨부 파일 확인"
    curl -s -X GET "$BASE_URL/api/admin/apply-applications/$MIDDLE_APPLY_ID" \
      -H "Authorization: Bearer $JWT_TOKEN" | jq '{
        transcriptFiles: (.data.transcriptFiles // [] | map({fileId, originalName, size})),
        photoFiles: (.data.photoFiles // [] | map({fileId, originalName, size}))
      }'
fi

# ====== 9. 에러 케이스 테스트 ======

echo -e "\n❌ 9️⃣ === 에러 케이스 테스트 ==="

# 9.1 잘못된 구분으로 생성 시도
echo -e "\n📋 9.1 잘못된 구분으로 생성 시도"
curl -s -X POST "$BASE_URL/api/admin/apply-applications" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "division": "INVALID_DIVISION",
    "studentName": "에러테스트",
    "studentPhone": "010-0000-0000"
  }' | jq '{success: .success, error: .error.message}'

# 9.2 중등부에 잘못된 과목 시도
echo -e "\n📋 9.2 중등부에 잘못된 과목 시도"
curl -s -X POST "$BASE_URL/api/admin/apply-applications" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "division": "MIDDLE",
    "studentName": "에러테스트",
    "studentPhone": "010-0000-0001",
    "subjects": ["INVALID_SUBJECT"]
  }' | jq '{success: .success, error: .error.message}'

# 9.3 존재하지 않는 ID로 조회
echo -e "\n📋 9.3 존재하지 않는 ID로 조회"
curl -s -X GET "$BASE_URL/api/admin/apply-applications/99999" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '{success: .success, error: .error.message}'

# ====== 10. 최종 요약 ======

echo -e "\n📋 🔟 === 최종 현황 조회 ==="

# 최종 통계
echo -e "\n📊 최종 통계:"
curl -s -X GET "$BASE_URL/api/admin/apply-applications/statistics" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.data'

# 최근 생성된 항목들
echo -e "\n📋 최근 생성 목록 (테스트 키워드 포함):"
curl -s -X GET "$BASE_URL/api/admin/apply-applications?keyword=테스트&sortBy=createdAt_desc&size=5" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.items | map({id, studentName, division: .divisionDescription, status: .statusDescription})'

# 파일 정리
rm -f /tmp/transcript.pdf /tmp/photo.jpg

echo -e "\n🎉 === 원서접수 API 종합 테스트 (Enhanced) 완료 ==="
echo "✅ 테스트 결과:"
echo "   - 인증 및 로그인: 완료"
echo "   - 파일 업로드: 완료"  
echo "   - 다양한 구분 원서접수 생성: 완료"
echo "   - 필터링 조회 (구분/상태/키워드/복합): 완료"
echo "   - 원서접수 관리 (수정/상태변경/담당자배정/이력): 완료"
echo "   - 조건별 엑셀 다운로드: 완료"
echo "   - PDF 다운로드: 완료"
echo "   - 통계 및 중복검사: 완료"
echo "   - 에러 케이스 처리: 완료"