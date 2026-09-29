package com.eduscope.loader.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.dataset.tasklet.DatasetFileBootstrapTasklet;

/**
 * OULAD 원본 CSV 7개의 메타정보를
 * DATASET_FILE에 등록하는 Batch Job.
 */
@Configuration
public class DatasetFileBootstrapJobConfig {

    @Bean
    public Step datasetFileBootstrapStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DatasetFileBootstrapTasklet tasklet) {

        return new StepBuilder(
                "datasetFileBootstrapStep",
                jobRepository
        )
        .tasklet(
                tasklet,
                transactionManager
        )
        .build();
    }

    @Bean
    public Job datasetFileBootstrapJob(
            JobRepository jobRepository,
            Step datasetFileBootstrapStep) {

        return new JobBuilder(
                "datasetFileBootstrapJob",
                jobRepository
        )
        .start(datasetFileBootstrapStep)
        .build();
    }
}