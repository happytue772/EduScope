package com.eduscope.web.dataset.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.dataset.dto.DatasetDetailResponse;
import com.eduscope.web.dataset.dto.DatasetSummaryResponse;

/**
 * Dataset 조회 전용 Repository.
 */
@Repository
public class DatasetViewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DatasetViewRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * 삭제되지 않은 Dataset 목록.
     */
    public List<DatasetSummaryResponse> findDatasets() {

        String sql = """
            SELECT
                DATASET_ID,
                DISPLAY_NAME,
                SOURCE_NAME,
                DATASET_VERSION,
                SCHEMA_VERSION,
                FILE_COUNT

            FROM DATASET

            WHERE IS_DELETED = 'N'

            ORDER BY DATASET_ID
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(),
            (rs, rowNum) ->
                new DatasetSummaryResponse(
                    rs.getLong("DATASET_ID"),
                    rs.getString("DISPLAY_NAME"),
                    rs.getString("SOURCE_NAME"),
                    rs.getString("DATASET_VERSION"),
                    rs.getString("SCHEMA_VERSION"),
                    rs.getLong("FILE_COUNT")
                )
        );
    }


    /**
     * Dataset 상세정보.
     */
    public DatasetDetailResponse.DatasetInfo
            findDataset(
                Long datasetId) {

        String sql = """
            SELECT
                DATASET_ID,
                DISPLAY_NAME,
                DESCRIPTION,
                SOURCE_TYPE,
                SOURCE_NAME,
                SOURCE_URL,
                DATASET_VERSION,
                SCHEMA_VERSION,
                HDFS_BASE_PATH,
                FILE_COUNT

            FROM DATASET

            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """;

        List<DatasetDetailResponse.DatasetInfo> rows =
            jdbcTemplate.query(
                sql,
                Map.of(
                    "datasetId",
                    datasetId
                ),
                (rs, rowNum) ->
                    new DatasetDetailResponse.DatasetInfo(
                        rs.getLong("DATASET_ID"),
                        rs.getString("DISPLAY_NAME"),
                        rs.getString("DESCRIPTION"),
                        rs.getString("SOURCE_TYPE"),
                        rs.getString("SOURCE_NAME"),
                        rs.getString("SOURCE_URL"),
                        rs.getString("DATASET_VERSION"),
                        rs.getString("SCHEMA_VERSION"),
                        rs.getString("HDFS_BASE_PATH"),
                        rs.getLong("FILE_COUNT")
                    )
            );

        return rows.isEmpty()
            ? null
            : rows.get(0);
    }


    /**
     * 실제 DATASET_FILE 목록.
     */
    public List<DatasetDetailResponse.DatasetFileInfo>
            findFiles(
                Long datasetId) {

        String sql = """
            SELECT
                DATASET_FILE_ID,
                FILE_TYPE,
                ORIGINAL_FILE_NAME,
                FILE_SIZE_BYTES,
                RECORD_COUNT,
                CONTENT_HASH,
                HDFS_RAW_PATH,
                HDFS_CLEAN_PATH,
                LOAD_STATUS

            FROM DATASET_FILE

            WHERE DATASET_ID = :datasetId

            ORDER BY DATASET_FILE_ID
            """;

        return jdbcTemplate.query(
            sql,
            Map.of(
                "datasetId",
                datasetId
            ),
            (rs, rowNum) ->
                new DatasetDetailResponse.DatasetFileInfo(
                    rs.getLong(
                        "DATASET_FILE_ID"
                    ),
                    rs.getString(
                        "FILE_TYPE"
                    ),
                    rs.getString(
                        "ORIGINAL_FILE_NAME"
                    ),
                    nullableLong(
                        rs.getObject(
                            "FILE_SIZE_BYTES"
                        )
                    ),
                    nullableLong(
                        rs.getObject(
                            "RECORD_COUNT"
                        )
                    ),
                    rs.getString(
                        "CONTENT_HASH"
                    ),
                    rs.getString(
                        "HDFS_RAW_PATH"
                    ),
                    rs.getString(
                        "HDFS_CLEAN_PATH"
                    ),
                    rs.getString(
                        "LOAD_STATUS"
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
}