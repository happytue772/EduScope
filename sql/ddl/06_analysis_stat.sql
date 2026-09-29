SET SQLBLANKLINES ON

-- ============================================================
-- 14. STUDENT_ACTIVITY_STAT
-- ============================================================

CREATE TABLE STUDENT_ACTIVITY_STAT (
    job_id NUMBER NOT NULL,
    student_course_id NUMBER NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    active_day_count NUMBER DEFAULT 0 NOT NULL,
    used_material_count NUMBER DEFAULT 0 NOT NULL,
    avg_daily_click_count NUMBER(19,4) DEFAULT 0 NOT NULL,
    first_activity_day NUMBER,
    last_activity_day NUMBER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ST_ACTIVITY PRIMARY KEY (job_id, student_course_id)
);

CREATE INDEX IX_ST_ACT_CLICK
ON STUDENT_ACTIVITY_STAT (job_id, total_click_count);


-- ============================================================
-- 15. COURSE_ACTIVITY_STAT
-- ============================================================

CREATE TABLE COURSE_ACTIVITY_STAT (
    job_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    active_student_count NUMBER DEFAULT 0 NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    avg_click_per_student NUMBER(19,4) DEFAULT 0 NOT NULL,
    active_day_count NUMBER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_COURSE_ACTIVITY PRIMARY KEY (job_id, course_presentation_id)
);

CREATE INDEX IX_COURSE_ACT_CLICK
ON COURSE_ACTIVITY_STAT (job_id, total_click_count);


-- ============================================================
-- 16. COURSE_WEEKLY_ACTIVITY_STAT
-- ============================================================

CREATE TABLE COURSE_WEEKLY_ACTIVITY_STAT (
    job_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    relative_week_no NUMBER NOT NULL,
    active_student_count NUMBER DEFAULT 0 NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    avg_click_per_student NUMBER(19,4) DEFAULT 0 NOT NULL,
    active_material_count NUMBER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_COURSE_WEEK_ACT PRIMARY KEY (job_id, course_presentation_id, relative_week_no)
);

CREATE INDEX IX_WEEK_ACT_CLICK
ON COURSE_WEEKLY_ACTIVITY_STAT (job_id, total_click_count);


-- ============================================================
-- 17. VLE_ACTIVITY_STAT
-- ============================================================

CREATE TABLE VLE_ACTIVITY_STAT (
    job_id NUMBER NOT NULL,
    vle_material_id NUMBER NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    active_student_count NUMBER DEFAULT 0 NOT NULL,
    active_day_count NUMBER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_VLE_ACTIVITY PRIMARY KEY (job_id, vle_material_id)
);

CREATE INDEX IX_VLE_ACT_CLICK
ON VLE_ACTIVITY_STAT (job_id, total_click_count);


-- ============================================================
-- 18. ASSESSMENT_STAT
-- ============================================================

CREATE TABLE ASSESSMENT_STAT (
    job_id NUMBER NOT NULL,
    assessment_id NUMBER NOT NULL,
    submission_count NUMBER DEFAULT 0 NOT NULL,
    avg_score NUMBER(7,2) DEFAULT 0 NOT NULL,
    fail_count NUMBER DEFAULT 0 NOT NULL,
    fail_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    banked_count NUMBER DEFAULT 0 NOT NULL,
    avg_submission_day NUMBER(10,2),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ASSESS_STAT PRIMARY KEY (job_id, assessment_id),
    CONSTRAINT CK_ASSESS_FAIL_RATE CHECK (fail_rate BETWEEN 0 AND 100)
);

CREATE INDEX IX_ASSESS_STAT_SCORE
ON ASSESSMENT_STAT (job_id, avg_score);

CREATE INDEX IX_ASSESS_STAT_FAIL
ON ASSESSMENT_STAT (job_id, fail_rate);


-- ============================================================
-- 19. REGISTRATION_STAT
-- ============================================================

CREATE TABLE REGISTRATION_STAT (
    job_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    registration_count NUMBER DEFAULT 0 NOT NULL,
    unregistration_count NUMBER DEFAULT 0 NOT NULL,
    withdrawn_result_count NUMBER DEFAULT 0 NOT NULL,
    unregistration_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    avg_registration_day NUMBER(10,2),
    avg_unregistration_day NUMBER(10,2),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_REGISTRATION_STAT PRIMARY KEY (job_id, course_presentation_id),
    CONSTRAINT CK_REG_UNREG_RATE CHECK (unregistration_rate BETWEEN 0 AND 100)
);

CREATE INDEX IX_REG_STAT_RATE
ON REGISTRATION_STAT (job_id, unregistration_rate);


-- ============================================================
-- 20. COURSE_RESULT_STAT
-- ============================================================

CREATE TABLE COURSE_RESULT_STAT (
    job_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    student_count NUMBER DEFAULT 0 NOT NULL,
    pass_count NUMBER DEFAULT 0 NOT NULL,
    fail_count NUMBER DEFAULT 0 NOT NULL,
    withdrawn_count NUMBER DEFAULT 0 NOT NULL,
    distinction_count NUMBER DEFAULT 0 NOT NULL,
    pass_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    fail_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    withdrawn_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    distinction_rate NUMBER(7,4) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_COURSE_RESULT PRIMARY KEY (job_id, course_presentation_id),
    CONSTRAINT CK_CR_PASS_RATE CHECK (pass_rate BETWEEN 0 AND 100),
    CONSTRAINT CK_CR_FAIL_RATE CHECK (fail_rate BETWEEN 0 AND 100),
    CONSTRAINT CK_CR_WITH_RATE CHECK (withdrawn_rate BETWEEN 0 AND 100),
    CONSTRAINT CK_CR_DIST_RATE CHECK (distinction_rate BETWEEN 0 AND 100)
);

CREATE INDEX IX_CR_PASS_RATE
ON COURSE_RESULT_STAT (job_id, pass_rate);

CREATE INDEX IX_CR_WITH_RATE
ON COURSE_RESULT_STAT (job_id, withdrawn_rate);


-- ============================================================
-- 21. ACTIVITY_RESULT_STAT
-- ============================================================

CREATE TABLE ACTIVITY_RESULT_STAT (
    job_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    final_result VARCHAR2(30) NOT NULL,
    student_count NUMBER DEFAULT 0 NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    avg_click_count NUMBER(19,4) DEFAULT 0 NOT NULL,
    avg_active_day_count NUMBER(19,4) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ACTIVITY_RESULT PRIMARY KEY (job_id, course_presentation_id, final_result),
    CONSTRAINT CK_ACT_RESULT CHECK (final_result IN ('Pass','Fail','Withdrawn','Distinction'))
);

CREATE INDEX IX_ACT_RESULT_GROUP
ON ACTIVITY_RESULT_STAT (job_id, final_result);


-- ============================================================
-- 22. STUDENT_LEARNING_SUMMARY_STAT
-- ============================================================

CREATE TABLE STUDENT_LEARNING_SUMMARY_STAT (
    job_id NUMBER NOT NULL,
    student_course_id NUMBER NOT NULL,
    total_click_count NUMBER DEFAULT 0 NOT NULL,
    active_day_count NUMBER DEFAULT 0 NOT NULL,
    used_material_count NUMBER DEFAULT 0 NOT NULL,
    submitted_assessment_count NUMBER DEFAULT 0 NOT NULL,
    avg_assessment_score NUMBER(7,2),
    failed_assessment_count NUMBER DEFAULT 0 NOT NULL,
    first_activity_day NUMBER,
    last_activity_day NUMBER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ST_LEARN_SUM PRIMARY KEY (job_id, student_course_id)
);

CREATE INDEX IX_LEARN_SUM_SCORE
ON STUDENT_LEARNING_SUMMARY_STAT (job_id, avg_assessment_score);

CREATE INDEX IX_LEARN_SUM_CLICK
ON STUDENT_LEARNING_SUMMARY_STAT (job_id, total_click_count);