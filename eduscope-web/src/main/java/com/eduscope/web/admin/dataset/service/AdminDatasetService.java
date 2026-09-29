package com.eduscope.web.admin.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.admin.dataset.dto.AdminDatasetFileResponse;
import com.eduscope.web.admin.dataset.dto.AdminDatasetResponse;
import com.eduscope.web.admin.dataset.dto.DatasetCreateRequest;
import com.eduscope.web.admin.dataset.dto.DatasetUpdateRequest;
import com.eduscope.web.admin.dataset.repository.AdminDatasetRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;

/**
 * ADMIN Dataset 관리 Service.
 */
@Service
@Transactional(readOnly = true)
public class AdminDatasetService {

    private final AdminDatasetRepository repository;

    private final AppUserRepository appUserRepository;

    private final AuditLogService auditLogService;
    
    /**
     * Dataset에 포함된 실제 파일 정보 조회.
     *
     * 조회 작업이므로 Audit Log는 남기지 않는다.
     */
    public List<AdminDatasetFileResponse> getDatasetFiles(
            Long datasetId) {

        if (
            !repository.exists(
                datasetId
            )
        ) {

            throw new IllegalArgumentException(
                "Dataset을 찾을 수 없습니다."
            );
        }


        return repository
            .findFilesByDatasetId(
                datasetId
            );
    }


    public AdminDatasetService(
            AdminDatasetRepository repository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService) {

        this.repository =
            repository;

        this.appUserRepository =
            appUserRepository;

        this.auditLogService =
            auditLogService;
    }


    public List<AdminDatasetResponse> getDatasets() {

        return repository.findAll();
    }


    /**
     * 신규 Dataset 등록.
     */
    @Transactional
    public Long createDataset(
            DatasetCreateRequest request,
            String adminLoginId,
            String requestUri,
            String remoteAddr) {

        Long datasetId =
            repository.nextDatasetId();


        repository.insert(
            datasetId,
            request
        );


        AppUser admin =
            getAdmin(
                adminLoginId
            );


        auditLogService.record(
            admin.getUserId(),
            "DATASET_CREATE",
            "DATASET",
            String.valueOf(datasetId),
            requestUri,
            "Dataset 등록: "
                + request.displayName()
                + ", Version="
                + request.datasetVersion(),
            remoteAddr
        );


        return datasetId;
    }


    /**
     * Dataset 메타정보 수정.
     */
    @Transactional
    public void updateDataset(
            Long datasetId,
            DatasetUpdateRequest request,
            String adminLoginId,
            String requestUri,
            String remoteAddr) {

        int updated =
            repository.update(
                datasetId,
                request
            );


        if (updated == 0) {

            throw new IllegalArgumentException(
                "수정할 Dataset을 찾을 수 없습니다."
            );
        }


        AppUser admin =
            getAdmin(
                adminLoginId
            );


        /*
         * AUDIT_LOG.action_type은 VARCHAR2이며
         * 명세의 '등' 확장 규칙을 사용한다.
         */
        auditLogService.record(
            admin.getUserId(),
            "DATASET_UPDATE",
            "DATASET",
            String.valueOf(datasetId),
            requestUri,
            "Dataset 메타정보 수정",
            remoteAddr
        );
    }


    /**
     * Dataset 논리 삭제.
     */
    @Transactional
    public void deleteDataset(
            Long datasetId,
            String adminLoginId,
            String requestUri,
            String remoteAddr) {

        int updated =
            repository.logicalDelete(
                datasetId
            );


        if (updated == 0) {

            throw new IllegalArgumentException(
                "삭제할 Dataset을 찾을 수 없거나 이미 삭제되었습니다."
            );
        }


        AppUser admin =
            getAdmin(
                adminLoginId
            );


        auditLogService.record(
            admin.getUserId(),
            "DATASET_DELETE",
            "DATASET",
            String.valueOf(datasetId),
            requestUri,
            "Dataset 논리 삭제",
            remoteAddr
        );
    }


    /**
     * 작업 수행 ADMIN 조회.
     */
    private AppUser getAdmin(
            String loginId) {

        return appUserRepository
            .findByLoginId(
                loginId
            )
            .orElseThrow(() ->
                new IllegalStateException(
                    "현재 관리자 계정을 찾을 수 없습니다."
                )
            );
    }
}