package com.eduscope.loader.reference.batch;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eduscope.loader.dataset.config.DatasetProperties;
import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.repository.DatasetRepository;
import com.eduscope.loader.reference.dto.StudentInfoCsvRow;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.repository.OuladStudentRepository;

/**
 * studentInfo.csv에서 학생 자체 정보만 분리하여
 * OULAD_STUDENT Entity로 변환한다.
 */
@Component
@StepScope
public class OuladStudentProcessor
        implements ItemProcessor<StudentInfoCsvRow, OuladStudent> {

    private final DatasetRepository datasetRepository;
    private final DatasetProperties datasetProperties;
    private final OuladStudentRepository studentRepository;

    /*
     * studentInfo에는 동일 학생이 여러 강의에 등장할 수 있으므로
     * 같은 Job 실행 안에서 중복 INSERT를 방지한다.
     */
    private final Set<Long> seenStudentIds =
            new HashSet<>();

    private Dataset dataset;

    public OuladStudentProcessor(
            DatasetRepository datasetRepository,
            DatasetProperties datasetProperties,
            OuladStudentRepository studentRepository) {

        this.datasetRepository = datasetRepository;
        this.datasetProperties = datasetProperties;
        this.studentRepository = studentRepository;
    }

    @Override
    public OuladStudent process(StudentInfoCsvRow item) {

        Long sourceStudentId =
                item.getSourceStudentId();

        if (sourceStudentId == null) {
            throw new IllegalArgumentException(
                    "id_student 값이 없습니다."
            );
        }

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

        // 현재 Job 내 중복
        if (!seenStudentIds.add(sourceStudentId)) {
            return null;
        }

        // 이전 적재 데이터와 중복
        boolean alreadyExists =
                studentRepository
                    .findByDatasetAndSourceStudentId(
                            dataset,
                            sourceStudentId
                    )
                    .isPresent();

        if (alreadyExists) {
            return null;
        }

        OuladStudent student =
                new OuladStudent();

        student.setDataset(dataset);
        student.setSourceStudentId(sourceStudentId);
        student.setCreatedAt(LocalDateTime.now());

        return student;
    }
}