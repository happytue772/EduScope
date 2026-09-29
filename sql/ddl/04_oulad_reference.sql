SET SQLBLANKLINES ON

-- ============================================================
-- EduScope
-- 04_oulad_reference.sql
-- OULAD 관계형 기준 데이터
-- ============================================================

-- 6. courses.csv
CREATE TABLE COURSE_PRESENTATION (
    course_presentation_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    code_module VARCHAR2(20) NOT NULL,
    code_presentation VARCHAR2(20) NOT NULL,
    module_presentation_length NUMBER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_COURSE_PRESENT PRIMARY KEY (course_presentation_id),
    CONSTRAINT UK_COURSE_PRESENT UNIQUE (dataset_id, code_module, code_presentation)
);

CREATE INDEX IX_COURSE_CODE
ON COURSE_PRESENTATION (code_module, code_presentation);


-- 7. OULAD 익명 학생
CREATE TABLE OULAD_STUDENT (
    student_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    source_student_id NUMBER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_OULAD_STUDENT PRIMARY KEY (student_id),
    CONSTRAINT UK_OULAD_STUDENT UNIQUE (dataset_id, source_student_id)
);


-- 8. studentInfo.csv
CREATE TABLE STUDENT_COURSE (
    student_course_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    student_id NUMBER NOT NULL,
    gender VARCHAR2(10),
    region VARCHAR2(100),
    highest_education VARCHAR2(100),
    imd_band VARCHAR2(30),
    age_band VARCHAR2(30),
    num_of_prev_attempts NUMBER,
    studied_credits NUMBER,
    disability CHAR(1),
    final_result VARCHAR2(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_STUDENT_COURSE PRIMARY KEY (student_course_id),
    CONSTRAINT UK_STUDENT_COURSE UNIQUE (course_presentation_id, student_id),
    CONSTRAINT CK_ST_COURSE_RESULT CHECK (final_result IN ('Pass','Fail','Withdrawn','Distinction')),
    CONSTRAINT CK_ST_COURSE_DIS CHECK (disability IS NULL OR disability IN ('Y','N'))
);

CREATE INDEX IX_ST_COURSE_RESULT
ON STUDENT_COURSE (course_presentation_id, final_result);

CREATE INDEX IX_ST_FINAL_RESULT
ON STUDENT_COURSE (final_result);


-- 9. studentRegistration.csv
CREATE TABLE STUDENT_REGISTRATION (
    student_course_id NUMBER NOT NULL,
    registration_day NUMBER,
    unregistration_day NUMBER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ST_REGISTRATION PRIMARY KEY (student_course_id)
);

CREATE INDEX IX_ST_REG_UNREG_DAY
ON STUDENT_REGISTRATION (unregistration_day);


-- 10. assessments.csv
CREATE TABLE ASSESSMENT (
    assessment_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    source_assessment_id NUMBER NOT NULL,
    assessment_type VARCHAR2(20) NOT NULL,
    assessment_due_day NUMBER,
    assessment_weight NUMBER(7,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ASSESSMENT PRIMARY KEY (assessment_id),
    CONSTRAINT UK_ASSESS_SOURCE UNIQUE (dataset_id, source_assessment_id),
    CONSTRAINT CK_ASSESS_TYPE CHECK (assessment_type IN ('TMA','CMA','Exam'))
);

CREATE INDEX IX_ASSESS_COURSE_TYPE
ON ASSESSMENT (course_presentation_id, assessment_type);


-- 11. studentAssessment.csv
CREATE TABLE STUDENT_ASSESSMENT (
    student_assessment_id NUMBER NOT NULL,
    student_course_id NUMBER NOT NULL,
    assessment_id NUMBER NOT NULL,
    submitted_day NUMBER NOT NULL,
    is_banked CHAR(1) NOT NULL,
    score NUMBER(7,2),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_ST_ASSESSMENT PRIMARY KEY (student_assessment_id),
    CONSTRAINT UK_ST_ASSESSMENT UNIQUE (student_course_id, assessment_id),
    CONSTRAINT CK_ST_ASSESS_BANKED CHECK (is_banked IN ('Y','N'))
);

CREATE INDEX IX_ST_ASSESS_ID
ON STUDENT_ASSESSMENT (assessment_id);

CREATE INDEX IX_ST_ASSESS_SCORE
ON STUDENT_ASSESSMENT (score);


-- 12. vle.csv
CREATE TABLE VLE_MATERIAL (
    vle_material_id NUMBER NOT NULL,
    dataset_id NUMBER NOT NULL,
    course_presentation_id NUMBER NOT NULL,
    source_site_id NUMBER NOT NULL,
    activity_type VARCHAR2(100) NOT NULL,
    week_from NUMBER,
    week_to NUMBER,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_VLE_MATERIAL PRIMARY KEY (vle_material_id),
    CONSTRAINT UK_VLE_MATERIAL UNIQUE (dataset_id, course_presentation_id, source_site_id)
);

CREATE INDEX IX_VLE_ACTIVITY_TYPE
ON VLE_MATERIAL (activity_type);