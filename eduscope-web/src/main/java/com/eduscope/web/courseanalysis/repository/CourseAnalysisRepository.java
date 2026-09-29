package com.eduscope.web.courseanalysis.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.courseanalysis.dto.CourseAnalysisResponse;

/**
 * 강의 분석 전용 조회 Repository.
 *
 * 기존 Oracle 테이블을 변경하지 않고
 * 여러 통계 테이블을 조합해서 조회한다.
 */
@Repository
public class CourseAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CourseAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 강의 기본정보.
     */
    public CourseAnalysisResponse.CourseInfo findCourse(
            Long coursePresentationId) {

        String sql = """
            SELECT
                COURSE_PRESENTATION_ID,
                DATASET_ID,
                CODE_MODULE,
                CODE_PRESENTATION,
                MODULE_PRESENTATION_LENGTH
            FROM COURSE_PRESENTATION
            WHERE COURSE_PRESENTATION_ID = :coursePresentationId
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId
            ),
            (rs, rowNum) ->
                new CourseAnalysisResponse.CourseInfo(
                    rs.getLong("COURSE_PRESENTATION_ID"),
                    rs.getLong("DATASET_ID"),
                    rs.getString("CODE_MODULE"),
                    rs.getString("CODE_PRESENTATION"),
                    rs.getInt("MODULE_PRESENTATION_LENGTH")
                )
        );
    }

    /**
     * 최신 COURSE_ACTIVITY 분석 결과.
     */
    public CourseAnalysisResponse.ActivitySummary findActivity(
            Long coursePresentationId,
            Long datasetId) {

        String sql = """
            SELECT
                S.JOB_ID,
                S.ACTIVE_STUDENT_COUNT,
                S.TOTAL_CLICK_COUNT,
                S.AVG_CLICK_PER_STUDENT,
                S.ACTIVE_DAY_COUNT
            FROM COURSE_ACTIVITY_STAT S
            WHERE S.COURSE_PRESENTATION_ID = :coursePresentationId
              AND S.JOB_ID = (
                    SELECT MAX(J.JOB_ID)
                    FROM ANALYSIS_JOB J
                    WHERE J.DATASET_ID = :datasetId
                      AND J.ANALYSIS_TYPE = 'COURSE_ACTIVITY'
                      AND J.STATUS = 'SUCCESS'
                      AND J.RESULT_IMPORTED_YN = 'Y'
              )
            """;

        List<CourseAnalysisResponse.ActivitySummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "coursePresentationId",
                    coursePresentationId,
                    "datasetId",
                    datasetId
                ),
                (rs, rowNum) ->
                    new CourseAnalysisResponse.ActivitySummary(
                        rs.getLong("JOB_ID"),
                        rs.getLong("ACTIVE_STUDENT_COUNT"),
                        rs.getLong("TOTAL_CLICK_COUNT"),
                        rs.getBigDecimal("AVG_CLICK_PER_STUDENT"),
                        rs.getLong("ACTIVE_DAY_COUNT")
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }

    /**
     * 최신 COURSE_RESULT 결과.
     */
    public CourseAnalysisResponse.ResultSummary findResult(
            Long coursePresentationId,
            Long datasetId) {

        String sql = """
            SELECT
                S.JOB_ID,
                S.STUDENT_COUNT,
                S.PASS_COUNT,
                S.FAIL_COUNT,
                S.WITHDRAWN_COUNT,
                S.DISTINCTION_COUNT,
                S.PASS_RATE,
                S.FAIL_RATE,
                S.WITHDRAWN_RATE,
                S.DISTINCTION_RATE
            FROM COURSE_RESULT_STAT S
            WHERE S.COURSE_PRESENTATION_ID = :coursePresentationId
              AND S.JOB_ID = (
                    SELECT MAX(J.JOB_ID)
                    FROM ANALYSIS_JOB J
                    WHERE J.DATASET_ID = :datasetId
                      AND J.ANALYSIS_TYPE = 'COURSE_RESULT'
                      AND J.STATUS = 'SUCCESS'
                      AND J.RESULT_IMPORTED_YN = 'Y'
              )
            """;

        List<CourseAnalysisResponse.ResultSummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "coursePresentationId",
                    coursePresentationId,
                    "datasetId",
                    datasetId
                ),
                (rs, rowNum) ->
                    new CourseAnalysisResponse.ResultSummary(
                        rs.getLong("JOB_ID"),
                        rs.getLong("STUDENT_COUNT"),
                        rs.getLong("PASS_COUNT"),
                        rs.getLong("FAIL_COUNT"),
                        rs.getLong("WITHDRAWN_COUNT"),
                        rs.getLong("DISTINCTION_COUNT"),
                        rs.getBigDecimal("PASS_RATE"),
                        rs.getBigDecimal("FAIL_RATE"),
                        rs.getBigDecimal("WITHDRAWN_RATE"),
                        rs.getBigDecimal("DISTINCTION_RATE")
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }

    /**
     * 최신 REGISTRATION 결과.
     */
    public CourseAnalysisResponse.RegistrationSummary findRegistration(
            Long coursePresentationId,
            Long datasetId) {

        String sql = """
            SELECT
                S.JOB_ID,
                S.REGISTRATION_COUNT,
                S.UNREGISTRATION_COUNT,
                S.WITHDRAWN_RESULT_COUNT,
                S.UNREGISTRATION_RATE,
                S.AVG_REGISTRATION_DAY,
                S.AVG_UNREGISTRATION_DAY
            FROM REGISTRATION_STAT S
            WHERE S.COURSE_PRESENTATION_ID = :coursePresentationId
              AND S.JOB_ID = (
                    SELECT MAX(J.JOB_ID)
                    FROM ANALYSIS_JOB J
                    WHERE J.DATASET_ID = :datasetId
                      AND J.ANALYSIS_TYPE = 'REGISTRATION'
                      AND J.STATUS = 'SUCCESS'
                      AND J.RESULT_IMPORTED_YN = 'Y'
              )
            """;

        List<CourseAnalysisResponse.RegistrationSummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "coursePresentationId",
                    coursePresentationId,
                    "datasetId",
                    datasetId
                ),
                (rs, rowNum) ->
                    new CourseAnalysisResponse.RegistrationSummary(
                        rs.getLong("JOB_ID"),
                        rs.getLong("REGISTRATION_COUNT"),
                        rs.getLong("UNREGISTRATION_COUNT"),
                        rs.getLong("WITHDRAWN_RESULT_COUNT"),
                        rs.getBigDecimal("UNREGISTRATION_RATE"),
                        rs.getBigDecimal("AVG_REGISTRATION_DAY"),
                        rs.getBigDecimal("AVG_UNREGISTRATION_DAY")
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }

    /**
     * 최신 주차별 활동 데이터.
     *
     * 음수 상대주차도 원본 분석 결과이므로 그대로 반환한다.
     */
    public List<CourseAnalysisResponse.WeeklyActivity> findWeeklyActivity(
            Long coursePresentationId,
            Long datasetId) {

        String sql = """
            SELECT
                S.RELATIVE_WEEK_NO,
                S.ACTIVE_STUDENT_COUNT,
                S.TOTAL_CLICK_COUNT,
                S.AVG_CLICK_PER_STUDENT,
                S.ACTIVE_MATERIAL_COUNT
            FROM COURSE_WEEKLY_ACTIVITY_STAT S
            WHERE S.COURSE_PRESENTATION_ID = :coursePresentationId
              AND S.JOB_ID = (
                    SELECT MAX(J.JOB_ID)
                    FROM ANALYSIS_JOB J
                    WHERE J.DATASET_ID = :datasetId
                      AND J.ANALYSIS_TYPE = 'COURSE_WEEKLY_ACTIVITY'
                      AND J.STATUS = 'SUCCESS'
                      AND J.RESULT_IMPORTED_YN = 'Y'
              )
            ORDER BY S.RELATIVE_WEEK_NO
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId,
                "datasetId",
                datasetId
            ),
            (rs, rowNum) ->
                new CourseAnalysisResponse.WeeklyActivity(
                    rs.getInt("RELATIVE_WEEK_NO"),
                    rs.getLong("ACTIVE_STUDENT_COUNT"),
                    rs.getLong("TOTAL_CLICK_COUNT"),
                    rs.getBigDecimal("AVG_CLICK_PER_STUDENT"),
                    rs.getLong("ACTIVE_MATERIAL_COUNT")
                )
        );
    }
}