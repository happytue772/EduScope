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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.reference.batch.OuladStudentProcessor;
import com.eduscope.loader.reference.dto.StudentInfoCsvRow;
import com.eduscope.loader.reference.entity.OuladStudent;

/**
 * studentInfo.csv → OULAD_STUDENT 적재 Job.
 */
@Configuration
public class OuladStudentJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentInfoCsvRow>
            ouladStudentInfoReader(
                    InputPathProperties inputPathProperties) {

        Path file = Paths.get(
                inputPathProperties.getRawPath(),
                "studentInfo.csv"
        );

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                    "studentInfo.csv 파일을 찾을 수 없습니다: "
                    + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentInfoCsvRow>()
                .name("ouladStudentInfoReader")
                .resource(new FileSystemResource(file))
                .encoding(StandardCharsets.UTF_8.name())
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "codeModule",
                        "codePresentation",
                        "sourceStudentId",
                        "gender",
                        "region",
                        "highestEducation",
                        "imdBand",
                        "ageBand",
                        "numOfPrevAttempts",
                        "studiedCredits",
                        "disability",
                        "finalResult"
                )
                .fieldSetMapper(fieldSet -> {

                    StudentInfoCsvRow row =
                            new StudentInfoCsvRow();

                    row.setCodeModule(
                            fieldSet.readString("codeModule")
                    );

                    row.setCodePresentation(
                            fieldSet.readString("codePresentation")
                    );

                    row.setSourceStudentId(
                            fieldSet.readLong("sourceStudentId")
                    );

                    row.setGender(
                            fieldSet.readString("gender")
                    );

                    row.setRegion(
                            fieldSet.readString("region")
                    );

                    row.setHighestEducation(
                            fieldSet.readString("highestEducation")
                    );

                    row.setImdBand(
                            emptyToNull(
                                fieldSet.readString("imdBand")
                            )
                    );

                    row.setAgeBand(
                            fieldSet.readString("ageBand")
                    );

                    row.setNumOfPrevAttempts(
                            fieldSet.readInt("numOfPrevAttempts")
                    );

                    row.setStudiedCredits(
                            fieldSet.readInt("studiedCredits")
                    );

                    row.setDisability(
                            fieldSet.readString("disability")
                    );

                    row.setFinalResult(
                            fieldSet.readString("finalResult")
                    );

                    return row;
                })
                .strict(true)
                .build();
    }

    @Bean
    public JpaItemWriter<OuladStudent>
            ouladStudentWriter(
                    EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<OuladStudent>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step ouladStudentImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<StudentInfoCsvRow>
                    ouladStudentInfoReader,
            OuladStudentProcessor processor,
            JpaItemWriter<OuladStudent>
                    ouladStudentWriter) {

        return new StepBuilder(
                "ouladStudentImportStep",
                jobRepository
        )
        .<StudentInfoCsvRow, OuladStudent>chunk(
                500,
                transactionManager
        )
        .reader(ouladStudentInfoReader)
        .processor(processor)
        .writer(ouladStudentWriter)
        .build();
    }

    @Bean
    public Job ouladStudentImportJob(
            JobRepository jobRepository,
            Step ouladStudentImportStep) {

        return new JobBuilder(
                "ouladStudentImportJob",
                jobRepository
        )
        .start(ouladStudentImportStep)
        .build();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value;
    }
}