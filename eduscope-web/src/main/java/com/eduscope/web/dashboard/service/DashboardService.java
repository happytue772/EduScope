package com.eduscope.web.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.dashboard.dto.DashboardSummaryResponse;
import com.eduscope.web.dashboard.repository.DashboardSummaryRepository;

/**
 * Dashboard Service.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardSummaryRepository repository;

    public DashboardService(
            DashboardSummaryRepository repository) {

        this.repository = repository;
    }

    public DashboardSummaryResponse getSummary(
            Long datasetId) {

        if (datasetId == null
                || datasetId <= 0) {

            throw new IllegalArgumentException(
                "datasetId는 1 이상의 값이어야 합니다."
            );
        }

        DashboardSummaryResponse response =
            repository.findSummary(
                datasetId
            );

        if (response.getDatasetCount() == 0) {

            throw new IllegalArgumentException(
                "존재하지 않는 Dataset입니다. datasetId="
                + datasetId
            );
        }

        return response;
    }
}