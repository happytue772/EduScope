package com.eduscope.web.security;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * APP_USER의 상태 또는 Role 변경 후
 * 기존 로그인 Session을 만료시키는 Service.
 */
@Service
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public UserSessionService(
            SessionRegistry sessionRegistry) {

        this.sessionRegistry = sessionRegistry;
    }

    /**
     * DB 변경이 Commit된 뒤 대상 사용자의 기존 Session 만료.
     */
    public void expireUserSessionsAfterCommit(
            String loginId) {

        if (
            loginId == null
            || loginId.isBlank()
        ) {
            return;
        }

        if (
            TransactionSynchronizationManager
                .isActualTransactionActive()
        ) {

            TransactionSynchronizationManager
                .registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            expireUserSessions(loginId);
                        }
                    }
                );

            return;
        }

        expireUserSessions(loginId);
    }

    /** 동일 loginId의 모든 등록 Session 만료. */
    private void expireUserSessions(
            String loginId) {

        for (
            Object principal
            : sessionRegistry.getAllPrincipals()
        ) {

            String principalUsername =
                extractUsername(principal);

            if (!loginId.equals(principalUsername)) {
                continue;
            }

            for (
                SessionInformation session
                : sessionRegistry.getAllSessions(
                    principal,
                    false
                )
            ) {
                session.expireNow();
            }
        }
    }

    private String extractUsername(
            Object principal) {

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String stringPrincipal) {
            return stringPrincipal;
        }

        return null;
    }
}
