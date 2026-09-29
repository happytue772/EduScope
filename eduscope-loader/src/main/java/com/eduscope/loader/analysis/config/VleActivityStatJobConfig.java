package com.eduscope.loader.analysis.config;

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

import com.eduscope.loader.analysis.batch.VleActivityStatProcessor;
import com.eduscope.loader.analysis.dto.VleActivityStatCsvRow;
import com.eduscope.loader.analysis.entity.VleActivityStat;
import com.eduscope.loader.analysis.tasklet.VleActivityImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * vle-activity.tsv
 * → VLE_ACTIVITY_STAT
 * → ANALYSIS_JOB RESULT_IMPORTED_YN=Y
 */
@Configuration
public class VleActivityStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<VleActivityStatCsvRow>
            vleActivityStatReader(
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
                "vle-activity.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "vle-activity.tsv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<VleActivityStatCsvRow>()
            .name("vleActivityStatReader")
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
     * AAA|2013J|546614<TAB>
     * 140345<TAB>378<TAB>279
     */
    private static VleActivityStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
                line.split("\\t", -1);

        // KEY 1 + 통계값 3 = 총 4개
        if (values.length != 4) {

            throw new IllegalArgumentException(
                "vle-activity.tsv 컬럼 수 오류"
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
                "VLE_ACTIVITY Key 형식 오류"
                + " / line="
                + lineNumber
                + " / key="
                + values[0]
            );
        }

        VleActivityStatCsvRow row =
                new VleActivityStatCsvRow();

        row.setCodeModule(
            key[0].trim()
        );

        row.setCodePresentation(
            key[1].trim()
        );

        row.setSourceSiteId(
            Long.valueOf(
                key[2].trim()
            )
        );

        row.setTotalClickCount(
            Long.valueOf(
                values[1].trim()
            )
        );

        row.setActiveStudentCount(
            Long.valueOf(
                values[2].trim()
            )
        );

        row.setActiveDayCount(
            Long.valueOf(
                values[3].trim()
            )
        );

        return row;
    }

    @Bean
    public JpaItemWriter<VleActivityStat>
            vleActivityStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<VleActivityStat>()
            .entityManagerFactory(
                entityManagerFactory
            )
            .usePersist(true)
            .build();
    }

    @Bean
    public Step vleActivityStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("vleActivityStatReader")
            FlatFileItemReader<VleActivityStatCsvRow> reader,
            VleActivityStatProcessor processor,
            @Qualifier("vleActivityStatWriter")
            JpaItemWriter<VleActivityStat> writer) {

        return new StepBuilder(
            "vleActivityStatImportStep",
            jobRepository
        )
        .<VleActivityStatCsvRow, VleActivityStat>chunk(
            500,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Step vleActivityFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            VleActivityImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "vleActivityFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    @Bean
    public Job vleActivityStatImportJob(
            JobRepository jobRepository,
            @Qualifier("vleActivityStatImportStep")
            Step importStep,
            @Qualifier("vleActivityFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "vleActivityStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}