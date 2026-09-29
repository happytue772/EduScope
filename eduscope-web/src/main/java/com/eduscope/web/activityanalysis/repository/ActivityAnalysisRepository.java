package com.eduscope.web.activityanalysis.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.activityanalysis.dto.ActivityAnalysisResponse;

/**
 * VLE 학습활동 분석 전용 Repository.
 */
@Repository
public class ActivityAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ActivityAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 강의 기본정보 조회.
     */
    public ActivityAnalysisResponse.CourseInfo findCourse(
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
                new ActivityAnalysisResponse.CourseInfo(
                    rs.getLong("COURSE_PRESENTATION_ID"),
                    rs.getLong("DATASET_ID"),
                    rs.getString("CODE_MODULE"),
                    rs.getString("CODE_PRESENTATION")
                )
        );
    }

    /**
     * 현재 Dataset에서 성공적으로 적재된
     * 최신 VLE_ACTIVITY Job 조회.
     */
    public Long findLatestVleActivityJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'VLE_ACTIVITY'
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
     * 선택 강의의 VLE 자료 + 실제 활동 통계 조회.
     */
    public List<ActivityAnalysisResponse.MaterialActivity>
            findMaterials(
                Long coursePresentationId,
                Long jobId) {

        String sql = """
            SELECT
                V.VLE_MATERIAL_ID,
                V.SOURCE_SITE_ID,
                V.ACTIVITY_TYPE,
                V.WEEK_FROM,
                V.WEEK_TO,

                S.TOTAL_CLICK_COUNT,
                S.ACTIVE_STUDENT_COUNT,
                S.ACTIVE_DAY_COUNT

            FROM VLE_MATERIAL V

            JOIN VLE_ACTIVITY_STAT S
              ON S.VLE_MATERIAL_ID =
                 V.VLE_MATERIAL_ID

            WHERE
                V.COURSE_PRESENTATION_ID =
                :coursePresentationId

              AND S.JOB_ID =
                :jobId

            ORDER BY
                S.TOTAL_CLICK_COUNT DESC,
                V.SOURCE_SITE_ID
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

                Number weekFrom =
                    (Number)
                    rs.getObject(
                        "WEEK_FROM"
                    );

                Number weekTo =
                    (Number)
                    rs.getObject(
                        "WEEK_TO"
                    );

                return new ActivityAnalysisResponse.MaterialActivity(
                    rs.getLong(
                        "VLE_MATERIAL_ID"
                    ),
                    rs.getLong(
                        "SOURCE_SITE_ID"
                    ),
                    rs.getString(
                        "ACTIVITY_TYPE"
                    ),
                    weekFrom == null
                        ? null
                        : weekFrom.intValue(),
                    weekTo == null
                        ? null
                        : weekTo.intValue(),
                    rs.getLong(
                        "TOTAL_CLICK_COUNT"
                    ),
                    rs.getLong(
                        "ACTIVE_STUDENT_COUNT"
                    ),
                    rs.getLong(
                        "ACTIVE_DAY_COUNT"
                    )
                );
            }
        );
    }
}