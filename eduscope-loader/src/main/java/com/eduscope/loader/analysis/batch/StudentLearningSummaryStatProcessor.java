package com.eduscope.loader.analysis.batch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eduscope.loader.analysis.dto.StudentLearningSummaryStatCsvRow;
import com.eduscope.loader.analysis.entity.AnalysisJob;
import com.eduscope.loader.analysis.entity.StudentLearningSummaryStat;
import com.eduscope.loader.analysis.repository.AnalysisJobRepository;
import com.eduscope.loader.analysis.repository.StudentLearningSummaryStatRepository;
import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.entity.StudentCourse;
import com.eduscope.loader.reference.repository.CoursePresentationRepository;
import com.eduscope.loader.reference.repository.OuladStudentRepository;
import com.eduscope.loader.reference.repository.StudentCourseRepository;

/**
 * student-learning-summary.tsv의 원본 키를
 * STUDENT_COURSE_ID로 변환한다.
 */
@Component
@StepScope
public class StudentLearningSummaryStatProcessor
        implements ItemProcessor<
            StudentLearningSummaryStatCsvRow,
            StudentLearningSummaryStat> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;

    private final AnalysisJobRepository analysisJobRepository;

    private final StudentLearningSummaryStatRepository statRepository;

    private final CoursePresentationRepository courseRepository;
    private final OuladStudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;

    // jobId 지정 시 정확한 ANALYSIS_JOB을 선택한다.
    private final Long requestedJobId;

    private Dataset dataset;
    private AnalysisJob analysisJob;

    private final Map<String, CoursePresentation> courseCache =
            new HashMap<>();

    private final Map<Long, OuladStudent> studentCache =
            new HashMap<>();

    private final Map<String, StudentCourse> studentCourseCache =
            new HashMap<>();

    private Set<Long> existingStudentCourseIds;

    public StudentLearningSummaryStatProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            AnalysisJobRepository analysisJobRepository,
            StudentLearningSummaryStatRepository statRepository,
            CoursePresentationRepository courseRepository,
            OuladStudentRepository studentRepository,
            StudentCourseRepository studentCourseRepository,
            @Value("#{jobParameters['jobId']}")
            Long requestedJobId
) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.analysisJobRepository = analysisJobRepository;
        this.statRepository = statRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.requestedJobId = requestedJobId;
    }

    @Override
    public StudentLearningSummaryStat process(
            StudentLearningSummaryStatCsvRow item) {

        initialize();

        String courseKey =
                item.getCodeModule()
                + "|"
                + item.getCodePresentation();

        CoursePresentation course =
                courseCache.computeIfAbsent(
                    courseKey,
                    key -> courseRepository
                        .findByDatasetAndCodeModuleAndCodePresentation(
                            dataset,
                            item.getCodeModule(),
                            item.getCodePresentation()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "COURSE_PRESENTATION 없음: "
                                + courseKey
                            )
                        )
                );

        OuladStudent student =
                studentCache.computeIfAbsent(
                    item.getSourceStudentId(),
                    sourceId -> studentRepository
                        .findByDatasetAndSourceStudentId(
                            dataset,
                            sourceId
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "OULAD_STUDENT 없음: "
                                + sourceId
                            )
                        )
                );

        String studentCourseKey =
                course.getCoursePresentationId()
                + "|"
                + student.getStudentId();

        StudentCourse studentCourse =
                studentCourseCache.computeIfAbsent(
                    studentCourseKey,
                    key -> studentCourseRepository
                        .findByCoursePresentationAndStudent(
                            course,
                            student
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "STUDENT_COURSE 없음: "
                                + courseKey
                                + "|"
                                + item.getSourceStudentId()
                            )
                        )
                );

        Long studentCourseId =
                studentCourse.getStudentCourseId();

        /*
         * DB 재실행 및 TSV 자체 중복 방지.
         */
        if (!existingStudentCourseIds.add(
                studentCourseId)) {

            return null;
        }

        StudentLearningSummaryStat stat =
                new StudentLearningSummaryStat();

        stat.setJobId(
            analysisJob.getJobId()
        );

        stat.setStudentCourseId(
            studentCourseId
        );

        stat.setTotalClickCount(
            item.getTotalClickCount()
        );

        stat.setActiveDayCount(
            item.getActiveDayCount()
        );

        stat.setUsedMaterialCount(
            item.getUsedMaterialCount()
        );

        stat.setSubmittedAssessmentCount(
            item.getSubmittedAssessmentCount()
        );

        stat.setAvgAssessmentScore(
            item.getAvgAssessmentScore()
        );

        stat.setFailedAssessmentCount(
            item.getFailedAssessmentCount()
        );

        stat.setFirstActivityDay(
            item.getFirstActivityDay()
        );

        stat.setLastActivityDay(
            item.getLastActivityDay()
        );

        stat.setCreatedAt(
            LocalDateTime.now()
        );

        return stat;
    }

    private void initialize() {

        if (analysisJob == null) {

            if (requestedJobId != null) {
                analysisJob = analysisJobRepository
                    .findById(requestedJobId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ANALYSIS_JOB을 찾을 수 없습니다."
                            + " / jobId=" + requestedJobId
                        )
                    );

                if (!"STUDENT_LEARNING_SUMMARY".equals(analysisJob.getAnalysisType())) {
                    throw new IllegalStateException(
                        "STUDENT_LEARNING_SUMMARY 분석 Job이 아닙니다."
                        + " / jobId=" + analysisJob.getJobId()
                        + " / analysisType=" + analysisJob.getAnalysisType()
                    );
                }

                if (!"SUCCESS".equals(analysisJob.getStatus())) {
                    throw new IllegalStateException(
                        "SUCCESS 상태의 ANALYSIS_JOB이 아닙니다."
                        + " / jobId=" + analysisJob.getJobId()
                        + " / status=" + analysisJob.getStatus()
                    );
                }

                dataset = analysisJob.getDataset();

            } else {
                // 기존 최신 SUCCESS Job 선택 방식 유지.
                if (dataset == null) {
                    dataset = datasetRepository
                        .findBySourceNameAndDatasetVersion(
                            datasetProperties.getSourceName(),
                            datasetProperties.getDatasetVersion()
                        )
                        .orElseThrow(() ->
                            new IllegalStateException(
                                "DATASET을 찾을 수 없습니다."
                            )
                        );
                }

                analysisJob = analysisJobRepository
                    .findFirstByDatasetAndAnalysisTypeAndStatusOrderByJobIdDesc(
                        dataset,
                        "STUDENT_LEARNING_SUMMARY",
                        "SUCCESS"
                    )
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "STUDENT_LEARNING_SUMMARY ANALYSIS_JOB 없음"
                        )
                    );
            }
        }

        if (existingStudentCourseIds == null) {

            existingStudentCourseIds =
                new HashSet<>(
                    statRepository
                        .findStudentCourseIdsByJobId(
                            analysisJob.getJobId()
                        )
                );
        }
    }
}