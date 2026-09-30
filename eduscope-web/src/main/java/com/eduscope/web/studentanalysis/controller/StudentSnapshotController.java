package com.eduscope.web.studentanalysis.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.studentanalysis.dto.StudentSnapshotPageResponse;
import com.eduscope.web.studentanalysis.service.StudentSnapshotService;

/**
 * Demo Snapshot 생성 전용 읽기 API.
 *
 * 기존 학생 분석 화면 API와 분리하며
 * DEMO_ADMIN / ADMIN만 접근할 수 있다.
 */
@RestController
@RequestMapping("/api/admin/snapshot")
@PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
public class StudentSnapshotController {

    private final StudentSnapshotService service;

    public StudentSnapshotController(
            StudentSnapshotService service) {

        this.service = service;
    }

    /**
     * 학생 상세 데이터를 최대 500명 단위로 반환한다.
     */
    @GetMapping("/students")
    public StudentSnapshotPageResponse getStudents(
            @RequestParam(
                required = false,
                defaultValue = "0"
            )
            Integer offset,

            @RequestParam(
                required = false,
                defaultValue = "500"
            )
            Integer limit) {

        return service.getPage(
            offset,
            limit
        );
    }
}
