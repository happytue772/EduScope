package com.eduscope.loader.reference.dto;

/**
 * studentInfo.csv 한 행을 표현하는 DTO.
 */
public class StudentInfoCsvRow {

    private String codeModule;
    private String codePresentation;
    private Long sourceStudentId;

    private String gender;
    private String region;
    private String highestEducation;
    private String imdBand;
    private String ageBand;

    private Integer numOfPrevAttempts;
    private Integer studiedCredits;

    private String disability;
    private String finalResult;

    public StudentInfoCsvRow() {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getImdBand() {
        return imdBand;
    }

    public void setImdBand(String imdBand) {
        this.imdBand = imdBand;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public void setAgeBand(String ageBand) {
        this.ageBand = ageBand;
    }

    public Integer getNumOfPrevAttempts() {
        return numOfPrevAttempts;
    }

    public void setNumOfPrevAttempts(Integer numOfPrevAttempts) {
        this.numOfPrevAttempts = numOfPrevAttempts;
    }

    public Integer getStudiedCredits() {
        return studiedCredits;
    }

    public void setStudiedCredits(Integer studiedCredits) {
        this.studiedCredits = studiedCredits;
    }

    public String getDisability() {
        return disability;
    }

    public void setDisability(String disability) {
        this.disability = disability;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }
}