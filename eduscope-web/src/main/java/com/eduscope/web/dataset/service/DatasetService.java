package com.eduscope.web.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.dataset.dto.DatasetCreateRequest;
import com.eduscope.web.dataset.dto.DatasetResponse;
import com.eduscope.web.dataset.dto.DatasetVersionCreateRequest;
import com.eduscope.web.dataset.entity.Dataset;
import com.eduscope.web.dataset.repository.DatasetRepository;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

/**
 * DATASET 조회 및 관리 비즈니스 로직.
 */
@Service
@Transactional(readOnly = true)
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    public DatasetService(
            DatasetRepository datasetRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService) {

        this.datasetRepository = datasetRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * 논리 삭제되지 않은 Dataset만 조회한다.
     */
    public List<DatasetResponse> getAllDatasets() {

        return datasetRepository
            .findByIsDeletedOrderByDatasetIdAsc("N")
            .stream()
            .map(DatasetResponse::from)
            .toList();
    }

    /**
     * DATASET_ID 기준 활성 Dataset 단건 조회.
     */
    public DatasetResponse getDataset(Long datasetId) {

        return DatasetResponse.from(
            getActiveDataset(datasetId)
        );
    }

    /**
     * 신규 Dataset을 등록한다.
     *
     * 현재 프로젝트 정책상 sourceType은 REAL로 고정한다.
     */
    @Transactional
    public DatasetResponse createDataset(
            DatasetCreateRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        String displayName = normalizeRequired(
            request.displayName(),
            "displayName"
        );

        String sourceName = normalizeRequired(
            request.sourceName(),
            "sourceName"
        );

        String datasetVersion = normalizeRequired(
            request.datasetVersion(),
            "datasetVersion"
        );

        String schemaVersion = normalizeRequired(
            request.schemaVersion(),
            "schemaVersion"
        );

        validateSourceVersionNotUsed(
            sourceName,
            datasetVersion
        );

        Dataset dataset = Dataset.createReal(
            displayName,
            normalizeNullable(request.description()),
            sourceName,
            normalizeNullable(request.sourceUrl()),
            datasetVersion,
            schemaVersion,
            normalizeNullable(request.hdfsBasePath())
        );

        Dataset saved = datasetRepository.save(dataset);

        AppUser actor = getActor(loginId);

        auditLogService.record(
            actor.getUserId(),
            "DATASET_CREATE",
            "DATASET",
            String.valueOf(saved.getDatasetId()),
            requestUri,
            "Dataset 등록: "
                + saved.getSourceName()
                + ", Version="
                + saved.getDatasetVersion(),
            ipAddress
        );

        return DatasetResponse.from(saved);
    }

    /**
     * 기존 Dataset의 버전을 덮어쓰지 않고
     * 새로운 DATASET 행으로 버전을 생성한다.
     */
    @Transactional
    public DatasetResponse createVersion(
            Long sourceDatasetId,
            DatasetVersionCreateRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        Dataset source = getActiveDataset(sourceDatasetId);

        String datasetVersion = normalizeRequired(
            request.datasetVersion(),
            "datasetVersion"
        );

        String schemaVersion = normalizeRequired(
            request.schemaVersion(),
            "schemaVersion"
        );

        String hdfsBasePath = normalizeRequired(
            request.hdfsBasePath(),
            "hdfsBasePath"
        );

        validateSourceVersionNotUsed(
            source.getSourceName(),
            datasetVersion
        );

        Dataset newVersion = Dataset.createVersionFrom(
            source,
            datasetVersion,
            schemaVersion,
            hdfsBasePath
        );

        Dataset saved = datasetRepository.save(newVersion);

        AppUser actor = getActor(loginId);

        /*
         * 새로운 DATASET 행을 만드는 작업이므로
         * 기존 DATASET_CREATE Audit 유형을 재사용한다.
         */
        auditLogService.record(
            actor.getUserId(),
            "DATASET_CREATE",
            "DATASET",
            String.valueOf(saved.getDatasetId()),
            requestUri,
            "Dataset 새 버전 등록: 기준 Dataset #"
                + sourceDatasetId
                + " → 신규 Dataset #"
                + saved.getDatasetId()
                + ", Version="
                + saved.getDatasetVersion(),
            ipAddress
        );

        return DatasetResponse.from(saved);
    }

    /**
     * Dataset은 물리 삭제하지 않고 IS_DELETED='Y'로 처리한다.
     * 기존 분석/파일 이력은 보존한다.
     */
    @Transactional
    public void deleteDataset(
            Long datasetId,
            String loginId,
            String requestUri,
            String ipAddress) {

        Dataset dataset = getActiveDataset(datasetId);

        dataset.markDeleted();
        datasetRepository.save(dataset);

        AppUser actor = getActor(loginId);

        auditLogService.record(
            actor.getUserId(),
            "DATASET_DELETE",
            "DATASET",
            String.valueOf(datasetId),
            requestUri,
            "Dataset 논리 삭제: "
                + dataset.getSourceName()
                + ", Version="
                + dataset.getDatasetVersion(),
            ipAddress
        );
    }

    /**
     * 논리 삭제되지 않은 Dataset 조회.
     */
    private Dataset getActiveDataset(Long datasetId) {

        if (datasetId == null || datasetId <= 0) {
            throw new IllegalArgumentException(
                "datasetId는 1 이상의 값이어야 합니다."
            );
        }

        Dataset dataset = datasetRepository
            .findById(datasetId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "DATASET을 찾을 수 없습니다. datasetId="
                    + datasetId
                )
            );

        if (!"N".equals(dataset.getIsDeleted())) {
            throw new IllegalArgumentException(
                "삭제되었거나 사용할 수 없는 DATASET입니다. datasetId="
                + datasetId
            );
        }

        return dataset;
    }

    /**
     * DB의 (SOURCE_NAME, DATASET_VERSION) Unique 규칙을
     * Service에서도 명확하게 검증한다.
     */
    private void validateSourceVersionNotUsed(
            String sourceName,
            String datasetVersion) {

        if (
            datasetRepository
                .findBySourceNameAndDatasetVersion(
                    sourceName,
                    datasetVersion
                )
                .isPresent()
        ) {
            throw new IllegalArgumentException(
                "이미 등록된 Dataset 버전입니다. sourceName="
                + sourceName
                + ", datasetVersion="
                + datasetVersion
            );
        }
    }

    private AppUser getActor(String loginId) {

        return appUserRepository
            .findByLoginId(loginId)
            .orElseThrow(() ->
                new IllegalStateException(
                    "현재 로그인 사용자를 찾을 수 없습니다."
                )
            );
    }

    private String normalizeRequired(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + "은(는) 필수입니다."
            );
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
