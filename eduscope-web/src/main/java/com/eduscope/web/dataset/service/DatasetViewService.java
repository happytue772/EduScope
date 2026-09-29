package com.eduscope.web.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.dataset.dto.DatasetDetailResponse;
import com.eduscope.web.dataset.dto.DatasetSummaryResponse;
import com.eduscope.web.dataset.repository.DatasetViewRepository;

/**
 * Dataset 조회 Service.
 */
@Service
@Transactional(readOnly = true)
public class DatasetViewService {

    private final DatasetViewRepository repository;

    public DatasetViewService(
            DatasetViewRepository repository) {

        this.repository = repository;
    }


    public List<DatasetSummaryResponse> getDatasets() {

        return repository.findDatasets();
    }


    public DatasetDetailResponse getDatasetDetail(
            Long datasetId) {

        if (
            datasetId == null
            ||
            datasetId <= 0
        ) {

            throw new IllegalArgumentException(
                "datasetId는 1 이상의 값이어야 합니다."
            );
        }


        DatasetDetailResponse.DatasetInfo dataset =
            repository.findDataset(
                datasetId
            );


        if (dataset == null) {

            throw new IllegalArgumentException(
                "Dataset을 찾을 수 없습니다. datasetId="
                + datasetId
            );
        }


        List<DatasetDetailResponse.DatasetFileInfo> files =
            repository.findFiles(
                datasetId
            );


        long totalRecordCount =
            files.stream()
                .map(
                    DatasetDetailResponse
                        .DatasetFileInfo
                        ::getRecordCount
                )
                .filter(
                    value ->
                        value != null
                )
                .mapToLong(
                    Long::longValue
                )
                .sum();


        long totalFileSizeBytes =
            files.stream()
                .map(
                    DatasetDetailResponse
                        .DatasetFileInfo
                        ::getFileSizeBytes
                )
                .filter(
                    value ->
                        value != null
                )
                .mapToLong(
                    Long::longValue
                )
                .sum();


        long hashedFileCount =
            files.stream()
                .filter(
                    file ->
                        file.getContentHash() != null
                        &&
                        !file.getContentHash().isBlank()
                )
                .count();


        /*
         * 실제 LOAD_STATUS 값을 보존한다.
         *
         * SUCCESS라는 값을 임의로 가정하지 않고,
         * null/blank가 아닌 파일을 현재 상태가 기록된
         * 파일로 집계한다.
         */
        long loadedFileCount =
            files.stream()
                .filter(
                    file ->
                        file.getLoadStatus() != null
                        &&
                        !file.getLoadStatus().isBlank()
                )
                .count();


        DatasetDetailResponse.DatasetSummary summary =
            new DatasetDetailResponse.DatasetSummary(
                (long) files.size(),
                totalRecordCount,
                totalFileSizeBytes,
                hashedFileCount,
                loadedFileCount
            );


        return new DatasetDetailResponse(
            dataset,
            summary,
            files
        );
    }
}