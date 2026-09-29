# EduScope DEMO_ADMIN Frontend Patch

## 반영 내용

- DEMO_ADMIN Role을 Sidebar/Header에 정상 표시
- DEMO_ADMIN이 07~14 메뉴를 조회할 수 있도록 잠금 해제
- `/admin/system-health`를 로그인 보호 Layout 내부로 이동
- AppLayout의 Outlet context로 기존 로그인 user를 AnalysisJobPage에 전달
- Analysis Job 생성/실행/재실행은 ADMIN만 가능
- DEMO_ADMIN에서는 Job 이력/상세 조회는 유지하고 명령 버튼은 비활성화
- Analysis Job 명령 Handler에도 권한 방어 로직 추가
- Admin User Role 선택 항목에 DEMO_ADMIN 추가
- System Health의 ADMIN 전용 표시 문구를 ADMIN/DEMO_ADMIN 조회용으로 정리

## 변경 파일

- `src/App.jsx`
- `src/components/layout/AppLayout.jsx`
- `src/components/layout/Sidebar.jsx`
- `src/pages/AnalysisJobPage.jsx`
- `src/pages/AdminUserPage.jsx`
- `src/pages/SystemHealthPage.jsx`

## 기존 기능 보존

VIEWER / ANALYST / ADMIN의 기존 조회 구조는 삭제하지 않고 DEMO_ADMIN 조회 권한을 추가했다.
분석 Job의 조회/Polling/필터/상세 기능은 그대로 유지한다.

## 중요: Backend 현재 정책과의 차이

이번 ZIP은 Frontend 수정본이다.
현재 적용된 Backend DEMO_ADMIN 패치는 Analysis Job뿐 아니라 Dataset 변경, 사용자 권한 변경, Audit 삭제 등 쓰기 요청도 403으로 차단하도록 설계되어 있다.
따라서 Frontend에서는 07~14 메뉴를 모두 조회할 수 있지만, Backend가 차단하는 관리자 쓰기 요청은 여전히 403이 정상이다.

최종 요구사항이 "DEMO_ADMIN은 Analysis Job 요청/실행/재실행만 금지하고 나머지 관리자 쓰기는 허용"이라면 Backend SecurityConfig/Controller 권한 정책도 별도로 변경해야 한다.

## 적용 후 검증

```powershell
cd C:\EduScope\eduscope-frontend
npm run build
npm run dev
```

DEMO_ADMIN 로그인 후 확인:

- 07~14 메뉴 잠금 해제
- 학생/데이터셋/데이터 품질/분석 작업/관리자 조회 화면 접근
- Analysis Job 신규 요청 버튼 비활성화
- PENDING 실제 실행 버튼 비활성화
- FAILED 재실행 요청 입력/버튼 비활성화
- ADMIN 로그인 시 기존 Analysis Job 명령 기능 정상 유지
