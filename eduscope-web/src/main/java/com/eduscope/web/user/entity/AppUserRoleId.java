package com.eduscope.web.user.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * APP_USER_ROLE 복합 PK.
 */
public class AppUserRoleId implements Serializable {

    private Long userId;
    private Long roleId;

    public AppUserRoleId() {
    }

    public AppUserRoleId(
            Long userId,
            Long roleId) {

        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof AppUserRoleId that)) {
            return false;
        }

        return Objects.equals(userId, that.userId)
            && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            userId,
            roleId
        );
    }
}