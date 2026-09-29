package com.eduscope.web.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.student.dto.OuladStudentResponse;
import com.eduscope.web.student.entity.OuladStudent;
import com.eduscope.web.student.repository.OuladStudentRepository;

/**
 * OULAD 학생 조회 비즈니스 로직.
 */
@Service
@Transactional(readOnly = true)
public class OuladStudentService {

    private final OuladStudentRepository studentRepository;

    public OuladStudentService(
            OuladStudentRepository studentRepository) {

        this.studentRepository = studentRepository;
    }

    /**
     * 학생 목록 페이지 조회.
     * 대량 데이터를 한 번에 반환하지 않는다.
     */
    public Page<OuladStudentResponse> getStudents(
            int page,
            int size) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("studentId").ascending()
            );

        return studentRepository
            .findAll(pageable)
            .map(OuladStudentResponse::from);
    }

    /**
     * Oracle 내부 STUDENT_ID 기준 조회.
     */
    public OuladStudentResponse getStudent(
            Long studentId) {

        OuladStudent student =
            studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "OULAD_STUDENT를 찾을 수 없습니다. studentId="
                        + studentId
                    )
                );

        return OuladStudentResponse.from(student);
    }
}