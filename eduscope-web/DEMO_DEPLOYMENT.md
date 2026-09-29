# EduScope 공개 데모 배포 안내

## 1. 공개 데모 권한

`DEMO_ADMIN`은 관리자 화면을 읽을 수 있지만 데이터를 변경할 수 없다.

허용:

- 일반 대시보드와 분석 결과 조회
- 학생, Dataset, Data Quality 조회
- Analysis Job 이력과 성공·실패 정보 조회
- 사용자·역할 목록, Audit Log, 시스템 상태 조회

차단:

- Analysis Job 생성·실행·재실행
- Dataset 생성·수정·삭제
- 사용자 상태·Role 변경
- Audit Log 삭제
- Actuator 관리 Endpoint
- 공개 환경 회원가입

프론트에서 버튼을 숨기는 것과 별개로 Spring Security가 변경 요청을 `403`으로 차단한다.

## 2. 로컬 Oracle에 DEMO_ADMIN 추가

1. SQL Developer에서 `sql/04_demo_admin_role.sql`을 스크립트 실행한다.
2. EduScope 회원가입 화면에서 공개용 계정을 한 개 생성한다.
3. `sql/05_assign_demo_admin.sql`의 `DEMO_LOGIN_ID`를 공개용 로그인 ID로 변경한다.
4. SQL Developer에서 두 번째 스크립트를 실행한다.
5. 공개용 계정으로 로그인해 조회는 성공하고 변경 요청은 `403`인지 확인한다.

공개용 계정에는 기존 `ADMIN` Role을 함께 부여하지 않는다.

## 3. 데모용 DB 원칙

공개 환경에는 로컬 운영 DB 전체를 그대로 복사하지 않는다.

- OULAD 원본은 공개 데이터지만 APP_USER, AUDIT_LOG에는 서비스 운영정보가 포함된다.
- 공개 DB에는 DEMO_ADMIN 계정과 공개 가능한 OULAD 기준정보·집계결과·Job 이력만 복사한다.
- 개인 이메일, 실제 IP, 로컬 경로, SSH 경로와 비밀정보는 제외하거나 마스킹한다.
- 통계값을 임의로 만들지 않고 현재 Oracle에서 검증한 결과를 스냅샷으로 복사한다.

현재 프로젝트에 DB Export 파일이 포함되어 있지 않으므로, 실제 Oracle에서 공개할 행을 확정한 뒤 Export해야 한다.

## 4. Render 백엔드 배포

프로젝트의 `render.yaml`은 다음 구성을 사용한다.

```text
Dockerfile
Spring Profile = demo
회원가입 비활성화
Swagger 비활성화
Secure/HttpOnly Session Cookie
공개 Health Check = /actuator/health
Hadoop 실행 설정 비활성 값
```

Render 환경변수에 다음 세 값을 직접 등록한다.

```text
EDUSCOPE_DB_URL
EDUSCOPE_DB_USERNAME
EDUSCOPE_DB_PASSWORD
```

비밀번호는 Git과 `render.yaml`에 직접 적지 않는다.

저장소가 `eduscope-web` 자체라면 `render.yaml`을 현재 위치에 둔다. 저장소 최상위가
`EduScope`이고 백엔드가 하위 폴더라면 제공된 `render.monorepo.yaml`을 저장소 최상위의
`render.yaml`로 복사한다. 핵심 설정은 다음과 같다.

```yaml
rootDir: eduscope-web
dockerfilePath: ./Dockerfile
dockerContext: .
```

`rootDir`을 설정하면 Dockerfile 경로와 Docker Context도 해당 폴더 기준으로 처리된다.
두 저장소 구조를 섞으면 Dockerfile을 찾지 못한다.

배포 후 확인:

```text
GET  /actuator/health                      → 200
GET  /api/auth/csrf                        → 200
POST /api/auth/login                       → 200
GET  /api/auth/me                          → DEMO_ADMIN
GET  /api/analysis-jobs                    → 200
GET  /api/admin/users                      → 200
POST /api/analysis-jobs                    → 403
POST /api/analysis-jobs/{id}/execute       → 403
PATCH /api/admin/users/{id}/access         → 403
DELETE /api/admin/audit-logs/purge          → 403
```

## 5. Vercel 연결

Vercel 프론트의 `/api/:path*` rewrite 목적지를 Render 백엔드 주소로 변경한다.

예시:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://eduscope-api.example.onrender.com/api/:path*"
    },
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

실제 Render 주소로 바꾼 뒤 Vercel Production을 다시 배포한다.

```powershell
cd C:\EduScope\eduscope-frontend
vercel --prod
```

브라우저 개발자 도구의 Network에서 `/api/auth/csrf` 응답이 `200`이고 로그인 응답에
`Set-Cookie: JSESSIONID=...; Secure; HttpOnly`가 포함되는지 확인한다. 프론트와 API를
모두 같은 Vercel 주소의 `/api` 경로로 호출해야 세션 쿠키가 안정적으로 유지된다.

## 6. 프론트 권한 처리

`/api/auth/me`의 `roles`에 `DEMO_ADMIN`이 있으면 관리자 메뉴와 조회 화면은 표시한다.

다음 버튼은 숨기거나 비활성화한다.

```text
Job 요청
Job 실행
Job 재실행
Dataset 등록·수정·삭제
사용자 승인·잠금·Role 변경
Audit Purge
```

버튼에는 다음 안내를 표시한다.

```text
공개 데모에서는 조회만 가능합니다.
```

## 7. 남은 입력물

실제 공개 배포를 완료하려면 다음 두 입력물이 필요하다.

1. `eduscope-frontend` 소스
2. 공개 가능한 Oracle 데이터 스냅샷 또는 Cloud DB 접속정보

이 둘이 준비되면 프론트 권한 UI, Vercel rewrite, Render 환경변수, 로그인과 403 보안 흐름을 최종 연결할 수 있다.

## 8. Windows 최종 검증

```powershell
cd C:\EduScope\eduscope-web
.\mvnw.cmd -Pci clean verify
```

로컬에서 `demo` Profile을 HTTP로 직접 시험할 때만 Secure Cookie를 임시 해제한다.
공개 HTTPS 배포에서는 반드시 `true`를 유지한다.

```powershell
$env:SPRING_PROFILES_ACTIVE = "demo"
$env:SERVER_SERVLET_SESSION_COOKIE_SECURE = "false"
.\mvnw.cmd spring-boot:run
```
