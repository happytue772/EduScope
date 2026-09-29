package com.eduscope.mapreduce.common.model;

/**
 * OULAD studentAssessment.csv 한 행을 표현하는 모델.
 */
public class StudentAssessmentRecord {

    private final long assessmentId;
    private final long studentId;

    // OULAD 강의 시작일 기준 상대 제출 일수
    private final int submittedDay;

    /*
     * 원본 is_banked 값은 0 / 1.
     * Parser 단계에서는 원본 의미를 유지한다.
     *
     * Oracle 적재 시 Spring Batch에서
     * 0 -> N
     * 1 -> Y
     * 로 변환할 수 있다.
     */
    private final int banked;

    // 원본에서 빈 값이 있을 수 있으므로 Double 사용
    private final Double score;

    public StudentAssessmentRecord(
            long assessmentId,
            long studentId,
            int submittedDay,
            int banked,
            Double score) {

        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.submittedDay = submittedDay;
        this.banked = banked;
        this.score = score;
    }

    public long getAssessmentId() {
        return assessmentId;
    }

    public long getStudentId() {
        return studentId;
    }

    public int getSubmittedDay() {
        return submittedDay;
    }

    public int getBanked() {
        return banked;
    }

    public Double getScore() {
        return score;
    }
}