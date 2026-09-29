package com.eduscope.web.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.web.user.entity.AppUser;

/**
 * APP_USER 조회 Repository.
 */
public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    /**
     * Spring Security 로그인 시 사용.
     */
    Optional<AppUser> findByLoginId(
        String loginId
    );
}