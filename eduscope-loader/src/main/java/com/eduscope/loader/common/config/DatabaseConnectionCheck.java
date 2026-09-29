package com.eduscope.loader.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * EduScope Loader 시작 시 Oracle 연결 여부를 확인한다.
 * 데이터 변경 작업은 수행하지 않는다.
 */
@Component
public class DatabaseConnectionCheck implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    // JdbcTemplate을 Spring으로부터 주입받는다.
    public DatabaseConnectionCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {

        // 현재 Oracle 접속 사용자 확인
        String oracleUser = jdbcTemplate.queryForObject(
                "SELECT USER FROM DUAL",
                String.class
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println("[EduScope Loader] Oracle 연결 성공");
        System.out.println("[Oracle User] " + oracleUser);
        System.out.println("========================================");
        System.out.println();
    }
}