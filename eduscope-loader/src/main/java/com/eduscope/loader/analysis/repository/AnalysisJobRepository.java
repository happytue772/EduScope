package com.eduscope.loader.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.dataset.entity.Dataset;

/**
 * ANALYSIS_JOB Repository.
 */
public interface AnalysisJobRepository
        extends JpaRepository<AnalysisJob, Long> {

    List<AnalysisJob>
        findByDatasetAndAnalysisTypeOrderByJobIdDesc(
            Dataset dataset,
            String analysisType
            );
            
            boolean existsByDatasetAndAnalysisTypeAndHdfsOutputPath(
                    Dataset dataset,
                    String analysisType,
                    String hdfsOutputPath
        );
            Optional<AnalysisJob>
            findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                Dataset dataset,
                String analysisType,
                String status
            );
}