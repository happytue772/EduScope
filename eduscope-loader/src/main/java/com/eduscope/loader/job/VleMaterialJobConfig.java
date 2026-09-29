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
import com.eduscope.loader.reference.batch.VleMaterialProcessor;
import com.eduscope.loader.reference.dto.VleMaterialCsvRow;
import com.eduscope.loader.reference.entity.VleMaterial;

/**
 * vle.csv → VLE_MATERIAL 적재 Job.
 */
@Configuration
public class VleMaterialJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<VleMaterialCsvRow>
            vleMaterialReader(
                InputPathProperties inputPathProperties) {

        Path file = Paths.get(
            inputPathProperties.getRawPath(),
            "vle.csv"
        );

        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "vle.csv를 찾을 수 없습니다: "
                + file
            );
        }

        return new FlatFileItemReaderBuilder<VleMaterialCsvRow>()
            .name("vleMaterialReader")
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
                "sourceSiteId",
                "codeModule",
                "codePresentation",
                "activityType",
                "weekFrom",
                "weekTo"
            )
            .fieldSetMapper(fieldSet -> {

                VleMaterialCsvRow row =
                        new VleMaterialCsvRow();

                row.setSourceSiteId(
                    fieldSet.readLong(
                        "sourceSiteId"
                    )
                );

                row.setCodeModule(
                    fieldSet.readString(
                        "codeModule"
                    )
                );

                row.setCodePresentation(
                    fieldSet.readString(
                        "codePresentation"
                    )
                );

                row.setActivityType(
                    fieldSet.readString(
                        "activityType"
                    )
                );

                row.setWeekFrom(
                    nullableInteger(
                        fieldSet.readString(
                            "weekFrom"
                        )
                    )
                );

                row.setWeekTo(
                    nullableInteger(
                        fieldSet.readString(
                            "weekTo"
                        )
                    )
                );

                return row;
            })
            .strict(true)
            .build();
    }

    @Bean
    public JpaItemWriter<VleMaterial>
            vleMaterialWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<VleMaterial>()
            .entityManagerFactory(
                entityManagerFactory
            )

            // 신규 VLE 기준정보 INSERT
            .usePersist(true)

            .build();
    }

    @Bean
    public Step vleMaterialImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("vleMaterialReader")
            FlatFileItemReader<VleMaterialCsvRow> reader,
            VleMaterialProcessor processor,
            @Qualifier("vleMaterialWriter")
            JpaItemWriter<VleMaterial> writer) {

        return new StepBuilder(
            "vleMaterialImportStep",
            jobRepository
        )
        .<VleMaterialCsvRow, VleMaterial>chunk(
            500,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    public Job vleMaterialImportJob(
            JobRepository jobRepository,
            @Qualifier("vleMaterialImportStep")
            Step step) {

        return new JobBuilder(
            "vleMaterialImportJob",
            jobRepository
        )
        .start(step)
        .build();
    }

    /**
     * WEEK_FROM / WEEK_TO의 빈 값을 null로 보존한다.
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