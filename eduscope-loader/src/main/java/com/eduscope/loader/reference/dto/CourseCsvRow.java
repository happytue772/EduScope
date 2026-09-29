package com.eduscope.loader.reference.dto;

/**
 * courses.csv 한 행을 담는 DTO.
 */
public class CourseCsvRow {

    private String codeModule;
    private String codePresentation;
    private Integer modulePresentationLength;

    public CourseCsvRow() {
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

    public Integer getModulePresentationLength() {
        return modulePresentationLength;
    }

    public void setModulePresentationLength(
            Integer modulePresentationLength) {
        this.modulePresentationLength = modulePresentationLength;
    }
}