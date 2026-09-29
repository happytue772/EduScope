package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

public class ActivityResultStatId implements Serializable {

    private Long jobId;
    private Long coursePresentationId;
    private String finalResult;

    public ActivityResultStatId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ActivityResultStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(coursePresentationId,
                              that.coursePresentationId)
            && Objects.equals(finalResult,
                              that.finalResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            coursePresentationId,
            finalResult
        );
    }
}