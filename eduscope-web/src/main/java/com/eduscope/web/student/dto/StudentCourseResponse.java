package com.eduscope.web.student.dto;

import com.eduscope.web.student.entity.StudentCourse;

/**
 * 학생 수강정보 REST 응답 DTO.
 */
public class StudentCourseResponse {

    private final Long studentCourseId;
    private final Long studentId;
    private final Long sourceStudentId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final String gender;
    private final String region;
    private final String highestEducation;
    private final String imdBand;
    private final String ageBand;

    private final Integer numOfPrevAttempts;
    private final Integer studiedCredits;

    private final String disability;
    private final String finalResult;

    public StudentCourseResponse(
            Long studentCourseId,
            Long studentId,
            Long sourceStudentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            String gender,
            String region,
            String highestEducation,
            String imdBand,
            String ageBand,
            Integer numOfPrevAttempts,
            Integer studiedCredits,
            String disability,
            String finalResult) {

        this.studentCourseId = studentCourseId;
        this.studentId = studentId;
        this.sourceStudentId = sourceStudentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.gender = gender;
        this.region = region;
        this.highestEducation = highestEducation;
        this.imdBand = imdBand;
        this.ageBand = ageBand;
        this.numOfPrevAttempts = numOfPrevAttempts;
        this.studiedCredits = studiedCredits;
        this.disability = disability;
        this.finalResult = finalResult;
    }

    public static StudentCourseResponse from(
            StudentCourse course) {

        return new StudentCourseResponse(
            course.getStudentCourseId(),

            course.getStudent().getStudentId(),
            course.getStudent().getSourceStudentId(),

            course.getCoursePresentation()
                  .getCoursePresentationId(),

            course.getCoursePresentation()
                  .getCodeModule(),

            course.getCoursePresentation()
                  .getCodePresentation(),

            course.getGender(),
            course.getRegion(),
            course.getHighestEducation(),
            course.getImdBand(),
            course.getAgeBand(),
            course.getNumOfPrevAttempts(),
            course.getStudiedCredits(),
            course.getDisability(),
            course.getFinalResult()
        );
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

    public Long getCoursePresentationId() {
        return coursePresentationId;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public String getGender() {
        return gender;
    }

    public String getRegion() {
        return region;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public String getImdBand() {
        return imdBand;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public Integer getNumOfPrevAttempts() {
        return numOfPrevAttempts;
    }

    public Integer getStudiedCredits() {
        return studiedCredits;
    }

    public String getDisability() {
        return disability;
    }

    public String getFinalResult() {
        return finalResult;
    }
}