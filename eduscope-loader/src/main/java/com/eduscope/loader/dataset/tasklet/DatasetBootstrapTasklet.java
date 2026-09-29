package com.eduscope.loader.dataset.tasklet;

import java.time.LocalDateTime;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;

/**
 * EduScope Dataset 최초 등록 Tasklet.
 *
 * 동일 SOURCE_NAME + DATASET_VERSION이 이미 존재하면
 * 중복 INSERT하지 않는다.
 */
@Component
public class DatasetBootstrapTasklet implements Tasklet {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    public DatasetBootstrapTasklet(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
    }

    @Override
    @Transactional
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        // 필수 설정값 검증
        validateRequiredProperties();

        boolean alreadyExists =
                datasetRepository
                    .findBySourceNameAndDatasetVersion(
                            datasetProperties.getSourceName(),
                            datasetProperties.getDatasetVersion()
                    )
                    .isPresent();

        if (alreadyExists) {

            System.out.println(
                    "[DATASET] 이미 등록된 Dataset입니다. INSERT 생략"
            );

            return RepeatStatus.FINISHED;
        }

        LocalDateTime now = LocalDateTime.now();

        Dataset dataset = new Dataset();

        dataset.setDisplayName(
                datasetProperties.getDisplayName()
        );

        dataset.setDescription(
                emptyToNull(datasetProperties.getDescription())
        );

        // Oracle CK_DATASET_SOURCE 제약조건
        dataset.setSourceType("REAL");

        dataset.setSourceName(
                datasetProperties.getSourceName()
        );

        dataset.setSourceUrl(
                emptyToNull(datasetProperties.getSourceUrl())
        );

        dataset.setDatasetVersion(
                datasetProperties.getDatasetVersion()
        );

        dataset.setSchemaVersion(
                datasetProperties.getSchemaVersion()
        );

        dataset.setHdfsBasePath(
                datasetProperties.getHdfsBasePath()
        );

        // 실제 OULAD 원본 파일 7개
        dataset.setFileCount(7);

        // 최초 등록 시 삭제되지 않은 데이터셋
        dataset.setIsDeleted("N");

        dataset.setCreatedAt(now);
        dataset.setUpdatedAt(now);

        Dataset saved =
                datasetRepository.save(dataset);

        System.out.println(
                "[DATASET] 등록 완료 - DATASET_ID="
                + saved.getDatasetId()
        );

        return RepeatStatus.FINISHED;
    }

    private void validateRequiredProperties() {

        require(
            datasetProperties.getDisplayName(),
            "EDUSCOPE_DATASET_DISPLAY_NAME"
        );

        require(
            datasetProperties.getSourceName(),
            "EDUSCOPE_DATASET_SOURCE_NAME"
        );

        require(
            datasetProperties.getDatasetVersion(),
            "EDUSCOPE_DATASET_VERSION"
        );

        require(
            datasetProperties.getSchemaVersion(),
            "EDUSCOPE_SCHEMA_VERSION"
        );

        require(
            datasetProperties.getHdfsBasePath(),
            "HDFS_BASE_PATH"
        );
    }

    private void require(String value, String name) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "필수 설정값 누락: " + name
            );
        }
    }

    private String emptyToNull(String value) {

        return value == null || value.isBlank()
                ? null
                : value;
    }
}