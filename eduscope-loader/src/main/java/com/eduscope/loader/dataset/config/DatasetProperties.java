package com.eduscope.loader.dataset.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yml의 EduScope Dataset 설정값을 관리한다.
 * 데이터셋 명칭/버전을 코드에 하드코딩하지 않는다.
 */
@Component
@ConfigurationProperties(prefix = "eduscope.dataset")
public class DatasetProperties {

    private String displayName;
    private String sourceName;
    private String datasetVersion;
    private String schemaVersion;
    private String sourceUrl;
    private String description;
    private String hdfsBasePath;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHdfsBasePath() {
        return hdfsBasePath;
    }

    public void setHdfsBasePath(String hdfsBasePath) {
        this.hdfsBasePath = hdfsBasePath;
    }
}