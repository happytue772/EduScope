package com.eduscope.web.system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.env.Environment;

import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.system.dto.SystemHealthResponse;


/**
 * SystemHealthService 단위 테스트.
 *
 * 실제 Oracle에 접속하지 않는다.
 * Mockito Test Double을 사용하여
 * 장애 상황과 예외 처리 로직을 검증한다.
 *
 * 테스트 데이터는 DB에 INSERT되지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class SystemHealthServiceTest {


    @Mock
    private DataSource dataSource;


    @Mock
    private AnalysisJobRepository
        analysisJobRepository;


    @Mock
    private Environment environment;


    @Mock
    private Connection connection;


    @Mock
    private DatabaseMetaData databaseMetaData;


    private SystemHealthService
        systemHealthService;


    /**
     * 각 테스트 실행 전에
     * 실제 Service 객체를 생성한다.
     *
     * 의존 객체만 Mockito Mock으로 전달한다.
     */
    @BeforeEach
    void setUp() {

        systemHealthService =
            new SystemHealthService(

                dataSource,

                analysisJobRepository,

                environment
            );
    }


    /**
     * Oracle Connection 획득 자체가 실패해도
     * System Health 전체 요청이 예외로 종료되지 않고
     * DB 상태만 DOWN으로 반환되는지 검증한다.
     */
    @Test
    void databaseConnectionFailureShouldReturnDown()
            throws Exception {

        when(
            environment.getProperty(
                "spring.application.name",
                "eduscope-web"
            )
        )
        .thenReturn(
            "eduscope-web"
        );


        /*
         * 실제 Oracle을 중단시키지 않고
         * Connection 오류 상황만 Mock으로 재현한다.
         */
        when(
            dataSource.getConnection()
        )
        .thenThrow(
            new SQLException(
                "TEST_DATABASE_CONNECTION_FAILURE"
            )
        );


        when(
            analysisJobRepository
                .findTopByOrderByJobIdDesc()
        )
        .thenReturn(
            Optional.empty()
        );


        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        /*
         * DB가 DOWN이어도
         * System Health 응답 자체는 살아 있어야 한다.
         */
        assertNotNull(
            response
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


        assertEquals(
            "UP",
            response
                .application()
                .status()
        );


        assertEquals(
            "DOWN",
            response
                .database()
                .status()
        );


        /*
         * Connection을 얻지 못했으므로
         * DB 제품/버전 정보는 존재하지 않는다.
         */
        assertNull(
            response
                .database()
                .databaseProduct()
        );


        assertNull(
            response
                .database()
                .databaseVersion()
        );


        /*
         * 실패하더라도 측정 시간은
         * 음수가 될 수 없다.
         */
        assertTrue(
            response
                .database()
                .responseTimeMs()
            >= 0
        );


        /*
         * 실제 Job 데이터가 없는 상황.
         */
        assertNull(
            response.latestAnalysisJob()
        );
    }


    /**
     * ANALYSIS_JOB 조회 중 Repository 오류가 발생해도
     * System Health 전체가 500으로 무너지지 않는지 검증한다.
     */
    @Test
    void analysisJobRepositoryFailureShouldNotBreakHealth()
            throws Exception {

        when(
            environment.getProperty(
                "spring.application.name",
                "eduscope-web"
            )
        )
        .thenReturn(
            "eduscope-web"
        );


        /*
         * DB Connection 자체는 정상인 상황을
         * Test Double로 구성한다.
         */
        when(
            dataSource.getConnection()
        )
        .thenReturn(
            connection
        );


        when(
            connection.isValid(2)
        )
        .thenReturn(
            true
        );


        when(
            connection.getMetaData()
        )
        .thenReturn(
            databaseMetaData
        );


        /*
         * DatabaseMetaData의 테스트 응답이다.
         * Oracle에 저장되는 데이터가 아니다.
         */
        when(
            databaseMetaData
                .getDatabaseProductName()
        )
        .thenReturn(
            "Oracle"
        );


        when(
            databaseMetaData
                .getDatabaseProductVersion()
        )
        .thenReturn(
            "TEST_VERSION"
        );


        /*
         * 최신 Job 조회 중 장애 상황 재현.
         */
        when(
            analysisJobRepository
                .findTopByOrderByJobIdDesc()
        )
        .thenThrow(
            new RuntimeException(
                "TEST_ANALYSIS_JOB_QUERY_FAILURE"
            )
        );


        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        /*
         * Repository 오류가 있어도
         * 전체 System Health는 반환되어야 한다.
         */
        assertNotNull(
            response
        );


        assertEquals(
            "UP",
            response
                .database()
                .status()
        );


        /*
         * 최신 Job 조회만 실패했으므로
         * Job 정보만 null이어야 한다.
         */
        assertNull(
            response.latestAnalysisJob()
        );
    }


    /**
     * ANALYSIS_JOB 테이블에 조회 가능한 Job이 없는 경우.
     *
     * 오류 상황이 아니라
     * 정상적으로 latestAnalysisJob=null을 반환해야 한다.
     */
    @Test
    void noLatestAnalysisJobShouldReturnNull()
            throws Exception {

        when(
            environment.getProperty(
                "spring.application.name",
                "eduscope-web"
            )
        )
        .thenReturn(
            "eduscope-web"
        );


        when(
            dataSource.getConnection()
        )
        .thenReturn(
            connection
        );


        when(
            connection.isValid(2)
        )
        .thenReturn(
            true
        );


        when(
            connection.getMetaData()
        )
        .thenReturn(
            databaseMetaData
        );


        when(
            databaseMetaData
                .getDatabaseProductName()
        )
        .thenReturn(
            "Oracle"
        );


        when(
            databaseMetaData
                .getDatabaseProductVersion()
        )
        .thenReturn(
            "TEST_VERSION"
        );


        when(
            analysisJobRepository
                .findTopByOrderByJobIdDesc()
        )
        .thenReturn(
            Optional.empty()
        );


        SystemHealthResponse response =
            systemHealthService
                .getSystemHealth();


        assertNotNull(
            response
        );


        assertEquals(
            "UP",
            response
                .database()
                .status()
        );


        assertNull(
            response.latestAnalysisJob()
        );
    }
}