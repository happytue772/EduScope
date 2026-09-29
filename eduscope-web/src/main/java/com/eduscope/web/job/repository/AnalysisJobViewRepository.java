package com.eduscope.web.job.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.job.dto.AnalysisJobResponse;

/**
 * ANALYSIS_JOB 조회 전용 Repository.
 */
@Repository
public class AnalysisJobViewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AnalysisJobViewRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    public List<AnalysisJobResponse.JobItem>
            findJobs(Long datasetId) {

        String sql = """
            SELECT
                JOB_ID,
                ANALYSIS_TYPE,
                STATUS,
                ANALYSIS_VERSION,

                HDFS_INPUT_PATH,
                HDFS_OUTPUT_PATH,
                RESULT_FILE_PATH,

                REQUESTED_AT,
                STARTED_AT,
                FINISHED_AT,

                INPUT_RECORD_COUNT,
                OUTPUT_RECORD_COUNT,
                VALID_RECORD_COUNT,
                INVALID_RECORD_COUNT,
                DUPLICATE_RECORD_COUNT,

                PROCESSING_TIME_MS,

                ERROR_STEP,
                ERROR_MESSAGE,

                RESULT_IMPORTED_YN

            FROM ANALYSIS_JOB

            WHERE DATASET_ID = :datasetId

            ORDER BY JOB_ID DESC
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "datasetId",
                datasetId
            ),
            (rs, rowNum) ->
                new AnalysisJobResponse.JobItem(

                    rs.getLong("JOB_ID"),

                    rs.getString(
                        "ANALYSIS_TYPE"
                    ),

                    rs.getString(
                        "STATUS"
                    ),

                    rs.getString(
                        "ANALYSIS_VERSION"
                    ),

                    rs.getString(
                        "HDFS_INPUT_PATH"
                    ),

                    rs.getString(
                        "HDFS_OUTPUT_PATH"
                    ),

                    rs.getString(
                        "RESULT_FILE_PATH"
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "REQUESTED_AT"
                        )
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "STARTED_AT"
                        )
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "FINISHED_AT"
                        )
                    ),

                    rs.getLong(
                        "INPUT_RECORD_COUNT"
                    ),

                    rs.getLong(
                        "OUTPUT_RECORD_COUNT"
                    ),

                    rs.getLong(
                        "VALID_RECORD_COUNT"
                    ),

                    rs.getLong(
                        "INVALID_RECORD_COUNT"
                    ),

                    rs.getLong(
                        "DUPLICATE_RECORD_COUNT"
                    ),

                    nullableLong(
                        rs.getObject(
                            "PROCESSING_TIME_MS"
                        )
                    ),

                    rs.getString(
                        "ERROR_STEP"
                    ),

                    rs.getString(
                        "ERROR_MESSAGE"
                    ),

                    rs.getString(
                        "RESULT_IMPORTED_YN"
                    )
                )
        );
    }


    private Long nullableLong(
            Object value) {

        if (value == null) {
            return null;
        }

        return ((Number) value)
            .longValue();
    }


    private java.time.LocalDateTime
            toLocalDateTime(
                Timestamp timestamp) {

        return timestamp == null
            ? null
            : timestamp.toLocalDateTime();
    }
}