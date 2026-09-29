package com.eduscope.loader.analysis.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.analysis.tasklet.AnalysisJobBootstrapTasklet;

/**
 * 기존 Hadoop 분석 결과를
 * ANALYSIS_JOB에 최초 등록하는 Bootstrap Job.
 */
@Configuration
public class AnalysisJobBootstrapJobConfig {

    @Bean
    public Step analysisJobBootstrapStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AnalysisJobBootstrapTasklet tasklet) {

        return new StepBuilder(
            "analysisJobBootstrapStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job analysisJobBootstrapJob(
            JobRepository jobRepository,
            @Qualifier("analysisJobBootstrapStep")
            Step step) {

        return new JobBuilder(
            "analysisJobBootstrapJob",
            jobRepository
        )
        .start(step)
        .build();
    }
}