package com.eduscope.loader.analysis.tasklet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * 이미 성공한 Hadoop 분석 결과를
 * ANALYSIS_JOB 관리 이력에 최초 등록한다.
 *
 * 주의:
 * 과거 Hadoop 실행 당시 수집하지 못한 Counter/실행시간은
 * 임의 추정하지 않는다.
 */
@Component
@StepScope
public class AnalysisJobBootstrapTasklet implements Tasklet {

    private static final String RAW =
            "/user/user/eduscope/raw";

    private static final String OUTPUT =
            "/user/user/eduscope/output";

    /**
     * 실제 최종 Hadoop 결과만 등록한다.
     *
     * test 결과, 중간 join 결과, 구버전 결과는 제외한다.
     */
    private static final List<AnalysisDefinition> DEFINITIONS =
            List.of(

                new AnalysisDefinition(
                    "STUDENT_ACTIVITY",
                    "v1",
                    RAW + "/student-vle",
                    OUTPUT + "/student-activity-v1",
                    List.of("student-activity.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "COURSE_ACTIVITY",
                    "v1",
                    RAW + "/student-vle;"
                        + RAW + "/courses",
                    OUTPUT + "/course-activity-v1",
                    List.of("course-activity.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "COURSE_WEEKLY_ACTIVITY",
                    "v1",
                    RAW + "/student-vle",
                    OUTPUT + "/course-weekly-activity-v1",
                    List.of("course-weekly-activity.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "VLE_ACTIVITY",
                    "v1",
                    RAW + "/student-vle;"
                        + RAW + "/vle",
                    OUTPUT + "/vle-activity-v1",
                    List.of("vle-activity.tsv"),
                    null
                ),

                /*
                 * assessment-v3가 최종 확정본.
                 */
                new AnalysisDefinition(
                    "ASSESSMENT",
                    "v3",
                    RAW + "/assessments;"
                        + RAW + "/student-assessment",
                    OUTPUT + "/assessment-v3",
                    List.of("assessment.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "REGISTRATION",
                    "v1",
                    RAW + "/student-registration;"
                        + RAW + "/student-info",
                    OUTPUT + "/registration-v1",
                    List.of("registration.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "COURSE_RESULT",
                    "v1",
                    RAW + "/student-info",
                    OUTPUT + "/course-result-v1",
                    List.of("course-result.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "ACTIVITY_RESULT",
                    "v1",
                    RAW + "/student-vle;"
                        + RAW + "/student-info",
                    OUTPUT + "/activity-result-v1",
                    List.of("activity-result.tsv"),
                    null
                ),

                new AnalysisDefinition(
                    "STUDENT_LEARNING_SUMMARY",
                    "v1",
                    RAW + "/student-vle;"
                        + RAW + "/student-assessment",
                    OUTPUT + "/student-learning-summary-v1",
                    List.of(
                        "student-learning-summary.tsv"
                    ),
                    null
                ),

                /*
                 * DATA_QUALITY는 분석 타입 1개지만
                 * 최종 결과 TSV는 2개이다.
                 */
                new AnalysisDefinition(
                    "DATA_QUALITY",
                    "v1",
                    RAW,
                    OUTPUT + "/data-quality-row-v1;"
                        + OUTPUT + "/data-quality-duplicate-v1",
                    List.of(
                        "data-quality-row.tsv",
                        "data-quality-duplicate.tsv"
                    ),
                    "data-quality-duplicate.tsv"
                )
            );

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final InputPathProperties inputPathProperties;

    private final AnalysisJobRepository analysisJobRepository;

    public AnalysisJobBootstrapTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            InputPathProperties inputPathProperties,
            AnalysisJobRepository analysisJobRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.inputPathProperties = inputPathProperties;
        this.analysisJobRepository = analysisJobRepository;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        Dataset dataset =
                datasetRepository
                    .findBySourceNameAndDatasetVersion(
                        datasetProperties.getSourceName(),
                        datasetProperties.getDatasetVersion()
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "DATASET을 찾을 수 없습니다."
                        )
                    );

        Path analysisBase =
                Paths.get(
                    inputPathProperties.getAnalysisPath()
                );

        if (!Files.isDirectory(analysisBase)) {

            throw new IllegalStateException(
                "분석 TSV 디렉터리를 찾을 수 없습니다: "
                + analysisBase
            );
        }

        for (AnalysisDefinition definition
                : DEFINITIONS) {

            registerAnalysisJob(
                dataset,
                analysisBase,
                definition
            );
        }

        return RepeatStatus.FINISHED;
    }

    private void registerAnalysisJob(
            Dataset dataset,
            Path analysisBase,
            AnalysisDefinition definition)
            throws IOException {

        /*
         * 재실행 시 동일 Hadoop 결과에 대한
         * ANALYSIS_JOB 중복 생성 방지.
         */
        boolean alreadyExists =
                analysisJobRepository
                    .existsByDatasetAndAnalysisTypeAndHdfsOutputPath(
                        dataset,
                        definition.analysisType(),
                        definition.hdfsOutputPath()
                    );

        if (alreadyExists) {

            System.out.println(
                "[ANALYSIS_JOB] 이미 존재하여 건너뜀: "
                + definition.analysisType()
            );

            return;
        }

        List<Path> resultPaths =
                definition.resultFileNames()
                    .stream()
                    .map(analysisBase::resolve)
                    .collect(Collectors.toList());

        /*
         * Windows staging에 실제 결과가 존재하는지 검증.
         */
        for (Path resultPath : resultPaths) {

            if (!Files.isRegularFile(resultPath)) {

                throw new IllegalStateException(
                    "분석 결과 파일을 찾을 수 없습니다: "
                    + resultPath
                );
            }
        }

        /*
         * OUTPUT_RECORD_COUNT는
         * 현재 실제 TSV 행 수를 직접 계산한다.
         */
        long outputRecordCount = 0L;

        for (Path resultPath : resultPaths) {

            outputRecordCount +=
                    countLines(resultPath);
        }

        /*
         * DATA_QUALITY의 duplicate 결과만
         * 실제 중복 출력 행 수를 저장한다.
         */
        long duplicateRecordCount = 0L;

        if (definition.duplicateFileName() != null) {

            Path duplicateFile =
                    analysisBase.resolve(
                        definition.duplicateFileName()
                    );

            duplicateRecordCount =
                    countLines(duplicateFile);
        }

        LocalDateTime registeredAt =
                LocalDateTime.now();

        AnalysisJob job =
                new AnalysisJob();

        job.setDataset(dataset);

        /*
         * 자동 Bootstrap이므로 APP_USER와 연결하지 않는다.
         */
        job.setRequestedBy(null);

        job.setAnalysisType(
            definition.analysisType()
        );

        /*
         * HDFS _SUCCESS가 이미 확인된 과거 최종 결과.
         */
        job.setStatus("SUCCESS");

        job.setAnalysisVersion(
            definition.analysisVersion()
        );

        job.setHdfsInputPath(
            definition.hdfsInputPath()
        );

        job.setHdfsOutputPath(
            definition.hdfsOutputPath()
        );

        /*
         * Windows Spring Batch staging 파일 경로.
         * 여러 파일이면 ; 로 구분한다.
         */
        String resultFilePath =
                resultPaths.stream()
                    .map(Path::toString)
                    .collect(
                        Collectors.joining(";")
                    );

        job.setResultFilePath(
            resultFilePath
        );

        /*
         * 과거 MapReduce의 실제 요청시간이 아니라
         * ANALYSIS_JOB Bootstrap 등록 시각이다.
         */
        job.setRequestedAt(
            registeredAt
        );

        /*
         * 당시 정확한 시작/종료시간을
         * 임의 생성하지 않는다.
         */
        job.setStartedAt(null);
        job.setFinishedAt(null);

        /*
         * 과거 Hadoop Counter를 보존하지 않았으므로
         * DB 설계 기본값인 0을 사용한다.
         *
         * OUTPUT은 현재 실제 TSV에서 계산한다.
         */
        job.setInputRecordCount(0L);

        job.setOutputRecordCount(
            outputRecordCount
        );

        job.setValidRecordCount(0L);
        job.setInvalidRecordCount(0L);

        job.setDuplicateRecordCount(
            duplicateRecordCount
        );

        job.setProcessingTimeMs(null);

        job.setErrorStep(null);
        job.setErrorMessage(null);

        /*
         * 아직 *_STAT Oracle 적재 전.
         */
        job.setResultImportedYn("N");

        job.setCreatedAt(
            registeredAt
        );

        AnalysisJob saved =
                analysisJobRepository.save(job);

        System.out.println(
            "[ANALYSIS_JOB] 등록 완료"
            + " / jobId="
            + saved.getJobId()
            + " / type="
            + saved.getAnalysisType()
            + " / outputRows="
            + saved.getOutputRecordCount()
        );
    }

    /**
     * 대용량 TSV도 메모리에 전부 올리지 않고
     * 스트리밍 방식으로 실제 행 수를 계산한다.
     */
    private long countLines(Path file)
            throws IOException {

        long count = 0L;

        try (BufferedReader reader =
                Files.newBufferedReader(file)) {

            while (reader.readLine() != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * 분석 타입별 실제 최종 결과 메타정보.
     */
    private record AnalysisDefinition(
        String analysisType,
        String analysisVersion,
        String hdfsInputPath,
        String hdfsOutputPath,
        List<String> resultFileNames,
        String duplicateFileName
    ) {
    }
}