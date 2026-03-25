# 미구현 API 목록 및 TODO

> 기준: 2026-03-25 빌드 성공 후 코드 분석
> 담당 범위: 서비스 CRUD 팀 (`/api/business/**`, `/api/dashboard/**`, `/api/inbox/**`, `/api/templates/**`, `/api/calendar/**`, `/api/automations/**`, `/api/notifications/**`)

---

## ✅ 구현 완료 API

### /api/business
- [x] `GET    /api/business/profile` — 비즈니스 프로필 조회
- [x] `PUT    /api/business/profile` — 비즈니스 프로필 저장/수정
- [x] `GET    /api/business/resources/files` — 파일 목록 조회
- [x] `POST   /api/business/resources/files` — 파일 업로드
- [x] `DELETE /api/business/resources/files/{resourceId}` — 파일 삭제
- [x] `GET    /api/business/resources/faqs` — FAQ 목록 조회
- [x] `POST   /api/business/resources/faqs` — FAQ 생성
- [x] `PUT    /api/business/resources/faqs/{faqId}` — FAQ 수정
- [x] `DELETE /api/business/resources/faqs/{faqId}` — FAQ 삭제
- [x] `GET    /api/business/categories` — 카테고리 목록 조회
- [x] `POST   /api/business/categories` — 카테고리 생성
- [x] `DELETE /api/business/categories/{categoryId}` — 카테고리 삭제
- [x] `POST   /api/business/templates/regenerate` — 템플릿 재생성(LLM)

### /api/dashboard
- [x] `GET /api/dashboard/summary` — 대시보드 요약
- [x] `GET /api/dashboard/schedules` — 예정된 일정
- [x] `GET /api/dashboard/weekly-summary` — 주간 요약
- [x] `GET /api/dashboard/recent-emails` — 최근 이메일 목록

### /api/inbox
- [x] `GET  /api/inbox` — 수신함 목록 (페이징, status 필터)
- [x] `GET  /api/inbox/{emailId}` — 수신함 상세 (AI 초안 포함)
- [x] `POST /api/inbox/{emailId}/reply` — 답장 처리 (SKIP 액션)
- [x] `POST /api/inbox/{emailId}/calendar` — 일정 등록

### /api/templates
- [x] `GET    /api/templates` — 템플릿 목록 조회
- [x] `POST   /api/templates` — 템플릿 생성
- [x] `PUT    /api/templates/{templateId}` — 템플릿 수정
- [x] `DELETE /api/templates/{templateId}` — 템플릿 삭제

---

## ⚠️ 부분 구현 (내부 로직 TODO 남음)

### /api/inbox
- [ ] `POST /api/inbox/{emailId}/reply` — **SEND 액션**: Gmail API 실제 발송 미구현
  - 파일: `InboxService.java:119-123`
  - 현황: 상태 변경(PROCESSED/SENT)만 됨, 실제 메일 발송 없음
  - 담당: **Google OAuth 팀** (주석에 명시됨)
- [ ] `POST /api/inbox/{emailId}/reply` — **EDIT_SEND 액션**: Gmail API 실제 발송 미구현
  - 파일: `InboxService.java:125-133`
  - 현황: 상태 변경(PROCESSED/EDITED)만 됨, 실제 메일 발송 없음
  - 담당: **Google OAuth 팀** (주석에 명시됨)

---

## ❌ 미구현 API (Controller/Service 없음)

> API 설계서 기준 스펙 확인 필요. 아래는 `claude.md` 담당 범위 기준으로 누락된 영역.

### /api/calendar ✅ 구현 완료 (2026-03-25)
- [x] `GET    /api/calendar/events?start_date=&end_date=` — 기간 내 일정 목록
- [x] `GET    /api/calendar/events/{event_id}` — 일정 상세 조회
- [x] `POST   /api/calendar/events` — 수동 일정 추가 (source=MANUAL, status=CONFIRMED, is_calendar_added=true)
- [x] `PATCH  /api/calendar/events/{event_id}/confirm` — PENDING → CONFIRMED 상태 변경
- [x] `PUT    /api/calendar/events/{event_id}` — 일정 수정
- ※ Google Calendar API 실제 호출은 TODO 주석 처리 (Google OAuth 팀 담당)
- 생성 파일: `CalendarController.java`, `CalendarService.java`, `CalendarEventRequest.java`, `CalendarEventResponse.java`, `CalendarEventDetailResponse.java`
- 수정 파일: `CalendarEvent.java` (update 메서드 추가), `CalendarEventRepository.java` (findByPeriod 쿼리 추가)

### /api/automations ✅ 구현 완료 (2026-03-25)
- [x] `GET    /api/automations/rules` — 자동화 규칙 목록 조회
- [x] `POST   /api/automations/rules` — 규칙 생성 (category 없으면 자동 INSERT)
- [x] `PUT    /api/automations/rules/{rule_id}` — 규칙 수정
- [x] `DELETE /api/automations/rules/{rule_id}` — 규칙 삭제 (Categories 보존)
- [x] `PATCH  /api/automations/rules/{rule_id}/auto-send` — 자동 발송 토글
- [x] `PATCH  /api/automations/rules/{rule_id}/auto-calendar` — 일정 자동 등록 토글
- 생성 파일: `AutomationController.java`, `AutomationService.java`, `AutomationRule.java`, `AutomationRuleRepository.java`, `AutomationRuleRequest.java`, `AutomationToggleRequest.java`, `AutomationRuleResponse.java`
- 수정 파일: `CategoryRepository.java` (findByUser_UserIdAndCategoryName 추가)

### /api/notifications ✅ 구현 완료 (2026-03-25)
- [x] `GET   /api/notifications?is_read=false` — 알림 목록 (is_read 필터 선택)
- [x] `PATCH /api/notifications/{notification_id}/read` — 단일 알림 읽음 처리
- [x] `PATCH /api/notifications/read-all` — 전체 알림 일괄 읽음 처리
- 생성 파일: `NotificationController.java`, `NotificationService.java`, `Notification.java`, `NotificationRepository.java`, `NotificationResponse.java`

### /api/support-tickets ✅ 구현 완료 (2026-03-25)
- [x] `GET  /api/support-tickets?status=` — 문의 목록 (status 필터 선택)
- [x] `GET  /api/support-tickets/{ticket_id}` — 문의 상세 조회
- [x] `POST /api/support-tickets` — 문의 작성 (status=PENDING, admin_reply=null)
- 생성 파일: `SupportTicketController.java`, `SupportTicketService.java`, `SupportTicket.java`, `SupportTicketRepository.java`, `SupportTicketRequest.java`, `SupportTicketListResponse.java`, `SupportTicketDetailResponse.java`

---

## 📋 우선순위 제안

| 순위 | 항목 | 이유 |1
|------|------|------|
| 1 | `/api/calendar/**` | Entity·Repository 이미 존재, 연관 기능(inbox calendar 등록) 연결 필요 |
| 2 | `/api/notifications/**` | 사용자 알림은 다른 기능과 의존성 낮음, 독립 구현 가능 |
| 3 | `/api/automations/**` | 자동화 규칙은 이메일 처리 파이프라인과 연동 필요, 복잡도 높음 |
| - | Gmail SEND/EDIT_SEND | Google OAuth 팀 담당, 해당 팀에 전달 필요 |

---

## 🐛 빌드 수정 이력 (2026-03-25)

- `DashboardService.java:77` — `integration.getStatus()` → `integration.getSyncStatus()` (필드명 불일치 수정)
- `FileTextExtractor.java:44` — PDFBox 3.x API 변경 대응: `PDDocument.load()` → `Loader.loadPDF()` + import 추가
