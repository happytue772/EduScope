package com.eduscope.mapreduce.common.model;

/**
 * courses.csv 한 행.
 */
public class CourseRecord {

    private final String codeModule;
    private final String codePresentation;
    private final int presentationLength;

    public CourseRecord(
            String codeModule,
            String codePresentation,
            int presentationLength) {

        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.presentationLength = presentationLength;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public int getPresentationLength() {
        return presentationLength;
    }
}