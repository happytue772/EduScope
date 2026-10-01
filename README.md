# EduScope

> **OULAD 학습 데이터를 HDFS → Java MapReduce → Spring Batch → Oracle → Spring Boot → React로 연결하고, Live 배포와 Oracle 독립형 Read-Only Snapshot 데모까지 구성한 학습 데이터 분석 플랫폼**

---

## 1. 왜 만들었는가

EduScope는 단순히 분석 결과를 차트로 보여주는 프로젝트가 아니라, **대용량 원본 데이터가 서비스 화면에 도달하기까지의 전체 흐름**을 직접 연결해보는 것을 목표로 만들었습니다.

프로젝트에서 해결하려고 한 문제는 크게 네 가지였습니다.

1. 대용량 학습 활동 원본을 서비스 DB에 그대로 넣지 않고 어떻게 처리할 것인가
2. Hadoop 분석 결과를 어떻게 Oracle의 서비스 데이터로 안전하게 적재할 것인가
3. 분석 Job의 성공/실패/재실행/적재 상태를 어떻게 추적할 것인가
4. 실제 공개 배포에서는 운영 DB를 보호하면서도 포트폴리오 화면을 계속 보여줄 수 있는가

최종적으로 **Live Mode와 Snapshot Mode를 함께 유지**하는 구조로 확장했습니다.

---

## 2. 전체 구조

### 데이터 처리 흐름

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

### Live 배포 흐름

```text
사용자
  ↓
Vercel
React Frontend
  ↓
Render
Spring Boot Backend
  ↓
Oracle Autonomous DB
```

### 공개 Snapshot 흐름

```text
사용자
  ↓
Vercel
  ↓
snapshot.json
  ↓
students/page-xxx.json
  ↓
React Dashboard
```

Snapshot Mode에서는 Render/Oracle 없이도 저장된 실제 분석 결과를 조회할 수 있고, 변경성 요청은 차단합니다.

---

## 3. 기술을 왜 사용했는가

기술을 많이 쓰는 것보다 **왜 이 기술을 이 위치에 사용했는지 설명할 수 있는 구조**를 우선했습니다.

| 기술 | 적용한 이유 |
|---|---|
| **Java 17 + Spring Boot** | REST API, 인증/인가, Analysis Job 상태 관리, 운영 기능을 계층적으로 구성하기 위해 사용 |
| **Oracle** | 사용자/RBAC, 기준정보, Job 이력, 분석 결과처럼 관계와 무결성이 중요한 서비스 데이터를 관리 |
| **HDFS + Java MapReduce** | 규모가 큰 학습 활동 원본을 Oracle에 중복 저장하지 않고 분산 저장·집계 |
| **Spring Batch** | MapReduce 결과를 검증한 뒤 Oracle `*_STAT` 테이블로 적재하고 실제 적재 건수를 확인 |
| **Spring Security** | 화면 버튼 숨김이 아니라 Backend에서 Role/CSRF/Session을 실제로 검증 |
| **React + Vite** | 분석 결과와 운영 상태를 빠르게 확인할 수 있는 SPA Dashboard 구성 |
| **Render** | Spring Boot Backend를 Docker 기반으로 외부에 배포 |
| **Vercel** | React Frontend 배포와 GitHub main 기반 자동 Production 배포 |
| **Static Snapshot** | 운영 DB가 없어도 실제 분석 결과를 읽기 전용으로 보여주고 공개 데모의 변경 위험을 줄이기 위해 추가 |

---

## 4. 데이터베이스 설계

현재 확정 DB는 **24개 핵심 테이블**로 구성했습니다.

| 영역 | 주요 테이블 |
|---|---|
| Dataset | `DATASET`, `DATASET_FILE` |
| 사용자 / RBAC | `APP_USER`, `APP_ROLE`, `APP_USER_ROLE` |
| OULAD 기준정보 | `COURSE_PRESENTATION`, `OULAD_STUDENT`, `STUDENT_COURSE`, `STUDENT_REGISTRATION`, `ASSESSMENT`, `STUDENT_ASSESSMENT`, `VLE_MATERIAL` |
| Job 관리 | `ANALYSIS_JOB` |
| 분석 결과 | `STUDENT_ACTIVITY_STAT`, `COURSE_ACTIVITY_STAT`, `COURSE_WEEKLY_ACTIVITY_STAT`, `VLE_ACTIVITY_STAT`, `ASSESSMENT_STAT`, `REGISTRATION_STAT`, `COURSE_RESULT_STAT`, `ACTIVITY_RESULT_STAT`, `STUDENT_LEARNING_SUMMARY_STAT` |
| 품질 / 운영 | `DATA_QUALITY_STAT`, `AUDIT_LOG` |

중요한 모델링 원칙은 다음과 같습니다.

```text
APP_USER
= EduScope 웹서비스 로그인 사용자

OULAD_STUDENT
= OULAD 익명 학습 데이터의 학생
```

두 Domain을 임의로 연결하지 않았습니다.

또한 `studentVle`처럼 큰 원본 활동 로그는 Oracle에 그대로 적재하지 않고 HDFS에 보존하고, Oracle에는 서비스에서 필요한 기준정보와 분석 결과를 저장했습니다.

---

## 5. Hadoop 분석과 Analysis Job

현재 분석 구조는 다음 10종을 기준으로 구성했습니다.

```text
STUDENT_ACTIVITY
COURSE_ACTIVITY
COURSE_WEEKLY_ACTIVITY
VLE_ACTIVITY
ASSESSMENT
REGISTRATION
COURSE_RESULT
ACTIVITY_RESULT
STUDENT_LEARNING_SUMMARY
DATA_QUALITY
```

Job 실행 흐름은 다음과 같습니다.

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
HDFS Input/Output 검사
  ↓
Hadoop MapReduce
  ↓
_SUCCESS + part-* 검사
  ↓
Output Record Count 수집
  ↓
SUCCESS
```

실패 시에는 `ANALYSIS_JOB`에 다음 값을 남깁니다.

```text
STATUS = FAILED
ERROR_STEP
ERROR_MESSAGE
PROCESSING_TIME_MS
```

### 실제 검증 사례

ASSESSMENT Job #12를 실제 실행해 다음을 확인했습니다.

```text
PENDING → RUNNING → SUCCESS
HDFS Output: /user/user/eduscope/output/assessment-job-12
_SUCCESS 존재
part-r-00000 존재
Output Record Count: 188
```

반대로 존재하지 않는 HDFS Input을 사용해 실제 실패 흐름도 검증했습니다.

```text
RUNNING
  ↓
HDFS Input 검사
  ↓
INPUT_NOT_FOUND
  ↓
FAILED
```

즉 UI에서 성공/실패 상태를 임의로 만들어 보여주는 것이 아니라 **실제 Hadoop/HDFS 결과를 기준으로 상태가 바뀌도록 구성했습니다.**

---

## 6. Spring Batch 적재

MapReduce 결과를 Oracle 분석 통계 테이블로 옮기는 과정은 Spring Batch로 분리했습니다.

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
ANALYSIS_JOB.RESULT_IMPORTED_YN
```

여기서 중요하게 분리한 개념은 다음 두 상태입니다.

```text
STATUS = SUCCESS
→ Hadoop 분석 자체가 성공

RESULT_IMPORTED_YN = Y
→ 분석 결과가 Oracle에 정상 적재됨
```

분석 성공과 DB 적재 성공을 같은 의미로 취급하지 않았습니다.

---

## 7. Spring Boot / Security / 운영 기능

일반 API 흐름은 다음처럼 분리했습니다.

```text
React
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
JPA / JDBC
  ↓
Oracle
```

### 인증/인가

```text
POST /api/auth/login
        ↓
Spring Security
        ↓
CustomUserDetailsService
        ↓
APP_USER + APP_USER_ROLE + APP_ROLE
        ↓
Session
        ↓
Role별 API 접근 제어
```

적용한 내용:

- Session 기반 인증
- CSRF 보호
- BCrypt Password Hash
- Role 기반 인가
- 401 / 403 Security Handler 분리
- 로그인 성공 시 LAST_LOGIN_AT 갱신
- LOGIN / LOGIN_FAILED Audit
- Role 변경 Audit

Role은 다음처럼 사용합니다.

| Role | 주요 범위 |
|---|---|
| `VIEWER` | 일반 Dashboard / 통계 조회 |
| `ANALYST` | 학생 분석, Dataset, Data Quality, Analysis Job 조회 |
| `ADMIN` | Job 운영, Dataset 관리, 사용자/RBAC, Audit, System Health |
| `DEMO_ADMIN` | 공개 데모용 관리자 조회. 변경/분석 실행 API는 제한 |

Frontend에서 버튼을 숨기는 것만으로 보안을 처리하지 않고, Backend Spring Security에서 다시 검증합니다.

---

## 8. Audit / System Health / 예외 처리

### Audit

다음 이벤트를 실제 운영 기록 대상으로 구성했습니다.

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

DB 연결 실패를 무조건 전체 API 500으로 종료하는 대신 DB 상태를 DOWN으로 표현할 수 있도록 분리했습니다.

### 공통 예외 처리

`GlobalExceptionHandler` / `ApiErrorResponse`를 통해 Validation, Data Integrity, Method, Media Type 등의 오류 응답을 공통화하고, 401/403은 Spring Security Filter Chain에서 별도로 처리합니다.

---

## 9. 공개 배포

현재 공개 구조는 다음 두 가지 모드를 같이 유지합니다.

### Live Mode

```text
Vercel
  ↓
Render
  ↓
Spring Boot
  ↓
Oracle Autonomous DB
```

실제로 다음 흐름을 확인했습니다.

```text
Render Deploy 성공
/actuator/health = UP
Vercel → Render 연결
CSRF 정상
로그인 정상
/api/auth/me 정상
Dashboard 조회 정상
```

장시간 미접속 후 `/api/auth/csrf`에서 일시적으로 502가 발생한 적이 있었지만, 바로 재빌드하지 않고 Render Health를 먼저 확인해 Backend가 정상으로 복구되는지 판단했습니다.

### Snapshot Mode

운영 DB가 없어도 실제 분석 결과를 공개 포트폴리오에서 조회할 수 있도록 추가했습니다.

```text
Vercel
  ↓
snapshot.json
  ↓
students/page-xxx.json
  ↓
React
```

Snapshot Mode에서는:

- Backend 로그인 없이 읽기 전용 Snapshot Session 사용
- 화면에 `읽기 전용 스냅샷` 표시
- POST / PUT / PATCH / DELETE 차단
- 분석 실행 / 회원관리 / 데이터 변경 차단
- 실제 API 응답 구조를 최대한 그대로 유지

---

## 10. Snapshot 성능 문제를 어떻게 해결했는가

### 문제 1. PowerShell 직렬화 때문에 API Array 구조가 바뀜

초기 Snapshot에서 원래 배열 응답이 다음처럼 변형됐습니다.

```json
{
  "value": [...],
  "Count": 22
}
```

React는 원래 배열을 기대했기 때문에 `coursePresentationId` 접근 오류가 발생했습니다.

해결 방향은 Frontend 여러 곳에 임시 분기 코드를 넣는 것이 아니라 **Snapshot JSON 자체를 기존 Spring Boot API 응답 구조와 동일하게 보존**하는 것이었습니다.

### 문제 2. 학생 32,593명을 한 명씩 요청

초기 방식:

```text
/api/student-analysis/1
/api/student-analysis/2
...
약 32,000회 요청
```

이를 Snapshot 전용 Bulk API로 변경했습니다.

```http
GET /api/admin/snapshot/students?offset=0&limit=500
GET /api/admin/snapshot/students?offset=500&limit=500
...
```

최종 결과:

```text
Student bulk pages: 66
Student index rows: 32,593
마지막 페이지: 93명
```

기존 학생 분석 API는 그대로 유지하고 Snapshot Export에서만 Bulk API를 사용했습니다.

### 문제 3. 단일 Snapshot 파일 61.72 MB

학생 상세 전체를 한 파일에 넣으니 초기 `snapshot.json`이 **61.72 MB**까지 커졌습니다.

그래서 학생 상세를 500명 단위 Shard로 분리했습니다.

```text
demo-snapshot/
├─ snapshot.json
└─ students/
   ├─ page-001.json
   ├─ page-002.json
   ├─ ...
   └─ page-066.json
```

현재 GitHub main 기준:

```text
Core snapshot.json: 약 25.57 MB
Student shard: 66개
가장 큰 shard: 약 5.74 MB
```

학생 상세 화면을 열 때 필요한 Shard만 지연 로딩합니다.

---

## 11. Snapshot 검증 결과

Snapshot 생성 후 단순히 파일이 만들어졌다는 것으로 끝내지 않고 별도 검증 스크립트를 추가했습니다.

### 데이터/보안 검증

```text
Student details: 32,593
Student index rows: 32,593
Sensitive data check: PASS
Snapshot validation: PASS
```

검사 대상에는 다음이 포함됩니다.

```text
passwordHash
ipAddress
DB password variable
Private Key
Windows absolute path
```

### 화면 Coverage 검증

```text
[01] Dashboard: PASS
[02] Course Analysis: PASS
[03] Assessment Analysis: PASS
[04] Activity Analysis: PASS
[05] Registration Analysis: PASS
[06] Result Analysis: PASS
[07] Student Analysis: PASS
[08] Dataset: PASS
[09] Data Quality: PASS
[10] Analysis Job: PASS

Datasets checked: 2
Courses checked: 22
Student index rows checked: 32593
Snapshot 01~10 coverage validation: PASS
```

Vercel 실제 화면에서도 읽기 전용 Snapshot 표시, 학생 상세 조회, 분석 실행 차단을 확인했습니다.

---

## 12. 테스트

Java/Spring Boot 검증에서는 프로젝트의 CI Profile을 사용합니다.

```powershell
cd C:\EduScope\eduscope-web
.\mvnw.cmd -Pci clean verify
```

일반 `mvn test`에서는 Oracle 의존 통합 테스트까지 함께 실행되면서 ApplicationContext 오류가 연쇄 발생한 적이 있어, H2 기반 CI 환경과 Oracle 의존 테스트를 분리한 기존 Profile을 사용해 최종 검증했습니다.

Frontend는 다음으로 Production Build를 확인합니다.

```powershell
cd C:\EduScope\eduscope-frontend
npm run build
```

---

## 13. 실제로 겪고 해결한 문제

| 문제 | 원인 | 해결 |
|---|---|---|
| Java 17 Compile 오류 | Maven이 Java 11로 실행 | JAVA_HOME / Path를 JDK 17로 통일 |
| Port 8080 충돌 | 이전 Spring Boot 프로세스 잔존 | Listener PID 확인 후 종료 |
| `Ambiguous mapping` | 기존 Controller와 Endpoint 중복 | 신규 Controller 제거 후 기존 Mapping 기준 확장 |
| Hadoop Job 실패 | 존재하지 않는 HDFS Input | 실행 전 Input 검사 + FAILED / ERROR_STEP / ERROR_MESSAGE 저장 |
| VMware HGFS Permission | Shared Folder mount / 권한 문제 | mount 구조와 uid/gid/allow_other 설정 보정 |
| Vercel → Render 502 | 배포/연결 설정 또는 Backend 일시 응답 지연 | Render 직접 Health → Vercel 연결 순서로 분리 점검 |
| Snapshot Array Wrapper | PowerShell 직렬화로 원래 API 배열 구조 변형 | 원래 API JSON 구조 보존 방식으로 수정 |
| Snapshot Export 지연 | 학생 상세를 1명씩 약 32,593번 호출 | 500명 Bulk API 추가, 66페이지로 축소 |
| Snapshot 61.72MB | 학생 상세 전체를 단일 JSON에 포함 | 500명 단위 66 Shard + 지연 로딩 |
| PowerShell Parser Error | Windows PowerShell 5.1 멀티라인 조건식 | 조건식을 PowerShell 5.1 호환 문법으로 변경 |
| PowerShell FormatError | JSON 중괄호와 format string 충돌 | `[string]::Concat`으로 최종 JSON 조립 |

문제를 해결할 때 기존 정상 기능을 초기화하지 않고 **원인 범위만 좁혀 수정하는 방식**을 유지했습니다.

---

## 14. Repository Structure

```text
EduScope/
├─ eduscope-web/
│  └─ Spring Boot / REST / Security / 운영 기능
├─ eduscope-mapreduce/
│  └─ Java Hadoop MapReduce
├─ eduscope-loader/
│  └─ Spring Batch → Oracle 적재
├─ eduscope-frontend/
│  └─ React + Vite
├─ scripts/
│  ├─ export-demo-snapshot.ps1
│  ├─ split-demo-snapshot.ps1
│  ├─ validate-demo-snapshot.ps1
│  └─ validate-demo-snapshot-coverage.ps1
├─ sql/
├─ data/
├─ docs/
├─ docker-compose.yml
└─ README.md
```

---

## 15. 자주 사용하는 검증 명령

### Backend

```powershell
cd C:\EduScope\eduscope-web
.\mvnw.cmd -Pci clean verify
.\mvnw.cmd spring-boot:run
```

### Frontend

```powershell
cd C:\EduScope\eduscope-frontend
npm run build
npm run dev
```

### Hadoop

```bash
source /home/user/bigdata/user-eduscope-hadoop.sh
jps
hdfs dfs -ls /user/user/eduscope/output
hdfs dfs -test -e <HDFS_PATH>
echo $?
```

### Snapshot

```powershell
cd C:\EduScope

.\scripts\export-demo-snapshot.ps1 `
    -BaseUrl "https://eduscope-8ekl.onrender.com"

.\scripts\split-demo-snapshot.ps1 -ShardSize 500

.\scripts\validate-demo-snapshot.ps1

.\scripts\validate-demo-snapshot-coverage.ps1
```

---

## 16. 포트폴리오에서 강조할 포인트

이 프로젝트에서 강조하려는 것은 사용한 기술의 개수가 아니라 **기술 선택의 이유와 문제 해결 과정**입니다.

### 1) 데이터 처리와 서비스 DB 역할 분리

대용량 활동 원본은 HDFS에 두고, Oracle은 기준정보/운영정보/분석 결과 중심으로 사용했습니다.

### 2) 성공 화면만 만든 것이 아니라 실패 흐름까지 구현

Hadoop Job의 SUCCESS뿐 아니라 실제 INPUT_NOT_FOUND → FAILED 흐름과 오류 저장을 검증했습니다.

### 3) 상태를 한 단계로 뭉개지 않음

`Hadoop SUCCESS`와 `Oracle Imported`를 서로 다른 상태로 관리했습니다.

### 4) 공개 데모도 운영 구조와 분리

Live Mode는 실제 Backend/DB를 사용하고, Snapshot Mode는 읽기 전용 정적 데이터로 분리해 공개 환경의 변경 위험을 줄였습니다.

### 5) 성능 문제를 수치로 확인하고 구조를 변경

```text
학생 상세 약 32,593회 개별 요청
→ 500명 Bulk
→ 66페이지

61.72 MB 단일 Snapshot
→ Core 약 25.57 MB
→ 학생 상세 66 Shard
```

### 6) 검증 자동화

Snapshot이 만들어졌다는 사실만 확인하지 않고 데이터 건수, 민감정보, 01~10 화면 Coverage까지 별도 스크립트로 검증했습니다.

---

## 17. 30초 설명

> EduScope는 OULAD 학습 데이터를 HDFS와 Java MapReduce로 분석하고, Spring Batch로 결과를 Oracle에 적재한 뒤 Spring Boot와 React에서 조회하는 프로젝트입니다. 분석 Job의 성공/실패와 적재 상태를 분리해 추적하고 Spring Security 기반 RBAC, Audit, System Health까지 구현했습니다. 배포 단계에서는 Vercel-Render-Oracle Live 구조와 함께, 실제 32,593명의 학생 분석 결과를 500명 단위로 분할한 읽기 전용 Snapshot 데모를 추가해 Backend나 DB가 없어도 포트폴리오 조회 화면을 유지할 수 있도록 구성했습니다.

---

## 18. 현재 상태

```text
OULAD RAW → HDFS                         완료
10종 MapReduce 분석 구조                  완료
Spring Boot / React 주요 조회 화면         완료
Spring Security / RBAC                    완료
Analysis Job SUCCESS / FAILED 실제 검증    완료
Audit / System Health                     완료
Vercel → Render → Oracle Live 연결         완료
DEMO_ADMIN 공개 조회 구조                  완료
Static Snapshot Mode                      완료
학생 32,593건 Snapshot                     완료
500명 Bulk Export / 66 Shard               완료
Snapshot 민감정보 검사                     PASS
01~10 Snapshot Coverage                   PASS
```

Oracle은 현재 유지하고 있으며, Snapshot은 **Oracle이 없다고 가정했을 때도 조회 화면을 유지하기 위한 별도 공개 데모 구조**로 사용합니다.

---

## 19. 한 줄 요약

> **대용량 학습 데이터 처리, Java/Spring 백엔드, 보안·운영 기능, Cloud 배포와 DB 독립형 공개 데모까지 하나의 서비스 흐름으로 연결한 프로젝트**
