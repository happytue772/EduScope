package com.eduscope.web.audit.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.web.audit.entity.AuditLog;

/**
 * AUDIT_LOG 조회 / 보존 정리 Repository.
 */
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    /** 목록 DTO에서 사용하는 사용자 로그인 ID를 한 번에 조회한다. */
    @Override
    @EntityGraph(attributePaths = "user")
    Page<AuditLog> findAll(Pageable pageable);

    /**
     * 사용자별 Audit 조회.
     */
    @EntityGraph(attributePaths = "user")
    Page<AuditLog> findByUserId(
        Long userId,
        Pageable pageable
    );


    /**
     * Action Type별 Audit 조회.
     */
    @EntityGraph(attributePaths = "user")
    Page<AuditLog> findByActionType(
        String actionType,
        Pageable pageable
    );


    /**
     * 특정 시각 이전의 Audit Log 건수.
     *
     * 실제 삭제 전에 Preview 용도로 사용한다.
     */
    long countByCreatedAtBefore(
        LocalDateTime before
    );


    /**
     * 특정 시각 이전 Audit Log를 일괄 삭제한다.
     *
     * 개별 행 삭제가 아니라
     * 보존기간 기반 Purge 용도다.
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Query("""
        delete from AuditLog a
        where a.createdAt < :before
        """)
    int deleteCreatedBefore(
        @Param("before")
        LocalDateTime before
    );
}
