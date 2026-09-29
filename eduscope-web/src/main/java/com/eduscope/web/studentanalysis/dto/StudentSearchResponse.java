package com.eduscope.web.studentanalysis.dto;

/**
 * 학생 검색 결과 한 행.
 */
public class StudentSearchResponse {

    private final Long studentCourseId;
    private final Long sourceStudentId;

    private final Long coursePresentationId;

    private final String codeModule;
    private final String codePresentation;
    private final String finalResult;

    public StudentSearchResponse(
            Long studentCourseId,
            Long sourceStudentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String finalResult) {

        this.studentCourseId = studentCourseId;
        this.sourceStudentId = sourceStudentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.finalResult = finalResult;
    }

    public Long getStudentCourseId() {
        return studentCourseId;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public String getFinalResult() {
        return finalResult;
    }
}