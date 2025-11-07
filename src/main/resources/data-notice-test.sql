-- 공지사항 테스트 데이터 초기화
-- 카테고리 그룹 ID 1의 카테고리들 확인 후 공지사항 데이터 추가

-- 공지사항 테스트 데이터 (카테고리별 2개씩)
INSERT INTO notices (title, content, is_important, is_published, exposure_type, exposure_start_at, exposure_end_at, category_id, view_count, file_group_key, created_by, created_at, updated_by, updated_at) VALUES 
-- 카테고리 1 공지사항
('2024 spring 입학 안내', '<h2>2024년 1학기 신입생 입학 안내</h2><p>안녕하세요. 2024년 1학기 신입생 입학 관련 안내드립니다.</p><h3>주요 일정</h3><ul><li>입학식: 2024년 3월 2일</li><li>수강 신청: 2024년 2월 20일 ~ 2월 28일</li><li>개강: 2024년 3월 4일</li></ul><img src="/api/files/sample-campus.jpg" alt="캠퍼스 전경" style="width:100%; max-width:600px;"><p>자세한 사항은 학사팀에 문의하시기 바랍니다.</p>', true, true, 'ALWAYS', null, null, 1, 156, 'file-group-001', 1, '2024-01-15 09:00:00', 1, '2024-01-15 09:00:00'),

('학사일정 변경 안내', '<h2>2024학년도 학사일정 일부 변경 안내</h2><p>코로나19 상황에 따른 학사일정 변경 사항을 안내드립니다.</p><h3>변경된 일정</h3><table border="1" style="width:100%; border-collapse:collapse;"><tr><th>구분</th><th>기존 일정</th><th>변경 일정</th></tr><tr><td>중간고사</td><td>4월 15일~19일</td><td>4월 22일~26일</td></tr><tr><td>기말고사</td><td>6월 10일~14일</td><td>6월 17일~21일</td></tr></table><p><strong>변경 사유:</strong> 전국적인 학사일정 조정에 따른 불가피한 변경</p>', false, true, 'ALWAYS', null, null, 1, 89, null, 1, '2024-01-10 14:30:00', 1, '2024-01-10 14:30:00'),

-- 카테고리 2 공지사항
('시스템 정기 점검 안내', '<h2>시스템 정기 점검 안내</h2><p>학사정보시스템 정기 점검을 아래와 같이 실시합니다.</p><div style="background:#f0f8ff; padding:15px; border-left:4px solid #007bff; margin:15px 0;"><h3 style="color:#007bff;">점검 일시</h3><p><strong>일시:</strong> 2024년 1월 20일 (토) 오전 2시 ~ 오전 6시 (4시간)</p><p><strong>영향:</strong> 수강신청, 성적조회, 학사정보 조회 등 모든 서비스</p></div><h3>주의사항</h3><ul><li>점검 시간 중에는 모든 온라인 서비스 이용 불가</li><li>급한 업무는 점검 완료 후 처리</li><li>점검 시간은 상황에 따라 연장될 수 있음</li></ul><p>이용에 불편을 드려 죄송합니다.</p>', true, true, 'PERIOD', '2024-01-18 00:00:00', '2024-01-25 23:59:59', 2, 234, 'file-group-002', 1, '2024-01-18 10:00:00', 1, '2024-01-18 10:00:00'),

('온라인 강의 플랫폼 업데이트', '<h2>온라인 강의 플랫폼 업데이트 안내</h2><p>더 나은 서비스 제공을 위해 온라인 강의 플랫폼을 업데이트합니다.</p><h3>주요 업데이트 내용</h3><ol><li>영상 재생 성능 개선</li><li>모바일 앱 안정성 향상</li><li>새로운 화면 공유 기능 추가</li><li>자막 기능 개선</li></ol><blockquote style="border-left:4px solid #28a745; padding:10px; margin:15px 0; background:#f8fff8;"><p><strong>업데이트 일정:</strong> 2024년 1월 25일 오후 6시 적용</p></blockquote><h3>사용 가이드</h3><p>새로운 기능 사용법은 <a href="#guide">이용 가이드</a>를 참고하세요.</p>', false, true, 'ALWAYS', null, null, 2, 67, null, 1, '2024-01-12 16:45:00', 1, '2024-01-12 16:45:00'),

-- 카테고리 3 공지사항
('장학금 신청 안내 (2024-1학기)', '<h2>2024학년도 1학기 장학금 신청 안내</h2><p>2024년 1학기 각종 장학금 신청을 다음과 같이 받습니다.</p><h3>신청 가능 장학금</h3><table border="1" style="width:100%; border-collapse:collapse; margin:15px 0;"><tr style="background:#f8f9fa;"><th>장학금명</th><th>지급액</th><th>신청 자격</th></tr><tr><td>성적우수장학금</td><td>등록금 50%</td><td>직전학기 평점 3.8 이상</td></tr><tr><td>근로장학금</td><td>시급 12,000원</td><td>가계곤란 증명</td></tr><tr><td>특기장학금</td><td>등록금 전액</td><td>특기활동 우수자</td></tr></table><img src="/api/files/scholarship-info.jpg" alt="장학금 안내 포스터" style="width:100%; max-width:500px; margin:15px 0;"><h3>신청 방법</h3><ol><li>학사정보시스템 로그인</li><li>장학금 신청 메뉴 접속</li><li>필요 서류 업로드</li><li>온라인 신청 완료</li></ol><div style="background:#fff3cd; border:1px solid #ffeaa7; padding:10px; margin:15px 0;"><strong>⚠️ 주의:</strong> 신청 기간 내에만 접수 가능하며, 서류 미비 시 선정에서 제외됩니다.</div>', true, true, 'PERIOD', '2024-01-15 00:00:00', '2024-02-15 23:59:59', 3, 445, 'file-group-003', 1, '2024-01-15 11:20:00', 1, '2024-01-15 11:20:00'),

('학생 상담 프로그램 운영', '<p>학생들의 대학 생활 적응과 진로 고민 해결을 위한 상담 프로그램을 운영합니다.</p><h3>상담 프로그램 안내</h3><ul><li><strong>개인 상담:</strong> 전문 상담사와의 1:1 상담 (사전 예약 필수)</li><li><strong>집단 상담:</strong> 주제별 소그룹 상담 프로그램</li><li><strong>온라인 상담:</strong> 화상 또는 채팅을 통한 비대면 상담</li></ul><h3>상담 분야</h3><div style="display:flex; flex-wrap:wrap; gap:10px; margin:15px 0;"><span style="background:#e3f2fd; padding:5px 10px; border-radius:15px;">학업 고민</span><span style="background:#f3e5f5; padding:5px 10px; border-radius:15px;">대인 관계</span><span style="background:#e8f5e8; padding:5px 10px; border-radius:15px;">진로 설계</span><span style="background:#fff8e1; padding:5px 10px; border-radius:15px;">스트레스 관리</span></div><h3>신청 방법</h3><p>학생지원팀(02-1234-5678)으로 전화 또는 학사정보시스템에서 신청 가능합니다.</p>', false, true, 'ALWAYS', null, null, 3, 123, null, 1, '2024-01-08 13:15:00', 1, '2024-01-08 13:15:00'),

-- 카테고리 4 공지사항
('도서관 이용 시간 연장 안내', '<h2>도서관 이용 시간 연장 운영</h2><p>기말고사 기간을 맞아 도서관 이용 시간을 연장 운영합니다.</p><div style="background:#f8f9fa; padding:20px; border-radius:8px; margin:15px 0;"><h3 style="color:#495057;">연장 운영 기간</h3><p><strong>기간:</strong> 2024년 6월 10일(월) ~ 6월 21일(금)</p><p><strong>시간:</strong> 오전 7시 ~ 오후 12시 (17시간 운영)</p><p><strong>대상:</strong> 중앙도서관, 제1분관</p></div><h3>이용 안내</h3><ul><li>학생증 또는 신분증 지참 필수</li><li>연장 운영 시간 중 카페테리아는 오후 10시까지 운영</li><li>그룹 스터디룸은 사전 예약 필요</li><li>노트북 대여는 평상시와 동일 (오후 9시까지)</li></ul><img src="/api/files/library-study.jpg" alt="도서관 학습공간" style="width:100%; max-width:600px; margin:15px 0;"><p>많은 이용 바랍니다.</p>', false, true, 'PERIOD', '2024-06-08 00:00:00', '2024-06-25 23:59:59', 4, 298, 'file-group-004', 1, '2024-06-05 09:30:00', 1, '2024-06-05 09:30:00'),

('신간 도서 입고 안내', '<h2>2024년 1월 신간 도서 입고 안내</h2><p>교육과정 개선 및 최신 학술 동향 반영을 위해 신간 도서를 입고하였습니다.</p><h3>주요 입고 분야</h3><div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(250px, 1fr)); gap:15px; margin:20px 0;"><div style="background:#e3f2fd; padding:15px; border-radius:8px;"><h4>컴퓨터공학</h4><p>인공지능, 머신러닝, 데이터사이언스 관련 최신서 50권</p></div><div style="background:#f3e5f5; padding:15px; border-radius:8px;"><h4>경영학</h4><p>디지털 전환, 스타트업, ESG 경영 관련서 35권</p></div><div style="background:#e8f5e8; padding:15px; border-radius:8px;"><h4>교양</h4><p>인문학, 철학, 역사 관련 교양서 40권</p></div></div><h3>이용 안내</h3><p>신간 도서는 <strong>입고 후 1주일간 관내 열람만 가능</strong>하며, 이후 일반 대출 규정에 따라 대출 가능합니다.</p><blockquote style="border-left:4px solid #007bff; padding:15px; background:#f8f9ff; margin:15px 0;"><p><strong>📚 추천 도서</strong><br>"디지털 시대의 학습 혁명" (교육학과 교수 추천)<br>"AI 시대의 윤리학" (철학과 교수 추천)</p></blockquote>', false, true, 'ALWAYS', null, null, 4, 76, null, 1, '2024-01-03 15:45:00', 1, '2024-01-03 15:45:00');

-- 첨부 파일 테스트 데이터 (file_groups 테이블에 추가)
INSERT INTO file_groups (id, group_key, created_at) VALUES 
(1, 'file-group-001', '2024-01-15 09:00:00'),
(2, 'file-group-002', '2024-01-18 10:00:00'),
(3, 'file-group-003', '2024-01-15 11:20:00'),
(4, 'file-group-004', '2024-06-05 09:30:00');

-- 첨부 파일 상세 정보 (upload_files 테이블에 추가)
INSERT INTO upload_files (id, file_group_id, original_name, saved_name, file_path, file_size, content_type, uploaded_at, uploaded_by) VALUES 
(1, 1, '2024_입학안내서.pdf', 'admission_guide_2024_001.pdf', '/uploads/2024/01/admission_guide_2024_001.pdf', 2048576, 'application/pdf', '2024-01-15 09:00:00', 1),
(2, 1, '캠퍼스_지도.jpg', 'campus_map_001.jpg', '/uploads/2024/01/campus_map_001.jpg', 512000, 'image/jpeg', '2024-01-15 09:00:00', 1),

(3, 2, '시스템점검_상세일정.xlsx', 'maintenance_schedule_001.xlsx', '/uploads/2024/01/maintenance_schedule_001.xlsx', 15360, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', '2024-01-18 10:00:00', 1),

(4, 3, '장학금신청서.hwp', 'scholarship_form_2024.hwp', '/uploads/2024/01/scholarship_form_2024.hwp', 51200, 'application/x-hwp', '2024-01-15 11:20:00', 1),
(5, 3, '소득증명서_양식.pdf', 'income_certificate_form.pdf', '/uploads/2024/01/income_certificate_form.pdf', 204800, 'application/pdf', '2024-01-15 11:20:00', 1),

(6, 4, '도서관_이용수칙.pdf', 'library_rules_2024.pdf', '/uploads/2024/06/library_rules_2024.pdf', 307200, 'application/pdf', '2024-06-05 09:30:00', 1);