package com.eduscope.web.dataquality.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.dataquality.dto.DataQualityResponse;

/**
 * 데이터 품질 분석 전용 조회 Repository.
 */
@Repository
public class DataQualityRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DataQualityRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * Dataset 기본정보 조회.
     */
    public DataQualityResponse.DatasetInfo findDataset(
            Long datasetId) {

        String sql = """
            SELECT
                DATASET_ID,
                DISPLAY_NAME,
                DATASET_VERSION,
                SCHEMA_VERSION,
                FILE_COUNT,
                HDFS_BASE_PATH

            FROM DATASET

            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """;

        List<DataQualityResponse.DatasetInfo> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "datasetId",
                    datasetId
                ),
                (rs, rowNum) ->
                    new DataQualityResponse.DatasetInfo(
                        rs.getLong(
                            "DATASET_ID"
                        ),
                        rs.getString(
                            "DISPLAY_NAME"
                        ),
                        rs.getString(
                            "DATASET_VERSION"
                        ),
                        rs.getString(
                            "SCHEMA_VERSION"
                        ),
                        rs.getLong(
                            "FILE_COUNT"
                        ),
                        rs.getString(
                            "HDFS_BASE_PATH"
                        )
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * 최신 성공 + Oracle 적재완료
     * DATA_QUALITY Job 조회.
     */
    public Long findLatestJobId(
            Long datasetId) {

        String sql = """
            SELECT MAX(JOB_ID)

            FROM ANALYSIS_JOB

            WHERE DATASET_ID = :datasetId
              AND ANALYSIS_TYPE = 'DATA_QUALITY'
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
     * 분석 Job 상세정보.
     */
    public DataQualityResponse.JobInfo findJob(
            Long jobId) {

        String sql = """
            SELECT
                JOB_ID,
                STATUS,
                OUTPUT_RECORD_COUNT,
                DUPLICATE_RECORD_COUNT,
                RESULT_IMPORTED_YN,
                HDFS_OUTPUT_PATH,
                CREATED_AT

            FROM ANALYSIS_JOB

            WHERE JOB_ID = :jobId
            """;

        List<DataQualityResponse.JobInfo> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "jobId",
                    jobId
                ),
                (rs, rowNum) -> {

                    var timestamp =
                        rs.getTimestamp(
                            "CREATED_AT"
                        );

                    return new DataQualityResponse.JobInfo(
                        rs.getLong(
                            "JOB_ID"
                        ),
                        rs.getString(
                            "STATUS"
                        ),
                        rs.getLong(
                            "OUTPUT_RECORD_COUNT"
                        ),
                        rs.getLong(
                            "DUPLICATE_RECORD_COUNT"
                        ),
                        rs.getString(
                            "RESULT_IMPORTED_YN"
                        ),
                        rs.getString(
                            "HDFS_OUTPUT_PATH"
                        ),
                        timestamp == null
                            ? null
                            : timestamp
                                .toLocalDateTime()
                    );
                }
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * 품질 이슈와 원본 Dataset File을 함께 조회한다.
     */
    /**
     * Dataset의 모든 파일을 기준으로
     * Data Quality 결과를 LEFT JOIN한다.
     */
    public List<DataQualityResponse.QualityItem>
            findQualityItems(
                Long datasetId,
                Long jobId) {

        String sql = """
            SELECT
                DF.DATASET_FILE_ID,
                DF.FILE_TYPE,
                DF.ORIGINAL_FILE_NAME,
                DF.RECORD_COUNT AS FILE_RECORD_COUNT,
                DF.LOAD_STATUS,

                DQ.QUALITY_TYPE,
                DQ.RECORD_COUNT AS QUALITY_RECORD_COUNT,
                DQ.SAMPLE_MESSAGE

            FROM DATASET_FILE DF

            LEFT JOIN DATA_QUALITY_STAT DQ
              ON DQ.DATASET_FILE_ID = DF.DATASET_FILE_ID
             AND DQ.JOB_ID = :jobId

            WHERE DF.DATASET_ID = :datasetId

            ORDER BY
                DF.DATASET_FILE_ID,
                DQ.QUALITY_TYPE
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "datasetId",
                datasetId,
                "jobId",
                jobId
            ),
            (rs, rowNum) ->
                new DataQualityResponse.QualityItem(
                    rs.getLong("DATASET_FILE_ID"),
                    rs.getString("FILE_TYPE"),
                    rs.getString("ORIGINAL_FILE_NAME"),
                    getNullableLong(
                        rs.getObject("FILE_RECORD_COUNT")
                    ),
                    rs.getString("LOAD_STATUS"),
                    rs.getString("QUALITY_TYPE"),
                    getNullableLong(
                        rs.getObject("QUALITY_RECORD_COUNT")
                    ),
                    rs.getString("SAMPLE_MESSAGE")
                )
        );
    }

    private Long getNullableLong(
            Object value) {

        if (value == null) {
            return null;
        }

        return ((Number) value)
            .longValue();
    }
}