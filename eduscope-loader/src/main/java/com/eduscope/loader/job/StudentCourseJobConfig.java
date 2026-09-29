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
import com.eduscope.loader.reference.batch.StudentCourseProcessor;
import com.eduscope.loader.reference.dto.StudentInfoCsvRow;
import com.eduscope.loader.reference.entity.StudentCourse;

@Configuration
public class StudentCourseJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentInfoCsvRow>
            studentCourseInfoReader(
                    InputPathProperties inputPathProperties) {

        Path file = Paths.get(
                inputPathProperties.getRawPath(),
                "studentInfo.csv"
        );

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                    "studentInfo.csv를 찾을 수 없습니다: "
                    + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentInfoCsvRow>()
                .name("studentCourseInfoReader")
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
    public JpaItemWriter<StudentCourse>
            studentCourseWriter(
                    EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<StudentCourse>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step studentCourseImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<StudentInfoCsvRow>
                    studentCourseInfoReader,
            StudentCourseProcessor processor,
            JpaItemWriter<StudentCourse>
                    studentCourseWriter) {

        return new StepBuilder(
                "studentCourseImportStep",
                jobRepository
        )
        .<StudentInfoCsvRow, StudentCourse>chunk(
                500,
                transactionManager
        )
        .reader(studentCourseInfoReader)
        .processor(processor)
        .writer(studentCourseWriter)
        .build();
    }

    @Bean
    public Job studentCourseImportJob(
            JobRepository jobRepository,
            Step studentCourseImportStep) {

        return new JobBuilder(
                "studentCourseImportJob",
                jobRepository
        )
        .start(studentCourseImportStep)
        .build();
    }

    private static String emptyToNull(String value) {

        return value == null || value.isBlank()
                ? null
                : value;
    }
}