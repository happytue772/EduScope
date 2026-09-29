package com.eduscope.web.dashboard.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.dashboard.dto.DashboardSummaryResponse;

/**
 * Dashboard 조회 전용 Repository.
 *
 * 많은 Entity를 읽은 뒤 Java에서 집계하지 않고,
 * Oracle에서 필요한 KPI만 집계한다.
 */
@Repository
public class DashboardSummaryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DashboardSummaryRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 Dataset 기준 Dashboard 핵심 KPI 조회.
     */
    public DashboardSummaryResponse findSummary(
            Long datasetId) {

        String sql = """
            WITH LATEST_COURSE_ACTIVITY_JOB AS (
                SELECT MAX(JOB_ID) AS JOB_ID
                FROM ANALYSIS_JOB
                WHERE DATASET_ID = :datasetId
                  AND ANALYSIS_TYPE = 'COURSE_ACTIVITY'
                  AND STATUS = 'SUCCESS'
                  AND RESULT_IMPORTED_YN = 'Y'
            ),
            LATEST_COURSE_RESULT_JOB AS (
                SELECT MAX(JOB_ID) AS JOB_ID
                FROM ANALYSIS_JOB
                WHERE DATASET_ID = :datasetId
                  AND ANALYSIS_TYPE = 'COURSE_RESULT'
                  AND STATUS = 'SUCCESS'
                  AND RESULT_IMPORTED_YN = 'Y'
            )
            SELECT

                (
                    SELECT COUNT(*)
                    FROM DATASET
                    WHERE DATASET_ID = :datasetId
                      AND IS_DELETED = 'N'
                ) AS DATASET_COUNT,

                (
                    SELECT COUNT(*)
                    FROM DATASET_FILE
                    WHERE DATASET_ID = :datasetId
                ) AS DATASET_FILE_COUNT,

                (
                    SELECT COUNT(*)
                    FROM COURSE_PRESENTATION
                    WHERE DATASET_ID = :datasetId
                ) AS COURSE_PRESENTATION_COUNT,

                (
                    SELECT COUNT(*)
                    FROM OULAD_STUDENT
                    WHERE DATASET_ID = :datasetId
                ) AS UNIQUE_STUDENT_COUNT,

                (
                    SELECT COUNT(*)
                    FROM STUDENT_COURSE SC
                    JOIN COURSE_PRESENTATION CP
                      ON CP.COURSE_PRESENTATION_ID =
                         SC.COURSE_PRESENTATION_ID
                    WHERE CP.DATASET_ID = :datasetId
                ) AS ENROLLMENT_COUNT,

                (
                    SELECT NVL(SUM(TOTAL_CLICK_COUNT), 0)
                    FROM COURSE_ACTIVITY_STAT
                    WHERE JOB_ID = (
                        SELECT JOB_ID
                        FROM LATEST_COURSE_ACTIVITY_JOB
                    )
                ) AS TOTAL_CLICK_COUNT,

                (
                    SELECT NVL(SUM(PASS_COUNT), 0)
                    FROM COURSE_RESULT_STAT
                    WHERE JOB_ID = (
                        SELECT JOB_ID
                        FROM LATEST_COURSE_RESULT_JOB
                    )
                ) AS PASS_COUNT,

                (
                    SELECT NVL(SUM(FAIL_COUNT), 0)
                    FROM COURSE_RESULT_STAT
                    WHERE JOB_ID = (
                        SELECT JOB_ID
                        FROM LATEST_COURSE_RESULT_JOB
                    )
                ) AS FAIL_COUNT,

                (
                    SELECT NVL(SUM(WITHDRAWN_COUNT), 0)
                    FROM COURSE_RESULT_STAT
                    WHERE JOB_ID = (
                        SELECT JOB_ID
                        FROM LATEST_COURSE_RESULT_JOB
                    )
                ) AS WITHDRAWN_COUNT,

                (
                    SELECT NVL(SUM(DISTINCTION_COUNT), 0)
                    FROM COURSE_RESULT_STAT
                    WHERE JOB_ID = (
                        SELECT JOB_ID
                        FROM LATEST_COURSE_RESULT_JOB
                    )
                ) AS DISTINCTION_COUNT,

                (
                    SELECT JOB_ID
                    FROM LATEST_COURSE_ACTIVITY_JOB
                ) AS COURSE_ACTIVITY_JOB_ID,

                (
                    SELECT JOB_ID
                    FROM LATEST_COURSE_RESULT_JOB
                ) AS COURSE_RESULT_JOB_ID

            FROM DUAL
            """;

        Map<String, Object> params =
            Map.of(
                "datasetId",
                datasetId
            );

        return jdbcTemplate.queryForObject(
            sql,
            params,
            (rs, rowNum) ->
                mapResponse(rs)
        );
    }

    /**
     * JDBC 결과 → Dashboard DTO.
     */
    private DashboardSummaryResponse mapResponse(
            ResultSet rs)
            throws SQLException {

        return new DashboardSummaryResponse(

            rs.getLong("DATASET_COUNT"),
            rs.getLong("DATASET_FILE_COUNT"),

            rs.getLong("COURSE_PRESENTATION_COUNT"),

            rs.getLong("UNIQUE_STUDENT_COUNT"),
            rs.getLong("ENROLLMENT_COUNT"),

            rs.getLong("TOTAL_CLICK_COUNT"),

            rs.getLong("PASS_COUNT"),
            rs.getLong("FAIL_COUNT"),
            rs.getLong("WITHDRAWN_COUNT"),
            rs.getLong("DISTINCTION_COUNT"),

            nullableLong(
                rs,
                "COURSE_ACTIVITY_JOB_ID"
            ),

            nullableLong(
                rs,
                "COURSE_RESULT_JOB_ID"
            )
        );
    }

    /**
     * Oracle NUMBER NULL 처리를 위한 공통 메서드.
     */
    private Long nullableLong(
            ResultSet rs,
            String columnName)
            throws SQLException {

        Number value =
            (Number) rs.getObject(
                columnName
            );

        return value == null
            ? null
            : value.longValue();
    }
}