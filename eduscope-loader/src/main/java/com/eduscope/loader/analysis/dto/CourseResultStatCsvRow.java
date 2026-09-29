package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

public class CourseResultStatCsvRow {

    private String codeModule;
    private String codePresentation;

    private Long studentCount;
    private Long passCount;
    private Long failCount;
    private Long withdrawnCount;
    private Long distinctionCount;

    private BigDecimal passRate;
    private BigDecimal failRate;
    private BigDecimal withdrawnRate;
    private BigDecimal distinctionRate;

    public String getCodeModule() { return codeModule; }
    public void setCodeModule(String v) { codeModule = v; }

    public String getCodePresentation() { return codePresentation; }
    public void setCodePresentation(String v) { codePresentation = v; }

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long v) { studentCount = v; }

    public Long getPassCount() { return passCount; }
    public void setPassCount(Long v) { passCount = v; }

    public Long getFailCount() { return failCount; }
    public void setFailCount(Long v) { failCount = v; }

    public Long getWithdrawnCount() { return withdrawnCount; }
    public void setWithdrawnCount(Long v) { withdrawnCount = v; }

    public Long getDistinctionCount() { return distinctionCount; }
    public void setDistinctionCount(Long v) { distinctionCount = v; }

    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal v) { passRate = v; }

    public BigDecimal getFailRate() { return failRate; }
    public void setFailRate(BigDecimal v) { failRate = v; }

    public BigDecimal getWithdrawnRate() { return withdrawnRate; }
    public void setWithdrawnRate(BigDecimal v) { withdrawnRate = v; }

    public BigDecimal getDistinctionRate() { return distinctionRate; }
    public void setDistinctionRate(BigDecimal v) { distinctionRate = v; }
}