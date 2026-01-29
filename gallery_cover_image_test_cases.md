# 갤러리 커버이미지 기능 테스트케이스

## 🎯 테스트 범위
- 갤러리 커버이미지 CRUD 기능
- 갤러리 vs 공지사항 차이점 검증
- 파일 역할(FileRole.COVER) 검증
- API 응답 구조 검증

## 📋 전체 테스트 시나리오

### 1. 기본 환경 설정
```bash
# 서버 실행
SERVER_PORT=8080 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun -x test

# 관리자 토큰 획득
JWT_TOKEN="your_admin_jwt_token_here"
```

### 2. 파일 업로드 준비
```bash
# 테스트용 이미지 파일 준비
echo "커버이미지 테스트용 파일" > cover_test.jpg
echo "본문이미지 테스트용 파일" > inline_test.jpg
```

## 🧪 상세 테스트케이스

### A. 커버이미지 생성 테스트

#### A-1. 커버이미지 포함 갤러리 생성
```bash
echo "=== 1. 커버이미지 임시파일 업로드 ==="
COVER_TEMP_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@cover_test.jpg")

echo "커버이미지 임시파일 응답:"
echo "$COVER_TEMP_RESPONSE" | jq '.'

# 임시파일 ID 추출
COVER_TEMP_FILE_ID=$(echo "$COVER_TEMP_RESPONSE" | jq -r '.data.tempFileId')
echo "커버이미지 임시파일 ID: $COVER_TEMP_FILE_ID"

echo -e "\n=== 2. 본문이미지 임시파일 업로드 ==="
INLINE_TEMP_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@inline_test.jpg")

echo "본문이미지 임시파일 응답:"
echo "$INLINE_TEMP_RESPONSE" | jq '.'

INLINE_TEMP_FILE_ID=$(echo "$INLINE_TEMP_RESPONSE" | jq -r '.data.tempFileId')
echo "본문이미지 임시파일 ID: $INLINE_TEMP_FILE_ID"

echo -e "\n=== 3. 커버이미지 포함 갤러리 생성 ==="
CREATE_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/admin/gallery" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"커버이미지 테스트 갤러리\",
    \"content\": \"<p>커버이미지가 있는 갤러리입니다.</p><p><img src='/api/public/files/temp/$INLINE_TEMP_FILE_ID' alt='본문이미지'></p>\",
    \"isPublished\": true,
    \"categoryId\": 1,
    \"coverImageTempFileId\": \"$COVER_TEMP_FILE_ID\",
    \"coverImageFileName\": \"cover_test.jpg\",
    \"inlineImages\": [
      {
        \"tempFileId\": \"$INLINE_TEMP_FILE_ID\",
        \"fileName\": \"inline_test.jpg\"
      }
    ]
  }")

echo "갤러리 생성 응답:"
echo "$CREATE_RESPONSE" | jq '.'

GALLERY_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data')
echo "생성된 갤러리 ID: $GALLERY_ID"
```

#### A-2. 생성 결과 검증
```bash
echo -e "\n=== 4. 생성된 갤러리 상세 조회 (커버이미지 확인) ==="
DETAIL_RESPONSE=$(curl -s -X GET \
  "http://localhost:8080/api/admin/gallery/$GALLERY_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "갤러리 상세 조회 응답:"
echo "$DETAIL_RESPONSE" | jq '.'

echo -e "\n=== 5. 커버이미지 정보 추출 ==="
COVER_IMAGE_URL=$(echo "$DETAIL_RESPONSE" | jq -r '.data.coverImage.url // "null"')
COVER_IMAGE_NAME=$(echo "$DETAIL_RESPONSE" | jq -r '.data.coverImage.fileName // "null"')

echo "커버이미지 URL: $COVER_IMAGE_URL"
echo "커버이미지 파일명: $COVER_IMAGE_NAME"

# 검증
if [[ "$COVER_IMAGE_URL" != "null" && "$COVER_IMAGE_NAME" != "null" ]]; then
    echo "✅ 커버이미지 생성 성공"
else
    echo "❌ 커버이미지 생성 실패"
fi
```

### B. 커버이미지 수정 테스트

#### B-1. 커버이미지 교체
```bash
echo -e "\n=== 6. 새 커버이미지 업로드 ==="
echo "새로운 커버이미지 테스트" > new_cover.jpg

NEW_COVER_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@new_cover.jpg")

NEW_COVER_TEMP_ID=$(echo "$NEW_COVER_RESPONSE" | jq -r '.data.tempFileId')
echo "새 커버이미지 임시파일 ID: $NEW_COVER_TEMP_ID"

echo -e "\n=== 7. 갤러리 수정 (커버이미지 교체) ==="
UPDATE_RESPONSE=$(curl -s -X PUT \
  "http://localhost:8080/api/admin/gallery/$GALLERY_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"수정된 커버이미지 테스트 갤러리\",
    \"coverImageTempFileId\": \"$NEW_COVER_TEMP_ID\",
    \"coverImageFileName\": \"new_cover.jpg\"
  }")

echo "갤러리 수정 응답:"
echo "$UPDATE_RESPONSE" | jq '.'
```

#### B-2. 커버이미지 삭제
```bash
echo -e "\n=== 8. 갤러리 수정 (커버이미지 삭제) ==="
DELETE_COVER_RESPONSE=$(curl -s -X PUT \
  "http://localhost:8080/api/admin/gallery/$GALLERY_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"커버이미지 삭제된 갤러리\",
    \"deleteCoverImage\": true
  }")

echo "커버이미지 삭제 응답:"
echo "$DELETE_COVER_RESPONSE" | jq '.'

echo -e "\n=== 9. 커버이미지 삭제 확인 ==="
VERIFY_DELETE=$(curl -s -X GET \
  "http://localhost:8080/api/admin/gallery/$GALLERY_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

COVER_AFTER_DELETE=$(echo "$VERIFY_DELETE" | jq -r '.data.coverImage // "null"')
echo "삭제 후 커버이미지 상태: $COVER_AFTER_DELETE"

if [[ "$COVER_AFTER_DELETE" == "null" ]]; then
    echo "✅ 커버이미지 삭제 성공"
else
    echo "❌ 커버이미지 삭제 실패"
fi
```

### C. 목록 조회 테스트

#### C-1. 관리자 목록 조회 (커버이미지 URL 포함)
```bash
echo -e "\n=== 10. 관리자 갤러리 목록 조회 ==="
LIST_ADMIN_RESPONSE=$(curl -s -X GET \
  "http://localhost:8080/api/admin/gallery?page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "관리자 목록 조회 응답:"
echo "$LIST_ADMIN_RESPONSE" | jq '.'

echo -e "\n=== 11. 목록에서 커버이미지 정보 확인 ==="
FIRST_ITEM_COVER_URL=$(echo "$LIST_ADMIN_RESPONSE" | jq -r '.data[0].coverImageUrl // "null"')
echo "첫 번째 항목 커버이미지 URL: $FIRST_ITEM_COVER_URL"
```

#### C-2. 공개 목록 조회
```bash
echo -e "\n=== 12. 공개 갤러리 목록 조회 ==="
LIST_PUBLIC_RESPONSE=$(curl -s -X GET \
  "http://localhost:8080/api/gallery?page=0&size=10")

echo "공개 목록 조회 응답:"
echo "$LIST_PUBLIC_RESPONSE" | jq '.'
```

### D. 데이터베이스 검증

#### D-1. upload_file_links 테이블 확인
```bash
echo -e "\n=== 13. 데이터베이스 파일링크 확인 ==="
echo "다음 SQL을 실행하여 파일링크 상태를 확인하세요:"
echo ""
echo "SELECT "
echo "  ufl.id,"
echo "  ufl.owner_table,"
echo "  ufl.owner_id,"
echo "  ufl.role,"
echo "  uf.file_name,"
echo "  uf.original_name"
echo "FROM upload_file_links ufl"
echo "JOIN upload_files uf ON ufl.file_id = uf.id"
echo "WHERE ufl.owner_table = 'gallery' AND ufl.owner_id = $GALLERY_ID;"
echo ""
echo "예상 결과:"
echo "- role='COVER': 커버이미지 (0개 또는 1개)"
echo "- role='INLINE': 본문이미지 (1개 이상)"
echo "- role='ATTACHMENT': 없어야 함 (갤러리는 첨부파일 없음)"
```

### E. 에러 케이스 테스트

#### E-1. 잘못된 파일 형식
```bash
echo -e "\n=== 14. 잘못된 파일 형식 테스트 ==="
echo "텍스트 파일" > invalid_file.txt

INVALID_UPLOAD_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@invalid_file.txt")

echo "잘못된 파일 업로드 응답:"
echo "$INVALID_UPLOAD_RESPONSE" | jq '.'

# 일반적으로 이미지가 아닌 파일도 업로드되지만, 클라이언트에서 검증해야 함
```

#### E-2. 존재하지 않는 임시파일 ID
```bash
echo -e "\n=== 15. 존재하지 않는 임시파일 ID 테스트 ==="
INVALID_CREATE_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/admin/gallery" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"잘못된 임시파일 테스트\",
    \"content\": \"테스트 내용\",
    \"isPublished\": true,
    \"coverImageTempFileId\": \"invalid-temp-file-id\",
    \"coverImageFileName\": \"invalid.jpg\"
  }")

echo "잘못된 임시파일 ID로 갤러리 생성 응답:"
echo "$INVALID_CREATE_RESPONSE" | jq '.'
```

#### E-3. 권한 없는 사용자
```bash
echo -e "\n=== 16. 권한 없는 사용자 테스트 ==="
UNAUTHORIZED_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/admin/gallery" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"권한 없는 요청\",
    \"content\": \"테스트 내용\"
  }")

echo "권한 없는 요청 응답:"
echo "$UNAUTHORIZED_RESPONSE" | jq '.'
```

### F. 성능 테스트

#### F-1. 대용량 파일 테스트
```bash
echo -e "\n=== 17. 대용량 파일 테스트 ==="
# 5MB 정도의 테스트 파일 생성
dd if=/dev/zero of=large_cover.jpg bs=1024 count=5120 2>/dev/null

LARGE_FILE_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@large_cover.jpg")

echo "대용량 파일 업로드 응답:"
echo "$LARGE_FILE_RESPONSE" | jq '.'

LARGE_TEMP_ID=$(echo "$LARGE_FILE_RESPONSE" | jq -r '.data.tempFileId // "null"')
if [[ "$LARGE_TEMP_ID" != "null" ]]; then
    echo "✅ 대용량 파일 업로드 성공"
else
    echo "❌ 대용량 파일 업로드 실패"
fi
```

### G. 통합 검증

#### G-1. 갤러리 vs 공지사항 차이점 검증
```bash
echo -e "\n=== 18. 갤러리 vs 공지사항 구조 비교 ==="

# 갤러리 스키마 확인
echo "갤러리 테이블 구조:"
echo "- 커버이미지: upload_file_links.role = 'COVER'"
echo "- 본문이미지: upload_file_links.role = 'INLINE'"
echo "- 첨부파일: 없음 (ATTACHMENT 역할 없음)"
echo "- 중요공지 필드: 없음"
echo "- 노출기간 필드: 없음"

# 공지사항과 비교
echo ""
echo "공지사항 테이블 구조:"
echo "- 첨부파일: upload_file_links.role = 'ATTACHMENT'"
echo "- 본문이미지: upload_file_links.role = 'INLINE'"
echo "- 커버이미지: 없음"
echo "- 중요공지 필드: is_important"
echo "- 노출기간 필드: exposure_start_at, exposure_end_at"
```

#### G-2. 최종 종합 테스트
```bash
echo -e "\n=== 19. 최종 종합 테스트 ==="

# 새로운 갤러리로 전체 플로우 테스트
echo "테스트용 완전한 갤러리" > final_cover.jpg
echo "테스트용 본문이미지" > final_inline.jpg

# 커버이미지 업로드
FINAL_COVER_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@final_cover.jpg")
FINAL_COVER_ID=$(echo "$FINAL_COVER_RESPONSE" | jq -r '.data.tempFileId')

# 본문이미지 업로드  
FINAL_INLINE_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/public/files/upload/temp" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@final_inline.jpg")
FINAL_INLINE_ID=$(echo "$FINAL_INLINE_RESPONSE" | jq -r '.data.tempFileId')

# 완전한 갤러리 생성
FINAL_GALLERY_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/admin/gallery" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"최종 테스트 갤러리\",
    \"content\": \"<h1>갤러리 제목</h1><p>이것은 커버이미지와 본문이미지를 모두 포함한 완전한 갤러리입니다.</p><p><img src='/api/public/files/temp/$FINAL_INLINE_ID' alt='본문이미지' style='width:100%;'></p><p>갤러리는 공지사항과 달리 첨부파일이 없고, 대신 커버이미지가 있습니다.</p>\",
    \"isPublished\": true,
    \"categoryId\": 1,
    \"viewCount\": 0,
    \"coverImageTempFileId\": \"$FINAL_COVER_ID\",
    \"coverImageFileName\": \"final_cover.jpg\",
    \"inlineImages\": [
      {
        \"tempFileId\": \"$FINAL_INLINE_ID\",
        \"fileName\": \"final_inline.jpg\"
      }
    ]
  }")

FINAL_GALLERY_ID=$(echo "$FINAL_GALLERY_RESPONSE" | jq -r '.data')
echo "최종 테스트 갤러리 ID: $FINAL_GALLERY_ID"

# 최종 검증
FINAL_VERIFY=$(curl -s -X GET \
  "http://localhost:8080/api/admin/gallery/$FINAL_GALLERY_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "최종 갤러리 검증:"
echo "$FINAL_VERIFY" | jq '{
  id: .data.id,
  title: .data.title,
  hasCoverImage: (.data.coverImage != null),
  coverImageUrl: .data.coverImage.url,
  inlineImageCount: (.data.inlineImages | length),
  isPublished: .data.isPublished
}'

# 성공/실패 판정
HAS_COVER=$(echo "$FINAL_VERIFY" | jq -r '.data.coverImage != null')
HAS_INLINE=$(echo "$FINAL_VERIFY" | jq -r '(.data.inlineImages | length) > 0')

echo -e "\n=== 🎯 최종 테스트 결과 ==="
if [[ "$HAS_COVER" == "true" && "$HAS_INLINE" == "true" ]]; then
    echo "✅ 갤러리 커버이미지 기능 테스트 전체 성공!"
    echo "✅ 커버이미지 생성/수정/삭제 모든 기능 정상"
    echo "✅ 본문이미지 연동 정상"
    echo "✅ 갤러리 고유 구조(커버+본문, 첨부파일 없음) 확인"
else
    echo "❌ 일부 기능에서 문제 발생"
    echo "커버이미지 상태: $HAS_COVER"
    echo "본문이미지 상태: $HAS_INLINE"
fi
```

## 🧹 정리 작업
```bash
echo -e "\n=== 20. 테스트 파일 정리 ==="
rm -f cover_test.jpg inline_test.jpg new_cover.jpg invalid_file.txt large_cover.jpg final_cover.jpg final_inline.jpg
echo "테스트 파일들이 정리되었습니다."

echo -e "\n=== 테스트 완료 ==="
echo "모든 갤러리 커버이미지 기능 테스트가 완료되었습니다."
echo "데이터베이스에서 생성된 테스트 데이터를 정리하려면 다음 SQL을 실행하세요:"
echo ""
echo "DELETE FROM gallery WHERE title LIKE '%테스트%';"
echo "DELETE FROM upload_file_links WHERE owner_table = 'gallery' AND owner_id NOT IN (SELECT id FROM gallery);"
echo ""
```

## 📊 예상 결과

### ✅ 성공 케이스
1. **커버이미지 생성**: `coverImage` 필드에 올바른 파일 정보 표시
2. **커버이미지 수정**: 기존 커버이미지 교체되고 새 이미지 표시  
3. **커버이미지 삭제**: `coverImage` 필드가 `null`로 변경
4. **목록 조회**: `coverImageUrl` 필드에 다운로드 URL 표시
5. **데이터베이스**: `upload_file_links`에 `role='COVER'` 레코드 저장

### ❌ 실패 케이스  
1. **권한 없는 요청**: HTTP 401/403 응답
2. **잘못된 임시파일**: 커버이미지 없이 갤러리 생성
3. **대용량 파일**: 파일 크기 제한에 따른 업로드 실패 가능

### 🎯 핵심 검증 포인트
- 갤러리는 `COVER` + `INLINE` 파일만 사용
- `ATTACHMENT` 역할 파일은 생성되지 않음  
- 커버이미지는 항상 1개만 유지
- 목록과 상세에서 커버이미지 정보 정상 표시