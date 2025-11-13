# 📝 공지사항 파일 첨부 통합 테스트 가이드

## 🎯 테스트 목적
- 공지사항의 첨부파일(ATTACHMENT) 및 본문이미지(INLINE) 기능 검증
- 파일 업로드 → 임시저장 → 정식저장 → 연결 → 삭제 전체 생명주기 테스트
- 다양한 파일 형식(Excel, PDF, TXT, 이미지) 지원 검증

## 🏗️ 파일 시스템 구조

> **⚠️ 중요**: `uploads/` 폴더는 `.gitignore`에 포함되어 Git에 커밋되지 않습니다.  
> 실제 파일들은 로컬 파일시스템에 저장되며, 운영환경에서는 AWS S3 등 별도 스토리지 사용을 권장합니다.

### A. 임시 파일 저장
```
uploads/temp/년/월/
├── 2025/11/
│   ├── uuid1.xlsx
│   ├── uuid2.pdf
│   └── uuid3.jpg
```

### B. 정식 파일 저장 
```
uploads/general/년/월/
├── 2025/11/
│   ├── uuid1.xlsx
│   ├── uuid2.pdf
│   └── uuid3.jpg
```

### C. 데이터베이스 구조
```sql
-- 파일 메타데이터
upload_files (id, server_path, file_name, ext, size, storage_type, reg_date)

-- 파일-소유자 연결
upload_file_links (id, file_id, owner_table, owner_id, role, created_at)
```

## 📋 파일 역할 (FileRole)

| 역할 | 코드 | 설명 | 공지사항 지원 |
|------|------|------|--------------|
| 첨부파일 | ATTACHMENT | 다운로드 가능한 파일 (Excel, PDF, TXT 등) | ✅ |
| 본문이미지 | INLINE | 에디터 내 삽입된 이미지 (JPG, PNG 등) | ✅ |
| 커버이미지 | COVER | 대표 이미지/썸네일 | ❌ (사용안함) |

## 🔄 파일 처리 흐름

### 1단계: 임시 파일 업로드
```bash
curl -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/file.xlsx"
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "fileId": "uuid-12345",
    "originalFileName": "report.xlsx",
    "fileSize": 2048,
    "extension": "xlsx"
  }
}
```

### 2단계: 공지사항 생성 (파일 연결)
```bash
curl -X POST "http://localhost:8080/api/admin/notices" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "파일 첨부 공지",
    "content": "첨부파일이 있는 공지사항입니다.",
    "categoryId": 20,
    "attachments": ["uuid-12345"],
    "inlineImages": ["uuid-67890"]
  }'
```

### 3단계: 내부 파일 처리 과정
1. **임시→정식 변환**: `FileService.promoteToFormalFile()`
   - `/temp/2025/11/uuid.xlsx` → `/general/2025/11/uuid.xlsx`
   - DB에 파일 메타데이터 저장

2. **파일 연결 생성**: `NoticeService.createFileLinks()`
   - upload_file_links 테이블에 연결 레코드 생성
   - owner_table: "academy.notices"
   - role: "ATTACHMENT" 또는 "INLINE"

## 🧪 테스트 시나리오

### Phase 1: 테스트 파일 준비
```bash
# Excel 파일 생성
echo -e "이름,점수,등급\n김철수,90,A\n이영희,85,B" > /tmp/test.csv

# PDF 테스트용 텍스트 파일
echo "PDF 테스트 내용입니다" > /tmp/test.txt

# 작은 이미지 파일 (1x1 PNG base64)
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==" | base64 -d > /tmp/test.png
```

### Phase 2: 첨부파일만 있는 공지사항 CRUD

#### 2-1. 파일 업로드
```bash
# Excel 파일 업로드
curl -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -F "file=@/tmp/test.csv" > excel_response.json

# PDF 파일 업로드  
curl -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -F "file=@/tmp/test.txt" > pdf_response.json

# fileId 추출
EXCEL_FILE_ID=$(cat excel_response.json | jq -r '.data.fileId')
PDF_FILE_ID=$(cat pdf_response.json | jq -r '.data.fileId')
```

#### 2-2. 공지사항 생성
```bash
curl -X POST "http://localhost:8080/api/admin/notices" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"첨부파일 테스트 공지\",
    \"content\": \"Excel과 PDF 파일이 첨부된 공지입니다.\",
    \"categoryId\": 20,
    \"attachments\": [\"$EXCEL_FILE_ID\", \"$PDF_FILE_ID\"],
    \"inlineImages\": null
  }"
```

#### 2-3. 공지사항 조회 (파일 정보 포함)
```bash
curl -X GET "http://localhost:8080/api/admin/notices/{noticeId}" \
  -H "Authorization: Bearer {token}"
```

**조회 응답 예시:**
```json
{
  "data": {
    "id": 1,
    "title": "첨부파일 테스트 공지",
    "attachments": [
      {
        "fileId": "uuid-12345",
        "fileName": "test.csv", 
        "fileSize": 1024,
        "downloadUrl": "/api/public/files/download/uuid-12345"
      }
    ],
    "inlineImages": []
  }
}
```

#### 2-4. 파일 다운로드 테스트
```bash
curl -X GET "http://localhost:8080/api/public/files/download/{fileId}" \
  --output downloaded_file
```

#### 2-5. 공지사항 수정 (파일 변경)
```bash
# 새 파일 업로드
curl -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -F "file=@/tmp/new_test.txt" > new_file_response.json

NEW_FILE_ID=$(cat new_file_response.json | jq -r '.data.fileId')

# 공지사항 수정
curl -X PUT "http://localhost:8080/api/admin/notices/{noticeId}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"수정된 첨부파일 공지\",
    \"content\": \"파일이 변경되었습니다.\",
    \"categoryId\": 20,
    \"attachments\": [\"$NEW_FILE_ID\"]
  }"
```

#### 2-6. 공지사항 삭제
```bash
curl -X DELETE "http://localhost:8080/api/admin/notices/{noticeId}" \
  -H "Authorization: Bearer {token}"
```

### Phase 3: 본문이미지만 있는 공지사항 CRUD

#### 3-1. 이미지 파일 업로드
```bash
curl -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -F "file=@/tmp/test.png" > image_response.json

IMAGE_FILE_ID=$(cat image_response.json | jq -r '.data.fileId')
```

#### 3-2. 공지사항 생성 (본문이미지)
```bash
curl -X POST "http://localhost:8080/api/admin/notices" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"본문이미지 테스트 공지\",
    \"content\": \"이미지가 포함된 본문 내용입니다.\",
    \"categoryId\": 21,
    \"attachments\": null,
    \"inlineImages\": [\"$IMAGE_FILE_ID\"]
  }"
```

### Phase 4: 복합 파일 공지사항 CRUD

```bash
curl -X POST "http://localhost:8080/api/admin/notices" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"복합 파일 테스트 공지\",
    \"content\": \"첨부파일과 본문이미지가 모두 있습니다.\",
    \"categoryId\": 20,
    \"attachments\": [\"$EXCEL_FILE_ID\", \"$PDF_FILE_ID\"],
    \"inlineImages\": [\"$IMAGE_FILE_ID\"]
  }"
```

## 🔍 검증 포인트

### A. 파일 시스템 검증
```bash
# 임시 폴더 확인
ls -la uploads/temp/2025/11/

# 정식 폴더 확인  
ls -la uploads/general/2025/11/

# 파일 이동 확인 (임시→정식)
# 공지사항 생성 후 임시 폴더는 비어야 함
```

### B. 데이터베이스 검증
```sql
-- 파일 메타데이터 확인
SELECT * FROM upload_files WHERE id = 'uuid-12345';

-- 파일 연결 확인
SELECT * FROM upload_file_links 
WHERE owner_table = 'academy.notices' 
AND owner_id = {noticeId};
```

### C. API 응답 검증
- **첨부파일**: downloadUrl 정상 접근, 파일 다운로드 가능
- **본문이미지**: 이미지 URL 접근, MIME 타입 확인
- **파일 메타정보**: 원본 파일명, 크기, 확장자 정확성

### D. 파일 치환 검증
```bash
# 수정 전후 연결 정보 비교
# 기존 연결 삭제 → 새 연결 생성 확인

# 고아 파일 확인 (연결이 끊어진 파일)
SELECT f.* FROM upload_files f
LEFT JOIN upload_file_links l ON f.id = l.file_id  
WHERE l.file_id IS NULL;
```

## ⚠️ 주의사항

1. **파일 크기 제한**: application.yml의 `file.max-size` 설정 확인
2. **권한 검증**: 관리자 권한(ADMIN) 필수
3. **카테고리 존재**: 유효한 categoryId 사용
4. **파일 형식**: 지원되지 않는 확장자는 업로드 실패 
5. **동시성**: 같은 파일ID로 동시 요청 시 충돌 가능성

## 🚀 자동화 스크립트 예시

```bash
#!/bin/bash
# 전체 파일 첨부 테스트 자동화

# 1. 인증 토큰 획득
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username":"testadmin","password":"admin123!"}' \
  | jq -r '.data.accessToken')

# 2. 테스트 파일 생성
echo "name,score\nJohn,90" > /tmp/test.csv
echo "Test PDF content" > /tmp/test.txt

# 3. 파일 업로드
EXCEL_ID=$(curl -s -X POST "http://localhost:8080/api/public/files/upload-multipart" \
  -F "file=@/tmp/test.csv" | jq -r '.data.fileId')

# 4. 공지사항 생성
NOTICE_ID=$(curl -s -X POST "http://localhost:8080/api/admin/notices" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"자동 테스트 공지\",\"content\":\"테스트\",\"categoryId\":20,\"attachments\":[\"$EXCEL_ID\"]}" \
  | jq -r '.data')

echo "생성된 공지사항 ID: $NOTICE_ID"

# 5. 조회 검증
curl -s -X GET "http://localhost:8080/api/admin/notices/$NOTICE_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.attachments'

# 6. 정리
curl -s -X DELETE "http://localhost:8080/api/admin/notices/$NOTICE_ID" \
  -H "Authorization: Bearer $TOKEN"

rm -f /tmp/test.csv /tmp/test.txt
```

---

📅 **최종 업데이트**: 2024.11.12  
🎯 **테스트 목표**: 공지사항 파일 첨부 기능 완전 검증  
💡 **핵심**: 임시파일 → 정식파일 → DB연결 → 파일치환 전 과정 안정성 확인