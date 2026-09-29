package com.eduscope.web.admin.audit.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.admin.audit.dto.AdminAuditLogResponse;

/**
 * AUDIT_LOG 관리자 조회 Repository.
 */
@Repository
public class AdminAuditRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    public AdminAuditRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate =
            jdbcTemplate;
    }


    /**
     * 최신 Audit Log부터 조회한다.
     *
     * APP_USER와 LEFT JOIN하여
     * 수행자의 Login ID / 표시 이름도 함께 조회한다.
     */
    public List<AdminAuditLogResponse> findAll() {

        String sql = """
            SELECT
                A.AUDIT_ID,
                A.USER_ID,
                U.LOGIN_ID,
                U.DISPLAY_NAME,
                A.ACTION_TYPE,
                A.TARGET_TYPE,
                A.TARGET_ID,
                A.REQUEST_URI,
                A.DESCRIPTION,
                A.IP_ADDRESS,
                A.CREATED_AT

            FROM AUDIT_LOG A

            LEFT JOIN APP_USER U
              ON U.USER_ID = A.USER_ID

            ORDER BY A.AUDIT_ID DESC
            """;


        return jdbcTemplate.query(
            sql,
            Map.of(),
            (rs, rowNum) ->
                new AdminAuditLogResponse(
                    rs.getLong(
                        "AUDIT_ID"
                    ),

                    getNullableLong(
                        rs,
                        "USER_ID"
                    ),

                    rs.getString(
                        "LOGIN_ID"
                    ),

                    rs.getString(
                        "DISPLAY_NAME"
                    ),

                    rs.getString(
                        "ACTION_TYPE"
                    ),

                    rs.getString(
                        "TARGET_TYPE"
                    ),

                    rs.getString(
                        "TARGET_ID"
                    ),

                    rs.getString(
                        "REQUEST_URI"
                    ),

                    rs.getString(
                        "DESCRIPTION"
                    ),

                    rs.getString(
                        "IP_ADDRESS"
                    ),

                    toLocalDateTime(
                        rs.getTimestamp(
                            "CREATED_AT"
                        )
                    )
                )
        );
    }


    /**
     * Nullable NUMBER 컬럼 처리.
     */
    private Long getNullableLong(
            ResultSet rs,
            String column)
            throws SQLException {

        long value =
            rs.getLong(column);


        if (rs.wasNull()) {

            return null;
        }


        return value;
    }


    /**
     * Timestamp → LocalDateTime.
     */
    private LocalDateTime toLocalDateTime(
            Timestamp timestamp) {

        return timestamp == null
            ? null
            : timestamp.toLocalDateTime();
    }
}