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

import com.eduscope.loader.analysis.batch.StudentActivityStatProcessor;
import com.eduscope.loader.analysis.dto.StudentActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.StudentActivityStat;
import com.eduscope.loader.analysis.tasklet.StudentActivityImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * student-activity.tsv
 * → STUDENT_ACTIVITY_STAT
 * → ANALYSIS_JOB RESULT_IMPORTED_YN 갱신.
 */
@Configuration
public class StudentActivityStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<StudentActivityStatCsvRow>
            studentActivityStatReader(
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
                "student-activity.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                "student-activity.tsv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<StudentActivityStatCsvRow>()
            .name("studentActivityStatReader")
            .resource(new FileSystemResource(file))
            .encoding(StandardCharsets.UTF_8.name())

            // MapReduce 결과에는 Header 없음
            .linesToSkip(0)

            .lineMapper(
                (line, lineNumber) ->
                    parseLine(line, lineNumber)
            )
            .strict(true)
            .build();
    }

    /**
     * 실제 MapReduce 출력:
     *
     * AAA|2013J|100893<TAB>
     * 744<TAB>52<TAB>47<TAB>
     * 14.3077<TAB>-9<TAB>250
     */
    private static StudentActivityStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
            line.split("\\t", -1);

        if (values.length != 7) {

            throw new IllegalArgumentException(
                "student-activity.tsv 컬럼 수 오류"
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
                "학생 활동 Key 형식 오류"
                + " / line="
                + lineNumber
                + " / key="
                + values[0]
            );
        }

        StudentActivityStatCsvRow row =
            new StudentActivityStatCsvRow();

        row.setCodeModule(key[0]);
        row.setCodePresentation(key[1]);

        row.setSourceStudentId(
            Long.valueOf(key[2])
        );

        row.setTotalClickCount(
            Long.valueOf(values[1])
        );

        row.setActiveDayCount(
            Long.valueOf(values[2])
        );

        row.setUsedMaterialCount(
            Long.valueOf(values[3])
        );

        row.setAvgDailyClickCount(
            new BigDecimal(values[4])
        );

        row.setFirstActivityDay(
            nullableInteger(values[5])
        );

        row.setLastActivityDay(
            nullableInteger(values[6])
        );

        return row;
    }

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

    @Bean
    public JpaItemWriter<StudentActivityStat>
            studentActivityStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<StudentActivityStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    @Bean
    public Step studentActivityStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("studentActivityStatReader")
            FlatFileItemReader<StudentActivityStatCsvRow> reader,
            StudentActivityStatProcessor processor,
            @Qualifier("studentActivityStatWriter")
            JpaItemWriter<StudentActivityStat> writer) {

        return new StepBuilder(
            "studentActivityStatImportStep",
            jobRepository
        )
        .<StudentActivityStatCsvRow, StudentActivityStat>chunk(
            500,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Step studentActivityFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            StudentActivityImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "studentActivityFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job studentActivityStatImportJob(
            JobRepository jobRepository,
            @Qualifier("studentActivityStatImportStep")
            Step importStep,
            @Qualifier("studentActivityFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "studentActivityStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}