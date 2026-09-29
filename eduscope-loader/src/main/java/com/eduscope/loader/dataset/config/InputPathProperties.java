package com.eduscope.loader.dataset.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * EduScope 원본/분석 파일 경로 설정.
 */
@Component
@ConfigurationProperties(prefix = "eduscope.input")
public class InputPathProperties {

    private String rawPath;
    private String analysisPath;

    public String getRawPath() {
        return rawPath;
    }

    public void setRawPath(String rawPath) {
        this.rawPath = rawPath;
    }

    public String getAnalysisPath() {
        return analysisPath;
    }

    public void setAnalysisPath(String analysisPath) {
        this.analysisPath = analysisPath;
    }
}