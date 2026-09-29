package com.eduscope.web.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.IntSupplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eduscope.web.analysis.service.AnalysisJobService;
import com.eduscope.web.assessment.service.AssessmentService;
import com.eduscope.web.assessment.service.StudentAssessmentService;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.student.service.StudentCourseService;
import com.eduscope.web.student.service.StudentRegistrationService;
import com.eduscope.web.vle.service.VleMaterialService;

import jakarta.persistence.EntityManagerFactory;

/**
 * 실제 Oracle 조회 시 Hibernate가 실행한 SQL 수를 측정한다.
 * 데이터를 생성·수정·삭제하지 않으며, 측정 전에는 임계값을 고정하지 않는다.
 */
@SpringBootTest
@Tag("oracle")
class JpaNPlusOneMeasurementTest {

    private static final int PAGE_SIZE = 20;

    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private AnalysisJobService analysisJobService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private VleMaterialService vleMaterialService;
    @Autowired private AssessmentService assessmentService;
    @Autowired private StudentCourseService studentCourseService;
    @Autowired private StudentAssessmentService studentAssessmentService;
    @Autowired private StudentRegistrationService studentRegistrationService;

    private Statistics statistics;
    private boolean statisticsPreviouslyEnabled;

    @BeforeEach
    void enableStatistics() {
        SessionFactory sessionFactory =
            entityManagerFactory.unwrap(SessionFactory.class);

        statistics = sessionFactory.getStatistics();
        statisticsPreviouslyEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @AfterEach
    void restoreStatisticsSetting() {
        statistics.clear();
        statistics.setStatisticsEnabled(statisticsPreviouslyEnabled);
    }

    @Test
    void measureAnalysisJobList() {
        measure("AnalysisJobService.getJobs", () ->
            analysisJobService.getJobs().size()
        );
    }

    @Test
    void measureAuditLogPage() {
        measure("AuditLogService.getLogs", () ->
            auditLogService.getLogs(0, PAGE_SIZE).getNumberOfElements()
        );
    }

    @Test
    void measureVleMaterialPage() {
        measure("VleMaterialService.getMaterials", () ->
            vleMaterialService.getMaterials(0, PAGE_SIZE).getNumberOfElements()
        );
    }

    @Test
    void measureAssessmentList() {
        measure("AssessmentService.getAssessments", () ->
            assessmentService.getAssessments().size()
        );
    }

    @Test
    void measureStudentCoursePage() {
        measure("StudentCourseService.getStudentCourses", () ->
            studentCourseService.getStudentCourses(0, PAGE_SIZE).getNumberOfElements()
        );
    }

    @Test
    void measureStudentAssessmentPage() {
        measure("StudentAssessmentService.getStudentAssessments", () ->
            studentAssessmentService
                .getStudentAssessments(0, PAGE_SIZE)
                .getNumberOfElements()
        );
    }

    @Test
    void measureStudentRegistrationPage() {
        measure("StudentRegistrationService.getRegistrations", () ->
            studentRegistrationService
                .getRegistrations(0, PAGE_SIZE)
                .getNumberOfElements()
        );
    }

    /** 각 측정 직전에 통계를 초기화해 다른 테스트의 SQL을 포함하지 않는다. */
    private void measure(
            String target,
            IntSupplier query) {

        statistics.clear();

        long startedAt = System.nanoTime();
        int rowCount = query.getAsInt();
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;

        long preparedStatements = statistics.getPrepareStatementCount();
        long entityFetches = statistics.getEntityFetchCount();
        long collectionFetches = statistics.getCollectionFetchCount();

        System.out.printf(
            "N_PLUS_ONE_MEASUREMENT target=%s rows=%d statements=%d "
                + "entityFetches=%d collectionFetches=%d elapsedMs=%d%n",
            target,
            rowCount,
            preparedStatements,
            entityFetches,
            collectionFetches,
            elapsedMs
        );

        // 측정 자체가 실행됐는지만 검증하고, 개선 기준은 실제 결과 확인 후 정한다.
        assertTrue(preparedStatements > 0L);
    }
}
