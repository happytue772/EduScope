package com.eduscope.web.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduscope.web.user.entity.AppUserRole;
import com.eduscope.web.user.entity.AppUserRoleId;

/**
 * APP_USER_ROLE 조회 Repository.
 */
public interface AppUserRoleRepository
        extends JpaRepository<AppUserRole, AppUserRoleId> {

    /**
     * 사용자 ID 기준으로
     * Role Entity까지 함께 조회한다.
     */
    @Query("""
        select ur
        from AppUserRole ur
        join fetch ur.role
        where ur.userId = :userId
        """)
    List<AppUserRole> findAllWithRoleByUserId(
            @Param("userId") Long userId
    );


    /**
     * 현재 ACTIVE + ADMIN 사용자 수.
     *
     * 마지막 관리자 보호에 사용한다.
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM APP_USER_ROLE aur
            JOIN APP_ROLE ar
              ON ar.ROLE_ID = aur.ROLE_ID
            JOIN APP_USER au
              ON au.USER_ID = aur.USER_ID
            WHERE ar.ROLE_CODE = 'ADMIN'
              AND au.ACCOUNT_STATUS = 'ACTIVE'
            """,
        nativeQuery = true
    )
    long countActiveAdmins();
}
