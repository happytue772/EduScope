package com.eduscope.web.dataquality.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.dataquality.dto.DataQualityResponse;
import com.eduscope.web.dataquality.repository.DataQualityRepository;

/**
 * Data Quality 분석 Service.
 */
@Service
@Transactional(readOnly = true)
public class DataQualityService {

    private final DataQualityRepository repository;

    public DataQualityService(
            DataQualityRepository repository) {

        this.repository = repository;
    }


    public DataQualityResponse getSummary(
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


        DataQualityResponse.DatasetInfo dataset =
            repository.findDataset(
                datasetId
            );


        if (dataset == null) {

            throw new IllegalArgumentException(
                "Dataset을 찾을 수 없습니다. datasetId="
                + datasetId
            );
        }


        Long jobId =
            repository.findLatestJobId(
                datasetId
            );


        /**
         * 아직 Data Quality Job이 없는 경우.
         *
         * 임의 데이터를 만들지 않고
         * 빈 품질 결과를 반환한다.
         */
        if (jobId == null) {

            return new DataQualityResponse(
                dataset,
                null,
                new DataQualityResponse.Summary(
                    0L,
                    0L,
                    0L
                ),
                List.of()
            );
        }


        DataQualityResponse.JobInfo job =
            repository.findJob(
                jobId
            );


        List<DataQualityResponse.QualityItem> items =
            repository.findQualityItems(
                datasetId,
                jobId
            );


        /*
         * DATA_QUALITY_STAT.RECORD_COUNT의
         * 실제 합계.
         */
        long totalIssueRecordCount =
        	    items.stream()
        	        .map(
        	            DataQualityResponse
        	                .QualityItem
        	                ::getRecordCount
        	        )
        	        .filter(value -> value != null)
        	        .mapToLong(Long::longValue)
        	        .sum();


        	long affectedFileCount =
        	    items.stream()
        	        .filter(
        	            item ->
        	                item.getQualityType() != null
        	                &&
        	                item.getRecordCount() != null
        	                &&
        	                item.getRecordCount() > 0
        	        )
        	        .map(
        	            DataQualityResponse
        	                .QualityItem
        	                ::getDatasetFileId
        	        )
        	        .distinct()
        	        .count();


        	long qualityTypeCount =
        	    items.stream()
        	        .map(
        	            DataQualityResponse
        	                .QualityItem
        	                ::getQualityType
        	        )
        	        .filter(
        	            value ->
        	                value != null
        	                &&
        	                !value.isBlank()
        	        )
        	        .distinct()
        	        .count();


        DataQualityResponse.Summary summary =
            new DataQualityResponse.Summary(
                totalIssueRecordCount,
                affectedFileCount,
                qualityTypeCount
            );


        return new DataQualityResponse(
            dataset,
            job,
            summary,
            items
        );
    }
}