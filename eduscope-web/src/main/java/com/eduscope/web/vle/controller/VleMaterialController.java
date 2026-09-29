package com.eduscope.web.vle.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.vle.dto.VleMaterialResponse;
import com.eduscope.web.vle.service.VleMaterialService;

/**
 * VLE_MATERIAL REST API.
 */
@RestController
@RequestMapping("/api/vle-materials")
public class VleMaterialController {

    private final VleMaterialService service;

    public VleMaterialController(
            VleMaterialService service) {

        this.service = service;
    }

    /**
     * 전체 VLE 학습자료 조회.
     */
    @GetMapping
    public Page<VleMaterialResponse> getMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getMaterials(page, size);
    }

    /**
     * VLE 학습자료 단건 조회.
     */
    @GetMapping("/{vleMaterialId}")
    public VleMaterialResponse getMaterial(
            @PathVariable Long vleMaterialId) {

        return service.getMaterial(vleMaterialId);
    }

    /**
     * 특정 강의의 VLE 자료 조회.
     */
    @GetMapping("/course/{coursePresentationId}")
    public Page<VleMaterialResponse> getMaterialsByCourse(
            @PathVariable Long coursePresentationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.getMaterialsByCourse(
            coursePresentationId,
            page,
            size
        );
    }
}