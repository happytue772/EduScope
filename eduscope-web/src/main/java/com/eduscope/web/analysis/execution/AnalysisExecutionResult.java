package com.eduscope.web.analysis.execution;

/**
 * 실제 Hadoop 실행 결과.
 */
public class AnalysisExecutionResult {

    private final boolean success;

    private final int exitCode;

    private final long outputRecordCount;

    private final long processingTimeMs;

    private final String errorStep;

    private final String message;


    private AnalysisExecutionResult(
            boolean success,
            int exitCode,
            long outputRecordCount,
            long processingTimeMs,
            String errorStep,
            String message) {

        this.success = success;

        this.exitCode = exitCode;

        this.outputRecordCount =
            outputRecordCount;

        this.processingTimeMs =
            processingTimeMs;

        this.errorStep =
            errorStep;

        this.message =
            message;
    }


    public static AnalysisExecutionResult success(
            int exitCode,
            long outputRecordCount,
            long processingTimeMs,
            String message) {

        return new AnalysisExecutionResult(
            true,
            exitCode,
            outputRecordCount,
            processingTimeMs,
            null,
            message
        );
    }


    public static AnalysisExecutionResult failure(
            int exitCode,
            long processingTimeMs,
            String errorStep,
            String message) {

        return new AnalysisExecutionResult(
            false,
            exitCode,
            0L,
            processingTimeMs,
            errorStep,
            message
        );
    }


    public boolean isSuccess() {
        return success;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long getOutputRecordCount() {
        return outputRecordCount;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public String getErrorStep() {
        return errorStep;
    }

    public String getMessage() {
        return message;
    }
}