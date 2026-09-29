package com.eduscope.web.admin.user.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.eduscope.web.admin.user.dto.AdminUserResponse;

/**
 * APP_USER + APP_USER_ROLE + APP_ROLE 조회 Repository.
 */
@Repository

public class AdminUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminUserRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }
    public String findAccountStatus(
            Long userId) {

        String sql = """
            SELECT ACCOUNT_STATUS
            FROM APP_USER
            WHERE USER_ID = :userId
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "userId",
                userId
            ),
            String.class
        );
    }


    /**
     * 현재 사용자에게 부여된 Role 조회.
     */
    public List<String> findRoleCodes(
            Long userId) {

        String sql = """
            SELECT R.ROLE_CODE
            FROM APP_USER_ROLE UR
            JOIN APP_ROLE R
              ON R.ROLE_ID = UR.ROLE_ID
            WHERE UR.USER_ID = :userId
            ORDER BY R.ROLE_CODE
            """;

        return jdbcTemplate.queryForList(
            sql,
            Map.of(
                "userId",
                userId
            ),
            String.class
        );
    }
    public void insertUserRole(
            Long userId,
            Long roleId) {

        String sql = """
            INSERT INTO APP_USER_ROLE (
                USER_ID,
                ROLE_ID,
                ASSIGNED_AT
            )
            VALUES (
                :userId,
                :roleId,
                SYSTIMESTAMP
            )
            """;

        jdbcTemplate.update(
            sql,
            Map.of(
                "userId",
                userId,
                "roleId",
                roleId
            )
        );
    }
    public Long findRoleId(
            String roleCode) {

        String sql = """
            SELECT ROLE_ID
            FROM APP_ROLE
            WHERE ROLE_CODE = :roleCode
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Map.of(
                "roleCode",
                roleCode
            ),
            Long.class
        );
    }
    public void deleteUserRoles(
            Long userId) {

        jdbcTemplate.update(
            """
            DELETE FROM APP_USER_ROLE
            WHERE USER_ID = :userId
            """,
            Map.of(
                "userId",
                userId
            )
        );
    }
    public void updateAccountStatus(
            Long userId,
            String status) {

        String sql = """
            UPDATE APP_USER
            SET
                ACCOUNT_STATUS = :status,
                UPDATED_AT = SYSTIMESTAMP
            WHERE USER_ID = :userId
            """;

        jdbcTemplate.update(
            sql,
            Map.of(
                "userId",
                userId,
                "status",
                status
            )
        );
    }
    public boolean existsUser(
            Long userId) {

        String sql = """
            SELECT COUNT(*)
            FROM APP_USER
            WHERE USER_ID = :userId
            """;

        Long count =
            jdbcTemplate.queryForObject(
                sql,
                Map.of(
                    "userId",
                    userId
                ),
                Long.class
            );

        return count != null
            && count > 0;
    }


    /**
     * 시스템 사용자와 현재 Role을 조회한다.
     */
    public List<AdminUserResponse> findAllUsers() {

        String sql = """
            SELECT
                U.USER_ID,
                U.LOGIN_ID,
                U.DISPLAY_NAME,
                U.ACCOUNT_STATUS,
                U.LAST_LOGIN_AT,
                U.CREATED_AT,
                U.UPDATED_AT,
                R.ROLE_CODE

            FROM APP_USER U

            LEFT JOIN APP_USER_ROLE UR
              ON UR.USER_ID = U.USER_ID

            LEFT JOIN APP_ROLE R
              ON R.ROLE_ID = UR.ROLE_ID

            ORDER BY
                U.USER_ID,
                R.ROLE_CODE
            """;

        Map<Long, MutableUser> users =
            new LinkedHashMap<>();

        jdbcTemplate.query(
            sql,
            rs -> {

                Long userId =
                    rs.getLong("USER_ID");

                MutableUser user =
                    users.computeIfAbsent(
                        userId,
                        id -> new MutableUser(
                            id,
                            getString(
                                rs,
                                "LOGIN_ID"
                            ),
                            getString(
                                rs,
                                "DISPLAY_NAME"
                            ),
                            getString(
                                rs,
                                "ACCOUNT_STATUS"
                            ),
                            toLocalDateTime(
                                getTimestamp(
                                    rs,
                                    "LAST_LOGIN_AT"
                                )
                            ),
                            toLocalDateTime(
                                getTimestamp(
                                    rs,
                                    "CREATED_AT"
                                )
                            ),
                            toLocalDateTime(
                                getTimestamp(
                                    rs,
                                    "UPDATED_AT"
                                )
                            )
                        )
                    );

                String roleCode =
                    rs.getString("ROLE_CODE");

                if (roleCode != null) {
                    user.roles.add(roleCode);
                }
            }
        );

        return users.values()
            .stream()
            .map(MutableUser::toResponse)
            .toList();
    }


    /*
     * Lambda 내부 checked exception 처리를
     * 단순화하기 위한 보조 메서드.
     */
    private String getString(
            java.sql.ResultSet rs,
            String column) {

        try {
            return rs.getString(column);

        } catch (java.sql.SQLException e) {

            throw new IllegalStateException(e);
        }
    }


    private Timestamp getTimestamp(
            java.sql.ResultSet rs,
            String column) {

        try {
            return rs.getTimestamp(column);

        } catch (java.sql.SQLException e) {

            throw new IllegalStateException(e);
        }
    }


    private LocalDateTime toLocalDateTime(
            Timestamp timestamp) {

        return timestamp == null
            ? null
            : timestamp.toLocalDateTime();
    }


    /**
     * JOIN 결과를 사용자 단위로 묶는 내부 객체.
     */
    private static class MutableUser {

        private final Long userId;
        private final String loginId;
        private final String displayName;
        private final String accountStatus;

        private final LocalDateTime lastLoginAt;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        private final List<String> roles =
            new ArrayList<>();


        private MutableUser(
                Long userId,
                String loginId,
                String displayName,
                String accountStatus,
                LocalDateTime lastLoginAt,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {

            this.userId = userId;
            this.loginId = loginId;
            this.displayName = displayName;
            this.accountStatus = accountStatus;
            this.lastLoginAt = lastLoginAt;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }


        private AdminUserResponse toResponse() {

            return new AdminUserResponse(
                userId,
                loginId,
                displayName,
                accountStatus,
                lastLoginAt,
                createdAt,
                updatedAt,
                List.copyOf(roles)
            );
        }
    }
}