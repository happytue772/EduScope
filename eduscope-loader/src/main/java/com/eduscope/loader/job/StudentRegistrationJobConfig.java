package com.eduscope.loader.job;

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
import com.eduscope.loader.reference.batch.StudentRegistrationProcessor;
import com.eduscope.loader.reference.dto.StudentRegistrationCsvRow;
import com.eduscope.loader.reference.entity.StudentRegistration;

/**
 * studentRegistration.csv
 * → STUDENT_REGISTRATION 적재 Job.
 */
@Configuration
public class StudentRegistrationJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentRegistrationCsvRow>
            studentRegistrationReader(
                    InputPathProperties inputPathProperties) {

        Path file = Paths.get(
                inputPathProperties.getRawPath(),
                "studentRegistration.csv"
        );

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                    "studentRegistration.csv를 찾을 수 없습니다: "
                    + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentRegistrationCsvRow>()
                .name("studentRegistrationReader")
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
                    "sourceStudentId",
                    "registrationDay",
                    "unregistrationDay"
                )
                .fieldSetMapper(fieldSet -> {

                    StudentRegistrationCsvRow row =
                            new StudentRegistrationCsvRow();

                    row.setCodeModule(
                        fieldSet.readString("codeModule")
                    );

                    row.setCodePresentation(
                        fieldSet.readString("codePresentation")
                    );

                    row.setSourceStudentId(
                        fieldSet.readLong("sourceStudentId")
                    );

                    row.setRegistrationDay(
                        nullableInteger(
                            fieldSet.readString(
                                "registrationDay"
                            )
                        )
                    );

                    row.setUnregistrationDay(
                        nullableInteger(
                            fieldSet.readString(
                                "unregistrationDay"
                            )
                        )
                    );

                    return row;
                })
                .strict(true)
                .build();
    }

    @Bean
    public JpaItemWriter<StudentRegistration>
            studentRegistrationWriter(
                    EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<StudentRegistration>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    @Bean
    public Step studentRegistrationImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("studentRegistrationReader")
            FlatFileItemReader<StudentRegistrationCsvRow> reader,
            StudentRegistrationProcessor processor,
            @Qualifier("studentRegistrationWriter")
            JpaItemWriter<StudentRegistration> writer) {

        return new StepBuilder(
                "studentRegistrationImportStep",
                jobRepository
        )
        .<StudentRegistrationCsvRow, StudentRegistration>chunk(
                500,
                transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Job studentRegistrationImportJob(
            JobRepository jobRepository,
            @Qualifier("studentRegistrationImportStep")
            Step step) {

        return new JobBuilder(
                "studentRegistrationImportJob",
                jobRepository
        )
        .start(step)
        .build();
    }

    /**
     * OULAD의 빈 날짜 값을 null로 보존한다.
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