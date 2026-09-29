package com.eduscope.web.admin.dataset.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.admin.dataset.dto.AdminDatasetFileResponse;
import com.eduscope.web.admin.dataset.dto.AdminDatasetResponse;
import com.eduscope.web.admin.dataset.dto.DatasetCreateRequest;
import com.eduscope.web.admin.dataset.dto.DatasetUpdateRequest;
import com.eduscope.web.admin.dataset.service.AdminDatasetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * ADMIN Dataset 관리 API.
 *
 * 기존 /api/datasets 조회 API와 분리한다.
 */
@RestController
@RequestMapping("/api/admin/datasets")
public class AdminDatasetController {

    private final AdminDatasetService service;


    public AdminDatasetController(
            AdminDatasetService service) {

        this.service =
            service;
    }
    /**
     * Dataset 원본 파일 목록 조회.
     */
    @GetMapping("/{datasetId}/files")
    @PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
    public List<AdminDatasetFileResponse> getDatasetFiles(
            @PathVariable Long datasetId) {

        return service
            .getDatasetFiles(
                datasetId
            );
    }


    /**
     * 삭제 Dataset을 포함한 관리자 전체 조회.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
    public List<AdminDatasetResponse> getDatasets() {

        return service.getDatasets();
    }


    /**
     * Dataset 신규 등록.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createDataset(
            @Valid
            @RequestBody
            DatasetCreateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        Long datasetId =
            service.createDataset(
                request,
                principal.getName(),
                httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr()
            );


        return Map.of(
            "message",
            "Dataset이 등록되었습니다.",
            "datasetId",
            datasetId
        );
    }


    /**
     * Dataset 메타정보 수정.
     */
    @PatchMapping("/{datasetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateDataset(
            @PathVariable Long datasetId,
            @Valid
            @RequestBody
            DatasetUpdateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        service.updateDataset(
            datasetId,
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );


        return Map.of(
            "message",
            "Dataset 메타정보가 수정되었습니다."
        );
    }


    /**
     * Dataset 논리 삭제.
     *
     * 실제 DELETE FROM DATASET은 수행하지 않는다.
     */
    @DeleteMapping("/{datasetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> deleteDataset(
            @PathVariable Long datasetId,
            Principal principal,
            HttpServletRequest httpRequest) {

        service.deleteDataset(
            datasetId,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );


        return Map.of(
            "message",
            "Dataset이 논리 삭제되었습니다."
        );
    }
}
