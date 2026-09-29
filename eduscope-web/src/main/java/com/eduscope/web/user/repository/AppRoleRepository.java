package com.eduscope.web.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.user.entity.AppRole;

/**
 * APP_ROLE 조회 Repository.
 */
public interface AppRoleRepository
        extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCode(
        String roleCode
    );
}