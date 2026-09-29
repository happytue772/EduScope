package com.eduscope.web.dataset.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.dataset.dto.DatasetFileChecksumRequest;
import com.eduscope.web.dataset.dto.DatasetFileCreateRequest;
import com.eduscope.web.dataset.dto.DatasetFileResponse;
import com.eduscope.web.dataset.entity.Dataset;
import com.eduscope.web.dataset.entity.DatasetFile;
import com.eduscope.web.dataset.repository.DatasetFileRepository;
import com.eduscope.web.dataset.repository.DatasetRepository;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

/**
 * Dataset 파일 메타정보 조회/관리 Service.
 */
@Service
@Transactional(readOnly = true)
public class DatasetFileService {

    /**
     * 확정 DB 설계의 OULAD 7개 파일 유형.
     */
    private static final Set<String> SUPPORTED_FILE_TYPES =
        Set.of(
            "COURSES",
            "ASSESSMENTS",
            "VLE",
            "STUDENT_INFO",
            "STUDENT_REGISTRATION",
            "STUDENT_ASSESSMENT",
            "STUDENT_VLE"
        );

    /**
     * 확정 DB 설계의 LOAD_STATUS 값.
     */
    private static final Set<String> SUPPORTED_LOAD_STATUS =
        Set.of(
            "REGISTERED",
            "VALIDATED",
            "HDFS_STORED",
            "FAILED"
        );

    private final DatasetFileRepository repository;
    private final DatasetRepository datasetRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    public DatasetFileService(
            DatasetFileRepository repository,
            DatasetRepository datasetRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService) {

        this.repository = repository;
        this.datasetRepository = datasetRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    public List<DatasetFileResponse> getFiles() {

        return repository
            .findAll()
            .stream()
            .map(DatasetFileResponse::from)
            .toList();
    }

    public DatasetFileResponse getFile(
            Long datasetFileId) {

        DatasetFile file = getFileEntity(datasetFileId);

        return DatasetFileResponse.from(file);
    }

    public List<DatasetFileResponse> getFilesByDataset(
            Long datasetId) {

        return repository
            .findByDataset_DatasetIdOrderByDatasetFileIdAsc(
                datasetId
            )
            .stream()
            .map(DatasetFileResponse::from)
            .toList();
    }

    /**
     * 실제 원본 파일의 메타정보를 DATASET_FILE에 등록한다.
     *
     * 임의 파일정보를 생성하지 않고 요청으로 전달된 실제 값만 저장한다.
     */
    @Transactional
    public DatasetFileResponse createFile(
            Long datasetId,
            DatasetFileCreateRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        Dataset dataset = getActiveDataset(datasetId);

        String fileType = normalizeFileType(
            request.fileType()
        );

        if (
            repository.existsByDataset_DatasetIdAndFileType(
                datasetId,
                fileType
            )
        ) {
            throw new IllegalArgumentException(
                "이미 등록된 Dataset 파일 유형입니다. datasetId="
                + datasetId
                + ", fileType="
                + fileType
            );
        }

        validateNonNegative(
            request.fileSizeBytes(),
            "fileSizeBytes"
        );

        validateNonNegative(
            request.recordCount(),
            "recordCount"
        );

        String contentHash = normalizeHash(
            request.contentHash(),
            false
        );

        String loadStatus = normalizeLoadStatus(
            request.loadStatus()
        );

        DatasetFile file = DatasetFile.create(
            dataset,
            fileType,
            normalizeRequired(
                request.originalFileName(),
                "originalFileName"
            ),
            request.fileSizeBytes(),
            request.recordCount(),
            contentHash,
            normalizeNullable(request.hdfsRawPath()),
            normalizeNullable(request.hdfsCleanPath()),
            loadStatus
        );

        DatasetFile saved = repository.save(file);

        /*
         * DATASET.FILE_COUNT를 실제 DATASET_FILE 행 수와 동기화한다.
         */
        long actualFileCount =
            repository.countByDataset_DatasetId(datasetId);

        dataset.syncFileCount(actualFileCount);
        datasetRepository.save(dataset);

        AppUser actor = getActor(loginId);

        auditLogService.record(
            actor.getUserId(),
            "DATASET_UPDATE",
            "DATASET_FILE",
            String.valueOf(saved.getDatasetFileId()),
            requestUri,
            "Dataset 파일 등록: datasetId="
                + datasetId
                + ", fileType="
                + fileType
                + ", fileName="
                + saved.getOriginalFileName(),
            ipAddress
        );

        return DatasetFileResponse.from(saved);
    }

    /**
     * 실제 파일에서 계산된 SHA-256 Checksum만 갱신한다.
     */
    @Transactional
    public DatasetFileResponse updateChecksum(
            Long datasetFileId,
            DatasetFileChecksumRequest request,
            String loginId,
            String requestUri,
            String ipAddress) {

        DatasetFile file = getFileEntity(datasetFileId);

        if (!"N".equals(file.getDataset().getIsDeleted())) {
            throw new IllegalStateException(
                "삭제된 Dataset의 파일은 수정할 수 없습니다."
            );
        }

        String contentHash = normalizeHash(
            request.contentHash(),
            true
        );

        file.updateContentHash(contentHash);

        DatasetFile saved = repository.save(file);

        AppUser actor = getActor(loginId);

        auditLogService.record(
            actor.getUserId(),
            "DATASET_UPDATE",
            "DATASET_FILE",
            String.valueOf(datasetFileId),
            requestUri,
            "Dataset 파일 SHA-256 갱신: datasetId="
                + file.getDataset().getDatasetId()
                + ", fileType="
                + file.getFileType(),
            ipAddress
        );

        return DatasetFileResponse.from(saved);
    }

    private DatasetFile getFileEntity(Long datasetFileId) {

        if (datasetFileId == null || datasetFileId <= 0) {
            throw new IllegalArgumentException(
                "datasetFileId는 1 이상의 값이어야 합니다."
            );
        }

        return repository
            .findById(datasetFileId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "DATASET_FILE을 찾을 수 없습니다. datasetFileId="
                    + datasetFileId
                )
            );
    }

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

    private String normalizeFileType(String fileType) {

        String normalized = normalizeRequired(
            fileType,
            "fileType"
        ).toUpperCase();

        if (!SUPPORTED_FILE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException(
                "지원하지 않는 fileType입니다: "
                + normalized
            );
        }

        return normalized;
    }

    private String normalizeLoadStatus(String loadStatus) {

        if (loadStatus == null || loadStatus.isBlank()) {
            return "REGISTERED";
        }

        String normalized = loadStatus.trim().toUpperCase();

        if (!SUPPORTED_LOAD_STATUS.contains(normalized)) {
            throw new IllegalArgumentException(
                "지원하지 않는 loadStatus입니다: "
                + normalized
            );
        }

        return normalized;
    }

    private String normalizeHash(
            String contentHash,
            boolean required) {

        if (contentHash == null || contentHash.isBlank()) {

            if (required) {
                throw new IllegalArgumentException(
                    "contentHash는 필수입니다."
                );
            }

            return null;
        }

        String normalized = contentHash.trim();

        if (!normalized.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException(
                "contentHash는 64자리 SHA-256 16진수여야 합니다."
            );
        }

        return normalized;
    }

    private void validateNonNegative(
            Long value,
            String fieldName) {

        if (value != null && value < 0) {
            throw new IllegalArgumentException(
                fieldName + "은(는) 0 이상이어야 합니다."
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
