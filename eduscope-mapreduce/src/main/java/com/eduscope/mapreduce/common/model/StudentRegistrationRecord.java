package com.eduscope.mapreduce.common.model;

/**
 * OULAD studentRegistration.csv 한 행을 표현하는 모델.
 *
 * registration / unregistration 날짜는
 * 실제 달력 날짜가 아니라 강의 시작일 기준 상대 일수이다.
 */
public class StudentRegistrationRecord {

    private final String codeModule;
    private final String codePresentation;

    private final long studentId;

    // 빈 값이 존재할 수 있으므로 Integer 사용
    private final Integer registrationDay;
    private final Integer unregistrationDay;

    public StudentRegistrationRecord(
            String codeModule,
            String codePresentation,
            long studentId,
            Integer registrationDay,
            Integer unregistrationDay) {

        this.codeModule = codeModule;
        this.codePresentation = codePresentation;
        this.studentId = studentId;
        this.registrationDay = registrationDay;
        this.unregistrationDay = unregistrationDay;
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

    public Integer getRegistrationDay() {
        return registrationDay;
    }

    public Integer getUnregistrationDay() {
        return unregistrationDay;
    }
}