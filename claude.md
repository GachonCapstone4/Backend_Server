# 프로젝트 개요
- 프로젝트명: LLM 기반 업무용 이메일 AI 에이전트
- 목적: 사용자의 이메일을 수집/분석하여 LLM 기반으로 업무를 자동화하는 AI 에이전트 서비스

# 기술 스택
- Backend: Java 21, Spring Boot 3.5.12, Maven
- Database: MariaDB
- ORM: Spring Data JPA 기반
- Message Queue: RabbitMQ (이메일 수집 및 LLM 비동기 처리용)
- Frontend: React

# 핵심 아키텍처 및 인증 룰 (매우 중요)
1. 투트랙 인증 체계:
    - 서비스 자체 인증: `Authorization: Bearer <JWT>` 헤더 사용
    - 외부 API 연동: Google OAuth 2.0 (Access/Refresh Token을 DB `Integrations` 테이블에 저장)

2. 서비스 자체 인증 룰:
    - 현재 서비스 인증은 JWT 기반으로 동작한다.
    - JWT에서 추출한 사용자 식별자(userId) 기준으로 로그인 사용자를 판별한다.
    - `/me` 계열 API는 항상 로그인한 본인 기준으로 구현한다.

3. Google OAuth 연동 룰:
    - 상태 유지(State)는 서버 세션이나 DB 대신, 짧은 수명의 Stateless 임시 JWT를 생성하여 `state` 파라미터로 사용한다.
    - Google API 호출 시 순수 HTTP 요청이 아닌 공식 Java 클라이언트 라이브러리(`google-api-client`, `google-api-services-gmail` 등)를 사용한다.
    - 세밀한 권한 제어(Granular Consent)를 고려해, 토큰 발급 후 필수 Scope(Gmail, Calendar) 누락 여부를 반드시 검증한다.
    - Scope, callback 처리 방식, 상태 변경(status) 의미가 명확하지 않으면 임의로 구현하지 말고 먼저 사용자에게 질문한다.

# API 구현 원칙
1. API Method / URI / 요청·응답 필드명 / 인증 방식은 이미 확정된 API 설계서를 절대 기준으로 한다.
2. 임의로 URI를 변경하거나 응답 JSON 구조를 바꾸지 않는다.
3. 없는 스펙은 추측하지 말고 사용자에게 먼저 질문한다.
4. Controller 매핑만 있고 내부 로직이 비어 있으면 구현 완료로 간주하지 않는다.

# 백엔드 코딩 컨벤션
1. 계층 분리:
    - Controller, Service, Repository, DTO 역할을 엄격히 분리한다.
    - 기본 패키지 구조(`controller`, `service`, `repository`, `domain/entity`, `dto/request`, `dto/response`, `security`, `exception`)를 우선 유지한다.

2. DB 접근:
    - 기본 DB 접근 방식은 Spring Data JPA를 사용한다.
    - 특별한 이유 없이 JdbcTemplate/MyBatis를 임의로 도입하지 않는다.

3. 응답 형식:
    - 응답 형식은 확정된 API 설계서의 필드명과 구조를 절대 우선한다.
    - 임의의 `success/data`, `code/message/data` 구조를 새로 만들지 않는다.

4. 예외 처리:
    - `try-catch`로 뭉뚱그리지 않고, `@RestControllerAdvice`를 활용한 전역 예외 처리를 지향한다.

5. 주석:
    - 비즈니스 로직이 복잡한 부분(예: JWT 파싱, OAuth 검증 로직)은 한국어로 동작 과정을 상세히 주석으로 단다.

6. Lombok:
    - Lombok annotation processing 이슈 가능성을 고려한다.
    - 생성자 주입이 중요한 클래스는 필요 시 명시적 생성자를 우선 제안한다.

# AI 도우미(Claude) 행동 지침
- 코드를 제공할 때는 파일 경로와 패키지명(`com.emailagent...`)을 명확히 명시한다.
- 불필요하거나 쓰이지 않는 import, 더미 코드는 최대한 제외한다.
- 내가 명시한 버전(Java 21, Spring Boot 3.5.12)과 호환되지 않는 레거시 라이브러리는 사용하지 않는다.
- 모호한 부분이 있다면 임의로 가정을 세워 코드를 짜지 말고, 반드시 먼저 질문한다.
- 기존 코드를 수정할 때는 수정 대상 파일 목록을 먼저 제시하고, 그 다음 코드를 제안한다.