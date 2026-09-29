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

import com.eduscope.loader.analysis.batch.RegistrationStatProcessor;
import com.eduscope.loader.analysis.dto.RegistrationStatCsvRow;
import com.eduscope.loader.analysis.entity.RegistrationStat;
import com.eduscope.loader.analysis.tasklet.RegistrationImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * registration.tsv
 * → REGISTRATION_STAT
 * → RESULT_IMPORTED_YN=Y
 */
@Configuration
public class RegistrationStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<RegistrationStatCsvRow>
            registrationStatReader(
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
                "registration.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                "registration.tsv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<RegistrationStatCsvRow>()
            .name("registrationStatReader")
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
     * AAA|2013J<TAB>383<TAB>60<TAB>60
     * <TAB>0.1567<TAB>-77.8877<TAB>102.8500
     */
    private static RegistrationStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
                line.split("\\t", -1);

        if (values.length != 7) {

            throw new IllegalArgumentException(
                "registration.tsv 컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        String[] key =
                values[0].split("\\|", -1);

        if (key.length != 2) {

            throw new IllegalArgumentException(
                "REGISTRATION Key 형식 오류"
                + " / line="
                + lineNumber
            );
        }

        RegistrationStatCsvRow row =
                new RegistrationStatCsvRow();

        row.setCodeModule(key[0].trim());
        row.setCodePresentation(key[1].trim());

        row.setRegistrationCount(
            Long.valueOf(values[1].trim())
        );

        row.setUnregistrationCount(
            Long.valueOf(values[2].trim())
        );

        row.setWithdrawnResultCount(
            Long.valueOf(values[3].trim())
        );

        row.setUnregistrationRate(
            new BigDecimal(values[4].trim())
        );

        row.setAvgRegistrationDay(
            nullableBigDecimal(values[5])
        );

        row.setAvgUnregistrationDay(
            nullableBigDecimal(values[6])
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

    @Bean
    public JpaItemWriter<RegistrationStat>
            registrationStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<RegistrationStat>()
            .entityManagerFactory(entityManagerFactory)
            .usePersist(true)
            .build();
    }

    @Bean
    public Step registrationStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("registrationStatReader")
            FlatFileItemReader<RegistrationStatCsvRow> reader,
            RegistrationStatProcessor processor,
            @Qualifier("registrationStatWriter")
            JpaItemWriter<RegistrationStat> writer) {

        return new StepBuilder(
            "registrationStatImportStep",
            jobRepository
        )
        .<RegistrationStatCsvRow, RegistrationStat>chunk(
            100,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Step registrationFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            RegistrationImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "registrationFinalizeStep",
            jobRepository
        )
        .tasklet(tasklet, transactionManager)
        .build();
    }

    @Bean
    public Job registrationStatImportJob(
            JobRepository jobRepository,
            @Qualifier("registrationStatImportStep")
            Step importStep,
            @Qualifier("registrationFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "registrationStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}