package com.eduscope.web.assessmentanalysis.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.assessmentanalysis.dto.AssessmentAnalysisResponse;

/**
 * 평가 분석 전용 조회 Repository.
 */
@Repository
public class AssessmentAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssessmentAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    public AssessmentAnalysisResponse.CourseInfo findCourse(
            Long coursePresentationId) {

        String sql = """
            SELECT
                COURSE_PRESENTATION_ID,
                DATASET_ID,
                CODE_MODULE,
                CODE_PRESENTATION
            FROM COURSE_PRESENTATION
            WHERE COURSE_PRESENTATION_ID =
                  :coursePresentationId
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId
            ),
            (rs, rowNum) ->
                new AssessmentAnalysisResponse.CourseInfo(
                    rs.getLong(
                        "COURSE_PRESENTATION_ID"
                    ),
                    rs.getLong(
                        "DATASET_ID"
                    ),
                    rs.getString(
                        "CODE_MODULE"
                    ),
                    rs.getString(
                        "CODE_PRESENTATION"
                    )
                )
        );
    }


    public Long findLatestAssessmentJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'ASSESSMENT'
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


    public List<AssessmentAnalysisResponse.AssessmentItem>
            findAssessments(
                Long coursePresentationId,
                Long jobId) {

        String sql = """
            SELECT
                A.ASSESSMENT_ID,
                A.SOURCE_ASSESSMENT_ID,
                A.ASSESSMENT_TYPE,
                A.ASSESSMENT_DUE_DAY,
                A.ASSESSMENT_WEIGHT,

                S.SUBMISSION_COUNT,
                S.AVG_SCORE,
                S.FAIL_COUNT,
                S.FAIL_RATE,
                S.BANKED_COUNT,
                S.AVG_SUBMISSION_DAY

            FROM ASSESSMENT A

            JOIN ASSESSMENT_STAT S
              ON S.ASSESSMENT_ID =
                 A.ASSESSMENT_ID

            WHERE
                A.COURSE_PRESENTATION_ID =
                :coursePresentationId

              AND S.JOB_ID =
                  :jobId

            ORDER BY
                NVL(
                    A.ASSESSMENT_DUE_DAY,
                    999999
                ),
                A.ASSESSMENT_ID
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "coursePresentationId",
                coursePresentationId,
                "jobId",
                jobId
            ),
            (rs, rowNum) -> {

                Number dueDay =
                    (Number)
                    rs.getObject(
                        "ASSESSMENT_DUE_DAY"
                    );

                return new AssessmentAnalysisResponse.AssessmentItem(
                    rs.getLong(
                        "ASSESSMENT_ID"
                    ),
                    rs.getLong(
                        "SOURCE_ASSESSMENT_ID"
                    ),
                    rs.getString(
                        "ASSESSMENT_TYPE"
                    ),
                    dueDay == null
                        ? null
                        : dueDay.intValue(),
                    rs.getBigDecimal(
                        "ASSESSMENT_WEIGHT"
                    ),
                    rs.getLong(
                        "SUBMISSION_COUNT"
                    ),
                    rs.getBigDecimal(
                        "AVG_SCORE"
                    ),
                    rs.getLong(
                        "FAIL_COUNT"
                    ),
                    rs.getBigDecimal(
                        "FAIL_RATE"
                    ),
                    rs.getLong(
                        "BANKED_COUNT"
                    ),
                    rs.getBigDecimal(
                        "AVG_SUBMISSION_DAY"
                    )
                );
            }
        );
    }
}