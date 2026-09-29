package com.eduscope.web.activityanalysis.dto;

import java.util.List;

/**
 * VLE 학습활동 분석 화면 응답 DTO.
 *
 * 기존 VLE_MATERIAL + VLE_ACTIVITY_STAT 데이터를
 * 프론트에서 사용하기 좋은 형태로 조합한다.
 */
public class ActivityAnalysisResponse {

    private final CourseInfo course;
    private final Long jobId;
    private final List<MaterialActivity> materials;

    public ActivityAnalysisResponse(
            CourseInfo course,
            Long jobId,
            List<MaterialActivity> materials) {

        this.course = course;
        this.jobId = jobId;
        this.materials = materials;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public Long getJobId() {
        return jobId;
    }

    public List<MaterialActivity> getMaterials() {
        return materials;
    }

    /**
     * 선택된 강의 정보.
     */
    public static class CourseInfo {

        private final Long coursePresentationId;
        private final Long datasetId;
        private final String codeModule;
        private final String codePresentation;

        public CourseInfo(
                Long coursePresentationId,
                Long datasetId,
                String codeModule,
                String codePresentation) {

            this.coursePresentationId = coursePresentationId;
            this.datasetId = datasetId;
            this.codeModule = codeModule;
            this.codePresentation = codePresentation;
        }

        public Long getCoursePresentationId() {
            return coursePresentationId;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public String getCodeModule() {
            return codeModule;
        }

        public String getCodePresentation() {
            return codePresentation;
        }
    }

    /**
     * VLE 자료별 실제 활동 집계.
     */
    public static class MaterialActivity {

        private final Long vleMaterialId;
        private final Long sourceSiteId;
        private final String activityType;

        private final Integer weekFrom;
        private final Integer weekTo;

        private final Long totalClickCount;
        private final Long activeStudentCount;
        private final Long activeDayCount;

        public MaterialActivity(
                Long vleMaterialId,
                Long sourceSiteId,
                String activityType,
                Integer weekFrom,
                Integer weekTo,
                Long totalClickCount,
                Long activeStudentCount,
                Long activeDayCount) {

            this.vleMaterialId = vleMaterialId;
            this.sourceSiteId = sourceSiteId;
            this.activityType = activityType;
            this.weekFrom = weekFrom;
            this.weekTo = weekTo;
            this.totalClickCount = totalClickCount;
            this.activeStudentCount = activeStudentCount;
            this.activeDayCount = activeDayCount;
        }

        public Long getVleMaterialId() {
            return vleMaterialId;
        }

        public Long getSourceSiteId() {
            return sourceSiteId;
        }

        public String getActivityType() {
            return activityType;
        }

        public Integer getWeekFrom() {
            return weekFrom;
        }

        public Integer getWeekTo() {
            return weekTo;
        }

        public Long getTotalClickCount() {
            return totalClickCount;
        }

        public Long getActiveStudentCount() {
            return activeStudentCount;
        }

        public Long getActiveDayCount() {
            return activeDayCount;
        }
    }
}