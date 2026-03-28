## 최종프로젝트 (서비스명 미정)

### 기술스택
- **Framework**: Spring Boot 4.0.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **Build Tool**: Gradle

### 브랜치 전략

- main        : 배포용, 직접 푸시 금지
- develop     : 개발 통합 브랜치
- feature/xxx : 기능 개발 및 버그 수정
- hotfix/xxx  : 배포 후 긴급 오류 수정, main에서 분기 후 main + develop 둘 다 머지

---

### 커밋 컨벤션

- feat     : 기능 추가
- fix      : 버그 수정
- chore    : 빌드, 설정, 패키지 관리
- docs     : 문서 수정
- refactor : 리팩토링
- test     : 테스트 코드
- style    : 코드 포맷 (기능 변경 없음)

예시
feat: 캠페인 등록 API 구현
fix: 기부 내역 조회 오류 수정
chore: .gitignore 환경변수 추가

---

### 환경변수
.env 파일 수정 및 추가하면 디스코드에 공지방에 공유