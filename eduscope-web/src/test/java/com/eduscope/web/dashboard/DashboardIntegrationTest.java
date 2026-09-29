package com.eduscope.web.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eduscope.web.dashboard.dto.DashboardSummaryResponse;
import com.eduscope.web.dashboard.service.DashboardService;

/**
 * 실제 Oracle Dashboard 집계 통합 테스트.
 */
@SpringBootTest
@Tag("oracle")
class DashboardIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void dashboardSummaryTest() {

        DashboardSummaryResponse summary =
            dashboardService.getSummary(1L);

        assertEquals(
            1,
            summary.getDatasetCount()
        );

        // OULAD 원본은 7개 CSV
        assertEquals(
            7,
            summary.getDatasetFileCount()
        );

        assertTrue(
            summary.getCoursePresentationCount() > 0
        );

        assertTrue(
            summary.getUniqueStudentCount() > 0
        );

        assertTrue(
            summary.getEnrollmentCount() > 0
        );

        assertNotNull(
            summary.getCourseActivityJobId()
        );

        assertNotNull(
            summary.getCourseResultJobId()
        );
    }
}
