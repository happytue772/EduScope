SET SQLBLANKLINES ON

-- ============================================================
-- Dataset
-- ============================================================

ALTER TABLE DATASET_FILE
ADD CONSTRAINT FK_DS_FILE_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);


-- ============================================================
-- RBAC
-- ============================================================

ALTER TABLE APP_USER_ROLE
ADD CONSTRAINT FK_USER_ROLE_USER
FOREIGN KEY (user_id)
REFERENCES APP_USER (user_id);

ALTER TABLE APP_USER_ROLE
ADD CONSTRAINT FK_USER_ROLE_ROLE
FOREIGN KEY (role_id)
REFERENCES APP_ROLE (role_id);


-- ============================================================
-- OULAD
-- ============================================================

ALTER TABLE COURSE_PRESENTATION
ADD CONSTRAINT FK_COURSE_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    R_CONSTRAINT_NAME,
    STATUS
FROM USER_CONSTRAINTS
WHERE CONSTRAINT_TYPE = 'R'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

ALTER TABLE OULAD_STUDENT
ADD CONSTRAINT FK_STUDENT_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);

ALTER TABLE STUDENT_COURSE
ADD CONSTRAINT FK_ST_COURSE_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);

ALTER TABLE STUDENT_COURSE
ADD CONSTRAINT FK_ST_COURSE_STUDENT
FOREIGN KEY (student_id)
REFERENCES OULAD_STUDENT (student_id);

ALTER TABLE STUDENT_REGISTRATION
ADD CONSTRAINT FK_REG_ST_COURSE
FOREIGN KEY (student_course_id)
REFERENCES STUDENT_COURSE (student_course_id);

ALTER TABLE ASSESSMENT
ADD CONSTRAINT FK_ASSESS_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);

ALTER TABLE ASSESSMENT
ADD CONSTRAINT FK_ASSESS_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);

ALTER TABLE STUDENT_ASSESSMENT
ADD CONSTRAINT FK_ST_ASSESS_COURSE
FOREIGN KEY (student_course_id)
REFERENCES STUDENT_COURSE (student_course_id);

ALTER TABLE STUDENT_ASSESSMENT
ADD CONSTRAINT FK_ST_ASSESS_ASSESS
FOREIGN KEY (assessment_id)
REFERENCES ASSESSMENT (assessment_id);

ALTER TABLE VLE_MATERIAL
ADD CONSTRAINT FK_VLE_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);

ALTER TABLE VLE_MATERIAL
ADD CONSTRAINT FK_VLE_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- Analysis Job
-- ============================================================

ALTER TABLE ANALYSIS_JOB
ADD CONSTRAINT FK_JOB_DATASET
FOREIGN KEY (dataset_id)
REFERENCES DATASET (dataset_id);

ALTER TABLE ANALYSIS_JOB
ADD CONSTRAINT FK_JOB_USER
FOREIGN KEY (requested_by)
REFERENCES APP_USER (user_id);


-- ============================================================
-- STUDENT_ACTIVITY_STAT
-- ============================================================

ALTER TABLE STUDENT_ACTIVITY_STAT
ADD CONSTRAINT FK_ST_ACT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE STUDENT_ACTIVITY_STAT
ADD CONSTRAINT FK_ST_ACT_COURSE
FOREIGN KEY (student_course_id)
REFERENCES STUDENT_COURSE (student_course_id);


-- ============================================================
-- COURSE_ACTIVITY_STAT
-- ============================================================

ALTER TABLE COURSE_ACTIVITY_STAT
ADD CONSTRAINT FK_COURSE_ACT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE COURSE_ACTIVITY_STAT
ADD CONSTRAINT FK_COURSE_ACT_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- COURSE_WEEKLY_ACTIVITY_STAT
-- ============================================================

ALTER TABLE COURSE_WEEKLY_ACTIVITY_STAT
ADD CONSTRAINT FK_WEEK_ACT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE COURSE_WEEKLY_ACTIVITY_STAT
ADD CONSTRAINT FK_WEEK_ACT_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- VLE_ACTIVITY_STAT
-- ============================================================

ALTER TABLE VLE_ACTIVITY_STAT
ADD CONSTRAINT FK_VLE_ACT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE VLE_ACTIVITY_STAT
ADD CONSTRAINT FK_VLE_ACT_MATERIAL
FOREIGN KEY (vle_material_id)
REFERENCES VLE_MATERIAL (vle_material_id);


-- ============================================================
-- ASSESSMENT_STAT
-- ============================================================

ALTER TABLE ASSESSMENT_STAT
ADD CONSTRAINT FK_ASSESS_STAT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE ASSESSMENT_STAT
ADD CONSTRAINT FK_ASSESS_STAT_ASSESS
FOREIGN KEY (assessment_id)
REFERENCES ASSESSMENT (assessment_id);


-- ============================================================
-- REGISTRATION_STAT
-- ============================================================

ALTER TABLE REGISTRATION_STAT
ADD CONSTRAINT FK_REG_STAT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE REGISTRATION_STAT
ADD CONSTRAINT FK_REG_STAT_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- COURSE_RESULT_STAT
-- ============================================================

ALTER TABLE COURSE_RESULT_STAT
ADD CONSTRAINT FK_CR_STAT_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE COURSE_RESULT_STAT
ADD CONSTRAINT FK_CR_STAT_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- ACTIVITY_RESULT_STAT
-- ============================================================

ALTER TABLE ACTIVITY_RESULT_STAT
ADD CONSTRAINT FK_ACT_RES_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE ACTIVITY_RESULT_STAT
ADD CONSTRAINT FK_ACT_RES_COURSE
FOREIGN KEY (course_presentation_id)
REFERENCES COURSE_PRESENTATION (course_presentation_id);


-- ============================================================
-- STUDENT_LEARNING_SUMMARY_STAT
-- ============================================================

ALTER TABLE STUDENT_LEARNING_SUMMARY_STAT
ADD CONSTRAINT FK_LEARN_SUM_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE STUDENT_LEARNING_SUMMARY_STAT
ADD CONSTRAINT FK_LEARN_SUM_ST_COURSE
FOREIGN KEY (student_course_id)
REFERENCES STUDENT_COURSE (student_course_id);


-- ============================================================
-- DATA_QUALITY_STAT
-- ============================================================

ALTER TABLE DATA_QUALITY_STAT
ADD CONSTRAINT FK_DQ_JOB
FOREIGN KEY (job_id)
REFERENCES ANALYSIS_JOB (job_id);

ALTER TABLE DATA_QUALITY_STAT
ADD CONSTRAINT FK_DQ_FILE
FOREIGN KEY (dataset_file_id)
REFERENCES DATASET_FILE (dataset_file_id);


-- ============================================================
-- AUDIT_LOG
-- ============================================================

ALTER TABLE AUDIT_LOG
ADD CONSTRAINT FK_AUDIT_USER
FOREIGN KEY (user_id)
REFERENCES APP_USER (user_id);

SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    STATUS
FROM USER_CONSTRAINTS
WHERE CONSTRAINT_TYPE = 'R'
  AND TABLE_NAME IN (
      'ACTIVITY_RESULT_STAT',
      'STUDENT_LEARNING_SUMMARY_STAT',
      'DATA_QUALITY_STAT',
      'AUDIT_LOG'
  )
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    STATUS
FROM USER_CONSTRAINTS
WHERE CONSTRAINT_TYPE = 'R'
  AND STATUS <> 'ENABLED'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

SELECT COUNT(*) AS CORE_TABLE_COUNT
FROM USER_TABLES
WHERE TABLE_NAME IN (
    'DATASET',
    'DATASET_FILE',
    'APP_USER',
    'APP_ROLE',
    'APP_USER_ROLE',
    'COURSE_PRESENTATION',
    'OULAD_STUDENT',
    'STUDENT_COURSE',
    'STUDENT_REGISTRATION',
    'ASSESSMENT',
    'STUDENT_ASSESSMENT',
    'VLE_MATERIAL',
    'ANALYSIS_JOB',
    'STUDENT_ACTIVITY_STAT',
    'COURSE_ACTIVITY_STAT',
    'COURSE_WEEKLY_ACTIVITY_STAT',
    'VLE_ACTIVITY_STAT',
    'ASSESSMENT_STAT',
    'REGISTRATION_STAT',
    'COURSE_RESULT_STAT',
    'ACTIVITY_RESULT_STAT',
    'STUDENT_LEARNING_SUMMARY_STAT',
    'DATA_QUALITY_STAT',
    'AUDIT_LOG'
);