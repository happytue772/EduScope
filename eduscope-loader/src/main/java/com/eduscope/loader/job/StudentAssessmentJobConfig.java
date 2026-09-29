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
import com.eduscope.loader.reference.batch.StudentAssessmentProcessor;
import com.eduscope.loader.reference.dto.StudentAssessmentCsvRow;
import com.eduscope.loader.reference.entity.StudentAssessment;

/**
 * studentAssessment.csv
 * → STUDENT_ASSESSMENT 적재 Job.
 */
@Configuration
public class StudentAssessmentJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentAssessmentCsvRow>
            studentAssessmentReader(
                InputPathProperties inputPathProperties) {

        Path file = Paths.get(
            inputPathProperties.getRawPath(),
            "studentAssessment.csv"
        );

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "studentAssessment.csv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentAssessmentCsvRow>()
            .name("studentAssessmentReader")
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
                "sourceAssessmentId",
                "sourceStudentId",
                "submittedDay",
                "bankedValue",
                "score"
            )
            .fieldSetMapper(fieldSet -> {

                StudentAssessmentCsvRow row =
                        new StudentAssessmentCsvRow();

                row.setSourceAssessmentId(
                    fieldSet.readLong(
                        "sourceAssessmentId"
                    )
                );

                row.setSourceStudentId(
                    fieldSet.readLong(
                        "sourceStudentId"
                    )
                );

                row.setSubmittedDay(
                    fieldSet.readInt(
                        "submittedDay"
                    )
                );

                row.setBankedValue(
                    fieldSet.readInt(
                        "bankedValue"
                    )
                );

                row.setScore(
                    nullableBigDecimal(
                        fieldSet.readString(
                            "score"
                        )
                    )
                );

                return row;
            })
            .strict(true)
            .build();
    }

    @Bean
    public JpaItemWriter<StudentAssessment>
            studentAssessmentWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<StudentAssessment>()
            .entityManagerFactory(
                entityManagerFactory
            )

            // 신규 결과 데이터 INSERT
            .usePersist(true)

            .build();
    }

    @Bean
    public Step studentAssessmentImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("studentAssessmentReader")
            FlatFileItemReader<StudentAssessmentCsvRow> reader,
            StudentAssessmentProcessor processor,
            @Qualifier("studentAssessmentWriter")
            JpaItemWriter<StudentAssessment> writer) {

        return new StepBuilder(
            "studentAssessmentImportStep",
            jobRepository
        )
        .<StudentAssessmentCsvRow, StudentAssessment>chunk(
            500,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Job studentAssessmentImportJob(
            JobRepository jobRepository,
            @Qualifier("studentAssessmentImportStep")
            Step step) {

        return new JobBuilder(
            "studentAssessmentImportJob",
            jobRepository
        )
        .start(step)
        .build();
    }

    /**
     * SCORE 공백은 null로 보존.
     */
    private static BigDecimal nullableBigDecimal(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return new BigDecimal(
            value.trim()
        );
    }
}