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

import com.eduscope.loader.analysis.tasklet.DataQualityImportTasklet;

/**
 * DATA_QUALITY 대용량 결과 스트리밍 집계 Job.
 */
@Configuration
public class DataQualityStatJobConfig {

    @Bean
    public Step dataQualityImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataQualityImportTasklet tasklet) {

        return new StepBuilder(
            "dataQualityImportStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job dataQualityStatImportJob(
            JobRepository jobRepository,

            @Qualifier("dataQualityImportStep")
            Step step) {

        return new JobBuilder(
            "dataQualityStatImportJob",
            jobRepository
        )
        .start(step)
        .build();
    }
}