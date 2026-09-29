-- ============================================================
-- EduScope DEMO_ADMIN Role CHECK 확장
-- 기존 Role은 유지하고 DEMO_ADMIN만 추가한다.
-- ============================================================

ALTER TABLE APP_ROLE
DROP CONSTRAINT CK_APP_ROLE_CODE;

ALTER TABLE APP_ROLE
ADD CONSTRAINT CK_APP_ROLE_CODE
CHECK (
    ROLE_CODE IN (
        'VIEWER',
        'ANALYST',
        'ADMIN',
        'DEMO_ADMIN'
    )
);