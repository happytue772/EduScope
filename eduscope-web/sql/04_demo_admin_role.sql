-- ============================================================
-- EduScope 공개 데모용 읽기 전용 관리자 Role
-- 여러 번 실행해도 DEMO_ADMIN은 중복 생성되지 않는다.
-- ============================================================

MERGE INTO APP_ROLE target
USING (
    SELECT
        'DEMO_ADMIN' AS ROLE_CODE,
        '데모 관리자' AS ROLE_NAME,
        '관리자 화면 조회 전용 역할. 데이터 변경 및 분석 실행 권한 없음.' AS DESCRIPTION
    FROM DUAL
) source
ON (target.ROLE_CODE = source.ROLE_CODE)

WHEN MATCHED THEN
    UPDATE SET
        target.ROLE_NAME = source.ROLE_NAME,
        target.DESCRIPTION = source.DESCRIPTION

WHEN NOT MATCHED THEN
    INSERT (
        ROLE_ID,
        ROLE_CODE,
        ROLE_NAME,
        DESCRIPTION,
        CREATED_AT
    )
    VALUES (
        SEQ_APP_ROLE_ID.NEXTVAL,
        source.ROLE_CODE,
        source.ROLE_NAME,
        source.DESCRIPTION,
        SYSTIMESTAMP
    );

COMMIT;

SELECT
    ROLE_ID,
    ROLE_CODE,
    ROLE_NAME,
    DESCRIPTION
FROM APP_ROLE
WHERE ROLE_CODE = 'DEMO_ADMIN';
