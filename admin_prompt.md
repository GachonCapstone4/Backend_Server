첨부된 claude.md 파일을 먼저 꼼꼼히 읽고 완벽히 숙지해 줘.

너는 이제부터 이 문서의 핵심 원칙을 철저히 따르는 우리 프로젝트의 '시니어 백엔드 개발자'야.

오늘 우리가 진행할 작업은 '관리자(Admin) 기능'의 백엔드 구현이야.                                                          
다른 기본 API와 기능들은 이미 다 짜여 있는 상태고, 너는 관리자 기능에만 집중하면 돼.                            
작업을 위해 아래에 관리자 기능 API 명세 목록을 제공할게.



[현재 확정된 관리자 기능 관련 API 목록]

GET Method

/api/admin/dashboard/summary
관리자 대시보드 상단 요약 카드 조회

/api/admin/dashboard/email-volume
기간별 메일 처리량 통계 조회

/api/admin/dashboard/domain-distribution
도메인별 이메일 분포 통계 조회

/api/admin/dashboard/weekly-trend
최근 7일 처리 추이 조회

/api/admin/users
전체 사용자 목록 조회 및 이름 / 이메일 / 업종 기준 검색

/api/admin/users/{user_id}
특정 사용자 상세 정보 조회

/api/admin/users/{user_id}/integration
특정 사용자의 Gmail / Calendar 연동 상태 조회

/api/admin/templates
전체 템플릿 목록 조회

/api/admin/templates/statistics/by-category
카테고리별 템플릿 사용 현황 통계 조회

/api/admin/automations/rules
전체 자동화 규칙 목록 조회

/api/admin/automations/rules/{rule_id}
특정 자동화 규칙 상세 조회

/api/admin/support-tickets
전체 문의 목록 조회 및 상태별 필터 조회

/api/admin/support-tickets/{ticket_id}
특정 문의 상세 조회

/api/admin/operations/jobs
최근 메일 처리 작업 목록 및 상태 조회

/api/admin/operations/jobs/summary
작업 상태별 건수 조회

/api/admin/operations/jobs/{job_id}
특정 작업 상세 조회

/api/admin/operations/jobs/{job_id}/error
특정 작업의 실패 원인 로그 조회



POST Method

/api/admin/automations/rules	
자동화 규칙 생성

/api/admin/support-tickets/{ticket_id}/reply	
특정 문의에 대한 관리자 답변 작성



PATCH Method

/api/admin/users/{user_id}/status
특정 사용자 계정 활성/비활성 상태 변경

/api/admin/automations/rules/{rule_id}
특정 자동화 규칙 정보 수정



DELETE Method

/api/admin/users/{user_id}/integration
특정 사용자의 구글 연동 강제 해제

/api/admin/automations/rules/{rule_id}
특정 자동화 규칙 삭제

api/admin/operations/jobs/{job_id}
특정 실패/대기 작업 강제 삭제



[첫 번째 지시사항]

당장 코드를 짜지 마. claude.md의 '워크플로우 원칙 - 1. 플랜 모드 기본화'에 따라, 내가 제공한 API 설계서를 구현하기 위해 어떤 파일들을 생성하거나 수정해야 할지 tasks/todo.md 형식의 체크리스트로 먼저 계획을 세워줘.

- 기존 인증 방식을 깨지 말 것
- 기존 예외 처리 방식을 최대한 유지할 것
- 기존 운영/작업 상태 관리 구조(Outbox/분석 작업 관련 구조)가 있다면 이를 우선 활용하는 방향으로 볼 것
- 관리자 기능은 일반 사용자 기능과 분리된 네이밍 및 패키지 구조를 유지할 것
- Controller / Service / Repository / DTO / Query / Exception / Security 영향 범위를 빠짐없이 검토할 것