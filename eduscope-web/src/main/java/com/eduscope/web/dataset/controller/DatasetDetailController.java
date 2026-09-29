package com.eduscope.web.dataset.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.dataset.dto.DatasetDetailResponse;
import com.eduscope.web.dataset.service.DatasetViewService;

/**
 * Dataset 상세 조회 전용 Controller.
 *
 * 기존 DatasetController의
 * /api/datasets API와 충돌하지 않도록
 * 별도의 URL을 사용한다.
 */
@RestController
@RequestMapping("/api/dataset-details")
public class DatasetDetailController {

    private final DatasetViewService datasetViewService;

    public DatasetDetailController(
            DatasetViewService datasetViewService) {

        this.datasetViewService =
            datasetViewService;
    }


    /**
     * Dataset + DATASET_FILE 상세 조회.
     *
     * 예:
     * GET /api/dataset-details/1
     */
    @GetMapping("/{datasetId}")
    public DatasetDetailResponse getDatasetDetail(
            @PathVariable Long datasetId) {

        return datasetViewService
            .getDatasetDetail(datasetId);
    }
}