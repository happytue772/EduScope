package com.eduscope.web.student.dto;

import com.eduscope.web.student.entity.StudentRegistration;

/**
 * 학생 등록정보 REST 응답 DTO.
 */
public class StudentRegistrationResponse {

    private final Long studentCourseId;

    private final Long studentId;
    private final Long sourceStudentId;

    private final Long coursePresentationId;
    private final String codeModule;
    private final String codePresentation;

    private final Integer registrationDay;
    private final Integer unregistrationDay;

    public StudentRegistrationResponse(
            Long studentCourseId,
            Long studentId,
            Long sourceStudentId,
            Long coursePresentationId,
            String codeModule,
            String codePresentation,
            Integer registrationDay,
            Integer unregistrationDay) {

        this.studentCourseId = studentCourseId;
        this.studentId = studentId;
        this.sourceStudentId = sourceStudentId;
        this.coursePresentationId = coursePresentationId;
        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.registrationDay = registrationDay;
        this.unregistrationDay = unregistrationDay;
    }

    /**
     * Entity → React 응답 DTO 변환.
     */
    public static StudentRegistrationResponse from(
            StudentRegistration registration) {

        return new StudentRegistrationResponse(
            registration.getStudentCourseId(),

            registration.getStudentCourse()
                .getStudent()
                .getStudentId(),

            registration.getStudentCourse()
                .getStudent()
                .getSourceStudentId(),

            registration.getStudentCourse()
                .getCoursePresentation()
                .getCoursePresentationId(),

            registration.getStudentCourse()
                .getCoursePresentation()
                .getCodeModule(),

            registration.getStudentCourse()
                .getCoursePresentation()
                .getCodePresentation(),

            registration.getRegistrationDay(),
            registration.getUnregistrationDay()
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

    public Integer getRegistrationDay() {
        return registrationDay;
    }

    public Integer getUnregistrationDay() {
        return unregistrationDay;
    }
}