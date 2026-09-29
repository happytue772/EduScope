SET SQLBLANKLINES ON

-- ============================================================
-- EduScope
-- 05_analysis_job.sql
-- 분석 Job 관리
-- ============================================================

CREATE TABLE ANALYSIS_JOB (
    job_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    requested_by NUMBER,
    analysis_type VARCHAR2(50) NOT NULL,
    status VARCHAR2(20) NOT NULL,
    analysis_version VARCHAR2(30) NOT NULL,
    hdfs_input_path VARCHAR2(1000) NOT NULL,
    hdfs_output_path VARCHAR2(1000) NOT NULL,
    result_file_path VARCHAR2(1000),
    requested_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    input_record_count NUMBER DEFAULT 0 NOT NULL,
    output_record_count NUMBER DEFAULT 0 NOT NULL,
    valid_record_count NUMBER DEFAULT 0 NOT NULL,
    invalid_record_count NUMBER DEFAULT 0 NOT NULL,
    duplicate_record_count NUMBER DEFAULT 0 NOT NULL,
    processing_time_ms NUMBER,
    error_step VARCHAR2(100),
    error_message VARCHAR2(2000),
    result_imported_yn CHAR(1) DEFAULT 'N' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ANALYSIS_JOB PRIMARY KEY (job_id),
    CONSTRAINT CK_JOB_TYPE CHECK (
        analysis_type IN (
            'STUDENT_ACTIVITY',
            'COURSE_ACTIVITY',
            'COURSE_WEEKLY_ACTIVITY',
            'VLE_ACTIVITY',
            'ASSESSMENT',
            'REGISTRATION',
            'COURSE_RESULT',
            'ACTIVITY_RESULT',
            'STUDENT_LEARNING_SUMMARY',
            'DATA_QUALITY'
        )
    ),
    CONSTRAINT CK_JOB_STATUS CHECK (status IN ('PENDING','RUNNING','SUCCESS','FAILED')),
    CONSTRAINT CK_JOB_IMPORTED CHECK (result_imported_yn IN ('Y','N'))
);

CREATE INDEX IX_JOB_DS_REQUEST
ON ANALYSIS_JOB (dataset_id, requested_at);

CREATE INDEX IX_JOB_TYPE_STATUS
ON ANALYSIS_JOB (analysis_type, status);

CREATE INDEX IX_JOB_STATUS
ON ANALYSIS_JOB (status);