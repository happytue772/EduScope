package com.eduscope.web.system.service;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.eduscope.web.analysis.entity.AnalysisJob;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.system.dto.SystemHealthResponse;


/**
 * EduScope System Health Service.
 *
 * 실제 Application / Oracle / JVM /
 * ANALYSIS_JOB 상태를 조회한다.
 *
 * 임의 UP 상태나 임의 Job 데이터를 생성하지 않는다.
 */
@Service
public class SystemHealthService {

    private static final Logger log =
        LoggerFactory.getLogger(
            SystemHealthService.class
        );


    private static final long BYTES_PER_MB =
        1024L * 1024L;


    private final DataSource dataSource;

    private final AnalysisJobRepository
        analysisJobRepository;

    private final Environment environment;


    public SystemHealthService(

            DataSource dataSource,

            AnalysisJobRepository analysisJobRepository,

            Environment environment) {

        this.dataSource =
            dataSource;

        this.analysisJobRepository =
            analysisJobRepository;

        this.environment =
            environment;
    }


    /**
     * 전체 시스템 상태 조회.
     */
    public SystemHealthResponse getSystemHealth() {

        return new SystemHealthResponse(

            LocalDateTime.now(),

            getApplicationHealth(),

            getDatabaseHealth(),

            getRuntimeHealth(),

            getLatestAnalysisJob()
        );
    }


    /**
     * 현재 요청이 정상적으로 Service까지 도달했다면
     * Spring Boot Application 자체는 실행 중이다.
     */
    private SystemHealthResponse.ApplicationHealth
            getApplicationHealth() {

        String applicationName =
            environment.getProperty(
                "spring.application.name",
                "eduscope-web"
            );


        return new SystemHealthResponse
            .ApplicationHealth(

                "UP",

                applicationName
            );
    }


    /**
     * 실제 DataSource Connection을 열어
     * Oracle 연결 상태를 검사한다.
     *
     * URL / 계정 / 비밀번호는 응답에 노출하지 않는다.
     */
    private SystemHealthResponse.DatabaseHealth
            getDatabaseHealth() {

        long startedAt =
            System.nanoTime();


        try (
            Connection connection =
                dataSource.getConnection()
        ) {

            boolean valid =
                connection.isValid(2);


            DatabaseMetaData metadata =
                connection.getMetaData();


            long responseTimeMs =
                (
                    System.nanoTime()
                    -
                    startedAt
                )
                /
                1_000_000L;


            return new SystemHealthResponse
                .DatabaseHealth(

                    valid
                        ? "UP"
                        : "DOWN",

                    metadata
                        .getDatabaseProductName(),

                    metadata
                        .getDatabaseProductVersion(),

                    responseTimeMs
                );

        } catch (Exception exception) {

            long responseTimeMs =
                (
                    System.nanoTime()
                    -
                    startedAt
                )
                /
                1_000_000L;


            /*
             * DB 상세 오류는 Server Log에만 남긴다.
             * 사용자 API에는 계정/URL/SQL 오류를 노출하지 않는다.
             */
            log.warn(
                "System Health DB 연결 확인 실패",
                exception
            );


            return new SystemHealthResponse
                .DatabaseHealth(

                    "DOWN",

                    null,

                    null,

                    responseTimeMs
                );
        }
    }


    /**
     * 현재 JVM 상태.
     */
    private SystemHealthResponse.RuntimeHealth
            getRuntimeHealth() {

        Runtime runtime =
            Runtime.getRuntime();


        long usedMemory =
            runtime.totalMemory()
            -
            runtime.freeMemory();


        long uptimeMs =
            ManagementFactory
                .getRuntimeMXBean()
                .getUptime();


        return new SystemHealthResponse
            .RuntimeHealth(

                System.getProperty(
                    "java.version"
                ),

                uptimeMs,

                runtime
                    .availableProcessors(),

                usedMemory
                    /
                    BYTES_PER_MB,

                runtime
                    .maxMemory()
                    /
                    BYTES_PER_MB
            );
    }


    /**
     * Oracle ANALYSIS_JOB에서
     * 실제 최신 Job 1건을 조회한다.
     *
     * Job이 하나도 없으면 null을 반환한다.
     */
    private SystemHealthResponse.LatestAnalysisJob
            getLatestAnalysisJob() {

        try {

            Optional<AnalysisJob> optionalJob =
                analysisJobRepository
                    .findTopByOrderByJobIdDesc();


            if (
                optionalJob.isEmpty()
            ) {

                return null;
            }


            AnalysisJob job =
                optionalJob.get();


            return new SystemHealthResponse
                .LatestAnalysisJob(

                    job.getJobId(),

                    job.getAnalysisType(),

                    job.getStatus(),

                    job.getResultImportedYn(),

                    job.getOutputRecordCount(),

                    job.getProcessingTimeMs(),

                    job.getFinishedAt(),

                    job.getErrorStep()
                );

        } catch (Exception exception) {

            /*
             * DB 자체가 DOWN이면 최신 Job 조회도 실패할 수 있다.
             * System Health API 전체를 500으로 만들지 않고
             * 최신 Job 정보만 null 처리한다.
             */
            log.warn(
                "최신 Analysis Job 조회 실패",
                exception
            );


            return null;
        }
    }
}