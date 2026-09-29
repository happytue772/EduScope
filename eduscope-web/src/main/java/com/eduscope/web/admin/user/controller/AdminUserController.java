package com.eduscope.web.admin.user.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduscope.web.admin.user.dto.AdminUserResponse;
import com.eduscope.web.admin.user.dto.UserAccessUpdateRequest;
import com.eduscope.web.admin.user.service.AdminUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * ADMIN 전용 사용자 관리 API.
 *
 * URL Security + Method Security를
 * 함께 적용한다.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService
        service;


    public AdminUserController(
            AdminUserService service) {

        this.service =
            service;
    }


    /**
     * EduScope 로그인 사용자 목록 조회.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEMO_ADMIN', 'ADMIN')")
    public List<AdminUserResponse> getUsers() {

        return service.getUsers();
    }


    /**
     * 사용자 계정 상태 및 Role 변경.
     *
     * PATCH /api/admin/users/{userId}/access
     */
    @PatchMapping("/{userId}/access")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateAccess(
            @PathVariable Long userId,
            @Valid
            @RequestBody
            UserAccessUpdateRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {

        service.updateAccess(
            userId,
            request,
            principal.getName(),
            httpRequest.getRequestURI(),
            httpRequest.getRemoteAddr()
        );


        return Map.of(
            "message",
            "사용자 권한이 변경되었습니다."
        );
    }
}
