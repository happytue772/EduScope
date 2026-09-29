package com.eduscope.web.student.dto;

import com.eduscope.web.student.entity.OuladStudent;

/**
 * React에 전달할 학생 기준정보 DTO.
 */
public class OuladStudentResponse {

    private final Long studentId;
    private final Long datasetId;
    private final Long sourceStudentId;

    public OuladStudentResponse(
            Long studentId,
            Long datasetId,
            Long sourceStudentId) {

        this.studentId = studentId;
        this.datasetId = datasetId;
        this.sourceStudentId = sourceStudentId;
    }

    /**
     * Entity → Response DTO.
     */
    public static OuladStudentResponse from(
            OuladStudent student) {

        return new OuladStudentResponse(
            student.getStudentId(),
            student.getDataset().getDatasetId(),
            student.getSourceStudentId()
        );
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }
}
