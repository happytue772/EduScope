# EduScope DEMO_ADMIN 변경 내역

## 목적

포트폴리오 방문자가 관리자 화면과 분석 결과는 볼 수 있게 하고, 데이터 변경과 Hadoop
분석 실행은 서버 권한으로 차단한다.

## 구현

- `DEMO_ADMIN` Role을 조회 권한 집합에 추가
- 관리자 GET API 허용
- 사용자·Dataset·Audit 변경 API 차단
- Analysis Job 생성·실행·재실행 차단
- 향후 추가되는 `/api/**` 쓰기 요청도 기본적으로 ADMIN만 허용
- 공개 Health Check만 익명 허용
- 데모 Profile에서 회원가입과 Swagger 비활성화
- Secure/HttpOnly/SameSite Session Cookie 적용
- Render Docker Blueprint와 Oracle Role 등록 SQL 추가

## 검증 범위

- DEMO_ADMIN 관리자 조회 200 테스트
- DEMO_ADMIN 사용자·Dataset·Audit 변경 403 테스트
- DEMO_ADMIN Analysis Job 쓰기 403 테스트
- 미래 쓰기 API 기본 차단 403 테스트
- YAML 파싱 및 변경 Java 파일 괄호 정합성 검사

Maven 통합 테스트는 의존성 다운로드가 가능한 환경에서 다음 명령으로 최종 실행한다.

```powershell
.\mvnw.cmd -Pci clean verify
```

## 실제 배포 전 필수 입력

- 공개 가능한 Oracle DB 또는 마스킹한 데모 스냅샷
- `eduscope-frontend` 소스의 DEMO_ADMIN UI 처리와 Vercel rewrite
