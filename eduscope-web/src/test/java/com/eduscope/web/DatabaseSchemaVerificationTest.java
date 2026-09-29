package com.eduscope.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * EduScope 최종 업무 테이블 24개 존재 여부 검증.
 *
 * 임의 데이터를 만들지 않고
 * Oracle USER_TABLES의 실제 Schema만 검사한다.
 */
@SpringBootTest
@Tag("oracle")
class DatabaseSchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allRequiredTablesShouldExist() {

        List<String> requiredTables = List.of(
            "DATASET",
            "DATASET_FILE",
            "APP_USER",
            "APP_ROLE",
            "APP_USER_ROLE",
            "COURSE_PRESENTATION",
            "OULAD_STUDENT",
            "STUDENT_COURSE",
            "STUDENT_REGISTRATION",
            "ASSESSMENT",
            "STUDENT_ASSESSMENT",
            "VLE_MATERIAL",
            "ANALYSIS_JOB",
            "STUDENT_ACTIVITY_STAT",
            "COURSE_ACTIVITY_STAT",
            "COURSE_WEEKLY_ACTIVITY_STAT",
            "VLE_ACTIVITY_STAT",
            "ASSESSMENT_STAT",
            "REGISTRATION_STAT",
            "COURSE_RESULT_STAT",
            "ACTIVITY_RESULT_STAT",
            "STUDENT_LEARNING_SUMMARY_STAT",
            "DATA_QUALITY_STAT",
            "AUDIT_LOG"
        );

        for (String tableName : requiredTables) {

            Integer count =
                jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    
                    
                    FROM USER_TABLES
                    WHERE TABLE_NAME = ?
                    """,
                    Integer.class,
                    tableName
                );

            assertEquals(
                1,
                count,
                "필수 테이블 누락: " + tableName
            );
        }
    }
}
