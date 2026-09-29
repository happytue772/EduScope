package com.eduscope.loader.analysis.tasklet;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.DataQualityStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.DataQualityStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.entity.DatasetFile;
import com.eduscope.loader.dataset.repository.DatasetFileRepository;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * DATA_QUALITY 결과 파일을 한 줄씩 읽어
 * FILE_TYPE + QUALITY_TYPE 단위로 집계 후 Oracle에 적재한다.
 *
 * 기존 Duplicate 결과 + Row Quality 결과를 모두 처리한다.
 */
@Component
@StepScope
public class DataQualityImportTasklet
        implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetFileRepository datasetFileRepository;

    private final DatasetProperties datasetProperties;
    private final InputPathProperties inputPathProperties;

    private final AnalysisJobRepository analysisJobRepository;
    private final DataQualityStatRepository statRepository;

    // DATA_QUALITY는 결과 파일이 2개라 별도 Parameter로 받는다.
    private final Long requestedJobId;
    private final String requestedDuplicateFile;
    private final String requestedRowFile;

    public DataQualityImportTasklet(
            DatasetRepository datasetRepository,
            DatasetFileRepository datasetFileRepository,
            DatasetProperties datasetProperties,
            InputPathProperties inputPathProperties,
            AnalysisJobRepository analysisJobRepository,
            DataQualityStatRepository statRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId,
            @Value("#{jobParameters['duplicateFile']}")
            String requestedDuplicateFile,
            @Value("#{jobParameters['rowFile']}")
            String requestedRowFile
    ) {

        this.datasetRepository = datasetRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.datasetProperties = datasetProperties;
        this.inputPathProperties = inputPathProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.requestedJobId = requestedJobId;
        this.requestedDuplicateFile = requestedDuplicateFile;
        this.requestedRowFile = requestedRowFile;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext)
            throws Exception {

        Dataset dataset;
        AnalysisJob job;

        if (requestedJobId != null) {

            job = analysisJobRepository
                .findById(requestedJobId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ANALYSIS_JOB을 찾을 수 없습니다."
                        + " / jobId=" + requestedJobId
                    )
                );

            if (!"DATA_QUALITY".equals(
                    job.getAnalysisType())) {

                throw new IllegalStateException(
                    "DATA_QUALITY 분석 Job이 아닙니다."
                    + " / jobId=" + job.getJobId()
                    + " / analysisType="
                    + job.getAnalysisType()
                );
            }

            if (!"SUCCESS".equals(
                    job.getStatus())) {

                throw new IllegalStateException(
                    "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                    + " / jobId=" + job.getJobId()
                    + " / status=" + job.getStatus()
                );
            }

            dataset = job.getDataset();

        } else {

            // 기존 최신 SUCCESS DATA_QUALITY Job 방식 유지.
            dataset = datasetRepository
                .findBySourceNameAndDatasetVersion(
                    datasetProperties.getSourceName(),
                    datasetProperties.getDatasetVersion()
                )
                .orElseThrow(() ->
                    new IllegalStateException("DATASET 없음")
                );

            job = analysisJobRepository
                .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                    dataset,
                    "DATA_QUALITY",
                    "SUCCESS"
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "DATA_QUALITY ANALYSIS_JOB 없음"
                    )
                );
        }

        Path duplicateFile =
                resolveInputFile(
                    requestedDuplicateFile,
                    "data-quality-duplicate.tsv"
                );

        Path rowFile =
                resolveInputFile(
                    requestedRowFile,
                    "data-quality-row.tsv"
                );

        validateInputFile(
                duplicateFile,
                "data-quality-duplicate.tsv"
        );

        validateInputFile(
                rowFile,
                "data-quality-row.tsv"
        );

        /*
         * FILE_TYPE|QUALITY_TYPE 단위 집계.
         *
         * 원본이 대용량이어도 Map에는 품질 집계 그룹만 유지한다.
         */
        Map<String, Aggregate> aggregates =
                new LinkedHashMap<>();

        long duplicateLineCount =
                readQualityFile(
                    duplicateFile,
                    aggregates,
                    "duplicate"
                );

        long rowLineCount =
                readQualityFile(
                    rowFile,
                    aggregates,
                    "row"
                );

        /*
         * Spring Boot의 실제 Hadoop Executor는 DATA_QUALITY의
         * 두 Output(row + duplicate) part-* 라인 수를 합산해
         * OUTPUT_RECORD_COUNT에 기록한다.
         */
        long totalOutputLineCount =
                duplicateLineCount
                + rowLineCount;

        if (totalOutputLineCount
                != job.getOutputRecordCount()) {

            throw new IllegalStateException(
                "DATA_QUALITY 전체 Output 행 수 불일치"
                + " / expected="
                + job.getOutputRecordCount()
                + " / actual="
                + totalOutputLineCount
                + " / duplicateRows="
                + duplicateLineCount
                + " / rowQualityRows="
                + rowLineCount
            );
        }

        /*
         * 과거 Bootstrap Job은 DUPLICATE_RECORD_COUNT에
         * duplicate TSV 라인 수를 이미 기록했다.
         * 웹에서 새로 실행한 Job은 현재 이 값이 0으로 남으므로,
         * 실제 duplicate TSV 라인 수로 보정한다.
         */
        Long storedDuplicateCount =
                job.getDuplicateRecordCount();

        if (storedDuplicateCount != null
                && storedDuplicateCount > 0L
                && storedDuplicateCount.longValue()
                    != duplicateLineCount) {

            throw new IllegalStateException(
                "DATA_QUALITY DUPLICATE 행 수 불일치"
                + " / expected="
                + storedDuplicateCount
                + " / actual="
                + duplicateLineCount
            );
        }

        job.setDuplicateRecordCount(
                duplicateLineCount
        );

        /*
         * Row Quality 결과는 Header / Parse / Missing /
         * Range / FK 품질 결과다.
         * 실제 파일을 읽어 Oracle 품질 통계에 함께 적재한다.
         */
        for (Aggregate aggregate
                : aggregates.values()) {

            DatasetFile datasetFile =
                    datasetFileRepository
                        .findByDatasetAndFileType(
                            dataset,
                            aggregate.fileType
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "DATASET_FILE 없음: "
                                + aggregate.fileType
                            )
                        );

            Long datasetFileId =
                    datasetFile.getDatasetFileId();

            /*
             * 재실행 시 복합 PK 중복 방지.
             */
            if (statRepository
                    .existsByJobIdAndDatasetFileIdAndQualityType(
                        job.getJobId(),
                        datasetFileId,
                        aggregate.qualityType
                    )) {

                continue;
            }

            DataQualityStat stat =
                    new DataQualityStat();

            stat.setJobId(
                job.getJobId()
            );

            stat.setDatasetFileId(
                datasetFileId
            );

            stat.setQualityType(
                aggregate.qualityType
            );

            stat.setRecordCount(
                aggregate.recordCount
            );

            stat.setSampleMessage(
                aggregate.sampleMessage
            );

            stat.setCreatedAt(
                LocalDateTime.now()
            );

            statRepository.save(stat);
        }

        /*
         * Duplicate + Row Quality 두 파일을 모두 검증하고
         * 집계 저장까지 성공한 경우에만 Y로 마감한다.
         */
        job.setResultImportedYn("Y");

        analysisJobRepository.save(job);

        Long storedRecordCount =
                statRepository
                    .sumRecordCountByJobId(
                        job.getJobId()
                    );

        System.out.println(
            "[DATA_QUALITY] 적재 완료"
            + " / duplicateRows="
            + duplicateLineCount
            + " / rowQualityRows="
            + rowLineCount
            + " / aggregateRows="
            + statRepository.countByJobId(
                job.getJobId()
            )
            + " / summedRecordCount="
            + storedRecordCount
            + " / RESULT_IMPORTED_YN=Y"
        );

        return RepeatStatus.FINISHED;
    }

    /**
     * Job Parameter가 있으면 해당 파일을 사용하고,
     * 없으면 기존 기본 파일명으로 fallback한다.
     */
    private Path resolveInputFile(
            String requestedFile,
            String fallbackFileName) {

        if (requestedFile != null
                && !requestedFile.isBlank()) {

            return Paths.get(
                    requestedFile.trim());
        }

        return Paths.get(
            inputPathProperties.getAnalysisPath(),
            fallbackFileName
        );
    }

    private void validateInputFile(
            Path path,
            String logicalName) {

        if (!Files.isRegularFile(path)) {

            throw new IllegalStateException(
                logicalName
                + " 없음: "
                + path
            );
        }
    }

    /**
     * MapReduce 결과 한 파일을 읽어
     * FILE_TYPE + QUALITY_TYPE 단위로 누적한다.
     *
     * 형식:
     * FILE_TYPE|QUALITY_TYPE<TAB>COUNT<TAB>SAMPLE
     */
    private long readQualityFile(
            Path path,
            Map<String, Aggregate> aggregates,
            String sourceName)
            throws Exception {

        long sourceLineCount = 0L;

        try (BufferedReader reader =
                Files.newBufferedReader(
                    path,
                    StandardCharsets.UTF_8
                )) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                sourceLineCount++;

                /*
                 * 3번째 필드 내부 문자열은 그대로 보존한다.
                 */
                String[] values =
                        line.split("\\t", 3);

                if (values.length != 3) {

                    throw new IllegalArgumentException(
                        "DATA_QUALITY TSV 컬럼 오류"
                        + " / source=" + sourceName
                        + " / line=" + sourceLineCount
                    );
                }

                String[] key =
                        values[0].split("\\|", -1);

                if (key.length != 2) {

                    throw new IllegalArgumentException(
                        "DATA_QUALITY Key 오류"
                        + " / source=" + sourceName
                        + " / line=" + sourceLineCount
                    );
                }

                String fileType =
                        key[0].trim();

                String qualityType =
                        key[1].trim();

                long recordCount =
                        Long.parseLong(
                            values[1].trim()
                        );

                String sampleMessage =
                        values[2].trim();

                if (fileType.isBlank()
                        || qualityType.isBlank()) {

                    throw new IllegalArgumentException(
                        "DATA_QUALITY Key 값이 비어 있습니다."
                        + " / source=" + sourceName
                        + " / line=" + sourceLineCount
                    );
                }

                if (recordCount < 0L) {

                    throw new IllegalArgumentException(
                        "DATA_QUALITY RECORD_COUNT가 음수입니다."
                        + " / source=" + sourceName
                        + " / line=" + sourceLineCount
                    );
                }

                if (sampleMessage.length() > 1000) {

                    throw new IllegalArgumentException(
                        "SAMPLE_MESSAGE 길이 초과"
                        + " / source=" + sourceName
                        + " / line=" + sourceLineCount
                    );
                }

                String aggregateKey =
                        fileType
                        + "|"
                        + qualityType;

                Aggregate aggregate =
                        aggregates.computeIfAbsent(
                            aggregateKey,
                            k -> new Aggregate(
                                fileType,
                                qualityType,
                                sampleMessage
                            )
                        );

                aggregate.add(
                    recordCount
                );
            }
        }

        return sourceLineCount;
    }

    /**
     * FILE_TYPE + QUALITY_TYPE별 누적 객체.
     */
    private static class Aggregate {

        private final String fileType;
        private final String qualityType;
        private final String sampleMessage;

        private long recordCount;

        private Aggregate(
                String fileType,
                String qualityType,
                String sampleMessage) {

            this.fileType = fileType;
            this.qualityType = qualityType;
            this.sampleMessage = sampleMessage;
        }

        private void add(long value) {
            this.recordCount += value;
        }
    }
}
