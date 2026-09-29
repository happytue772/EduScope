package com.eduscope.web.audit.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.audit.dto.AuditLogResponse;
import com.eduscope.web.audit.entity.AuditLog;
import com.eduscope.web.audit.repository.AuditLogRepository;

/**
 * Audit Log 조회 / 기록 / 보존 정리 Service.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository repository;


    public AuditLogService(
            AuditLogRepository repository) {

        this.repository = repository;
    }


    /**
     * 실제 관리 이벤트를 기록한다.
     */
    @Transactional
    public void record(
            Long userId,
            String actionType,
            String targetType,
            String targetId,
            String requestUri,
            String description,
            String ipAddress) {

        AuditLog log =
            new AuditLog(
                userId,
                actionType,
                targetType,
                targetId,
                requestUri,
                description,
                ipAddress
            );


        repository.save(log);
    }


    /**
     * 최신 Audit부터 조회.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogs(
            int page,
            int size) {

        return repository
            .findAll(
                PageRequest.of(
                    page,
                    size,
                    Sort.by(
                        "createdAt"
                    ).descending()
                )
            )
            .map(
                AuditLogResponse::from
            );
    }


    /**
     * 특정 날짜 이전 삭제 대상 건수 조회.
     *
     * 실제 데이터 삭제는 하지 않는다.
     */
    @Transactional(readOnly = true)
    public long countLogsBefore(
            LocalDateTime before) {

        return repository
            .countByCreatedAtBefore(
                before
            );
    }


    /**
     * 특정 날짜 이전 Audit Log 정리.
     *
     * 보존기간 기반으로만 일괄 삭제하며
     * 개별 Audit 삭제 기능은 제공하지 않는다.
     *
     * 삭제 완료 후 AUDIT_PURGE 기록을 새로 남긴다.
     */
    @Transactional
    public int purgeLogsBefore(
            LocalDateTime before,
            String adminLoginId,
            String requestUri,
            String ipAddress) {

        /*
         * 먼저 실제 삭제 대상 건수를 확인한다.
         */
        long targetCount =
            repository
                .countByCreatedAtBefore(
                    before
                );


        if (targetCount == 0) {

            return 0;
        }


        /*
         * Bulk DELETE 실행.
         */
        int deletedCount =
            repository
                .deleteCreatedBefore(
                    before
                );


        /*
         * Purge 자체도 다시 Audit에 남긴다.
         *
         * 현재 전달받은 3개 파일만으로는
         * APP_USER 조회 Repository가 확인되지 않았으므로
         * USER_ID를 임의 생성하지 않는다.
         *
         * 관리자 Login ID는 description에 기록한다.
         */
        record(
            null,
            "AUDIT_PURGE",
            "AUDIT_LOG",
            before.toLocalDate().toString(),
            requestUri,
            "관리자="
                + adminLoginId
                + ", "
                + before.toLocalDate()
                + " 이전 Audit Log "
                + deletedCount
                + "건 정리",
            ipAddress
        );


        return deletedCount;
    }
}