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

import com.eduscope.loader.analysis.batch.ActivityResultStatProcessor;
import com.eduscope.loader.analysis.dto.ActivityResultStatCsvRow;
import com.eduscope.loader.analysis.entity.ActivityResultStat;
import com.eduscope.loader.analysis.tasklet.ActivityResultImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * activity-result.tsv
 *
 * → Reader
 * → Processor
 * → ACTIVITY_RESULT_STAT
 * → 건수 검증
 * → RESULT_IMPORTED_YN = Y
 */
@Configuration
public class ActivityResultStatJobConfig {

    /**
     * 실제 activity-result.tsv를 읽는다.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<ActivityResultStatCsvRow>
            activityResultStatReader(
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
                "activity-result.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "activity-result.tsv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<ActivityResultStatCsvRow>()
            .name("activityResultStatReader")
            .resource(
                new FileSystemResource(file)
            )
            .encoding(
                StandardCharsets.UTF_8.name()
            )

            // MapReduce 결과에 Header 없음
            .linesToSkip(0)

            /*
             * 아래의 parse() 메서드를 호출한다.
             * 별도 Parser 클래스는 만들지 않는다.
             */
            .lineMapper(
                (line, lineNumber) ->
                    parse(line, lineNumber)
            )

            .strict(true)
            .build();
    }

    /**
     * activity-result.tsv 한 줄 파싱.
     *
     * 실제 예:
     *
     * AAA|2013J|Distinction
     * <TAB>20
     * <TAB>82419
     * <TAB>4120.9500
     * <TAB>173.6000
     */
    private static ActivityResultStatCsvRow parse(
            String line,
            int lineNumber) {

        /*
         * 전체 TSV 컬럼:
         *
         * [0] key
         * [1] student count
         * [2] total click
         * [3] avg click
         * [4] avg active day
         */
        String[] values =
                line.split("\\t", -1);

        if (values.length != 5) {

            throw new IllegalArgumentException(
                "activity-result.tsv 컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        /*
         * key:
         *
         * AAA|2013J|Distinction
         */
        String[] key =
                values[0].split("\\|", -1);

        if (key.length != 3) {

            throw new IllegalArgumentException(
                "ACTIVITY_RESULT Key 형식 오류"
                + " / line="
                + lineNumber
                + " / key="
                + values[0]
            );
        }

        ActivityResultStatCsvRow row =
                new ActivityResultStatCsvRow();

        row.setCodeModule(
            key[0].trim()
        );

        row.setCodePresentation(
            key[1].trim()
        );

        /*
         * Pass / Fail / Withdrawn / Distinction
         */
        row.setFinalResult(
            key[2].trim()
        );

        row.setStudentCount(
            Long.valueOf(
                values[1].trim()
            )
        );

        row.setTotalClickCount(
            Long.valueOf(
                values[2].trim()
            )
        );

        row.setAvgClickCount(
            new BigDecimal(
                values[3].trim()
            )
        );

        row.setAvgActiveDayCount(
            new BigDecimal(
                values[4].trim()
            )
        );

        return row;
    }

    /**
     * Processor가 만든 Entity를 Oracle에 INSERT.
     */
    @Bean
    public JpaItemWriter<ActivityResultStat>
            activityResultStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<ActivityResultStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    /**
     * TSV → Processor → Oracle 적재 Step.
     */
    @Bean
    public Step activityResultStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,

            @Qualifier("activityResultStatReader")
            FlatFileItemReader<ActivityResultStatCsvRow> reader,

            ActivityResultStatProcessor processor,

            @Qualifier("activityResultStatWriter")
            JpaItemWriter<ActivityResultStat> writer) {

        return new StepBuilder(
            "activityResultStatImportStep",
            jobRepository
        )
        .<ActivityResultStatCsvRow, ActivityResultStat>chunk(
            100,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    /**
     * Oracle 실제 적재 건수를 검증하는 Step.
     */
    @Bean
    public Step activityResultFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ActivityResultImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "activityResultFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    /**
     * 최종 Job.
     *
     * Import
     * ↓
     * Finalize 검증
     */
    @Bean
    public Job activityResultStatImportJob(
            JobRepository jobRepository,

            @Qualifier("activityResultStatImportStep")
            Step importStep,

            @Qualifier("activityResultFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "activityResultStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}