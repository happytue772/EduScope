package com.eduscope.web.studentanalysis.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Snapshot Export 전용 Bulk 조회 Repository.
 * 최대 500명의 상세 구성 데이터를 페이지 단위로 조회한다.
 */
@Repository
public class StudentSnapshotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StudentSnapshotRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 학생 기본정보 + 등록 + 최신 활동/학습요약을 한 번에 조회한다.
     */
    public List<Map<String, Object>> findPage(
            int offset,
            int limit) {

        String sql = """
            WITH ACTIVITY_JOB AS (
                SELECT DATASET_ID, MAX(JOB_ID) JOB_ID
                FROM ANALYSIS_JOB
                WHERE ANALYSIS_TYPE = 'STUDENT_ACTIVITY'
                  AND STATUS = 'SUCCESS'
                  AND RESULT_IMPORTED_YN = 'Y'
                GROUP BY DATASET_ID
            ),
            LEARNING_JOB AS (
                SELECT DATASET_ID, MAX(JOB_ID) JOB_ID
                FROM ANALYSIS_JOB
                WHERE ANALYSIS_TYPE = 'STUDENT_LEARNING_SUMMARY'
                  AND STATUS = 'SUCCESS'
                  AND RESULT_IMPORTED_YN = 'Y'
                GROUP BY DATASET_ID
            )
            SELECT
                SC.STUDENT_COURSE_ID,
                SC.STUDENT_ID,
                OS.SOURCE_STUDENT_ID,
                CP.COURSE_PRESENTATION_ID,
                CP.DATASET_ID,
                CP.CODE_MODULE,
                CP.CODE_PRESENTATION,
                SC.GENDER,
                SC.REGION,
                SC.HIGHEST_EDUCATION,
                SC.IMD_BAND,
                SC.AGE_BAND,
                SC.NUM_OF_PREV_ATTEMPTS,
                SC.STUDIED_CREDITS,
                SC.DISABILITY,
                SC.FINAL_RESULT,
                SR.REGISTRATION_DAY,
                SR.UNREGISTRATION_DAY,
                AJ.JOB_ID ACTIVITY_JOB_ID,
                SAS.TOTAL_CLICK_COUNT ACT_TOTAL_CLICK_COUNT,
                SAS.ACTIVE_DAY_COUNT ACT_ACTIVE_DAY_COUNT,
                SAS.USED_MATERIAL_COUNT ACT_USED_MATERIAL_COUNT,
                SAS.AVG_DAILY_CLICK_COUNT,
                SAS.FIRST_ACTIVITY_DAY ACT_FIRST_ACTIVITY_DAY,
                SAS.LAST_ACTIVITY_DAY ACT_LAST_ACTIVITY_DAY,
                LJ.JOB_ID LEARNING_JOB_ID,
                SLS.TOTAL_CLICK_COUNT LEARN_TOTAL_CLICK_COUNT,
                SLS.ACTIVE_DAY_COUNT LEARN_ACTIVE_DAY_COUNT,
                SLS.USED_MATERIAL_COUNT LEARN_USED_MATERIAL_COUNT,
                SLS.SUBMITTED_ASSESSMENT_COUNT,
                SLS.AVG_ASSESSMENT_SCORE,
                SLS.FAILED_ASSESSMENT_COUNT,
                SLS.FIRST_ACTIVITY_DAY LEARN_FIRST_ACTIVITY_DAY,
                SLS.LAST_ACTIVITY_DAY LEARN_LAST_ACTIVITY_DAY
            FROM STUDENT_COURSE SC
            JOIN OULAD_STUDENT OS
              ON OS.STUDENT_ID = SC.STUDENT_ID
            JOIN COURSE_PRESENTATION CP
              ON CP.COURSE_PRESENTATION_ID = SC.COURSE_PRESENTATION_ID
            LEFT JOIN STUDENT_REGISTRATION SR
              ON SR.STUDENT_COURSE_ID = SC.STUDENT_COURSE_ID
            LEFT JOIN ACTIVITY_JOB AJ
              ON AJ.DATASET_ID = CP.DATASET_ID
            LEFT JOIN STUDENT_ACTIVITY_STAT SAS
              ON SAS.JOB_ID = AJ.JOB_ID
             AND SAS.STUDENT_COURSE_ID = SC.STUDENT_COURSE_ID
            LEFT JOIN LEARNING_JOB LJ
              ON LJ.DATASET_ID = CP.DATASET_ID
            LEFT JOIN STUDENT_LEARNING_SUMMARY_STAT SLS
              ON SLS.JOB_ID = LJ.JOB_ID
             AND SLS.STUDENT_COURSE_ID = SC.STUDENT_COURSE_ID
            ORDER BY
                OS.SOURCE_STUDENT_ID,
                CP.CODE_MODULE,
                CP.CODE_PRESENTATION,
                SC.STUDENT_COURSE_ID
            OFFSET :offset ROWS
            FETCH NEXT :limit ROWS ONLY
            """;

        return jdbcTemplate.queryForList(
            sql,
            Map.of(
                "offset", offset,
                "limit", limit
            )
        );
    }

    /**
     * 현재 페이지 학생들의 Assessment 이력을 한 번에 조회한다.
     */
    public List<Map<String, Object>> findAssessments(
            List<Long> studentCourseIds) {

        if (studentCourseIds.isEmpty()) {
            return List.of();
        }

        String sql = """
            SELECT
                SA.STUDENT_COURSE_ID,
                A.ASSESSMENT_ID,
                A.SOURCE_ASSESSMENT_ID,
                A.ASSESSMENT_TYPE,
                A.ASSESSMENT_DUE_DAY,
                A.ASSESSMENT_WEIGHT,
                SA.SUBMITTED_DAY,
                SA.IS_BANKED,
                SA.SCORE
            FROM STUDENT_ASSESSMENT SA
            JOIN ASSESSMENT A
              ON A.ASSESSMENT_ID = SA.ASSESSMENT_ID
            WHERE SA.STUDENT_COURSE_ID IN (:studentCourseIds)
            ORDER BY
                SA.STUDENT_COURSE_ID,
                SA.SUBMITTED_DAY,
                A.ASSESSMENT_ID
            """;

        return jdbcTemplate.queryForList(
            sql,
            Map.of(
                "studentCourseIds",
                studentCourseIds
            )
        );
    }
}
