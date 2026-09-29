package com.eduscope.web.system.dto;

import java.time.LocalDateTime;


/**
 * EduScope 시스템 상태 응답.
 *
 * 모든 값은 실제 실행환경 또는
 * Oracle ANALYSIS_JOB에서 조회한다.
 */
public record SystemHealthResponse(

        LocalDateTime checkedAt,

        ApplicationHealth application,

        DatabaseHealth database,

        RuntimeHealth runtime,

        LatestAnalysisJob latestAnalysisJob

) {


    /**
     * Spring Boot Application 상태.
     */
    public record ApplicationHealth(

            String status,

            String applicationName

    ) {
    }


    /**
     * 실제 DB Connection 상태.
     */
    public record DatabaseHealth(

            String status,

            String databaseProduct,

            String databaseVersion,

            Long responseTimeMs

    ) {
    }


    /**
     * 현재 JVM Runtime 정보.
     */
    public record RuntimeHealth(

            String javaVersion,

            Long uptimeMs,

            Integer availableProcessors,

            Long usedMemoryMb,

            Long maxMemoryMb

    ) {
    }


    /**
     * 실제 ANALYSIS_JOB 최신 1건.
     */
    public record LatestAnalysisJob(

            Long jobId,

            String analysisType,

            String status,

            String resultImportedYn,

            Long outputRecordCount,

            Long processingTimeMs,

            LocalDateTime finishedAt,

            String errorStep

    ) {
    }
}