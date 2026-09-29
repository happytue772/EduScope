package com.eduscope.loader.analysis.config;

import java.math.BigDecimal;
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

import com.eduscope.loader.analysis.batch.CourseResultStatProcessor;
import com.eduscope.loader.analysis.dto.CourseResultStatCsvRow;
import com.eduscope.loader.analysis.entity.CourseResultStat;
import com.eduscope.loader.analysis.tasklet.CourseResultImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

@Configuration
public class CourseResultStatJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<CourseResultStatCsvRow>
            courseResultStatReader(
                InputPathProperties input,
                @Value("#{jobParameters['inputFile']}")
                String inputFile) {

        Path file;

        // 명시적 inputFile이 있으면 해당 파일을 사용하고, 없으면 기존 경로를 유지한다.
        if (inputFile != null && !inputFile.isBlank()) {
            file = Paths.get(inputFile.trim());
        } else {
            file = Paths.get(
                input.getAnalysisPath(),
                "course-result.tsv"
            );
        }

        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                "course-result.tsv 없음: " + file
            );
        }

        return new FlatFileItemReaderBuilder<CourseResultStatCsvRow>()
            .name("courseResultStatReader")
            .resource(new FileSystemResource(file))
            .lineMapper((line, no) -> parse(line, no))
            .build();
    }

    private static CourseResultStatCsvRow parse(
            String line,
            int lineNumber) {

        String[] v = line.split("\\t", -1);

        if (v.length != 10) {
            throw new IllegalArgumentException(
                "course-result.tsv 컬럼 오류 line="
                + lineNumber
            );
        }

        String[] key = v[0].split("\\|", -1);

        if (key.length != 2) {
            throw new IllegalArgumentException(
                "COURSE_RESULT key 오류"
            );
        }

        CourseResultStatCsvRow row =
                new CourseResultStatCsvRow();

        row.setCodeModule(key[0].trim());
        row.setCodePresentation(key[1].trim());

        row.setStudentCount(Long.valueOf(v[1].trim()));
        row.setPassCount(Long.valueOf(v[2].trim()));
        row.setFailCount(Long.valueOf(v[3].trim()));
        row.setWithdrawnCount(Long.valueOf(v[4].trim()));
        row.setDistinctionCount(Long.valueOf(v[5].trim()));

        row.setPassRate(new BigDecimal(v[6].trim()));
        row.setFailRate(new BigDecimal(v[7].trim()));
        row.setWithdrawnRate(new BigDecimal(v[8].trim()));
        row.setDistinctionRate(new BigDecimal(v[9].trim()));

        return row;
    }

    @Bean
    JpaItemWriter<CourseResultStat> courseResultStatWriter(
            EntityManagerFactory emf) {

        return new JpaItemWriterBuilder<CourseResultStat>()
            .entityManagerFactory(emf)
            .usePersist(true)
            .build();
    }

    @Bean
    Step courseResultImportStep(
            JobRepository repository,
            PlatformTransactionManager tx,
            @Qualifier("courseResultStatReader")
            FlatFileItemReader<CourseResultStatCsvRow> reader,
            CourseResultStatProcessor processor,
            @Qualifier("courseResultStatWriter")
            JpaItemWriter<CourseResultStat> writer) {

        return new StepBuilder(
            "courseResultImportStep",
            repository
        )
        .<CourseResultStatCsvRow, CourseResultStat>chunk(100, tx)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    @Bean
    Step courseResultFinalizeStep(
            JobRepository repository,
            PlatformTransactionManager tx,
            CourseResultImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "courseResultFinalizeStep",
            repository
        )
        .tasklet(tasklet, tx)
        .build();
    }

    @Bean
    Job courseResultStatImportJob(
            JobRepository repository,
            @Qualifier("courseResultImportStep") Step importStep,
            @Qualifier("courseResultFinalizeStep") Step finalizeStep) {

        return new JobBuilder(
            "courseResultStatImportJob",
            repository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}