package com.eduscope.web.admin.user.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.admin.user.dto.AdminUserResponse;
import com.eduscope.web.admin.user.dto.UserAccessUpdateRequest;
import com.eduscope.web.admin.user.repository.AdminUserRepository;
import com.eduscope.web.audit.service.AuditLogService;
import com.eduscope.web.security.UserSessionService;
import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;
import com.eduscope.web.user.repository.AppUserRoleRepository;

/**
 * ADMIN 사용자 관리 Service.
 *
 * 기존 기능:
 * 1. 사용자 조회
 * 2. 계정 상태 변경
 * 3. Role 변경
 * 4. ROLE_CHANGE Audit
 *
 * 보안 보강:
 * 5. 자기 잠금 / 비활성화 방지
 * 6. 자기 ADMIN Role 제거 방지
 * 7. 마지막 ACTIVE ADMIN 보호
 * 8. 상태 / Role 변경 후 기존 Session 만료
 */
@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private static final String STATUS_ACTIVE =
        "ACTIVE";

    private static final String ROLE_ADMIN =
        "ADMIN";


    private static final Set<String>
        ALLOWED_STATUSES =
            Set.of(
                "ACTIVE",
                "LOCKED",
                "DISABLED"
            );


    private static final Set<String>
        ALLOWED_ROLES =
            Set.of(
                "VIEWER",
                "ANALYST",
                "DEMO_ADMIN",
                "ADMIN"
            );


    private final AdminUserRepository
        repository;

    private final AppUserRepository
        appUserRepository;

    private final AppUserRoleRepository
        appUserRoleRepository;

    private final AuditLogService
        auditLogService;

    private final UserSessionService
        userSessionService;


    public AdminUserService(
            AdminUserRepository repository,
            AppUserRepository appUserRepository,
            AppUserRoleRepository appUserRoleRepository,
            AuditLogService auditLogService,
            UserSessionService userSessionService) {

        this.repository =
            repository;

        this.appUserRepository =
            appUserRepository;

        this.appUserRoleRepository =
            appUserRoleRepository;

        this.auditLogService =
            auditLogService;

        this.userSessionService =
            userSessionService;
    }


    /**
     * 전체 사용자 조회.
     */
    public List<AdminUserResponse> getUsers() {

        return repository.findAllUsers();
    }


    /**
     * 사용자 계정 상태 및 Role 변경.
     */
    @Transactional
    public void updateAccess(
            Long userId,
            UserAccessUpdateRequest request,
            String adminLoginId,
            String requestUri,
            String remoteAddr) {

        /*
         * 대상 사용자 존재 확인.
         */
        if (
            !repository.existsUser(
                userId
            )
        ) {

            throw new IllegalArgumentException(
                "존재하지 않는 사용자입니다."
            );
        }


        if (
            request == null
            ||
            request.accountStatus() == null
            ||
            request.roles() == null
        ) {

            throw new IllegalArgumentException(
                "계정 상태와 Role 정보가 필요합니다."
            );
        }


        /*
         * 변경 작업을 수행하는 ADMIN.
         */
        AppUser admin =
            appUserRepository
                .findByLoginId(
                    adminLoginId
                )
                .orElseThrow(() ->
                    new IllegalStateException(
                        "관리자 계정을 찾을 수 없습니다."
                    )
                );


        /*
         * 대상 사용자.
         * Session 만료 시 loginId가 필요하다.
         */
        AppUser targetUser =
            appUserRepository
                .findById(
                    userId
                )
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "존재하지 않는 사용자입니다."
                    )
                );


        /*
         * 계정 상태 정규화.
         */
        String status =
            request
                .accountStatus()
                .trim()
                .toUpperCase();


        if (
            !ALLOWED_STATUSES.contains(
                status
            )
        ) {

            throw new IllegalArgumentException(
                "잘못된 계정 상태입니다."
            );
        }


        /*
         * Role 정규화.
         */
        List<String> roles =
            request
                .roles()
                .stream()
                .map(role -> {

                    if (role == null) {

                        throw new IllegalArgumentException(
                            "Role에 null 값이 포함되어 있습니다."
                        );
                    }


                    return role
                        .trim()
                        .toUpperCase();
                })
                .distinct()
                .sorted()
                .toList();


        if (
            !ALLOWED_ROLES.containsAll(
                roles
            )
        ) {

            throw new IllegalArgumentException(
                "잘못된 Role이 포함되어 있습니다."
            );
        }


        /*
         * ACTIVE 계정은
         * 최소 하나의 Role이 필요하다.
         */
        if (
            STATUS_ACTIVE.equals(
                status
            )
            &&
            roles.isEmpty()
        ) {

            throw new IllegalArgumentException(
                "ACTIVE 사용자는 최소 하나의 Role이 필요합니다."
            );
        }


        /*
         * 변경 전 실제 DB 상태.
         */
        String previousStatus =
            repository
                .findAccountStatus(
                    userId
                );


        List<String> previousRoles =
            repository
                .findRoleCodes(
                    userId
                )
                .stream()
                .sorted()
                .toList();


        /*
         * 아무 변경도 없으면
         * UPDATE / Audit / Session 만료를 하지 않는다.
         */
        if (
            previousStatus.equals(
                status
            )
            &&
            previousRoles.equals(
                roles
            )
        ) {

            return;
        }


        /*
         * =====================================================
         * 보안 1.
         * 자기 계정 LOCKED / DISABLED 방지.
         * =====================================================
         */
        boolean changingSelf =
            admin
                .getUserId()
                .equals(
                    userId
                );


        if (
            changingSelf
            &&
            !STATUS_ACTIVE.equals(
                status
            )
        ) {

            throw new IllegalArgumentException(
                "현재 로그인한 관리자 자신의 계정을 "
                + "LOCKED 또는 DISABLED 상태로 변경할 수 없습니다."
            );
        }


        /*
         * =====================================================
         * 보안 2.
         * 자기 ADMIN Role 제거 방지.
         * =====================================================
         */
        if (
            changingSelf
            &&
            !roles.contains(
                ROLE_ADMIN
            )
        ) {

            throw new IllegalArgumentException(
                "현재 로그인한 관리자 자신의 "
                + "ADMIN Role을 제거할 수 없습니다."
            );
        }


        /*
         * =====================================================
         * 보안 3.
         * 마지막 ACTIVE ADMIN 보호.
         * =====================================================
         */
        boolean wasActiveAdmin =
            STATUS_ACTIVE.equals(
                previousStatus
            )
            &&
            previousRoles.contains(
                ROLE_ADMIN
            );


        boolean willBeActiveAdmin =
            STATUS_ACTIVE.equals(
                status
            )
            &&
            roles.contains(
                ROLE_ADMIN
            );


        if (
            wasActiveAdmin
            &&
            !willBeActiveAdmin
        ) {

            long activeAdminCount =
                appUserRoleRepository
                    .countActiveAdmins();


            if (
                activeAdminCount <= 1
            ) {

                throw new IllegalArgumentException(
                    "마지막 ACTIVE ADMIN은 "
                    + "잠금·비활성화하거나 "
                    + "ADMIN Role을 제거할 수 없습니다."
                );
            }
        }


        /*
         * APP_USER 상태 변경.
         */
        repository.updateAccountStatus(
            userId,
            status
        );


        /*
         * 기존 Role 제거.
         */
        repository.deleteUserRoles(
            userId
        );


        /*
         * 새로운 Role 부여.
         */
        for (
            String role : roles
        ) {

            Long roleId =
                repository.findRoleId(
                    role
                );


            if (
                roleId == null
            ) {

                throw new IllegalStateException(
                    "DB에서 Role을 찾을 수 없습니다: "
                    + role
                );
            }


            repository.insertUserRole(
                userId,
                roleId
            );
        }


        /*
         * 실제 변경이 발생했을 때만
         * ROLE_CHANGE Audit 기록.
         */
        auditLogService.record(
            admin.getUserId(),
            "ROLE_CHANGE",
            "APP_USER",
            String.valueOf(
                userId
            ),
            requestUri,
            "계정 상태="
                + previousStatus
                + "→"
                + status
                + ", Role="
                + formatRoles(
                    previousRoles
                )
                + "→"
                + formatRoles(
                    roles
                ),
            remoteAddr
        );


        /*
         * =====================================================
         * 보안 4.
         * 상태 또는 Role 변경이 Commit된 뒤
         * 대상 사용자의 기존 Session을 만료한다.
         *
         * 다음 로그인에서 최신 상태 / Role을 다시 읽는다.
         * =====================================================
         */
        userSessionService
            .expireUserSessionsAfterCommit(
                targetUser.getLoginId()
            );
    }


    /**
     * Audit Description용 Role 표현.
     */
    private String formatRoles(
            List<String> roles) {

        if (
            roles == null
            ||
            roles.isEmpty()
        ) {

            return "없음";
        }


        return String.join(
            ",",
            roles
        );
    }
}
