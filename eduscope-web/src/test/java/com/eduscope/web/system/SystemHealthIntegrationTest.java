package com.eduscope.web.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.system.dto.SystemHealthResponse;
import com.eduscope.web.system.service.SystemHealthService;


/**
 * EduScope System Health 실제 통합 테스트.
 *
 * 테스트 범위:
 *
 * Spring Context
 *   ↓
 * SystemHealthService
 *   ↓
 * DataSource
 *   ↓
 * Oracle
 *
 * 그리고 실제 ANALYSIS_JOB 최신 데이터를 검증한다.
 *
 * 임의 Job이나 임의 상태값은 생성하지 않는다.
 */
@SpringBootTest
@Tag("oracle")
class SystemHealthIntegrationTest {


    @Autowired
    private SystemHealthService
        systemHealthService;


    @Autowired
    private AnalysisJobRepository
        analysisJobRepository;


    /**
     * System Health 전체 응답이
     * 정상적으로 생성되는지 확인한다.
     */
    @Test
    void systemHealthShouldBeAvailable() {

        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        assertNotNull(
            response
        );


        assertNotNull(
            response.checkedAt()
        );


        assertNotNull(
            response.application()
        );


        assertNotNull(
            response.database()
        );


        assertNotNull(
            response.runtime()
        );
    }


    /**
     * 현재 Spring Boot Application이
     * 실제 실행 중인 상태인지 확인한다.
     */
    @Test
    void applicationShouldBeUp() {

        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        assertEquals(
            "UP",
            response
                .application()
                .status()
        );


        assertNotNull(
            response
                .application()
                .applicationName()
        );


        assertTrue(
            !response
                .application()
                .applicationName()
                .isBlank()
        );
    }


    /**
     * 실제 Oracle DataSource 연결 상태를 확인한다.
     *
     * 현재 EduScope 테스트 환경에서는
     * Oracle이 실행 중이어야 한다.
     */
    @Test
    void oracleDatabaseShouldBeUp() {

        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        SystemHealthResponse.DatabaseHealth database =
            response.database();


        assertEquals(
            "UP",
            database.status()
        );


        assertNotNull(
            database.databaseProduct()
        );


        assertTrue(
            database
                .databaseProduct()
                .toLowerCase()
                .contains(
                    "oracle"
                )
        );


        assertNotNull(
            database.databaseVersion()
        );


        /*
         * 1ms 미만이면 0ms가 될 수 있으므로
         * 0 이상인지 확인한다.
         */
        assertTrue(
            database.responseTimeMs()
            >= 0
        );
    }


    /**
     * 실제 JVM Runtime 정보가
     * 조회되는지 확인한다.
     */
    @Test
    void runtimeInformationShouldBeAvailable() {

        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        SystemHealthResponse.RuntimeHealth runtime =
            response.runtime();


        assertNotNull(
            runtime.javaVersion()
        );


        assertTrue(
            !runtime
                .javaVersion()
                .isBlank()
        );


        assertTrue(
            runtime.uptimeMs()
            >= 0
        );


        assertTrue(
            runtime.availableProcessors()
            > 0
        );


        assertTrue(
            runtime.usedMemoryMb()
            >= 0
        );


        assertTrue(
            runtime.maxMemoryMb()
            > 0
        );
    }


    /**
     * System Health가 표시하는 최신 Job과
     * AnalysisJobRepository의 실제 최신 Job이
     * 동일한지 확인한다.
     *
     * 특정 JOB_ID를 하드코딩하지 않는다.
     */
    @Test
    void latestAnalysisJobShouldMatchDatabase() {

        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        Optional<AnalysisJob> latestJob =
            analysisJobRepository
                .findTopByOrderByJobIdDesc();


        /*
         * ANALYSIS_JOB이 없는 환경이라면
         * API 역시 null을 반환해야 한다.
         */
        if (
            latestJob.isEmpty()
        ) {

            assertNull(
                response.latestAnalysisJob()
            );


            return;
        }


        /*
         * 실제 최신 Job이 존재하는 경우
         * 응답값과 DB 값을 직접 비교한다.
         */
        AnalysisJob expected =
            latestJob.get();


        SystemHealthResponse.LatestAnalysisJob actual =
            response.latestAnalysisJob();


        assertNotNull(
            actual
        );


        assertEquals(
            expected.getJobId(),
            actual.jobId()
        );


        assertEquals(
            expected.getAnalysisType(),
            actual.analysisType()
        );


        assertEquals(
            expected.getStatus(),
            actual.status()
        );


        assertEquals(
            expected.getResultImportedYn(),
            actual.resultImportedYn()
        );


        assertEquals(
            expected.getOutputRecordCount(),
            actual.outputRecordCount()
        );


        assertEquals(
            expected.getProcessingTimeMs(),
            actual.processingTimeMs()
        );


        assertEquals(
            expected.getFinishedAt(),
            actual.finishedAt()
        );


        assertEquals(
            expected.getErrorStep(),
            actual.errorStep()
        );
    }
}
