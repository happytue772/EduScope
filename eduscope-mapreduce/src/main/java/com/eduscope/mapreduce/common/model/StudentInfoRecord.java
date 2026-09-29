package com.eduscope.mapreduce.common.model;

/**
 * OULAD studentInfo.csv 한 행을 표현하는 모델.
 *
 * 학생 자체의 로그인 계정이 아니라
 * OULAD의 익명 학습 분석 대상 데이터이다.
 */
public class StudentInfoRecord {

    private final String codeModule;
    private final String codePresentation;

    private final long studentId;

    private final String gender;
    private final String region;
    private final String highestEducation;
    private final String imdBand;
    private final String ageBand;

    private final int numOfPrevAttempts;
    private final int studiedCredits;

    private final String disability;

    // Pass / Fail / Withdrawn / Distinction
    private final String finalResult;

    public StudentInfoRecord(
            String codeModule,
            String codePresentation,
            long studentId,
            String gender,
            String region,
            String highestEducation,
            String imdBand,
            String ageBand,
            int numOfPrevAttempts,
            int studiedCredits,
            String disability,
            String finalResult) {

        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.studentId = studentId;
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

    public String getCodeModule() {
        return codeModule;
    }

    public String getCodePresentation() {
        return codePresentation;
    }

    public long getStudentId() {
        return studentId;
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

    public int getNumOfPrevAttempts() {
        return numOfPrevAttempts;
    }

    public int getStudiedCredits() {
        return studiedCredits;
    }

    public String getDisability() {
        return disability;
    }

    public String getFinalResult() {
        return finalResult;
    }
}