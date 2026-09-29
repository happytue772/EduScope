SET SQLBLANKLINES ON

-- ============================================================
-- EduScope
-- 03_security.sql
-- Spring Security / RBAC
-- ============================================================

CREATE TABLE APP_USER (
    user_id NUMBER NOT NULL,
    login_id VARCHAR2(100) NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    display_name VARCHAR2(100) NOT NULL,
    account_status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_APP_USER PRIMARY KEY (user_id),
    CONSTRAINT UK_APP_USER_LOGIN UNIQUE (login_id),
    CONSTRAINT CK_APP_USER_STATUS CHECK (account_status IN ('ACTIVE','LOCKED','DISABLED'))
);

CREATE INDEX IX_APP_USER_STATUS
ON APP_USER (account_status);

CREATE TABLE APP_ROLE (
    role_id NUMBER NOT NULL,
    role_code VARCHAR2(30) NOT NULL,
    role_name VARCHAR2(100) NOT NULL,
    description VARCHAR2(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_APP_ROLE PRIMARY KEY (role_id),
    CONSTRAINT UK_APP_ROLE_CODE UNIQUE (role_code),
    CONSTRAINT CK_APP_ROLE_CODE CHECK (role_code IN ('VIEWER','ANALYST','ADMIN', 'DEMO_ADMIN'))
);

CREATE TABLE APP_USER_ROLE (
    user_id NUMBER NOT NULL,
    role_id NUMBER NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_APP_USER_ROLE PRIMARY KEY (user_id, role_id)
);

CREATE INDEX IX_USER_ROLE_ROLE
ON APP_USER_ROLE (role_id);