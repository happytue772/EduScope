SET SQLBLANKLINES ON

-- ============================================================
-- EduScope
-- 02_dataset.sql
-- DATASET / DATASET_FILE
-- ============================================================

CREATE TABLE DATASET (
    dataset_id NUMBER NOT NULL,
    display_name VARCHAR2(150) NOT NULL,
    description VARCHAR2(1000),
    source_type VARCHAR2(20) DEFAULT 'REAL' NOT NULL,
    source_name VARCHAR2(200) NOT NULL,
    source_url VARCHAR2(1000),
    dataset_version VARCHAR2(30) NOT NULL,
    schema_version VARCHAR2(30) NOT NULL,
    hdfs_base_path VARCHAR2(1000),
    file_count NUMBER DEFAULT 0 NOT NULL,
    is_deleted CHAR(1) DEFAULT 'N' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_DATASET PRIMARY KEY (dataset_id),
    CONSTRAINT UK_DATASET_SRC_VER UNIQUE (source_name, dataset_version),
    CONSTRAINT CK_DATASET_SOURCE CHECK (source_type = 'REAL'),
    CONSTRAINT CK_DATASET_DELETED CHECK (is_deleted IN ('Y', 'N'))
);

CREATE INDEX IX_DATASET_SRC_DEL
ON DATASET (source_type, is_deleted);

CREATE TABLE DATASET_FILE (
    dataset_file_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    file_type VARCHAR2(40) NOT NULL,
    original_file_name VARCHAR2(255) NOT NULL,
    file_size_bytes NUMBER,
    record_count NUMBER,
    content_hash VARCHAR2(64),
    hdfs_raw_path VARCHAR2(1000),
    hdfs_clean_path VARCHAR2(1000),
    load_status VARCHAR2(30) DEFAULT 'REGISTERED' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_DATASET_FILE PRIMARY KEY (dataset_file_id),
    CONSTRAINT UK_DS_FILE_TYPE UNIQUE (dataset_id, file_type),
    CONSTRAINT CK_DS_FILE_TYPE CHECK (file_type IN ('COURSES','ASSESSMENTS','VLE','STUDENT_INFO','STUDENT_REGISTRATION','STUDENT_ASSESSMENT','STUDENT_VLE')),
    CONSTRAINT CK_DS_FILE_STATUS CHECK (load_status IN ('REGISTERED','VALIDATED','HDFS_STORED','FAILED'))
);

CREATE INDEX IX_DS_FILE_HASH
ON DATASET_FILE (content_hash);

CREATE INDEX IX_DS_FILE_STATUS
ON DATASET_FILE (load_status);
