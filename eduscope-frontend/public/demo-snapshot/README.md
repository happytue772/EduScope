# EduScope Demo Snapshot

이 디렉터리는 Oracle Autonomous DB를 삭제하거나 정지한 이후에도
Vercel에서 기존 분석 결과를 읽기 전용으로 보여주기 위한 Snapshot 저장 위치입니다.

## 원칙

- 임의 데이터 생성 금지
- 현재 EduScope API가 실제 Oracle DB에서 반환한 값만 저장
- APP_USER, 비밀번호 Hash, Audit Log, IP 주소 등 운영/개인정보는 Snapshot에 포함하지 않음
- Snapshot 모드에서는 POST / PUT / PATCH / DELETE 요청을 허용하지 않음

## 생성

프로젝트 루트에서 다음 스크립트를 실행합니다.

    .\scripts\export-demo-snapshot.ps1

생성이 완료되면 이 디렉터리에 snapshot.json이 생성됩니다.

Oracle DB를 삭제하기 전에 반드시 Snapshot 화면 검증까지 완료해야 합니다.


## 대용량 Snapshot 분할

학생 상세 데이터가 포함된 snapshot.json이 50 MB 이상이면
브라우저 초기 로딩과 GitHub 파일 크기 측면에서 비효율적이므로
다음 스크립트로 500명 단위 Shard로 분할합니다.

    .\scripts\split-demo-snapshot.ps1 -ShardSize 500

분할 후 구조:

    demo-snapshot/
      snapshot.json
      students/
        page-001.json
        page-002.json
        ...

snapshot.json에는 공통 응답과 학생 검색 Index만 남고,
학생 상세는 해당 Shard 파일을 클릭 시점에만 불러옵니다.

분할 후 반드시 다음 검증을 수행합니다.

    .\scripts\validate-demo-snapshot.ps1
