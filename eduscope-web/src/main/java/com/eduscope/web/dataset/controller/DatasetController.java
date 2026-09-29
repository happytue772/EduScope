package com.eduscope.web.dataset.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.dataset.dto.DatasetCreateRequest;
import com.eduscope.web.dataset.dto.DatasetResponse;
import com.eduscope.web.dataset.dto.DatasetVersionCreateRequest;
import com.eduscope.web.dataset.service.DatasetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * DATASET 조회/관리 REST API.
 *
 * 기존 GET API는 유지하고
 * ADMIN 전용 관리 기능만 같은 Controller에 확장한다.
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(
            DatasetService datasetService) {

        this.datasetService = datasetService;
    }

    /**
     * 논리 삭제되지 않은 전체 Dataset 조회.
     */
    @GetMapping
    public List<DatasetResponse> getDatasets() {

        return datasetService.getAllDatasets();
    }

    /**
     * Dataset 한 건 조회.
     */
    @GetMapping("/{datasetId}")
    public DatasetResponse getDataset(
            @PathVariable Long datasetId) {

        return datasetService.getDataset(datasetId);
    }

    /**
     * 신규 Dataset 등록.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DatasetResponse createDataset(
            @Valid
            @RequestBody
            DatasetCreateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        return datasetService.createDataset(
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );
    }

    /**
     * 기존 Dataset을 기준으로 새 버전 등록.
     *
     * 기존 행의 DATASET_VERSION은 수정하지 않고
     * 새로운 DATASET 행을 생성한다.
     */
    @PostMapping("/{datasetId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DatasetResponse createVersion(
            @PathVariable Long datasetId,
            @Valid
            @RequestBody
            DatasetVersionCreateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        return datasetService.createVersion(
            datasetId,
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );
    }

    /**
     * Dataset 논리 삭제.
     */
    @DeleteMapping("/{datasetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDataset(
            @PathVariable Long datasetId,
            Principal principal,
            HttpServletRequest httpRequest) {

        datasetService.deleteDataset(
            datasetId,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );
    }
}
