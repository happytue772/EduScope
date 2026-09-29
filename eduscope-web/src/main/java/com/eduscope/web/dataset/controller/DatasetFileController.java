package com.eduscope.web.dataset.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.dataset.dto.DatasetFileChecksumRequest;
import com.eduscope.web.dataset.dto.DatasetFileCreateRequest;
import com.eduscope.web.dataset.dto.DatasetFileResponse;
import com.eduscope.web.dataset.service.DatasetFileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * DATASET_FILE 조회/관리 REST API.
 */
@RestController
@RequestMapping("/api/dataset-files")
public class DatasetFileController {

    private final DatasetFileService service;

    public DatasetFileController(
            DatasetFileService service) {

        this.service = service;
    }

    @GetMapping
    public List<DatasetFileResponse> getFiles() {
        return service.getFiles();
    }

    @GetMapping("/{datasetFileId}")
    public DatasetFileResponse getFile(
            @PathVariable Long datasetFileId) {

        return service.getFile(
            datasetFileId
        );
    }

    @GetMapping("/dataset/{datasetId}")
    public List<DatasetFileResponse> getFilesByDataset(
            @PathVariable Long datasetId) {

        return service.getFilesByDataset(
            datasetId
        );
    }

    /**
     * Dataset의 실제 원본 파일 메타정보 등록.
     */
    @PostMapping("/dataset/{datasetId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DatasetFileResponse createFile(
            @PathVariable Long datasetId,
            @Valid
            @RequestBody
            DatasetFileCreateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        return service.createFile(
            datasetId,
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );
    }

    /**
     * 실제 파일에서 계산한 SHA-256 Checksum 갱신.
     */
    @PutMapping("/{datasetFileId}/checksum")
    @PreAuthorize("hasRole('ADMIN')")
    public DatasetFileResponse updateChecksum(
            @PathVariable Long datasetFileId,
            @Valid
            @RequestBody
            DatasetFileChecksumRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        return service.updateChecksum(
            datasetFileId,
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );
    }
}
