package com.eduscope.mapreduce.common.model;

/**
 * OULAD studentVle.csv의 한 행을 표현하는 모델.
 *
 * 원본 컬럼:
 * code_module, code_presentation, id_student,
 * id_site, date, sum_click
 */
public class StudentVleRecord {

    private final String codeModule;
    private final String codePresentation;

    private final long studentId;
    private final long siteId;

    // OULAD의 date는 실제 날짜가 아닌 강의 시작일 기준 상대 일수
    private final int relativeDay;

    private final long sumClick;

    public StudentVleRecord(
            String codeModule,
            String codePresentation,
            long studentId,
            long siteId,
            int relativeDay,
            long sumClick) {

        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.studentId = studentId;
        this.siteId = siteId;
        this.relativeDay = relativeDay;
        this.sumClick = sumClick;
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

    public long getSiteId() {
        return siteId;
    }

    public int getRelativeDay() {
        return relativeDay;
    }

    public long getSumClick() {
        return sumClick;
    }
}