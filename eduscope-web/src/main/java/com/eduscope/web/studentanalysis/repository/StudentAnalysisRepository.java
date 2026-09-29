package com.eduscope.web.studentanalysis.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.studentanalysis.dto.StudentAnalysisResponse;
import com.eduscope.web.studentanalysis.dto.StudentSearchResponse;

/**
 * 학생 분석 전용 Repository.
 */
@Repository
public class StudentAnalysisRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StudentAnalysisRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * 익명 Student ID 및 강의를 기준으로 검색한다.
     *
     * 빈 검색어이면 실제 학생 수강 정보 중
     * 앞의 100건을 반환한다.
     */
    public List<StudentSearchResponse> search(
            String keyword,
            Long coursePresentationId) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                SC.STUDENT_COURSE_ID,
                OS.SOURCE_STUDENT_ID,
                CP.COURSE_PRESENTATION_ID,
                CP.CODE_MODULE,
                CP.CODE_PRESENTATION,
                SC.FINAL_RESULT

            FROM STUDENT_COURSE SC

            JOIN OULAD_STUDENT OS
              ON OS.STUDENT_ID =
                 SC.STUDENT_ID

            JOIN COURSE_PRESENTATION CP
              ON CP.COURSE_PRESENTATION_ID =
                 SC.COURSE_PRESENTATION_ID

            WHERE 1 = 1
            """);

        Map<String, Object> params =
            new HashMap<>();

        if (
            keyword != null
            &&
            !keyword.isBlank()
        ) {

            sql.append("""
                AND TO_CHAR(OS.SOURCE_STUDENT_ID)
                    LIKE :studentKeyword
                """);

            params.put(
                "studentKeyword",
                "%"
                + keyword.trim()
                + "%"
            );
        }

        if (coursePresentationId != null) {

            sql.append("""
                AND CP.COURSE_PRESENTATION_ID =
                    :coursePresentationId
                """);

            params.put(
                "coursePresentationId",
                coursePresentationId
            );
        }

        sql.append("""
            ORDER BY
                OS.SOURCE_STUDENT_ID,
                CP.CODE_MODULE,
                CP.CODE_PRESENTATION

            FETCH FIRST 100 ROWS ONLY
            """);

        return jdbcTemplate.query(
            sql.toString(),
            params,
            (rs, rowNum) ->
                new StudentSearchResponse(
                    rs.getLong(
                        "STUDENT_COURSE_ID"
                    ),
                    rs.getLong(
                        "SOURCE_STUDENT_ID"
                    ),
                    rs.getLong(
                        "COURSE_PRESENTATION_ID"
                    ),
                    rs.getString(
                        "CODE_MODULE"
                    ),
                    rs.getString(
                        "CODE_PRESENTATION"
                    ),
                    rs.getString(
                        "FINAL_RESULT"
                    )
                )
        );
    }


    /**
     * 선택한 STUDENT_COURSE의 상세 기본정보.
     */
    public StudentAnalysisResponse.StudentCourseInfo
            findStudentCourse(
                Long studentCourseId) {

        String sql = """
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
                SC.FINAL_RESULT

            FROM STUDENT_COURSE SC

            JOIN OULAD_STUDENT OS
              ON OS.STUDENT_ID =
                 SC.STUDENT_ID

            JOIN COURSE_PRESENTATION CP
              ON CP.COURSE_PRESENTATION_ID =
                 SC.COURSE_PRESENTATION_ID

            WHERE
                SC.STUDENT_COURSE_ID =
                :studentCourseId
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "studentCourseId",
                studentCourseId
            ),
            (rs, rowNum) ->
                new StudentAnalysisResponse.StudentCourseInfo(
                    rs.getLong("STUDENT_COURSE_ID"),
                    rs.getLong("STUDENT_ID"),
                    rs.getLong("SOURCE_STUDENT_ID"),
                    rs.getLong("COURSE_PRESENTATION_ID"),
                    rs.getLong("DATASET_ID"),
                    rs.getString("CODE_MODULE"),
                    rs.getString("CODE_PRESENTATION"),
                    rs.getString("GENDER"),
                    rs.getString("REGION"),
                    rs.getString("HIGHEST_EDUCATION"),
                    rs.getString("IMD_BAND"),
                    rs.getString("AGE_BAND"),
                    getNullableInteger(
                        rs.getObject(
                            "NUM_OF_PREV_ATTEMPTS"
                        )
                    ),
                    getNullableInteger(
                        rs.getObject(
                            "STUDIED_CREDITS"
                        )
                    ),
                    rs.getString("DISABILITY"),
                    rs.getString("FINAL_RESULT")
                )
        );
    }


    /**
     * 실제 등록 / 철회 시점.
     */
    public StudentAnalysisResponse.RegistrationInfo
            findRegistration(
                Long studentCourseId) {

        String sql = """
            SELECT
                REGISTRATION_DAY,
                UNREGISTRATION_DAY
            FROM STUDENT_REGISTRATION
            WHERE STUDENT_COURSE_ID =
                  :studentCourseId
            """;

        List<StudentAnalysisResponse.RegistrationInfo> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "studentCourseId",
                    studentCourseId
                ),
                (rs, rowNum) ->
                    new StudentAnalysisResponse.RegistrationInfo(
                        getNullableInteger(
                            rs.getObject(
                                "REGISTRATION_DAY"
                            )
                        ),
                        getNullableInteger(
                            rs.getObject(
                                "UNREGISTRATION_DAY"
                            )
                        )
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * Dataset별 최신 성공 + 적재완료 분석 Job.
     */
    public Long findLatestJobId(
            Long datasetId,
            String analysisType) {

        String sql = """
            SELECT MAX(JOB_ID)
            FROM ANALYSIS_JOB
            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = :analysisType
              AND STATUS = 'SUCCESS'
              AND RESULT_IMPORTED_YN = 'Y'
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "datasetId",
                datasetId,
                "analysisType",
                analysisType
            ),
            Long.class
        );
    }


    /**
     * STUDENT_ACTIVITY_STAT.
     */
    public StudentAnalysisResponse.ActivitySummary findActivity(
            Long studentCourseId,
            Long jobId) {

        String sql = """
            SELECT
                TOTAL_CLICK_COUNT,
                ACTIVE_DAY_COUNT,
                USED_MATERIAL_COUNT,
                AVG_DAILY_CLICK_COUNT,
                FIRST_ACTIVITY_DAY,
                LAST_ACTIVITY_DAY

            FROM STUDENT_ACTIVITY_STAT

            WHERE
                STUDENT_COURSE_ID =
                :studentCourseId

              AND JOB_ID =
                :jobId
            """;

        List<StudentAnalysisResponse.ActivitySummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "studentCourseId",
                    studentCourseId,
                    "jobId",
                    jobId
                ),
                (rs, rowNum) ->
                    new StudentAnalysisResponse.ActivitySummary(
                        rs.getLong(
                            "TOTAL_CLICK_COUNT"
                        ),
                        rs.getLong(
                            "ACTIVE_DAY_COUNT"
                        ),
                        rs.getLong(
                            "USED_MATERIAL_COUNT"
                        ),
                        rs.getBigDecimal(
                            "AVG_DAILY_CLICK_COUNT"
                        ),
                        getNullableInteger(
                            rs.getObject(
                                "FIRST_ACTIVITY_DAY"
                            )
                        ),
                        getNullableInteger(
                            rs.getObject(
                                "LAST_ACTIVITY_DAY"
                            )
                        )
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * STUDENT_LEARNING_SUMMARY_STAT.
     */
    public StudentAnalysisResponse.LearningSummary
            findLearningSummary(
                Long studentCourseId,
                Long jobId) {

        String sql = """
            SELECT
                TOTAL_CLICK_COUNT,
                ACTIVE_DAY_COUNT,
                USED_MATERIAL_COUNT,

                SUBMITTED_ASSESSMENT_COUNT,
                AVG_ASSESSMENT_SCORE,
                FAILED_ASSESSMENT_COUNT,

                FIRST_ACTIVITY_DAY,
                LAST_ACTIVITY_DAY

            FROM STUDENT_LEARNING_SUMMARY_STAT

            WHERE
                STUDENT_COURSE_ID =
                :studentCourseId

              AND JOB_ID =
                :jobId
            """;

        List<StudentAnalysisResponse.LearningSummary> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "studentCourseId",
                    studentCourseId,
                    "jobId",
                    jobId
                ),
                (rs, rowNum) ->
                    new StudentAnalysisResponse.LearningSummary(
                        rs.getLong(
                            "TOTAL_CLICK_COUNT"
                        ),
                        rs.getLong(
                            "ACTIVE_DAY_COUNT"
                        ),
                        rs.getLong(
                            "USED_MATERIAL_COUNT"
                        ),
                        rs.getLong(
                            "SUBMITTED_ASSESSMENT_COUNT"
                        ),
                        rs.getBigDecimal(
                            "AVG_ASSESSMENT_SCORE"
                        ),
                        rs.getLong(
                            "FAILED_ASSESSMENT_COUNT"
                        ),
                        getNullableInteger(
                            rs.getObject(
                                "FIRST_ACTIVITY_DAY"
                            )
                        ),
                        getNullableInteger(
                            rs.getObject(
                                "LAST_ACTIVITY_DAY"
                            )
                        )
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * 실제 학생 평가 제출 이력.
     */
    public List<StudentAnalysisResponse.AssessmentHistory>
            findAssessments(
                Long studentCourseId) {

        String sql = """
            SELECT
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
              ON A.ASSESSMENT_ID =
                 SA.ASSESSMENT_ID

            WHERE
                SA.STUDENT_COURSE_ID =
                :studentCourseId

            ORDER BY
                SA.SUBMITTED_DAY,
                A.ASSESSMENT_ID
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "studentCourseId",
                studentCourseId
            ),
            (rs, rowNum) ->
                new StudentAnalysisResponse.AssessmentHistory(
                    rs.getLong(
                        "ASSESSMENT_ID"
                    ),
                    rs.getLong(
                        "SOURCE_ASSESSMENT_ID"
                    ),
                    rs.getString(
                        "ASSESSMENT_TYPE"
                    ),
                    getNullableInteger(
                        rs.getObject(
                            "ASSESSMENT_DUE_DAY"
                        )
                    ),
                    rs.getBigDecimal(
                        "ASSESSMENT_WEIGHT"
                    ),
                    getNullableInteger(
                        rs.getObject(
                            "SUBMITTED_DAY"
                        )
                    ),
                    rs.getString(
                        "IS_BANKED"
                    ),
                    rs.getBigDecimal(
                        "SCORE"
                    )
                )
        );
    }


    private Integer getNullableInteger(
            Object value) {

        if (value == null) {
            return null;
        }

        return ((Number) value)
            .intValue();
    }
}