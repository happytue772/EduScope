package com.eduscope.web.admin.dataset.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.List;

import com.eduscope.web.admin.dataset.dto.AdminDatasetFileResponse;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.eduscope.web.admin.dataset.dto.AdminDatasetResponse;
import com.eduscope.web.admin.dataset.dto.DatasetCreateRequest;
import com.eduscope.web.admin.dataset.dto.DatasetUpdateRequest;

/**
 * ADMIN Dataset 관리 Repository.
 *
 * 기존 Dataset 조회 Repository와 분리하여
 * 관리자 쓰기 작업만 담당한다.
 */
@Repository
public class AdminDatasetRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    /**
     * Dataset 존재 여부.
     *
     * 삭제된 Dataset도 관리자 조회 대상이므로
     * IS_DELETED 조건을 사용하지 않는다.
     */
    public boolean exists(
            Long datasetId) {

        String sql = """
            SELECT COUNT(*)
            FROM DATASET
            WHERE DATASET_ID = :datasetId
            """;


        Integer count =
            jdbcTemplate.queryForObject(
                sql,
                Map.of(
                    "datasetId",
                    datasetId
                ),
                Integer.class
            );


        return count != null
            && count > 0;
    }


    /**
     * 특정 Dataset의 실제 파일 목록 조회.
     */
    public List<AdminDatasetFileResponse> findFilesByDatasetId(
            Long datasetId) {

        String sql = """
            SELECT
                DATASET_FILE_ID,
                DATASET_ID,
                FILE_TYPE,
                ORIGINAL_FILE_NAME,
                FILE_SIZE_BYTES,
                RECORD_COUNT,
                CONTENT_HASH,
                HDFS_RAW_PATH,
                HDFS_CLEAN_PATH,
                LOAD_STATUS,
                CREATED_AT,
                UPDATED_AT
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
                new AdminDatasetFileResponse(

                    rs.getLong(
                        "DATASET_FILE_ID"
                    ),

                    rs.getLong(
                        "DATASET_ID"
                    ),

                    rs.getString(
                        "FILE_TYPE"
                    ),

                    rs.getString(
                        "ORIGINAL_FILE_NAME"
                    ),

                    getNullableLong(
                        rs,
                        "FILE_SIZE_BYTES"
                    ),

                    getNullableLong(
                        rs,
                        "RECORD_COUNT"
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
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "CREATED_AT"
                        )
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "UPDATED_AT"
                        )
                    )
                )
        );
    }


    /**
     * Oracle NUMBER NULL을
     * Java null로 유지하기 위한 보조 메서드.
     */
    private Long getNullableLong(
            java.sql.ResultSet rs,
            String columnName)
            throws java.sql.SQLException {

        long value =
            rs.getLong(
                columnName
            );


        return rs.wasNull()
            ? null
            : value;
    }


    public AdminDatasetRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate =
            jdbcTemplate;
    }


    /**
     * 삭제된 Dataset까지 포함하여
     * 관리자용 전체 목록을 조회한다.
     */
    public List<AdminDatasetResponse> findAll() {

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
                FILE_COUNT,
                IS_DELETED,
                CREATED_AT,
                UPDATED_AT
            FROM DATASET
            ORDER BY DATASET_ID DESC
            """;


        return jdbcTemplate.query(
            sql,
            Map.of(),
            (rs, rowNum) ->
                new AdminDatasetResponse(
                    rs.getLong("DATASET_ID"),
                    rs.getString("DISPLAY_NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("SOURCE_TYPE"),
                    rs.getString("SOURCE_NAME"),
                    rs.getString("SOURCE_URL"),
                    rs.getString("DATASET_VERSION"),
                    rs.getString("SCHEMA_VERSION"),
                    rs.getString("HDFS_BASE_PATH"),
                    rs.getLong("FILE_COUNT"),
                    rs.getString("IS_DELETED"),
                    toLocalDateTime(
                        rs.getTimestamp(
                            "CREATED_AT"
                        )
                    ),
                    toLocalDateTime(
                        rs.getTimestamp(
                            "UPDATED_AT"
                        )
                    )
                )
        );
    }


    /**
     * Dataset 존재 여부.
     */
    public boolean existsActive(
            Long datasetId) {

        String sql = """
            SELECT COUNT(*)
            FROM DATASET
            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """;


        Integer count =
            jdbcTemplate.queryForObject(
                sql,
                Map.of(
                    "datasetId",
                    datasetId
                ),
                Integer.class
            );


        return count != null
            && count > 0;
    }


    /**
     * Sequence에서 Dataset ID 확보.
     */
    public Long nextDatasetId() {

        String sql = """
            SELECT SEQ_DATASET_ID.NEXTVAL
            FROM DUAL
            """;


        return jdbcTemplate.queryForObject(
            sql,
            Map.of(),
            Long.class
        );
    }


    /**
     * Dataset 메타정보 등록.
     */
    /**
     * Dataset 메타정보 등록.
     */
    public void insert(
            Long datasetId,
            DatasetCreateRequest request) {

        String sql = """
            INSERT INTO DATASET (
                DATASET_ID,
                DISPLAY_NAME,
                DESCRIPTION,
                SOURCE_TYPE,
                SOURCE_NAME,
                SOURCE_URL,
                DATASET_VERSION,
                SCHEMA_VERSION,
                HDFS_BASE_PATH,
                FILE_COUNT,
                IS_DELETED,
                CREATED_AT,
                UPDATED_AT
            )
            VALUES (
                :datasetId,
                :displayName,
                :description,
                'REAL',
                :sourceName,
                :sourceUrl,
                :datasetVersion,
                :schemaVersion,
                :hdfsBasePath,
                0,
                'N',
                SYSTIMESTAMP,
                SYSTIMESTAMP
            )
            """;


        /*
         * null 값이 들어갈 수 있으므로
         * Map.of() 대신 MapSqlParameterSource 사용.
         */
        MapSqlParameterSource params =
            new MapSqlParameterSource()
                .addValue(
                    "datasetId",
                    datasetId
                )
                .addValue(
                    "displayName",
                    request.displayName()
                )
                .addValue(
                    "description",
                    nullable(
                        request.description()
                    )
                )
                .addValue(
                    "sourceName",
                    request.sourceName()
                )
                .addValue(
                    "sourceUrl",
                    nullable(
                        request.sourceUrl()
                    )
                )
                .addValue(
                    "datasetVersion",
                    request.datasetVersion()
                )
                .addValue(
                    "schemaVersion",
                    request.schemaVersion()
                )
                .addValue(
                    "hdfsBasePath",
                    nullable(
                        request.hdfsBasePath()
                    )
                );


        jdbcTemplate.update(
            sql,
            params
        );
    }

    /**
     * Dataset 설명 / 출처 / HDFS 경로 수정.
     *
     * Version은 여기서 변경하지 않는다.
     */
    /**
     * Dataset 메타정보 수정.
     *
     * Version은 수정하지 않는다.
     */
    public int update(
            Long datasetId,
            DatasetUpdateRequest request) {

        String sql = """
            UPDATE DATASET
            SET
                DISPLAY_NAME = :displayName,
                DESCRIPTION = :description,
                SOURCE_NAME = :sourceName,
                SOURCE_URL = :sourceUrl,
                HDFS_BASE_PATH = :hdfsBasePath,
                UPDATED_AT = SYSTIMESTAMP
            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """;


        /*
         * nullable 컬럼 처리를 위해
         * MapSqlParameterSource 사용.
         */
        MapSqlParameterSource params =
            new MapSqlParameterSource()
                .addValue(
                    "datasetId",
                    datasetId
                )
                .addValue(
                    "displayName",
                    request.displayName()
                )
                .addValue(
                    "description",
                    nullable(
                        request.description()
                    )
                )
                .addValue(
                    "sourceName",
                    request.sourceName()
                )
                .addValue(
                    "sourceUrl",
                    nullable(
                        request.sourceUrl()
                    )
                )
                .addValue(
                    "hdfsBasePath",
                    nullable(
                        request.hdfsBasePath()
                    )
                );


        return jdbcTemplate.update(
            sql,
            params
        );
    }


    /**
     * Dataset 논리 삭제.
     *
     * 물리 DELETE는 수행하지 않는다.
     */
    public int logicalDelete(
            Long datasetId) {

        String sql = """
            UPDATE DATASET
            SET
                IS_DELETED = 'Y',
                UPDATED_AT = SYSTIMESTAMP
            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """;


        return jdbcTemplate.update(
            sql,
            Map.of(
                "datasetId",
                datasetId
            )
        );
    }


    /**
     * 빈 문자열은 NULL로 처리.
     */
    private String nullable(
            String value) {

        if (
            value == null
            ||
            value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }


    private LocalDateTime toLocalDateTime(
            Timestamp timestamp) {

        return timestamp == null
            ? null
            : timestamp.toLocalDateTime();
    }
}