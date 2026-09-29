-- EduScope Oracle performance baseline
-- Run in Oracle SQL Developer with F5 (Run Script) while connected as EDUSCOPE.
-- This script does not change EduScope business tables.

SET ECHO ON
SET FEEDBACK ON
SET VERIFY OFF
SET HEADING ON
SET PAGESIZE 50000
SET LINESIZE 240
SET LONG 100000
SET LONGCHUNKSIZE 100000
SET TRIMSPOOL ON
SET TIMING ON

SPOOL C:\EduScope\docs\oracle-performance-baseline-2026-09-23.log

PROMPT ============================================================
PROMPT 1. Database and schema
PROMPT ============================================================

SELECT
    SYS_CONTEXT('USERENV', 'DB_NAME')        AS db_name,
    SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS current_schema,
    SYS_CONTEXT('USERENV', 'SESSION_USER')   AS session_user
FROM dual;

PROMPT ============================================================
PROMPT 2. Row counts used by the N+1 measurement
PROMPT ============================================================

SELECT 'ANALYSIS_JOB' AS table_name, COUNT(*) AS row_count FROM analysis_job
UNION ALL
SELECT 'AUDIT_LOG', COUNT(*) FROM audit_log
UNION ALL
SELECT 'ASSESSMENT', COUNT(*) FROM assessment
UNION ALL
SELECT 'STUDENT_ASSESSMENT', COUNT(*) FROM student_assessment
UNION ALL
SELECT 'STUDENT_COURSE', COUNT(*) FROM student_course
UNION ALL
SELECT 'STUDENT_REGISTRATION', COUNT(*) FROM student_registration
UNION ALL
SELECT 'VLE_MATERIAL', COUNT(*) FROM vle_material
ORDER BY table_name;

PROMPT ============================================================
PROMPT 3. Optimizer statistics freshness
PROMPT ============================================================

SELECT
    table_name,
    num_rows,
    blocks,
    last_analyzed
FROM user_tables
WHERE table_name IN (
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
)
ORDER BY table_name;

PROMPT ============================================================
PROMPT 4. Existing indexes and indexed columns
PROMPT ============================================================

SELECT
    ui.table_name,
    ui.index_name,
    ui.uniqueness,
    ui.status,
    LISTAGG(uic.column_name, ', ')
        WITHIN GROUP (ORDER BY uic.column_position) AS index_columns
FROM user_indexes ui
JOIN user_ind_columns uic
  ON uic.index_name = ui.index_name
 AND uic.table_name = ui.table_name
WHERE ui.table_name IN (
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
)
GROUP BY
    ui.table_name,
    ui.index_name,
    ui.uniqueness,
    ui.status
ORDER BY
    ui.table_name,
    ui.index_name;

PROMPT ============================================================
PROMPT 5. Foreign-key columns without a leading index
PROMPT This is a candidate list only. Do not create indexes yet.
PROMPT ============================================================

SELECT
    c.table_name,
    c.constraint_name,
    cc.column_name AS fk_leading_column
FROM user_constraints c
JOIN user_cons_columns cc
  ON cc.constraint_name = c.constraint_name
 AND cc.table_name = c.table_name
WHERE c.constraint_type = 'R'
  AND cc.position = 1
  AND NOT EXISTS (
      SELECT 1
      FROM user_ind_columns ic
      WHERE ic.table_name = c.table_name
        AND ic.column_position = 1
        AND ic.column_name = cc.column_name
  )
ORDER BY
    c.table_name,
    c.constraint_name;

PROMPT ============================================================
PROMPT 6. Actual sample values selected from the current database
PROMPT ============================================================

COLUMN sample_dataset_id NEW_VALUE v_dataset_id NOPRINT
SELECT MIN(dataset_id) AS sample_dataset_id
FROM analysis_job;

COLUMN sample_analysis_type NEW_VALUE v_analysis_type NOPRINT
SELECT MIN(analysis_type) AS sample_analysis_type
FROM analysis_job;

SELECT
    &v_dataset_id AS sample_dataset_id,
    '&v_analysis_type' AS sample_analysis_type
FROM dual;

PROMPT ============================================================
PROMPT 7-1. AnalysisJobService.getJobs
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    aj.job_id,
    aj.dataset_id,
    aj.requested_by,
    aj.analysis_type,
    aj.status,
    aj.requested_at,
    aj.created_at
FROM analysis_job aj
ORDER BY aj.job_id DESC;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-2. Analysis job lookup by dataset and type
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    aj.job_id,
    aj.dataset_id,
    aj.analysis_type,
    aj.status,
    aj.requested_at
FROM analysis_job aj
WHERE aj.dataset_id = &v_dataset_id
  AND aj.analysis_type = '&v_analysis_type'
ORDER BY aj.job_id DESC;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-3. AuditLogService.getLogs first page with APP_USER
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    al.audit_id,
    al.user_id,
    au.login_id,
    al.action_type,
    al.target_type,
    al.target_id,
    al.created_at
FROM audit_log al
LEFT JOIN app_user au
  ON au.user_id = al.user_id
ORDER BY al.created_at DESC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-4. AssessmentService.getAssessments with course
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    a.assessment_id,
    a.source_assessment_id,
    a.assessment_type,
    a.assessment_due_day,
    a.assessment_weight,
    cp.course_presentation_id,
    cp.code_module,
    cp.code_presentation
FROM assessment a
JOIN course_presentation cp
  ON cp.course_presentation_id = a.course_presentation_id;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-5. StudentAssessmentService first page
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    sa.student_assessment_id,
    sa.student_course_id,
    sa.assessment_id,
    sa.submitted_day,
    sa.is_banked,
    sa.score,
    os.source_student_id,
    cp.code_module,
    cp.code_presentation,
    a.assessment_type
FROM student_assessment sa
JOIN student_course sc
  ON sc.student_course_id = sa.student_course_id
JOIN oulad_student os
  ON os.student_id = sc.student_id
JOIN course_presentation cp
  ON cp.course_presentation_id = sc.course_presentation_id
JOIN assessment a
  ON a.assessment_id = sa.assessment_id
ORDER BY sa.student_assessment_id
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-6. StudentCourseService first page
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    sc.student_course_id,
    sc.student_id,
    sc.course_presentation_id,
    sc.final_result,
    os.source_student_id,
    cp.code_module,
    cp.code_presentation
FROM student_course sc
JOIN oulad_student os
  ON os.student_id = sc.student_id
JOIN course_presentation cp
  ON cp.course_presentation_id = sc.course_presentation_id
ORDER BY sc.student_course_id
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-7. StudentRegistrationService first page
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    sr.student_course_id,
    sr.registration_day,
    sr.unregistration_day,
    os.source_student_id,
    cp.code_module,
    cp.code_presentation
FROM student_registration sr
JOIN student_course sc
  ON sc.student_course_id = sr.student_course_id
JOIN oulad_student os
  ON os.student_id = sc.student_id
JOIN course_presentation cp
  ON cp.course_presentation_id = sc.course_presentation_id
ORDER BY sr.student_course_id
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT 7-8. VleMaterialService first page
PROMPT ============================================================

EXPLAIN PLAN FOR
SELECT
    vm.vle_material_id,
    vm.source_site_id,
    vm.activity_type,
    vm.week_from,
    vm.week_to,
    cp.code_module,
    cp.code_presentation
FROM vle_material vm
JOIN course_presentation cp
  ON cp.course_presentation_id = vm.course_presentation_id
ORDER BY vm.vle_material_id
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;

SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY(
    NULL,
    NULL,
    'BASIC +ROWS +BYTES +COST +PREDICATE +ALIAS'
));

PROMPT ============================================================
PROMPT Baseline collection complete
PROMPT ============================================================

SPOOL OFF
SET TIMING OFF

WITH actual_counts (table_name, actual_rows) AS (
    SELECT 'ANALYSIS_JOB', COUNT(*) FROM analysis_job
    UNION ALL
    SELECT 'AUDIT_LOG', COUNT(*) FROM audit_log
    UNION ALL
    SELECT 'ASSESSMENT', COUNT(*) FROM assessment
    UNION ALL
    SELECT 'STUDENT_ASSESSMENT', COUNT(*) FROM student_assessment
    UNION ALL
    SELECT 'STUDENT_COURSE', COUNT(*) FROM student_course
    UNION ALL
    SELECT 'STUDENT_REGISTRATION', COUNT(*) FROM student_registration
    UNION ALL
    SELECT 'VLE_MATERIAL', COUNT(*) FROM vle_material
)
SELECT
    a.table_name,
    a.actual_rows,
    t.num_rows AS statistics_rows,
    a.actual_rows - NVL(t.num_rows, 0) AS row_difference,
    t.last_analyzed
FROM actual_counts a
LEFT JOIN user_tables t
    ON t.table_name = a.table_name
ORDER BY a.table_name;