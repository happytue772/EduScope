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

import com.eduscope.loader.analysis.batch.AssessmentStatProcessor;
import com.eduscope.loader.analysis.dto.AssessmentStatCsvRow;
import com.eduscope.loader.analysis.entity.AssessmentStat;
import com.eduscope.loader.analysis.tasklet.AssessmentImportFinalizeTasklet;
import com.eduscope.loader.dataset.config.InputPathProperties;

/**
 * ASSESSMENT MapReduce 결과 TSV
 * → ASSESSMENT_STAT
 * → ANALYSIS_JOB RESULT_IMPORTED_YN = Y
 *
 * inputFile Job Parameter가 전달되면
 * 지정된 파일을 사용한다.
 *
 * inputFile이 없으면 기존 방식인
 * analysisPath/assessment.tsv를 사용한다.
 */
@Configuration
public class AssessmentStatJobConfig {

    /**
     * ASSESSMENT 분석 결과 TSV Reader.
     *
     * 예:
     *
     * inputFile=
     * C:\EduScope\loader-input\analysis\assessment-job-12.tsv
     *
     * inputFile이 없으면:
     *
     * C:\EduScope\loader-input\analysis\assessment.tsv
     *
     * 를 사용한다.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<AssessmentStatCsvRow>
            assessmentStatReader(
                InputPathProperties inputPathProperties,
                @Value("#{jobParameters['inputFile']}")
                String inputFile) {

        Path file;

        /*
         * 신규 방식:
         * 실행할 Job의 결과 파일을
         * Job Parameter로 직접 전달한다.
         */
        if (
            inputFile != null
            && !inputFile.isBlank()
        ) {

            file = Paths.get(
                inputFile.trim()
            );

        } else {

            /*
             * 기존 방식 유지.
             *
             * inputFile이 전달되지 않은 기존 실행도
             * 그대로 동작할 수 있게 한다.
             */
            file = Paths.get(
                inputPathProperties.getAnalysisPath(),
                "assessment.tsv"
            );
        }

        /*
         * 실제 파일이 존재하는지 실행 전에 검증한다.
         */
        if (!Files.isRegularFile(file)) {

            throw new IllegalStateException(
                "ASSESSMENT 분석 결과 파일을 찾을 수 없습니다: "
                + file.toAbsolutePath()
            );
        }

        return new FlatFileItemReaderBuilder<AssessmentStatCsvRow>()
            .name("assessmentStatReader")
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
     * 실제 MapReduce TSV 형식:
     *
     * 1752<TAB>
     * 359<TAB>
     * 70.31<TAB>
     * 6<TAB>
     * 0.0168<TAB>
     * 0<TAB>
     * 19.3565
     *
     * 총 7개 컬럼을 사용한다.
     */
    private static AssessmentStatCsvRow parseLine(
            String line,
            int lineNumber) {

        String[] values =
            line.split(
                "\\t",
                -1
            );

        /*
         * ASSESSMENT 분석 결과는
         * 반드시 7개 컬럼이어야 한다.
         */
        if (values.length != 7) {

            throw new IllegalArgumentException(
                "ASSESSMENT 분석 결과 TSV 컬럼 수 오류"
                + " / line="
                + lineNumber
                + " / columns="
                + values.length
            );
        }

        AssessmentStatCsvRow row =
            new AssessmentStatCsvRow();

        /*
         * OULAD 원본 Assessment ID.
         */
        row.setSourceAssessmentId(
            Long.valueOf(
                values[0].trim()
            )
        );

        /*
         * 평가 제출 건수.
         */
        row.setSubmissionCount(
            Long.valueOf(
                values[1].trim()
            )
        );

        /*
         * 평균 평가 점수.
         */
        row.setAvgScore(
            new BigDecimal(
                values[2].trim()
            )
        );

        /*
         * score < 40 기준 Fail 건수.
         */
        row.setFailCount(
            Long.valueOf(
                values[3].trim()
            )
        );

        /*
         * Fail 비율.
         */
        row.setFailRate(
            new BigDecimal(
                values[4].trim()
            )
        );

        /*
         * Banked 평가 건수.
         */
        row.setBankedCount(
            Long.valueOf(
                values[5].trim()
            )
        );

        /*
         * 평균 제출 상대 일수.
         *
         * 비어 있을 수 있으므로
         * nullableBigDecimal()을 사용한다.
         */
        row.setAvgSubmissionDay(
            nullableBigDecimal(
                values[6]
            )
        );

        return row;
    }

    /**
     * 빈 문자열은 null로,
     * 값이 있으면 BigDecimal로 변환한다.
     */
    private static BigDecimal nullableBigDecimal(
            String value) {

        if (
            value == null
            || value.isBlank()
        ) {

            return null;
        }

        return new BigDecimal(
            value.trim()
        );
    }

    /**
     * ASSESSMENT_STAT JPA Writer.
     *
     * Processor가 변환한 AssessmentStat을
     * Oracle ASSESSMENT_STAT에 저장한다.
     */
    @Bean
    public JpaItemWriter<AssessmentStat>
            assessmentStatWriter(
                EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<AssessmentStat>()
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
     * → ASSESSMENT_STAT
     *
     * 100건 단위 Chunk 처리.
     */
    @Bean
    public Step assessmentStatImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("assessmentStatReader")
            FlatFileItemReader<AssessmentStatCsvRow> reader,
            AssessmentStatProcessor processor,
            @Qualifier("assessmentStatWriter")
            JpaItemWriter<AssessmentStat> writer) {

        return new StepBuilder(
            "assessmentStatImportStep",
            jobRepository
        )
        .<AssessmentStatCsvRow, AssessmentStat>chunk(
            100,
            transactionManager
        )
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
    }

    /**
     * ASSESSMENT_STAT 적재 후
     * 실제 DB 적재 건수를 검증한다.
     *
     * 검증 성공 시:
     *
     * ANALYSIS_JOB.RESULT_IMPORTED_YN = Y
     *
     * 로 변경한다.
     */
    @Bean
    public Step assessmentFinalizeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AssessmentImportFinalizeTasklet tasklet) {

        return new StepBuilder(
            "assessmentFinalizeStep",
            jobRepository
        )
        .tasklet(
            tasklet,
            transactionManager
        )
        .build();
    }

    /**
     * ASSESSMENT 분석 결과 적재 Job.
     *
     * 1. assessmentStatImportStep
     * 2. assessmentFinalizeStep
     *
     * 순서로 실행한다.
     */
    @Bean
    public Job assessmentStatImportJob(
            JobRepository jobRepository,
            @Qualifier("assessmentStatImportStep")
            Step importStep,
            @Qualifier("assessmentFinalizeStep")
            Step finalizeStep) {

        return new JobBuilder(
            "assessmentStatImportJob",
            jobRepository
        )
        .start(importStep)
        .next(finalizeStep)
        .build();
    }
}