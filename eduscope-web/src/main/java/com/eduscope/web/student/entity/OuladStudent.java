package com.eduscope.web.student.entity;

import java.time.LocalDateTime;

import com.eduscope.web.dataset.entity.Dataset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * OULAD 학생 기준정보 Entity.
 * Oracle OULAD_STUDENT 테이블과 연결된다.
 */
@Entity
@Table(name = "OULAD_STUDENT")
public class OuladStudent {

    @Id
    @Column(name = "STUDENT_ID", nullable = false)
    private Long studentId;

    /**
     * OULAD_STUDENT.DATASET_ID
     * → DATASET.DATASET_ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private Dataset dataset;

    /**
     * OULAD 원본 id_student.
     */
    @Column(name = "SOURCE_STUDENT_ID", nullable = false)
    private Long sourceStudentId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected OuladStudent() {
        // JPA 기본 생성자
    }

    public Long getStudentId() {
        return studentId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Long getSourceStudentId() {
        return sourceStudentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}