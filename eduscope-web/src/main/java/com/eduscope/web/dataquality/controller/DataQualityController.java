package com.eduscope.web.dataquality.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.dataquality.dto.DataQualityResponse;
import com.eduscope.web.dataquality.service.DataQualityService;

/**
 * 데이터 품질 분석 REST API.
 */
@RestController
@RequestMapping(
    "/api/analysis/data-quality"
)
public class DataQualityController {

    private final DataQualityService service;

    public DataQualityController(
            DataQualityService service) {

        this.service = service;
    }


    @GetMapping("/summary")
    public DataQualityResponse getSummary(
            @RequestParam Long datasetId) {

        return service.getSummary(
            datasetId
        );
    }
}