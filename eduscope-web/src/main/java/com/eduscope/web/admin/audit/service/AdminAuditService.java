package com.eduscope.web.admin.audit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.admin.audit.dto.AdminAuditLogResponse;
import com.eduscope.web.admin.audit.repository.AdminAuditRepository;

/**
 * 관리자 Audit Log Service.
 */
@Service
@Transactional(readOnly = true)
public class AdminAuditService {

    private final AdminAuditRepository repository;


    public AdminAuditService(
            AdminAuditRepository repository) {

        this.repository =
            repository;
    }


    public List<AdminAuditLogResponse> getAuditLogs() {

        return repository.findAll();
    }
}