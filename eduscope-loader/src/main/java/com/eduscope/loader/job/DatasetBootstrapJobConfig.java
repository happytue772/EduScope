package com.eduscope.loader.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.dataset.tasklet.DatasetBootstrapTasklet;

/**
 * DATASET 최초 적재용 Spring Batch Job 설정.
 */
@Configuration
public class DatasetBootstrapJobConfig {

    /**
     * DATASET 등록 Step.
     */
    @Bean
    public Step datasetBootstrapStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DatasetBootstrapTasklet datasetBootstrapTasklet) {

        return new StepBuilder(
                "datasetBootstrapStep",
                jobRepository
        )
        .tasklet(
                datasetBootstrapTasklet,
                transactionManager
        )
        .build();
    }

    /**
     * DATASET 최초 등록 Job.
     */
    @Bean
    public Job datasetBootstrapJob(
            JobRepository jobRepository,
            Step datasetBootstrapStep) {

        return new JobBuilder(
                "datasetBootstrapJob",
                jobRepository
        )
        .start(datasetBootstrapStep)
        .build();
    }
}