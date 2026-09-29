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

import com.eduscope.loader.analysis.batch.StudentLearningSummaryStatProcessor;
import com.eduscope.loader.analysis.dto.StudentLearningSummaryStatCsvRow;
import com.eduscope.loader.analysis.entity.StudentLearningSummaryStat;
import com.eduscope.loader.analysis.tasklet.StudentLearningSummaryImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * student-learning-summary.tsv
 * → STUDENT_LEARNING_SUMMARY_STAT
 */
@Configuration
public class StudentLearningSummaryStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentLearningSummaryStatCsvRow>
            studentLearningSummaryStatReader(
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
                "student-learning-summary.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "student-learning-summary.tsv 없음: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentLearningSummaryStatCsvRow>()
            .name("studentLearningSummaryStatReader")
            .resource(
                new FileSystemResource(file)
            )
            .encoding(
                StandardCharsets.UTF_8.name()
            )
            .linesToSkip(0)
            .lineMapper(
                (line, lineNumber) ->
                    parse(line, lineNumber)
            )
            .strict(true)
            .build();
    }

    /**
     * 실제:
     * AAA|2013J|100893
     * 744 52 47 5 68.40 0 -9 250
     */
    private static StudentLearningSummaryStatCsvRow parse(
            String line,
            int lineNumber) {

        String[] values =
                line.split("\\t", -1);

        if (values.length != 9) {

            throw new IllegalArgumentException(
                "student-learning-summary.tsv "
                + "컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        String[] key =
                values[0].split("\\|", -1);

        if (key.length != 3) {

            throw new IllegalArgumentException(
                "STUDENT_LEARNING_SUMMARY "
                + "Key 형식 오류"
                + " / line="
                + lineNumber
            );
        }

        StudentLearningSummaryStatCsvRow row =
                new StudentLearningSummaryStatCsvRow();

        row.setCodeModule(
            key[0].trim()
        );

        row.setCodePresentation(
            key[1].trim()
        );

        row.setSourceStudentId(
            Long.valueOf(
                key[2].trim()
            )
        );

        row.setTotalClickCount(
            Long.valueOf(
                values[1].trim()
            )
        );

        row.setActiveDayCount(
            Long.valueOf(
                values[2].trim()
            )
        );

        row.setUsedMaterialCount(
            Long.valueOf(
                values[3].trim()
            )
        );

        row.setSubmittedAssessmentCount(
            Long.valueOf(
                values[4].trim()
            )
        );

        row.setAvgAssessmentScore(
            nullableBigDecimal(
                values[5]
            )
        );

        row.setFailedAssessmentCount(
            Long.valueOf(
                values[6].trim()
            )
        );

        row.setFirstActivityDay(
            nullableInteger(
                values[7]
            )
        );

        row.setLastActivityDay(
            nullableInteger(
                values[8]
            )
        );

        return row;
    }

    private static BigDecimal nullableBigDecimal(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return new BigDecimal(
            value.trim()
        );
    }

    private static Integer nullableInteger(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Integer.valueOf(
            value.trim()
        );
    }

    @Bean
    public JpaItemWriter<StudentLearningSummaryStat>
            studentLearningSummaryStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<StudentLearningSummaryStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    @Bean
    public Step studentLearningSummaryImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,

            @Qualifier("studentLearningSummaryStatReader")
            FlatFileItemReader<StudentLearningSummaryStatCsvRow> reader,

            StudentLearningSummaryStatProcessor processor,

            @Qualifier("studentLearningSummaryStatWriter")
            JpaItemWriter<StudentLearningSummaryStat> writer) {

        return new StepBuilder(
            "studentLearningSummaryImportStep",
            jobRepository
        )
        .<StudentLearningSummaryStatCsvRow,
          StudentLearningSummaryStat>chunk(
            500,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Step studentLearningSummaryFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            StudentLearningSummaryImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "studentLearningSummaryFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job studentLearningSummaryStatImportJob(
            JobRepository jobRepository,

            @Qualifier("studentLearningSummaryImportStep")
            Step importStep,

            @Qualifier("studentLearningSummaryFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "studentLearningSummaryStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}