package com.eduscope.web.assessment.dto;

import java.math.BigDecimal;

import com.eduscope.web.assessment.entity.StudentAssessment;

/**
 * 학생 평가결과 REST 응답 DTO.
 */
public class StudentAssessmentResponse {

    private final Long studentAssessmentId;

    private final Long studentCourseId;
    private final Long studentId;
    private final Long sourceStudentId;

    private final Long assessmentId;
    private final Long sourceAssessmentId;

    private final String assessmentType;

    private final String codeModule;
    private final String codePresentation;

    private final Integer submittedDay;
    private final String isBanked;
    private final BigDecimal score;

    public StudentAssessmentResponse(
            Long studentAssessmentId,
            Long studentCourseId,
            Long studentId,
            Long sourceStudentId,
            Long assessmentId,
            Long sourceAssessmentId,
            String assessmentType,
            String codeModule,
            String codePresentation,
            Integer submittedDay,
            String isBanked,
            BigDecimal score) {

        this.studentAssessmentId = studentAssessmentId;
        this.studentCourseId = studentCourseId;
        this.studentId = studentId;
        this.sourceStudentId = sourceStudentId;
        this.assessmentId = assessmentId;
        this.sourceAssessmentId = sourceAssessmentId;
        this.assessmentType = assessmentType;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.submittedDay = submittedDay;
        this.isBanked = isBanked;
        this.score = score;
    }

    /**
     * Entity → React 응답 DTO.
     */
    public static StudentAssessmentResponse from(
            StudentAssessment studentAssessment) {

        return new StudentAssessmentResponse(
            studentAssessment.getStudentAssessmentId(),

            studentAssessment.getStudentCourse()
                .getStudentCourseId(),

            studentAssessment.getStudentCourse()
                .getStudent()
                .getStudentId(),

            studentAssessment.getStudentCourse()
                .getStudent()
                .getSourceStudentId(),

            studentAssessment.getAssessment()
                .getAssessmentId(),

            studentAssessment.getAssessment()
                .getSourceAssessmentId(),

            studentAssessment.getAssessment()
                .getAssessmentType(),

            studentAssessment.getStudentCourse()
                .getCoursePresentation()
                .getCodeModule(),

            studentAssessment.getStudentCourse()
                .getCoursePresentation()
                .getCodePresentation(),

            studentAssessment.getSubmittedDay(),
            studentAssessment.getIsBanked(),
            studentAssessment.getScore()
        );
    }

    public Long getStudentAssessmentId() {
        return studentAssessmentId;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Long getSourceAssessmentId() {
        return sourceAssessmentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public Integer getSubmittedDay() {
        return submittedDay;
    }

    public String getIsBanked() {
        return isBanked;
    }

    public BigDecimal getScore() {
        return score;
    }
}