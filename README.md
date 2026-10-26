# EduScope | 학습 데이터 분석 플랫폼

> **대용량 학습 데이터의 수집·분산 분석·DB 적재·웹 시각화를 Java 중심으로 연결하고, 운영 DB 없이도 확인할 수 있는 읽기 전용 포트폴리오 데모까지 구현한 프로젝트**

**핵심 성과** · OULAD 원본 **7개 CSV** · Hadoop 분석 **10종** · Oracle **24개 핵심 테이블** · 학생 분석 데이터 **32,593건** · Snapshot 화면 **01~10 검증 PASS**

**공개 데모:** [EduScope 접속](https://eduscope-frontend.vercel.app/login) → 로그인 화면에서 **`저장된 분석 결과 보기`** 선택 (Snapshot Mode)

## 1. 프로젝트 목적

학습 활동 원본을 DB에 모두 쌓아 차트만 보여주는 방식이 아닌 **원본 처리부터 분석 결과 제공까지 하나의 서비스 흐름**을 구축하는 것이 목표였습니다.

- **데이터 처리:** 대용량 활동 원본은 HDFS에 보존, Java MapReduce로 집계해 서비스 DB 부담을 분리
- **안정적 운영:** 분석 실행의 성공/실패와 Oracle 적재 완료 여부를 구분하고, 오류 원인을 추적
- **지속 가능한 시연:** 실제 운영 DB가 중지되더라도 저장된 분석 결과를 안전하게 조회할 수 있는 공개 데모 제공

## 2. 시스템 아키텍처

```text
OULAD 7 CSV → 원본 검증 → HDFS RAW → Java Hadoop MapReduce
                                      ↓
                              HDFS 분석 결과
                                      ↓
                             Windows Staging
                                      ↓
                              Spring Batch
                                      ↓
                           Oracle (24개 테이블)
                                      ↓
                        Spring Boot REST API
                                      ↓
                           React + Vite
```

| 제공 방식 | 구성 | 특징 |
|---|---|---|
| **Live Mode** | Vercel(React) → Render(Spring Boot) → Oracle Autonomous DB | 실제 API·DB 기반 조회 및 권한별 운영 기능 |
| **Snapshot Mode** | Vercel → `snapshot.json` + 500명 단위 학생 Shard → React | Render·Oracle 없이 **실제 저장 데이터 조회**, 변경 요청 차단 |

## 3. 사용 데이터와 분석 결과의 의미

**OULAD 실제 원본 CSV 7종**을 사용했습니다. 개인정보를 임의 생성하지 않으며 원본 데이터와 분석 결과를 구분해 관리합니다.

| 원본 데이터 | 의미 | 주요 활용 |
|---|---|---|
| `courses.csv` | 강좌·개설학기·강의 기간 | 강좌별 조회·집계 기준 |
| `assessments.csv` | 과제·시험의 유형·기준일·가중치 | 평가 분석 |
| `vle.csv` | 온라인 학습자료의 종류·제공 주차 | 학습자료 이용 분석 |
| `studentInfo.csv` | 익명 학생별 수강 정보·최종 성과 | 학생 분석·최종 결과 분석 |
| `studentRegistration.csv` | 강의 시작일 기준 등록·철회 시점 | 등록·철회 분석 |
| `studentAssessment.csv` | 학생별 평가 제출·점수 | 평가 결과·학습 요약 |
| `studentVle.csv` | 학생별 학습자료 접속·클릭 활동 | HDFS 원본 보존 → 분산 집계 |

**Java MapReduce 분석 결과 10종:**

| 분석 유형 | 의미 |
|---|---|
| `STUDENT_ACTIVITY` / `COURSE_ACTIVITY` | 학생별·강좌별 클릭 수와 활동 현황 |
| `COURSE_WEEKLY_ACTIVITY` / `VLE_ACTIVITY` | 주차별 활동 변화·학습자료별 이용량 |
| `ASSESSMENT` / `REGISTRATION` | 평가 점수·제출, 등록·철회 현황 |
| `COURSE_RESULT` / `ACTIVITY_RESULT` | 강좌별 최종 결과 분포·결과 그룹별 활동 비교 |
| `STUDENT_LEARNING_SUMMARY` | 학생별 학습활동·평가 통합 요약 |
| `DATA_QUALITY` | 원본 파일의 결측·중복·참조/형식 오류 집계 |

분석 결과는 각각 대응하는 Oracle `*_STAT` 테이블에 적재. 
일부 OULAD 날짜는 **강의 시작일=0 기준 상대 일수**이고 최종 성과는 원본 `Pass/Fail/Withdrawn/Distinction`을 따름. 웹 로그인 사용자와 OULAD 익명 학생은 연결하지 않음.

## 4. 핵심 구현 기능

| 영역 | 구현 내용 |
|---|---|
| **학습 데이터 분석** | 학생·강좌·주차별 활동, VLE, 평가, 등록, 결과, 학습 요약, 데이터 품질 등 **10종 Hadoop 분석** |
| **Analysis Job** | `PENDING → RUNNING → SUCCESS/FAILED` 상태 추적, HDFS 입력·출력 및 `_SUCCESS` 검증, 처리 건수·실패 단계·오류 메시지 기록 |
| **분석 결과 적재** | Spring Batch Reader/Processor/JPA Writer로 Oracle `*_STAT` 테이블 적재. **분석 성공과 적재 성공(`RESULT_IMPORTED_YN`)을 별도 관리** |
| **인증·권한** | Spring Security 기반 Session·CSRF·BCrypt·RBAC(`VIEWER`, `ANALYST`, `ADMIN`, `DEMO_ADMIN`), API 단위 접근 제어 |
| **운영·품질 관리** | 로그인/권한 변경 등 Audit Log, DB 연결 상태·응답 시간·JVM 정보 등 System Health, 공통 예외 응답 |
| **웹 화면** | Dashboard, Course/Assessment/Activity/Registration/Result/Student Analysis, Dataset, Data Quality, Analysis Job **총 10개 주요 화면** |

**DB 설계:** OULAD 학습 데이터와 서비스 로그인 사용자를 별도 도메인으로 구분하고, 기준정보·권한·Job 이력·분석 통계·운영 기록을 **24개 핵심 테이블**로 관리했습니다. `studentVle` 등 대용량 원본은 Oracle에 중복 적재하지 않았습니다.

## 5. 핵심 문제 해결 및 검증 성과

| 문제 | 해결 방법 | 결과 |
|---|---|---|
| Snapshot 추출 시 PowerShell이 API 배열을 객체로 변형 | 가공한 JSON이 아닌 **원본 API 응답 구조 보존** | 기존 React API 응답 형식 유지 |
| 학생 32,593명을 개별 API로 조회해 추출 지연 | **500건 단위 Snapshot 전용 Bulk API** 신설, 기존 학생 분석 API는 유지 | **66페이지**로 전체 학생 데이터 추출 |
| 단일 Snapshot JSON이 **61.72MB**까지 증가 | 학생 상세를 **500명 단위 66개 Shard**로 분리하고 필요 시 지연 로딩 | 핵심 `snapshot.json` **약 25.57MB**, 최대 Shard **약 5.74MB** |
| 존재하지 않는 HDFS 입력으로 Hadoop 분석 Job 실패 | 실행 전에 입력 경로를 검증하고 `INPUT_NOT_FOUND`·`ERROR_STEP`·`ERROR_MESSAGE`를 Job 이력에 저장 | **실제 실패 시나리오에서 `FAILED` 상태 전환 검증** |
| 신규 Controller 추가 후 Spring MVC `Ambiguous mapping` 충돌 | 중복 신규 Controller를 정리하고 **기존 Controller의 Mapping을 기준으로 확장** | 기존 API 경로를 유지하면서 중복 라우팅 오류 해결 |
| Oracle 의존 통합 테스트로 `mvn test` 실행 시 `ApplicationContext` 오류 | **H2 기반 CI Profile과 Oracle 의존 테스트를 분리**하고 `-Pci clean verify`로 검증 | Oracle 연결 없이 수행하는 **CI 검증 성공** |

**실제 검증 결과**

- **Hadoop Job:** ASSESSMENT Job #12 `PENDING → RUNNING → SUCCESS`, 출력 **188건**, `_SUCCESS` 및 결과 파일 확인. 잘못된 HDFS 입력에 대한 `FAILED` 처리도 검증
- **Snapshot 데이터:** 학생 상세 **32,593건 = 검색 인덱스 32,593건**, 민감정보 검사 **PASS**
- **화면 Coverage:** Dashboard부터 Analysis Job까지 **01~10 화면 검증 PASS**, 강좌 **22개** 확인
- **안전성:** Snapshot에서 POST·PUT·PATCH·DELETE와 분석 실행·관리 기능 차단. Live API와 기존 DB 구조는 유지

## 6. 기술 스택과 선택 이유

| 구분 | 기술 | 선택 이유 |
|---|---|---|
| **Backend** | Java 17, Spring Boot, Spring Security, JPA/JDBC | API·인증/인가·Job 상태·운영 기능을 계층적으로 분리 |
| **Data Engineering** | HDFS, Hadoop MapReduce(Java), Spring Batch | 대용량 원본의 분산 집계와 검증 가능한 DB 적재 분리 |
| **Database** | Oracle Autonomous Database | 사용자·권한·Job·분석 결과의 관계 및 무결성 관리 |
| **Frontend** | React, Vite | 분석 대시보드와 운영 현황 조회 |
| **Deployment** | Docker, Render, Vercel | 백엔드/프론트엔드 분리 배포 및 정적 Snapshot 제공 |
| **Snapshot/검증** | PowerShell, JSON Sharding, Maven CI Profile | 실제 API 결과 보존·민감정보 검사·회귀 검증 |

## 7. 프로젝트 폴더 구조

```text
EduScope/
├─ eduscope-web/          # Spring Boot REST API, Security, 운영 기능
├─ eduscope-mapreduce/    # Java Hadoop MapReduce 분석
├─ eduscope-loader/       # Spring Batch → Oracle 결과 적재
├─ eduscope-frontend/     # React + Vite 및 공개 Snapshot
├─ scripts/               # Snapshot 추출·분할·검증 자동화
├─ sql/                   # DB 관련 SQL
├─ data/                  # 데이터 관련 파일
├─ docs/                  # 프로젝트 문서
├─ docker-compose.yml
└─ README.md
```

## 8. 주요 검증 명령

```powershell
# Backend: Oracle 의존 테스트와 분리된 CI Profile 검증
cd eduscope-web
.\mvnw.cmd -Pci clean verify

# Frontend: 배포용 빌드 검증
cd ..\eduscope-frontend
npm run build

# Snapshot: 데이터·민감정보·화면 응답 범위 검증
cd ..
.\scripts\validate-demo-snapshot.ps1
.\scripts\validate-demo-snapshot-coverage.ps1

