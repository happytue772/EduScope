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

import com.eduscope.loader.analysis.batch.CourseWeeklyActivityStatProcessor;
import com.eduscope.loader.analysis.dto.CourseWeeklyActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.CourseWeeklyActivityStat;
import com.eduscope.loader.analysis.tasklet.CourseWeeklyActivityImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * course-weekly-activity.tsv
 * → COURSE_WEEKLY_ACTIVITY_STAT
 * → ANALYSIS_JOB RESULT_IMPORTED_YN=Y
 */
@Configuration
public class CourseWeeklyActivityStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<CourseWeeklyActivityStatCsvRow>
            courseWeeklyActivityStatReader(
                InputPathProperties inputPathProperties,
                @Value("#{jobParameters['inputFile']}")
                String inputFile) {

        Path file;

        // 명시적 inputFile이 있으면 해당 파일을 사용하고, 없으면 기존 경로를 유지한다.
        if (inputFile != null && !inputFile.isBlank()) {
            file = Paths.get(inputFile.trim());
        } else {
            file = Paths.get(
                inputPathProperties.getAnalysisPath(),
                "course-weekly-activity.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "course-weekly-activity.tsv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<CourseWeeklyActivityStatCsvRow>()
            .name("courseWeeklyActivityStatReader")
            .resource(new FileSystemResource(file))
            .encoding(StandardCharsets.UTF_8.name())
            .linesToSkip(0)
            .lineMapper(
                (line, lineNumber) ->
                    parseLine(line, lineNumber)
            )
            .strict(true)
            .build();
    }

    /**
     * 실제 TSV:
     *
     * AAA|2013J|-1<TAB>
     * 313<TAB>28523<TAB>91.1278<TAB>107
     */
    private static CourseWeeklyActivityStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
                line.split("\\t", -1);

        /*
         * KEY 1개 + 통계값 4개 = 총 5개
         */
        if (values.length != 5) {

            throw new IllegalArgumentException(
                "course-weekly-activity.tsv 컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        String[] key =
                values[0].split("\\|", -1);

        /*
         * module + presentation + week
         */
        if (key.length != 3) {

            throw new IllegalArgumentException(
                "COURSE_WEEKLY_ACTIVITY Key 형식 오류"
                + " / line="
                + lineNumber
                + " / key="
                + values[0]
            );
        }

        CourseWeeklyActivityStatCsvRow row =
                new CourseWeeklyActivityStatCsvRow();

        row.setCodeModule(
            key[0].trim()
        );

        row.setCodePresentation(
            key[1].trim()
        );

        row.setRelativeWeekNo(
            Integer.valueOf(
                key[2].trim()
            )
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

        row.setActiveMaterialCount(
            Long.valueOf(
                values[4].trim()
            )
        );

        return row;
    }

    @Bean
    public JpaItemWriter<CourseWeeklyActivityStat>
            courseWeeklyActivityStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<CourseWeeklyActivityStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    @Bean
    public Step courseWeeklyActivityStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("courseWeeklyActivityStatReader")
            FlatFileItemReader<CourseWeeklyActivityStatCsvRow> reader,
            CourseWeeklyActivityStatProcessor processor,
            @Qualifier("courseWeeklyActivityStatWriter")
            JpaItemWriter<CourseWeeklyActivityStat> writer) {

        return new StepBuilder(
            "courseWeeklyActivityStatImportStep",
            jobRepository
        )
        .<CourseWeeklyActivityStatCsvRow,
          CourseWeeklyActivityStat>chunk(
            200,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Step courseWeeklyActivityFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CourseWeeklyActivityImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "courseWeeklyActivityFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job courseWeeklyActivityStatImportJob(
            JobRepository jobRepository,
            @Qualifier("courseWeeklyActivityStatImportStep")
            Step importStep,
            @Qualifier("courseWeeklyActivityFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "courseWeeklyActivityStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}