package com.eduscope.web.resultanalysis.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.resultanalysis.dto.ResultAnalysisResponse;

/**
 * 최종성과 비교 전용 조회 Repository.
 */
@Repository
public class ResultAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ResultAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 강의 기본정보.
     */
    public ResultAnalysisResponse.CourseInfo findCourse(
            Long coursePresentationId) {

        String sql = """
            SELECT
                COURSE_PRESENTATION_ID,
                DATASET_ID,
                CODE_MODULE,
                CODE_PRESENTATION
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
                new ResultAnalysisResponse.CourseInfo(
                    rs.getLong("COURSE_PRESENTATION_ID"),
                    rs.getLong("DATASET_ID"),
                    rs.getString("CODE_MODULE"),
                    rs.getString("CODE_PRESENTATION")
                )
        );
    }

    /**
     * 최신 ACTIVITY_RESULT Job.
     */
    public Long findLatestActivityResultJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'ACTIVITY_RESULT'
              AND STATUS = 'SUCCESS'
              AND RESULT_IMPORTED_YN = 'Y'
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "datasetId",
                datasetId
            ),
            Long.class
        );
    }

    /**
     * 최신 COURSE_RESULT Job.
     */
    public Long findLatestCourseResultJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'COURSE_RESULT'
              AND STATUS = 'SUCCESS'
              AND RESULT_IMPORTED_YN = 'Y'
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "datasetId",
                datasetId
            ),
            Long.class
        );
    }

    /**
     * 강의 전체 최종결과 분포.
     */
    public ResultAnalysisResponse.CourseResultSummary findCourseResult(
            Long coursePresentationId,
            Long jobId) {

        String sql = """
            SELECT
                STUDENT_COUNT,
                PASS_COUNT,
                FAIL_COUNT,
                WITHDRAWN_COUNT,
                DISTINCTION_COUNT,
                PASS_RATE,
                FAIL_RATE,
                WITHDRAWN_RATE,
                DISTINCTION_RATE
            FROM COURSE_RESULT_STAT
            WHERE COURSE_PRESENTATION_ID = :coursePresentationId
              AND JOB_ID = :jobId
            """;

        List<ResultAnalysisResponse.CourseResultSummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "coursePresentationId",
                    coursePresentationId,
                    "jobId",
                    jobId
                ),
                (rs, rowNum) ->
                    new ResultAnalysisResponse.CourseResultSummary(
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
     * 최종 결과 그룹별 학습활동 비교.
     */
    public List<ResultAnalysisResponse.ResultActivityItem>
            findResultActivities(
                Long coursePresentationId,
                Long jobId) {

        String sql = """
            SELECT
                FINAL_RESULT,
                STUDENT_COUNT,
                TOTAL_CLICK_COUNT,
                AVG_CLICK_COUNT,
                AVG_ACTIVE_DAY_COUNT
            FROM ACTIVITY_RESULT_STAT
            WHERE COURSE_PRESENTATION_ID = :coursePresentationId
              AND JOB_ID = :jobId
            ORDER BY
                CASE FINAL_RESULT
                    WHEN 'Pass' THEN 1
                    WHEN 'Fail' THEN 2
                    WHEN 'Withdrawn' THEN 3
                    WHEN 'Distinction' THEN 4
                    ELSE 5
                END
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId,
                "jobId",
                jobId
            ),
            (rs, rowNum) ->
                new ResultAnalysisResponse.ResultActivityItem(
                    rs.getString("FINAL_RESULT"),
                    rs.getLong("STUDENT_COUNT"),
                    rs.getLong("TOTAL_CLICK_COUNT"),
                    rs.getBigDecimal("AVG_CLICK_COUNT"),
                    rs.getBigDecimal("AVG_ACTIVE_DAY_COUNT")
                )
        );
    }
}