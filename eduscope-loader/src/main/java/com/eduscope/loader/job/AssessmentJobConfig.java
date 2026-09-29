package com.eduscope.loader.job;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.reference.batch.AssessmentProcessor;
import com.eduscope.loader.reference.dto.AssessmentCsvRow;
import com.eduscope.loader.reference.entity.Assessment;

/**
 * assessments.csv → ASSESSMENT 적재 Job.
 */
@Configuration
public class AssessmentJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<AssessmentCsvRow>
            assessmentReader(
                InputPathProperties inputPathProperties) {

        Path file = Paths.get(
            inputPathProperties.getRawPath(),
            "assessments.csv"
        );

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "assessments.csv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<AssessmentCsvRow>()
                .name("assessmentReader")
                .resource(
                    new FileSystemResource(file)
                )
                .encoding(
                    StandardCharsets.UTF_8.name()
                )
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                    "codeModule",
                    "codePresentation",
                    "sourceAssessmentId",
                    "assessmentType",
                    "assessmentDueDay",
                    "assessmentWeight"
                )
                .fieldSetMapper(fieldSet -> {

                    AssessmentCsvRow row =
                            new AssessmentCsvRow();

                    row.setCodeModule(
                        fieldSet.readString("codeModule")
                    );

                    row.setCodePresentation(
                        fieldSet.readString(
                            "codePresentation"
                        )
                    );

                    row.setSourceAssessmentId(
                        fieldSet.readLong(
                            "sourceAssessmentId"
                        )
                    );

                    row.setAssessmentType(
                        fieldSet.readString(
                            "assessmentType"
                        )
                    );

                    row.setAssessmentDueDay(
                        nullableInteger(
                            fieldSet.readString(
                                "assessmentDueDay"
                            )
                        )
                    );

                    row.setAssessmentWeight(
                        new BigDecimal(
                            fieldSet.readString(
                                "assessmentWeight"
                            ).trim()
                        )
                    );

                    return row;
                })
                .strict(true)
                .build();
    }

    @Bean
    public JpaItemWriter<Assessment>
            assessmentWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<Assessment>()
                .entityManagerFactory(
                    entityManagerFactory
                )

                /*
                 * 이번 Job은 기존 Entity 갱신이 아닌
                 * 신규 ASSESSMENT INSERT.
                 */
                .usePersist(true)
                .build();
    }

    @Bean
    public Step assessmentImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("assessmentReader")
            FlatFileItemReader<AssessmentCsvRow> reader,
            AssessmentProcessor processor,
            @Qualifier("assessmentWriter")
            JpaItemWriter<Assessment> writer) {

        return new StepBuilder(
            "assessmentImportStep",
            jobRepository
        )
        .<AssessmentCsvRow, Assessment>chunk(
            200,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Job assessmentImportJob(
            JobRepository jobRepository,
            @Qualifier("assessmentImportStep")
            Step step) {

        return new JobBuilder(
            "assessmentImportJob",
            jobRepository
        )
        .start(step)
        .build();
    }

    /**
     * assessments.csv의 빈 date 값을 null로 보존.
     */
    private static Integer nullableInteger(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return Integer.valueOf(
            value.trim()
        );
    }
}