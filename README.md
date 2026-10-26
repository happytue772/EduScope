EduScope | 학습 데이터 분석 플랫폼
모드
핵심Alt+1딥 리서치Alt+2
자동사용 가능한 최적의 모델을 선택합니다
무료 모델
무료 모델0 크레딧


Readray구 LilysAI천재처럼 읽기Alt+R
OULAD 학습 데이터의 수집부터 분산 분석, DB 적재, 웹 시각화까지 Java 중심으로 연결한 프로젝트입니다. 운영 DB에 연결하지 않고도 실제 분석 결과를 확인할 수 있도록 읽기 전용 포트폴리오 데모도 구현했습니다.

핵심 성과 · OULAD 원본 7개 CSV · Hadoop 분석 10종 · Oracle 24개 핵심 테이블 · 학생 분석 데이터 32,593건 · Snapshot 주요 화면 01~10 검증 PASS
1. 프로젝트 목적
학습 활동 원본 데이터를 단순히 DB에 저장하고 차트로 표시하는 데 그치지 않고, 원본 데이터 처리부터 분석 결과 제공까지 이어지는 서비스 파이프라인을 구축하고자 했습니다.
- 데이터 처리: 대용량 활동 원본은 HDFS에 보존하고, Java MapReduce로 집계해 서비스 DB의 부담을 분리했습니다.
- 안정적인 운영: Hadoop 분석 작업의 성공 여부와 Oracle 적재 완료 여부를 별도로 관리하고, 오류가 발생한 단계와 원인을 추적하도록 구현했습니다.
- 지속 가능한 시연: 운영 DB가 중지되어도 저장된 분석 결과를 안전하게 조회할 수 있는 공개 데모를 제공했습니다.
2. 서비스 체험 및 배포
2.1 공개 데모
🔗 EduScope 웹사이트 바로가기

별도의 회원가입이나 계정 로그인 없이 Snapshot Mode에서 실제 저장된 학습 데이터와 분석 결과를 조회할 수 있습니다.

체험 방법
1. 위 링크를 통해 EduScope에 접속합니다.
2. 로그인 화면 하단의 **저장된 분석 결과 보기**를 선택합니다.
3. Dashboard와 주요 분석 화면에서 저장된 데이터를 확인합니다.
안내: 공개 데모는 조회 전용입니다. 데이터 등록·수정·삭제, 분석 실행 및 관리 기능은 제한되며, 별도의 설치나 로컬 실행 환경은 필요하지 않습니다.

2.2 개발 구성
구분	프로젝트 구성	사용 기술 및 배포
프론트엔드	eduscope-frontend	React + Vite / Vercel
백엔드	eduscope-web	Spring Boot REST API / Render
데이터 처리	eduscope-loader · eduscope-mapreduce	Spring Batch · Java Hadoop MapReduce
데이터베이스	Oracle Autonomous Database	학습 데이터·분석 결과·운영 정보 관리


서비스 제공 방식
모드	구성	특징
Live Mode	Vercel(React) → Render(Spring Boot) → Oracle Autonomous DB	실제 API·DB 기반 조회 및 권한별 운영 기능
Snapshot Mode	Vercel → snapshot.json + 학생 500명 단위 Shard → React	Render·Oracle 없이 저장된 실제 분석 데이터를 조회하며 변경 요청 차단


공개 데모는 Snapshot Mode로 제공하므로 Render 또는 Oracle DB의 실행 상태와 관계없이 저장된 분석 결과를 확인할 수 있습니다.
3. 시스템 아키텍처
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
원본 데이터를 분산 처리한 뒤 분석 결과만 Oracle에 적재하고, Spring Boot API를 통해 React 화면에 제공합니다. 공개 데모에서는 미리 검증해 저장한 Snapshot 데이터를 사용합니다.
4. 사용 데이터 및 분석 결과
OULAD 원본 CSV 7종을 사용했습니다. 원본과 분석 결과를 구분해 관리하며, 개인정보를 임의로 생성하지 않습니다.
원본 데이터	의미	주요 활용
courses.csv	강좌·개설학기·강의 기간	강좌별 조회·집계 기준
assessments.csv	과제·시험의 유형·기준일·가중치	평가 분석
vle.csv	온라인 학습자료의 종류·제공 주차	학습자료 이용 분석
studentInfo.csv	익명 학생별 수강 정보·최종 성과	학생 분석·최종 결과 분석
studentRegistration.csv	강의 시작일 기준 등록·철회 시점	등록·철회 분석
studentAssessment.csv	학생별 평가 제출·점수	평가 결과·학습 요약
studentVle.csv	학생별 학습자료 접속·클릭 활동	HDFS 원본 보존 및 분산 집계


Java MapReduce 분석 결과 10종
분석 유형	분석 내용
STUDENT_ACTIVITY / COURSE_ACTIVITY	학생별·강좌별 클릭 수와 활동 현황
COURSE_WEEKLY_ACTIVITY / VLE_ACTIVITY	주차별 활동 변화·학습자료별 이용량
ASSESSMENT / REGISTRATION	평가 점수·제출, 등록·철회 현황
COURSE_RESULT / ACTIVITY_RESULT	강좌별 최종 결과 분포·결과 그룹별 활동 비교
STUDENT_LEARNING_SUMMARY	학생별 학습활동·평가 통합 요약
DATA_QUALITY	원본 파일의 결측·중복·참조 및 형식 오류 집계


분석 결과는 각각에 대응하는 Oracle *_STAT 테이블에 적재합니다. 일부 OULAD 날짜는 강의 시작일을 0으로 하는 상대 일수이며, 최종 성과는 원본의 Pass / Fail / Withdrawn / Distinction 구분을 따릅니다. 웹 로그인 사용자와 OULAD의 익명 학생 데이터는 서로 연결하지 않았습니다.
5. 핵심 구현 기능
영역	구현 내용
학습 데이터 분석	학생·강좌·주차별 활동, VLE, 평가, 등록, 결과, 학습 요약, 데이터 품질 등 10종 Hadoop 분석
Analysis Job	PENDING → RUNNING → SUCCESS/FAILED 상태 추적, HDFS 입력·출력 및 _SUCCESS 검증, 처리 건수·실패 단계·오류 메시지 기록
분석 결과 적재	Spring Batch Reader/Processor/JPA Writer로 Oracle *_STAT 테이블 적재. 분석 성공과 적재 성공(RESULT_IMPORTED_YN)을 별도 관리
인증·권한	Spring Security 기반 Session·CSRF·BCrypt·RBAC(VIEWER, ANALYST, ADMIN, DEMO_ADMIN) 및 API 단위 접근 제어
운영·품질 관리	로그인·권한 변경 등의 Audit Log, DB 연결 상태·응답 시간·JVM 정보 등의 System Health, 공통 예외 응답
웹 화면	Dashboard, Course, Assessment, Activity, Registration, Result, Student Analysis, Dataset, Data Quality, Analysis Job 등 10개 주요 화면


DB 설계: OULAD 학습 데이터와 서비스 로그인 사용자를 별도 도메인으로 구분했습니다. 기준정보·권한·Job 이력·분석 통계·운영 기록을 24개 핵심 테이블로 관리하며, studentVle 등 대용량 원본은 Oracle에 중복 적재하지 않았습니다.
6. 주요 문제 해결 및 검증 성과
문제	해결 방법	결과
Snapshot 추출 중 PowerShell이 API 배열을 객체로 변형	가공된 JSON 대신 원본 API 응답 구조 보존	기존 React API 응답 형식 유지
학생 32,593명을 개별 API로 조회할 때 발생한 추출 지연	500건 단위 Snapshot 전용 Bulk API 신설, 기존 학생 분석 API는 유지	66페이지로 전체 학생 데이터 추출
단일 Snapshot JSON 크기가 61.72MB까지 증가	학생 상세를 500명 단위 66개 Shard로 분리하고 필요할 때 지연 로딩	핵심 snapshot.json 약 25.57MB, 최대 Shard 약 5.74MB
존재하지 않는 HDFS 입력으로 Hadoop Job 실패	실행 전 입력 경로 검증, INPUT_NOT_FOUND·ERROR_STEP·ERROR_MESSAGE 기록	실제 실패 시나리오에서 FAILED 상태 전환 검증
Controller 추가 후 Spring MVC Ambiguous mapping 충돌	중복 신규 Controller를 정리하고 기존 Controller의 Mapping 확장	기존 API 경로를 유지하면서 중복 라우팅 오류 해결
Oracle 의존 통합 테스트로 mvn test 실행 시 ApplicationContext 오류	H2 기반 CI Profile과 Oracle 의존 테스트 분리, -Pci clean verify로 검증	Oracle 연결 없이 CI 검증 성공


실제 검증 결과
- Hadoop Job: ASSESSMENT Job #12의 PENDING → RUNNING → SUCCESS 흐름과 출력 188건, _SUCCESS 및 결과 파일을 확인했습니다. 잘못된 HDFS 입력에 대한 FAILED 처리도 검증했습니다.
- Snapshot 데이터: 학생 상세 32,593건과 검색 인덱스 32,593건이 일치하며, 민감정보 검사 PASS를 확인했습니다.
- 화면 Coverage: Dashboard부터 Analysis Job까지 주요 화면 01~10 검증 PASS, 강좌 22개를 확인했습니다.
- 안전성: Snapshot에서 POST·PUT·PATCH·DELETE 요청과 분석 실행·관리 기능을 차단했습니다. Live API 및 기존 DB 구조는 유지했습니다.
7. 기술 스택 및 선택 이유
구분	기술	선택 이유
Backend	Java 17, Spring Boot, Spring Security, JPA/JDBC	API·인증/인가·Job 상태·운영 기능을 계층별로 분리
Data Engineering	HDFS, Hadoop MapReduce(Java), Spring Batch	대용량 원본의 분산 집계와 검증 가능한 DB 적재를 분리
Database	Oracle Autonomous Database	사용자·권한·Job·분석 결과의 관계 및 무결성 관리
Frontend	React, Vite	분석 대시보드와 운영 현황 조회
Deployment	Docker, Render, Vercel	백엔드·프론트엔드 분리 배포 및 정적 Snapshot 제공
Snapshot/검증	PowerShell, JSON Sharding, Maven CI Profile	실제 API 결과 보존·민감정보 검사·회귀 검증


8. 프로젝트 폴더 구조
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
9. 주요 검증 명령
아래 명령은 방문자의 서비스 이용에 필요한 실행 절차가 아니라, 개발 과정에서 사용한 빌드·데이터 검증 절차입니다.
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
한 줄 정리: HDFS/MapReduce → Spring Batch → Oracle → Spring Boot → React로 이어지는 백엔드 중심 데이터 파이프라인에, 작업 이력 추적·보안·DB 독립형 공개 데모를 더한 학습 데이터 분석 플랫폼입니다.
