package com.eduscope.loader.analysis.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * VLE_ACTIVITY_STAT 복합 PK.
 * PK = JOB_ID + VLE_MATERIAL_ID
 */
public class VleActivityStatId implements Serializable {

    private Long jobId;
    private Long vleMaterialId;

    public VleActivityStatId() {
    }

    public VleActivityStatId(
            Long jobId,
            Long vleMaterialId) {

        this.jobId = jobId;
        this.vleMaterialId = vleMaterialId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getVleMaterialId() {
        return vleMaterialId;
    }

    public void setVleMaterialId(Long vleMaterialId) {
        this.vleMaterialId = vleMaterialId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof VleActivityStatId that)) {
            return false;
        }

        return Objects.equals(jobId, that.jobId)
            && Objects.equals(
                vleMaterialId,
                that.vleMaterialId
            );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            jobId,
            vleMaterialId
        );
    }
}