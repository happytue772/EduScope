package com.eduscope.mapreduce.common.model;

/**
 * assessments.csv 한 행.
 */
public class AssessmentRecord {

    private final String codeModule;
    private final String codePresentation;
    private final long assessmentId;
    private final String assessmentType;

    // OULAD 상대 일수이므로 NULL 가능
    private final Integer dueDay;

    private final double weight;

    public AssessmentRecord(
            String codeModule,
            String codePresentation,
            long assessmentId,
            String assessmentType,
            Integer dueDay,
            double weight) {

        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.assessmentId = assessmentId;
        this.assessmentType = assessmentType;
        this.dueDay = dueDay;
        this.weight = weight;
    }

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public long getAssessmentId() {
        return assessmentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public double getWeight() {
        return weight;
    }
}