package com.eduscope.web.analysis.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 실제 Hadoop 실행에 필요한 실행 계획.
 */
public class AnalysisExecutionPlan {

    private final Long jobId;

    private final String analysisType;

    private final String driverClass;

    private final List<String> arguments;

    private final List<String> inputPaths;

    private final List<String> outputPaths;

    private final List<String> temporaryPaths;


    public AnalysisExecutionPlan(
            Long jobId,
            String analysisType,
            String driverClass,
            List<String> arguments,
            List<String> inputPaths,
            List<String> outputPaths,
            List<String> temporaryPaths) {

        this.jobId = jobId;

        this.analysisType = analysisType;

        this.driverClass = driverClass;

        this.arguments =
            Collections.unmodifiableList(
                new ArrayList<>(arguments)
            );

        this.inputPaths =
            Collections.unmodifiableList(
                new ArrayList<>(inputPaths)
            );

        this.outputPaths =
            Collections.unmodifiableList(
                new ArrayList<>(outputPaths)
            );

        this.temporaryPaths =
            Collections.unmodifiableList(
                new ArrayList<>(temporaryPaths)
            );
    }


    public Long getJobId() {
        return jobId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public List<String> getInputPaths() {
        return inputPaths;
    }

    public List<String> getOutputPaths() {
        return outputPaths;
    }

    public List<String> getTemporaryPaths() {
        return temporaryPaths;
    }
}