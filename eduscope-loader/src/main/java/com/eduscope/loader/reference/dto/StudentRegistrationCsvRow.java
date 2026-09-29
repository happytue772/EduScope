package com.eduscope.loader.reference.dto;

/**
 * studentRegistration.csv 한 행을 표현하는 DTO.
 */
public class StudentRegistrationCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceStudentId;
    private Integer registrationDay;
    private Integer unregistrationDay;

    public StudentRegistrationCsvRow() {
    }

    public String getCodeModule() {
        return codeModule;
    }

    public void setCodeModule(String codeModule) {
        this.codeModule = codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public void setCodePresentation(String codePresentation) {
        this.codePresentation = codePresentation;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public void setSourceStudentId(Long sourceStudentId) {
        this.sourceStudentId = sourceStudentId;
    }

    public Integer getRegistrationDay() {
        return registrationDay;
    }

    public void setRegistrationDay(Integer registrationDay) {
        this.registrationDay = registrationDay;
    }

    public Integer getUnregistrationDay() {
        return unregistrationDay;
    }

    public void setUnregistrationDay(Integer unregistrationDay) {
        this.unregistrationDay = unregistrationDay;
    }
}