package com.eduscope.web.analysis.execution;

/**
 * Analysis Job 실제 실행기.
 */
public interface AnalysisJobExecutor {

    AnalysisExecutionResult execute(
        AnalysisExecutionPlan plan
    );
}