package com.eduscope.loader.job;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.reference.batch.CoursePresentationProcessor;
import com.eduscope.loader.reference.dto.CourseCsvRow;
import com.eduscope.loader.reference.entity.CoursePresentation;

/**
 * courses.csv → COURSE_PRESENTATION 적재 Job.
 */
@Configuration
public class CoursePresentationJobConfig {

    /**
     * 실제 courses.csv Reader.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CourseCsvRow> courseCsvReader(
            InputPathProperties inputPathProperties) {

        Path file = Paths.get(
                inputPathProperties.getRawPath(),
                "courses.csv"
        );

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                    "courses.csv 파일을 찾을 수 없습니다: "
                    + file
            );
        }

        return new FlatFileItemReaderBuilder<CourseCsvRow>()
                .name("courseCsvReader")
                .resource(
                        new FileSystemResource(file)
                )
                .encoding(
                        StandardCharsets.UTF_8.name()
                )
                // 첫 번째 행은 CSV Header
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "codeModule",
                        "codePresentation",
                        "modulePresentationLength"
                )
                .fieldSetMapper(fieldSet -> {

                    CourseCsvRow row =
                            new CourseCsvRow();

                    row.setCodeModule(
                            fieldSet.readString(
                                    "codeModule"
                            )
                    );

                    row.setCodePresentation(
                            fieldSet.readString(
                                    "codePresentation"
                            )
                    );

                    row.setModulePresentationLength(
                            fieldSet.readInt(
                                    "modulePresentationLength"
                            )
                    );

                    return row;
                })
                .strict(true)
                .build();
    }

    /**
     * JPA 기반 Oracle Writer.
     */
    @Bean
    public JpaItemWriter<CoursePresentation>
            coursePresentationWriter(
                    EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<CoursePresentation>()
                .entityManagerFactory(
                        entityManagerFactory
                )
                .build();
    }

    /**
     * Reader → Processor → Writer Step.
     */
    @Bean
    public Step coursePresentationImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CourseCsvRow> courseCsvReader,
            CoursePresentationProcessor processor,
            JpaItemWriter<CoursePresentation> coursePresentationWriter) {

        return new StepBuilder(
                "coursePresentationImportStep",
                jobRepository
        )
        .<CourseCsvRow, CoursePresentation>chunk(
                100,
                transactionManager
        )
        .reader(courseCsvReader)
        .processor(processor)
        .writer(coursePresentationWriter)
        .build();
    }

    /**
     * COURSE_PRESENTATION 적재 Job.
     */
    @Bean
    public Job coursePresentationImportJob(
            JobRepository jobRepository,
            Step coursePresentationImportStep) {

        return new JobBuilder(
                "coursePresentationImportJob",
                jobRepository
        )
        .start(coursePresentationImportStep)
        .build();
    }
}