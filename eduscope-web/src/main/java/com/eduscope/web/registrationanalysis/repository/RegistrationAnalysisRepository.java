package com.eduscope.web.registrationanalysis.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.registrationanalysis.dto.RegistrationAnalysisResponse;

/**
 * 수강/철회 분석 전용 조회 Repository.
 */
@Repository
public class RegistrationAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RegistrationAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 강의 정보 조회.
     */
    public RegistrationAnalysisResponse.CourseInfo findCourse(
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
                new RegistrationAnalysisResponse.CourseInfo(
                    rs.getLong("COURSE_PRESENTATION_ID"),
                    rs.getLong("DATASET_ID"),
                    rs.getString("CODE_MODULE"),
                    rs.getString("CODE_PRESENTATION")
                )
        );
    }

    /**
     * 성공적으로 Oracle 적재까지 완료된
     * 최신 REGISTRATION 분석 Job.
     */
    public Long findLatestRegistrationJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'REGISTRATION'
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
     * REGISTRATION_STAT 요약.
     */
    public RegistrationAnalysisResponse.RegistrationSummary
            findSummary(
                Long coursePresentationId,
                Long jobId) {

        String sql = """
            SELECT
                REGISTRATION_COUNT,
                UNREGISTRATION_COUNT,
                WITHDRAWN_RESULT_COUNT,
                UNREGISTRATION_RATE,
                AVG_REGISTRATION_DAY,
                AVG_UNREGISTRATION_DAY
            FROM REGISTRATION_STAT
            WHERE COURSE_PRESENTATION_ID = :coursePresentationId
              AND JOB_ID = :jobId
            """;

        List<RegistrationAnalysisResponse.RegistrationSummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "coursePresentationId",
                    coursePresentationId,
                    "jobId",
                    jobId
                ),
                (rs, rowNum) ->
                    new RegistrationAnalysisResponse.RegistrationSummary(
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
     * 실제 STUDENT_REGISTRATION을 활용한
     * 상대 등록일별 인원 분포.
     */
    public List<RegistrationAnalysisResponse.DailyCount>
            findRegistrationByDay(
                Long coursePresentationId) {

        String sql = """
            SELECT
                SR.REGISTRATION_DAY AS RELATIVE_DAY,
                COUNT(*) AS RECORD_COUNT
            FROM STUDENT_REGISTRATION SR

            JOIN STUDENT_COURSE SC
              ON SC.STUDENT_COURSE_ID =
                 SR.STUDENT_COURSE_ID

            WHERE
                SC.COURSE_PRESENTATION_ID =
                :coursePresentationId

              AND SR.REGISTRATION_DAY
                  IS NOT NULL

            GROUP BY
                SR.REGISTRATION_DAY

            ORDER BY
                SR.REGISTRATION_DAY
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId
            ),
            (rs, rowNum) ->
                new RegistrationAnalysisResponse.DailyCount(
                    rs.getInt("RELATIVE_DAY"),
                    rs.getLong("RECORD_COUNT")
                )
        );
    }

    /**
     * 상대 철회일별 인원 분포.
     */
    public List<RegistrationAnalysisResponse.DailyCount>
            findUnregistrationByDay(
                Long coursePresentationId) {

        String sql = """
            SELECT
                SR.UNREGISTRATION_DAY AS RELATIVE_DAY,
                COUNT(*) AS RECORD_COUNT
            FROM STUDENT_REGISTRATION SR

            JOIN STUDENT_COURSE SC
              ON SC.STUDENT_COURSE_ID =
                 SR.STUDENT_COURSE_ID

            WHERE
                SC.COURSE_PRESENTATION_ID =
                :coursePresentationId

              AND SR.UNREGISTRATION_DAY
                  IS NOT NULL

            GROUP BY
                SR.UNREGISTRATION_DAY

            ORDER BY
                SR.UNREGISTRATION_DAY
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId
            ),
            (rs, rowNum) ->
                new RegistrationAnalysisResponse.DailyCount(
                    rs.getInt("RELATIVE_DAY"),
                    rs.getLong("RECORD_COUNT")
                )
        );
    }
}