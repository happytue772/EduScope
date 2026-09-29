# EduScope

> **OULAD 학습 데이터를 HDFS → Java MapReduce → Spring Batch → Oracle → Spring Boot REST API → React로 연결한 백엔드 중심 학습 데이터 분석 플랫폼**

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-19c-F80000?logo=oracle&logoColor=white)
![Hadoop](https://img.shields.io/badge/Hadoop-2.5.1-FFCC00?logo=apachehadoop&logoColor=black)
![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

---

## 1. 프로젝트 소개

EduScope는 **OULAD(Open University Learning Analytics Dataset)** 를 활용해 학습 데이터를 수집·분석·적재·조회·운영하는 전체 흐름을 구현한 프로젝트입니다.

단순히 분석 결과를 화면에 보여주는 데 그치지 않고, 다음 백엔드 문제를 직접 다루는 것을 목표로 했습니다.

- 대용량 원본 데이터를 서비스 DB와 분리해 관리하는 방법
- Hadoop MapReduce 분석 결과를 서비스 데이터로 안전하게 적재하는 방법
- 분석 Job의 성공·실패·재실행 상태를 추적하는 방법
- Spring Security 기반 인증·인가와 역할별 접근 제어
- Audit / Health Check / 예외 처리 등 운영 기능
- Docker 기반 실행 환경과 배포 가능한 구조

### 핵심 데이터 흐름

```text
OULAD 7 CSV
    ↓
원본 검증
    ↓
HDFS RAW
    ↓
Java Hadoop MapReduce
    ↓
HDFS 분석 결과
    ↓
Windows Staging
    ↓
Spring Batch
    ↓
Oracle
    ↓
Spring Boot REST API
    ↓
React + Vite Dashboard
```

`studentVle`처럼 규모가 큰 원본 활동 로그는 Oracle에 그대로 중복 저장하지 않고 **HDFS에 보존**하고, 서비스에서 필요한 기준 데이터와 집계 결과를 Oracle에서 관리합니다.

---

## 2. Backend Portfolio Point

| 관점 | 구현 내용 |
|---|---|
| **Java Backend** | Java 17 + Spring Boot 기반 REST API / Service / Repository 계층 구성 |
| **대용량 처리** | HDFS에 원본을 저장하고 Java MapReduce로 학습 활동 데이터 집계 |
| **Batch** | Spring Batch를 이용해 MapReduce 결과를 Oracle `*_STAT` 테이블로 적재 |
| **DB 설계** | 사용자/RBAC, OULAD 기준정보, 분석 Job, 집계 결과, 품질, Audit을 포함한 24개 핵심 테이블 |
| **보안** | Spring Security Session, CSRF, BCrypt, RBAC 적용 |
| **운영성** | Analysis Job 상태 관리, Audit Log, Actuator, System Health, 공통 예외 응답 |
| **실패 대응** | 잘못된 HDFS 입력 경로를 실제로 실행해 FAILED 전환 및 오류 정보 저장 검증 |
| **테스트** | Repository / Integration / Security 테스트 구조와 CI 프로필 구성 |
| **컨테이너** | Spring Boot + React/Nginx를 Docker Compose로 실행 |
| **공개 데모 보안** | 관리자 화면을 조회할 수 있는 `DEMO_ADMIN`을 별도 설계하고 변경·분석 실행 API를 Backend에서도 제한 |

---

## 3. 기술 스택

| 영역 | 기술 | 역할 |
|---|---|---|
| Language | **Java 17** | Backend / Batch / MapReduce |
| Backend | **Spring Boot 4.1.1**, Spring MVC | REST API, 서비스 계층 |
| Persistence | **Spring Data JPA, JDBC** | Oracle 조회 및 데이터 관리 |
| Security | **Spring Security** | Session, CSRF, BCrypt, RBAC |
| Batch | **Spring Batch** | MapReduce 결과 검증 및 Oracle 적재 |
| Big Data | **Hadoop 2.5.1, HDFS, YARN, MapReduce** | 대용량 원본 저장 및 분산 집계 |
| Database | **Oracle 19c / Oracle Autonomous DB Demo** | 서비스 데이터 및 분석 결과 저장 |
| Frontend | **React + Vite** | 분석 Dashboard / 운영 UI |
| Web | **Nginx** | Frontend 정적 서빙, `/api` Proxy |
| Infra | **Windows + VMware Ubuntu + SSH** | Spring Boot → Hadoop 원격 실행 |
| Container | **Docker / Docker Compose** | Backend + Frontend 실행 환경 |
| CI | **GitHub Actions** | Maven 기반 CI 구성 |
| Cloud | **Vercel / Render 설정 / Oracle Cloud** | 공개 Demo 배포 구조 |

> 초기 요구사항에는 JSP가 포함되어 있었지만, 현재 실제 Frontend는 **React + Vite** 기준입니다.

---

## 4. Repository Structure

```text
EduScope/
├─ eduscope-web/
│  └─ Spring Boot / REST API / JPA / Security / 운영 기능
│
├─ eduscope-mapreduce/
│  └─ Java Hadoop MapReduce 분석
│
├─ eduscope-loader/
│  └─ Spring Batch → Oracle 적재
│
├─ eduscope-frontend/
│  └─ React + Vite / Nginx
│
├─ sql/
│  └─ DDL / Sequence / FK / Index / Role 관련 SQL
│
├─ docs/
│  └─ 프로젝트 문서
│
├─ .github/
│  └─ GitHub Actions
│
├─ docker-compose.yml
├─ .env.example
└─ .gitignore
```

로컬 원본 데이터, Hadoop 결과 Staging, `.env`, Wallet, Build 결과물은 Git 저장소에서 제외합니다.

---

## 5. Backend Architecture

### 일반 API 요청 흐름

```text
React
  ↓
REST Controller
  ↓
Service
  ↓
Repository
  ↓
Spring Data JPA / JDBC
  ↓
Oracle
```

Controller에는 HTTP 요청/응답을, Service에는 비즈니스 로직을, Repository에는 데이터 접근을 분리했습니다.

### 인증 / 인가 흐름

```text
POST /api/auth/login
        ↓
Spring Security
        ↓
CustomUserDetailsService
        ↓
APP_USER + APP_USER_ROLE + APP_ROLE
        ↓
Session 생성
        ↓
권한별 API 접근 제어
```

인증 관련 주요 구성:

```text
Session 기반 인증
CSRF 보호
BCrypt Password Hash
Role 기반 인가
401 / 403 Security Handler 분리
로그인 성공 시 LAST_LOGIN_AT 갱신
LOGIN Audit 기록
로그인 실패 횟수 제한
```

---

## 6. RBAC

| Role | 주요 권한 |
|---|---|
| `VIEWER` | 일반 Dashboard / 통계 조회 |
| `ANALYST` | VIEWER 기능 + 학생 분석 + Dataset / Data Quality / Analysis Job 조회 |
| `ADMIN` | ANALYST 기능 + Job 운영 + Dataset 관리 + 사용자/RBAC + Audit + 시스템 관리 |
| `DEMO_ADMIN` | 공개 포트폴리오용 관리자 조회 Role. 분석/관리 화면 조회 가능, 변경성 API는 제한 |

공개 Demo에서는 Frontend에서 버튼을 숨기거나 비활성화하는 것만으로 끝내지 않고, **Backend Spring Security에서도 권한을 다시 검증**합니다.

---

## 7. Hadoop MapReduce 분석

현재 분석 구조는 다음 10종을 기준으로 구성되어 있습니다.

| Analysis Type | 목적 |
|---|---|
| `STUDENT_ACTIVITY` | 학생별 학습활동 집계 |
| `COURSE_ACTIVITY` | 강의별 활동 집계 |
| `COURSE_WEEKLY_ACTIVITY` | 강의별 주차 활동 집계 |
| `VLE_ACTIVITY` | 학습자료별 활동 집계 |
| `ASSESSMENT` | 평가별 제출/점수 통계 |
| `REGISTRATION` | 등록/철회 통계 |
| `COURSE_RESULT` | 강의별 최종 성과 통계 |
| `ACTIVITY_RESULT` | 활동량과 최종 결과 비교 |
| `STUDENT_LEARNING_SUMMARY` | 학생별 통합 학습 요약 |
| `DATA_QUALITY` | 누락/중복 등 데이터 품질 집계 |

### Analysis Job 실행 흐름

```text
PENDING
   ↓
RUNNING
   ↓
Spring Boot 비동기 Worker
   ↓
Windows ssh.exe
   ↓
VMware Ubuntu
   ↓
HDFS Input / Output 사전 검사
   ↓
Hadoop MapReduce
   ↓
_SUCCESS + part-* 확인
   ↓
Output Record Count 수집
   ↓
SUCCESS
```

실패 시에는 다음 정보를 Oracle `ANALYSIS_JOB`에 남깁니다.

```text
STATUS = FAILED
ERROR_STEP
ERROR_MESSAGE
PROCESSING_TIME_MS
```

---

## 8. 실제 실패 시나리오 검증

성공 경로만 구현하지 않고 실패 경로도 실제로 검증했습니다.

### 성공 사례

```text
Job #12
Analysis Type : ASSESSMENT
Status        : PENDING → RUNNING → SUCCESS
HDFS Output   : 188 rows
Result File   : part-r-00000
_SUCCESS      : 확인
```

### 실패 사례

존재하지 않는 HDFS Input을 의도적으로 전달하여 다음 전환을 확인했습니다.

```text
RUNNING
  ↓
HDFS Input 검사
  ↓
INPUT_NOT_FOUND
  ↓
FAILED
```

이를 통해 UI가 임의로 성공 상태를 표시하는 것이 아니라 **실제 Hadoop 실행 결과에 따라 Job 상태가 결정**되도록 구성했습니다.

---

## 9. Spring Batch 적재 전략

MapReduce 결과는 Spring Batch를 통해 Oracle의 분석 통계 테이블로 적재합니다.

```text
MapReduce Output
      ↓
TSV / Staging
      ↓
Spring Batch Reader
      ↓
Processor
      ↓
JPA Writer
      ↓
Oracle *_STAT
      ↓
실제 적재 건수 검증
      ↓
ANALYSIS_JOB.RESULT_IMPORTED_YN = Y
```

동일 Job을 다시 실행해도 중복 적재되지 않도록 Job ID 및 기존 Key를 기준으로 **멱등성**을 고려했습니다.

---

## 10. Oracle Database

EduScope는 **24개의 핵심 테이블**을 기준으로 구성됩니다.

| 영역 | 주요 테이블 |
|---|---|
| Dataset | `DATASET`, `DATASET_FILE` |
| 사용자 / RBAC | `APP_USER`, `APP_ROLE`, `APP_USER_ROLE` |
| OULAD 기준정보 | `COURSE_PRESENTATION`, `OULAD_STUDENT`, `STUDENT_COURSE`, `STUDENT_REGISTRATION`, `ASSESSMENT`, `STUDENT_ASSESSMENT`, `VLE_MATERIAL` |
| Job 관리 | `ANALYSIS_JOB` |
| 분석 결과 | `STUDENT_ACTIVITY_STAT`, `COURSE_ACTIVITY_STAT`, `COURSE_WEEKLY_ACTIVITY_STAT`, `VLE_ACTIVITY_STAT`, `ASSESSMENT_STAT`, `REGISTRATION_STAT`, `COURSE_RESULT_STAT`, `ACTIVITY_RESULT_STAT`, `STUDENT_LEARNING_SUMMARY_STAT` |
| 품질 / 운영 | `DATA_QUALITY_STAT`, `AUDIT_LOG` |

### 중요한 모델링 원칙

```text
APP_USER
= EduScope 웹서비스 로그인 사용자

OULAD_STUDENT
= OULAD의 익명 학습 데이터 학생
```

두 개념을 임의로 연결하지 않고 별도의 Domain으로 유지했습니다.

---

## 11. 실제 Cloud Demo 데이터 검증 예시

Cloud Demo DB로 복사한 실제 데이터에서 확인한 일부 건수입니다.

| Table | Row Count |
|---|---:|
| `OULAD_STUDENT` | 28,785 |
| `STUDENT_COURSE` | 32,593 |
| `STUDENT_REGISTRATION` | 32,593 |
| `STUDENT_ASSESSMENT` | 173,912 |
| `VLE_MATERIAL` | 6,364 |
| `STUDENT_ACTIVITY_STAT` | 29,228 |
| `STUDENT_LEARNING_SUMMARY_STAT` | 29,278 |
| `COURSE_WEEKLY_ACTIVITY_STAT` | 879 |

임의의 통계 데이터를 만들어 넣지 않고 실제 로컬 Oracle에서 검증한 데이터를 기준으로 Demo DB를 구성했습니다.

---

## 12. 주요 API

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/api/auth/login` | Spring Security 로그인 |
| `GET` | `/api/auth/me` | 현재 로그인 사용자 / Role 조회 |
| `GET` | `/api/auth/csrf` | CSRF Token 조회 |
| `GET` | `/api/analysis-jobs` | Analysis Job 목록 조회 |
| `GET` | `/api/admin/users` | 사용자 / Role 조회 |
| `GET` | `/api/admin/audit-logs` | Audit Log 조회 |
| `GET` | `/api/admin/system-health` | Application / Oracle / JVM / Latest Job 상태 조회 |
| `GET` | `/actuator/health` | 서비스 Health Check |

---

## 13. Exception Handling

공통 API 오류 형식을 위해 `GlobalExceptionHandler`와 `ApiErrorResponse`를 적용했습니다.

```text
Validation Error                 → 400
Constraint Violation             → 400
Illegal Argument                 → 400
Illegal State                    → 409
Data Integrity Violation         → 409
Upload Size Exceeded             → 413
Method Not Supported             → 405
Media Type Not Supported         → 415
Unhandled Exception              → 500
```

`401 / 403`은 일반 Exception Handler가 아닌 **Spring Security Filter Chain에서 별도 처리**합니다.

---

## 14. Audit & System Health

### Audit Log

```text
LOGIN
LOGIN_FAILED
ROLE_CHANGE
Analysis Job 실행
Dataset 관리
```

### System Health

`GET /api/admin/system-health`에서 다음 정보를 조회합니다.

```text
Spring Application Status
Oracle Connection Status
Oracle Product / Version
DB Response Time
Java Version
JVM Uptime
Processor
Memory
Latest Analysis Job
```

DB 조회에 실패했다고 전체 API를 무조건 500으로 종료하지 않고, DB 상태를 `DOWN`으로 표현하도록 구성했습니다.

---

## 15. Test & Validation

기존 Backend 테스트 Baseline에서 다음 결과를 확인했습니다.

```text
Tests run: 23
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

이후 다음 테스트를 추가/확장했습니다.

```text
Database Connection Test
Database Schema Verification Test
Dashboard Integration Test
System Health Integration Test
System Health Service Test
System Health Security Test
Analysis Job Security Test
DEMO_ADMIN Security Test
```

실제 Oracle을 사용하는 Integration Test와 Spring Security Role 별 `401 / 403 / 200` 시나리오를 구분해 검증하는 구조입니다.

> DEMO_ADMIN 및 최종 배포 설정까지 포함한 최신 전체 테스트는 배포 직전 `mvnw -Pci clean verify`로 최종 재검증합니다.

---

## 16. Troubleshooting

| 문제 | 원인 | 해결 |
|---|---|---|
| `Ambiguous mapping` | 기존 Controller와 동일 Endpoint를 신규 Controller에 중복 선언 | 전체 Mapping 검색 후 기존 Controller 확장 |
| `Port 8080 already in use` | 이전 Spring Boot 프로세스 잔존 | `Get-NetTCPConnection`으로 PID 확인 후 프로세스 종료 |
| Nginx `502 Bad Gateway` | Frontend Container의 API Proxy 대상 오류 | `proxy_pass http://eduscope-web:8080`으로 수정 |
| `localhost:5173` 충돌 | Vite Node 프로세스와 Docker가 동일 포트 사용 | 기존 Node 프로세스 종료 후 Docker 포트 정상화 |
| Spring Session 로그인 문제 | Nginx를 거치며 Cookie 전달 확인 필요 | `JSESSIONID`, CSRF, `credentials: include` 흐름 확인 |
| Hadoop Job 실패 | 존재하지 않는 HDFS Input | 실행 전 Input 검사 및 `ERROR_STEP / ERROR_MESSAGE` 저장 |
| Java 17 Compile 오류 | Maven이 Java 11로 실행 | `JAVA_HOME / Path`를 JDK 17로 통일 |
| VMware HGFS 접근 오류 | Shared Folder mount / FUSE 권한 문제 | mount 상태 확인 후 경로 및 권한 구조 보정 |

---

## 17. Docker

```text
localhost:5173
    ↓
Nginx / React
    ↓ /api
eduscope-web:8080
    ↓
Oracle
```

외부 포트 구성:

```text
Frontend : 5173 → Container 80
Backend  : 8081 → Container 8080
```

실행:

```bash
docker compose up -d --build
docker compose ps
```

---

## 18. Local Run

### 환경변수

```powershell
Copy-Item .env.example .env
```

`.env`에는 자신의 개발 환경에 맞는 Oracle / Hadoop SSH 설정을 입력합니다.

> 실제 비밀번호, Wallet, Private Key는 Git에 커밋하지 않습니다.

### Backend

```powershell
cd eduscope-web
.\mvnw.cmd spring-boot:run
```

### Frontend

```powershell
cd eduscope-frontend
npm install
npm run dev
```

### Build

```powershell
# Backend
cd eduscope-web
.\mvnw.cmd clean verify

# Frontend
cd ..\eduscope-frontend
npm run build
```

---

## 19. Deployment Status

| 항목 | 상태 |
|---|---|
| Local Spring Boot + Oracle | ✅ 검증 |
| Local Hadoop / HDFS / MapReduce | ✅ 검증 |
| Spring Batch 적재 구조 | ✅ 구현 |
| React + Nginx Docker | ✅ 검증 |
| Docker Compose 통합 실행 | ✅ 검증 |
| Vercel Frontend 배포 경험 | ✅ |
| Oracle Autonomous DB Demo 연결 | ✅ 검증 |
| `DEMO_ADMIN` Cloud 계정 / Role | ✅ 구성 |
| Render Backend 설정 | ✅ 파일 구성 |
| Render ↔ Oracle ↔ Vercel 최종 공개 연결 | 🚧 최종 배포 단계 |

현재 README에서는 **완료된 것과 배포 예정인 것을 구분**해서 기록합니다.

---

## 20. Engineering Decisions

### 왜 원본 `studentVle`을 Oracle에 전부 넣지 않았는가?

대규모 활동 로그의 원본 저장과 서비스 조회 목적의 DB를 분리했습니다.

```text
HDFS
→ 대용량 Raw 데이터 보존 / 분석

Oracle
→ 서비스 기준정보 / 분석 결과 / 사용자 / Job / Audit
```

### 왜 Job 이력을 DB에 저장했는가?

```text
누가 실행했는가?
어떤 Input으로 실행했는가?
언제 시작/종료됐는가?
몇 건을 처리했는가?
성공했는가?
실패했다면 어느 단계에서 왜 실패했는가?
Oracle까지 적재됐는가?
```

이를 `ANALYSIS_JOB`에 저장하여 추적 가능하게 했습니다.

### 왜 Frontend 권한만으로 막지 않았는가?

UI 버튼을 숨기는 것은 보안이 아니기 때문에, 실제 API 접근 권한은 **Spring Security가 최종 결정**하도록 구성했습니다.

---

## 21. 앞으로의 개선

```text
Render Backend 최종 배포 및 Vercel 연동
Oracle Network ACL / TLS 운영 설정
JPA N+1 실제 측정 및 개선 결과 기록
Oracle Execution Plan / Index 개선 전후 측정
Job 비교 화면
Dataset Lineage
Audit Dashboard
```

성능 개선 항목은 실제 측정값이 확보된 경우에만 README에 결과를 추가할 예정입니다.

---

## 22. 30초 프로젝트 설명

> EduScope는 OULAD 학습 데이터를 대상으로 HDFS와 Java MapReduce를 이용해 대용량 학습 활동을 분석하고, Spring Batch로 결과를 Oracle에 적재한 뒤 Spring Boot REST API와 React에서 조회하는 백엔드 중심 프로젝트입니다. 단순 Dashboard 구현에 그치지 않고 Spring Security 기반 RBAC, Analysis Job 상태·실패 관리, Audit Log, System Health, 공통 예외 처리, Docker 환경까지 구성해 데이터 처리부터 서비스 운영까지 하나의 흐름으로 구현했습니다.

---

## 23. 핵심 한 줄

> **대용량 데이터 처리와 Java/Spring 백엔드, 보안, Batch, 운영 기능을 하나의 서비스 흐름으로 연결한 프로젝트**
