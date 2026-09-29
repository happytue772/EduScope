package com.eduscope.loader.analysis.dto;

import java.math.BigDecimal;

public class ActivityResultStatCsvRow {

    private String codeModule;
    private String codePresentation;
    private String finalResult;

    private Long studentCount;
    private Long totalClickCount;
    private BigDecimal avgClickCount;
    private BigDecimal avgActiveDayCount;

    public String getCodeModule() { return codeModule; }
    public void setCodeModule(String v) { codeModule = v; }

    public String getCodePresentation() { return codePresentation; }
    public void setCodePresentation(String v) { codePresentation = v; }

    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String v) { finalResult = v; }

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long v) { studentCount = v; }

    public Long getTotalClickCount() { return totalClickCount; }
    public void setTotalClickCount(Long v) { totalClickCount = v; }

    public BigDecimal getAvgClickCount() { return avgClickCount; }
    public void setAvgClickCount(BigDecimal v) { avgClickCount = v; }

    public BigDecimal getAvgActiveDayCount() {
        return avgActiveDayCount;
    }

    public void setAvgActiveDayCount(BigDecimal v) {
        avgActiveDayCount = v;
    }
}