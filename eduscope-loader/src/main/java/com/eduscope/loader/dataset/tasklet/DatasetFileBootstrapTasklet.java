package com.eduscope.loader.dataset.tasklet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.config.InputPathProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.entity.DatasetFile;
import com.eduscope.loader.dataset.repository.DatasetFileRepository;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * 실제 OULAD 원본 CSV 7개의 메타데이터를 읽어
 * DATASET_FILE 테이블에 등록한다.
 */
@Component
public class DatasetFileBootstrapTasklet implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final DatasetProperties datasetProperties;
    private final InputPathProperties inputPathProperties;

    public DatasetFileBootstrapTasklet(
            DatasetRepository datasetRepository,
            DatasetFileRepository datasetFileRepository,
            DatasetProperties datasetProperties,
            InputPathProperties inputPathProperties) {

        this.datasetRepository = datasetRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.datasetProperties = datasetProperties;
        this.inputPathProperties = inputPathProperties;
    }

    /**
     * fileType / 실제 파일명 / HDFS raw 하위 경로
     */
    private static final List<FileDefinition> FILES = List.of(
            new FileDefinition(
                    "COURSES",
                    "courses.csv",
                    "raw/courses/courses.csv"
            ),
            new FileDefinition(
                    "ASSESSMENTS",
                    "assessments.csv",
                    "raw/assessments/assessments.csv"
            ),
            new FileDefinition(
                    "VLE",
                    "vle.csv",
                    "raw/vle/vle.csv"
            ),
            new FileDefinition(
                    "STUDENT_INFO",
                    "studentInfo.csv",
                    "raw/student-info/studentInfo.csv"
            ),
            new FileDefinition(
                    "STUDENT_REGISTRATION",
                    "studentRegistration.csv",
                    "raw/student-registration/studentRegistration.csv"
            ),
            new FileDefinition(
                    "STUDENT_ASSESSMENT",
                    "studentAssessment.csv",
                    "raw/student-assessment/studentAssessment.csv"
            ),
            new FileDefinition(
                    "STUDENT_VLE",
                    "studentVle.csv",
                    "raw/student-vle/studentVle.csv"
            )
    );

    @Override
    @Transactional
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        /*
         * 이전 단계에서 등록한 DATASET 조회.
         */
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

        Path rawBase =
                Paths.get(inputPathProperties.getRawPath());

        if (!Files.isDirectory(rawBase)) {
            throw new IllegalStateException(
                    "원본 CSV 디렉터리를 찾을 수 없습니다: "
                    + rawBase
            );
        }

        for (FileDefinition definition : FILES) {

            Path file =
                    rawBase.resolve(definition.fileName());

            /*
             * 원본 파일이 하나라도 없으면 임의로 진행하지 않고 중단.
             */
            if (!Files.isRegularFile(file)) {
                throw new IllegalStateException(
                        "원본 CSV 파일 누락: "
                        + file
                );
            }

            boolean alreadyExists =
                    datasetFileRepository
                            .findByDatasetAndFileType(
                                    dataset,
                                    definition.fileType()
                            )
                            .isPresent();

            // 재실행 시 중복 등록 방지
            if (alreadyExists) {

                System.out.println(
                        "[DATASET_FILE] 이미 등록됨 → "
                        + definition.fileType()
                );

                continue;
            }

            long fileSizeBytes =
                    Files.size(file);

            long recordCount =
                    countDataRecords(file);

            String sha256 =
                    calculateSha256(file);

            String hdfsRawPath =
                    buildHdfsPath(
                            datasetProperties.getHdfsBasePath(),
                            definition.hdfsRelativePath()
                    );

            LocalDateTime now =
                    LocalDateTime.now();

            DatasetFile datasetFile =
                    new DatasetFile();

            datasetFile.setDataset(dataset);

            datasetFile.setFileType(
                    definition.fileType()
            );

            datasetFile.setOriginalFileName(
                    definition.fileName()
            );

            datasetFile.setFileSizeBytes(
                    fileSizeBytes
            );

            datasetFile.setRecordCount(
                    recordCount
            );

            datasetFile.setContentHash(
                    sha256
            );

            datasetFile.setHdfsRawPath(
                    hdfsRawPath
            );

            /*
             * 현재 EduScope에는 별도 clean HDFS 계층을
             * 생성하지 않았으므로 NULL 유지.
             */
            datasetFile.setHdfsCleanPath(null);

            /*
             * 실제 파일들이 이미 HDFS raw에 적재되어 있고
             * _SUCCESS까지 검증한 현재 상태를 반영.
             */
            datasetFile.setLoadStatus(
                    "HDFS_STORED"
            );

            datasetFile.setCreatedAt(now);
            datasetFile.setUpdatedAt(now);

            datasetFileRepository.save(
                    datasetFile
            );

            System.out.println(
                    "[DATASET_FILE] 등록 완료"
                    + " / type="
                    + definition.fileType()
                    + " / records="
                    + recordCount
                    + " / bytes="
                    + fileSizeBytes
            );
        }

        return RepeatStatus.FINISHED;
    }

    /**
     * CSV 첫 행은 Header이므로 제외하고 실제 데이터 행만 계산.
     * 전체 파일을 메모리에 올리지 않는다.
     */
    private long countDataRecords(Path file)
            throws IOException {

        long lineCount = 0L;

        try (BufferedReader reader =
                Files.newBufferedReader(
                        file,
                        StandardCharsets.UTF_8
                )) {

            while (reader.readLine() != null) {
                lineCount++;
            }
        }

        return Math.max(
                lineCount - 1,
                0
        );
    }

    /**
     * 파일 전체를 스트리밍하면서 실제 SHA-256 계산.
     */
    private String calculateSha256(Path file)
            throws IOException, NoSuchAlgorithmException {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        try (BufferedInputStream input =
                new BufferedInputStream(
                        Files.newInputStream(file)
                )) {

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length = input.read(buffer)) != -1) {

                digest.update(
                        buffer,
                        0,
                        length
                );
            }
        }

        return HexFormat.of()
                .formatHex(digest.digest());
    }

    private String buildHdfsPath(
            String base,
            String relative) {

        String normalizedBase =
                base.endsWith("/")
                ? base.substring(
                        0,
                        base.length() - 1
                )
                : base;

        return normalizedBase
                + "/"
                + relative;
    }

    /**
     * DATASET_FILE 등록에 필요한 고정 매핑 구조.
     * 실제 OULAD 7개 파일명과 우리가 사용한 HDFS 경로만 정의한다.
     */
    private record FileDefinition(
            String fileType,
            String fileName,
            String hdfsRelativePath) {
    }
}