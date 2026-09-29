package com.eduscope.web.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.web.analysis.entity.AnalysisJob;


/**
 * ANALYSIS_JOB Repository.
 *
 * 기존 조회 기능 +
 * Job 생성 검증 기능 +
 * System Health 최신 Job 조회를 담당한다.
 */
public interface AnalysisJobRepository
        extends JpaRepository<AnalysisJob, Long> {


    /**
     * 최신 분석부터 전체 조회.
     */
    List<AnalysisJob>
        findAllByOrderByJobIdDesc();


    /**
     * 가장 최근 Analysis Job 1건 조회.
     *
     * System Health에서
     * 최신 Job 상태를 조회하기 위해 사용한다.
     */
    Optional<AnalysisJob>
        findTopByOrderByJobIdDesc();


    /**
     * 분석 종류별 조회.
     */
    List<AnalysisJob>
        findByAnalysisTypeOrderByJobIdDesc(
            String analysisType
        );


    /**
     * 실제 사용 가능한 Dataset인지 확인.
     *
     * 논리 삭제된 Dataset에는
     * 신규 분석 Job을 생성하지 않는다.
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM DATASET
            WHERE DATASET_ID = :datasetId
              AND IS_DELETED = 'N'
            """,
        nativeQuery = true
    )
    int countActiveDataset(
        @Param("datasetId")
        Long datasetId
    );


    /**
     * HDFS Output 경로 재사용 방지.
     */
    boolean existsByHdfsOutputPath(
        String hdfsOutputPath
    );
}