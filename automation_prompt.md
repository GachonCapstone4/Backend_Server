Backend_Server 기준으로 아래 작업 수행해줘.
모든 파일 먼저 읽고 수정해줘.

===================================
수정 대상: RagResultService.java
===================================

파일 경로:
src/main/java/com/emailagent/service/RagResultService.java

파일 먼저 읽고 handleTemplateMatched() 메서드 수정:

추가로 주입받을 Repository/Service:
- DraftReplyRepository
- AutomationRuleRepository
- GmailApiService (기존에 있으면 재사용, 없으면 확인 후 추가)
- EmailAnalysisResultRepository
- UserRepository

handleTemplateMatched() 메서드에서
추천 저장 완료 후 (savedCount > 0 조건 아래) 아래 로직 추가:

1. draft_replies 저장:
    - 가장 높은 score의 template 선택 (items.get(0), 이미 score순 정렬됨)
    - templateRepository.findById(templateId)로 템플릿 조회
    - template.getBodyTemplate()으로 본문 가져옴
    - template.getSubjectTemplate()으로 제목 가져옴
    - placeholder 치환:
        * {{고객명}} → email.getSenderName() 없으면 email.getSenderEmail()의 @ 앞부분
        * {{문의주제}} → emailAnalysisResultRepository.findByEmail_EmailId(emailId)의 intent
        * {{문의요약}} → analysisResult.getSummaryText()
        * {{수신일자}} → email.getReceivedAt()을 "yyyy-MM-dd HH:mm" 포맷
        * {{담당자명}} → userRepository.findById(userId)의 name
        * 나머지 {{변수명}}은 그대로 유지
    - draftReplyRepository.findByEmailIdAndUserId(emailId, userId)로 이미 있으면 업데이트
      없으면 새로 생성
    - 저장:
        * draft_content: 치환된 본문
        * draft_subject: 치환된 제목
        * template_id: 선택된 templateId
        * status: DraftStatus.PENDING_REVIEW
    - 로그 출력

2. 자동 발송 처리:
    - automationRuleRepository.findByUserIdWithDetails(userId)로 규칙 목록 조회
    - 규칙 중 아래 조건 모두 만족하는 것 찾기:
        * rule.isAutoSendEnabled() == true
        * rule.isActive() == true
        * rule.getTemplate() != null
        * rule.getTemplate().getTemplateId() == 저장된 draft의 templateId
    - 조건 만족하는 규칙 있으면:
        * gmailApiService.sendEmail(userId, email.getSenderEmail(), draft_subject, draft_content) 호출
        * email.updateStatus(EmailStatus.PROCESSED)
        * draft.updateStatus(DraftStatus.SENT)
        * 로그 출력: "[RagResultService] 자동 발송 완료 — emailId={}, templateId={}"

===================================
주의사항
===================================
- DraftReply, AutomationRule 엔티티 실제 필드명 먼저 확인 후 사용
- DraftReplyRepository.findByEmailIdAndUserId() 없으면 추가
- EmailStatus, DraftStatus enum 확인 후 사용
- GmailApiService 실제 메서드명 확인 후 사용
- @Transactional 이미 있으므로 추가 불필요
- 기존 코드 건드리지 말고 추가만
- Lombok, import 스타일 기존 파일과 동일하게