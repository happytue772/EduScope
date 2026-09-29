package com.eduscope.loader.analysis.config;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.eduscope.loader.analysis.batch.CourseActivityStatProcessor;
import com.eduscope.loader.analysis.dto.CourseActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.CourseActivityStat;
import com.eduscope.loader.analysis.tasklet.CourseActivityImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * COURSE_ACTIVITY MapReduce 결과 TSV
 * → COURSE_ACTIVITY_STAT
 * → ANALYSIS_JOB RESULT_IMPORTED_YN = Y
 *
 * inputFile이 있으면 해당 파일을 사용하고,
 * 없으면 기존 course-activity.tsv 방식을 유지한다.
 */
@Configuration
public class CourseActivityStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<CourseActivityStatCsvRow>
            courseActivityStatReader(
                InputPathProperties inputPathProperties,
                @Value("#{jobParameters['inputFile']}")
                String inputFile) {

        Path file;

        /*
         * 신규 방식:
         * 실행할 분석 결과 파일을 직접 전달한다.
         */
        if (inputFile != null
                && !inputFile.isBlank()) {

            file = Paths.get(
                inputFile.trim()
            );

        } else {

            /*
             * 기존 방식 유지.
             */
            file = Paths.get(
                inputPathProperties.getAnalysisPath(),
                "course-activity.tsv"
            );
        }

        /*
         * 실제 파일 존재 여부 검증.
         */
        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "COURSE_ACTIVITY 분석 결과 파일을 찾을 수 없습니다: "
                + file.toAbsolutePath()
            );
        }

        return new FlatFileItemReaderBuilder<CourseActivityStatCsvRow>()
            .name("courseActivityStatReader")
            .resource(
                new FileSystemResource(file)
            )
            .encoding(
                StandardCharsets.UTF_8.name()
            )
            .linesToSkip(0)
            .lineMapper(
                (line, lineNumber) ->
                    parseLine(
                        line,
                        lineNumber
                    )
            )
            .strict(true)
            .build();
    }

    /**
     * 실제 TSV 구조:
     *
     * AAA|2013J<TAB>
     * 378<TAB>
     * 648494<TAB>
     * 1715.5926<TAB>
     * 279
     *
     * 총 5개 컬럼.
     */
    private static CourseActivityStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
            line.split(
                "\\t",
                -1
            );

        if (values.length != 5) {

            throw new IllegalArgumentException(
                "COURSE_ACTIVITY TSV 컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        /*
         * 첫 번째 컬럼:
         * CODE_MODULE|CODE_PRESENTATION
         */
        String[] key =
            values[0].split(
                "\\|",
                -1
            );

        if (key.length != 2) {

            throw new IllegalArgumentException(
                "COURSE_ACTIVITY Key 형식 오류"
                + " / line="
                + lineNumber
                + " / key="
                + values[0]
            );
        }

        CourseActivityStatCsvRow row =
            new CourseActivityStatCsvRow();

        row.setCodeModule(
            key[0]
        );

        row.setCodePresentation(
            key[1]
        );

        row.setActiveStudentCount(
            Long.valueOf(
                values[1].trim()
            )
        );

        row.setTotalClickCount(
            Long.valueOf(
                values[2].trim()
            )
        );

        row.setAvgClickPerStudent(
            new BigDecimal(
                values[3].trim()
            )
        );

        row.setActiveDayCount(
            Long.valueOf(
                values[4].trim()
            )
        );

        return row;
    }

    /**
     * COURSE_ACTIVITY_STAT Writer.
     */
    @Bean
    public JpaItemWriter<CourseActivityStat>
            courseActivityStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<CourseActivityStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    /**
     * TSV
     * → Reader
     * → Processor
     * → COURSE_ACTIVITY_STAT
     *
     * 기존 chunk 100 유지.
     */
    @Bean
    public Step courseActivityStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("courseActivityStatReader")
            FlatFileItemReader<CourseActivityStatCsvRow> reader,
            CourseActivityStatProcessor processor,
            @Qualifier("courseActivityStatWriter")
            JpaItemWriter<CourseActivityStat> writer) {

        return new StepBuilder(
            "courseActivityStatImportStep",
            jobRepository
        )
        .<CourseActivityStatCsvRow, CourseActivityStat>chunk(
            100,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    /**
     * 실제 적재 건수 검증 Step.
     */
    @Bean
    public Step courseActivityFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CourseActivityImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "courseActivityFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    /**
     * COURSE_ACTIVITY 분석 결과 적재 Job.
     */
    @Bean
    public Job courseActivityStatImportJob(
            JobRepository jobRepository,
            @Qualifier("courseActivityStatImportStep")
            Step importStep,
            @Qualifier("courseActivityFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "courseActivityStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}