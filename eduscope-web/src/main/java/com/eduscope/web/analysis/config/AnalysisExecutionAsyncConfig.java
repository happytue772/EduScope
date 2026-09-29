package com.eduscope.web.analysis.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Hadoop Analysis Job 전용 비동기 Executor.
 *
 * 현재 단일 VMware Hadoop 환경이므로
 * 동시에 1개 Job만 실제 실행한다.
 */
@Configuration
@EnableAsync
public class AnalysisExecutionAsyncConfig {

    @Bean(
        name = "analysisJobTaskExecutor"
    )
    public Executor analysisJobTaskExecutor() {

        ThreadPoolTaskExecutor executor =
            new ThreadPoolTaskExecutor();


        executor.setCorePoolSize(
            1
        );

        executor.setMaxPoolSize(
            1
        );

        executor.setQueueCapacity(
            20
        );

        executor.setThreadNamePrefix(
            "eduscope-analysis-"
        );


        executor.initialize();


        return executor;
    }
}