package com.eduscope.web.analysis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.analysis.dto.DataQualityStatResponse;
import com.eduscope.web.analysis.repository.DataQualityStatRepository;

/**
 * 데이터 품질 통계 조회 Service.
 *
 * 기존 DATA_QUALITY_STAT 상세 조회 기능을 유지한다.
 * Summary 조합은 기존 dataquality 패키지에서 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class DataQualityStatService {

    private final DataQualityStatRepository repository;

    public DataQualityStatService(
            DataQualityStatRepository repository) {

        this.repository = repository;
    }

    public Page<DataQualityStatResponse> getStats(
            int page,
            int size) {

        return repository
            .findAll(
                PageRequest.of(
                    page,
                    size
                )
            )
            .map(
                DataQualityStatResponse::from
            );
    }

    public Page<DataQualityStatResponse> getByJob(
            Long jobId,
            int page,
            int size) {

        return repository
            .findByJobId(
                jobId,
                PageRequest.of(
                    page,
                    size
                )
            )
            .map(
                DataQualityStatResponse::from
            );
    }

    public Page<DataQualityStatResponse> getByFile(
            Long datasetFileId,
            int page,
            int size) {

        return repository
            .findByDatasetFileId(
                datasetFileId,
                PageRequest.of(
                    page,
                    size
                )
            )
            .map(
                DataQualityStatResponse::from
            );
    }

    public Page<DataQualityStatResponse> getByQualityType(
            String qualityType,
            int page,
            int size) {

        return repository
            .findByQualityType(
                qualityType,
                PageRequest.of(
                    page,
                    size
                )
            )
            .map(
                DataQualityStatResponse::from
            );
    }
}