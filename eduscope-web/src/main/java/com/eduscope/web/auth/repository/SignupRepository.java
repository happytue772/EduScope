package com.eduscope.web.auth.repository;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * APP_USER 회원가입 Repository.
 */
@Repository
public class SignupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SignupRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * Login ID 중복 확인.
     */
    public boolean existsByLoginId(
            String loginId) {

        String sql = """
            SELECT COUNT(*)
            FROM APP_USER
            WHERE LOGIN_ID = :loginId
            """;

        Long count =
            jdbcTemplate.queryForObject(
                sql,
                Map.of(
                    "loginId",
                    loginId
                ),
                Long.class
            );

        return count != null
            && count > 0;
    }


    /**
     * Oracle Sequence에서 신규 USER_ID 확보.
     */
    public Long nextUserId() {

        return jdbcTemplate
            .getJdbcTemplate()
            .queryForObject(
                """
                SELECT SEQ_APP_USER_ID.NEXTVAL
                FROM DUAL
                """,
                Long.class
            );
    }


    /**
     * 회원가입 사용자 저장.
     *
     * 신규 사용자는 관리자가 승인하기 전까지
     * DISABLED 상태로 생성한다.
     */
    public void insertUser(
            Long userId,
            String loginId,
            String passwordHash,
            String displayName) {

        String sql = """
            INSERT INTO APP_USER (
                USER_ID,
                LOGIN_ID,
                PASSWORD_HASH,
                DISPLAY_NAME,
                ACCOUNT_STATUS,
                CREATED_AT,
                UPDATED_AT
            )
            VALUES (
                :userId,
                :loginId,
                :passwordHash,
                :displayName,
                'DISABLED',
                SYSTIMESTAMP,
                SYSTIMESTAMP
            )
            """;

        jdbcTemplate.update(
            sql,
            Map.of(
                "userId",
                userId,
                "loginId",
                loginId,
                "passwordHash",
                passwordHash,
                "displayName",
                displayName
            )
        );
    }
}