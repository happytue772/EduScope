package com.eduscope.web.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 로그인 실패 횟수 기반 임시 로그인 제한.
 *
 * 기본 정책:
 * - 10분 이내 5회 실패
 * - 이후 5분 동안 로그인 제한
 */
@Component
public class LoginAttemptService {

    private final Map<String, AttemptState> attempts =
        new ConcurrentHashMap<>();

    private final int maxFailures;

    private final Duration failureWindow;

    private final Duration blockDuration;


    public LoginAttemptService(

            @Value("${eduscope.security.login.max-failures:5}")
            int maxFailures,

            @Value("${eduscope.security.login.failure-window-minutes:10}")
            long failureWindowMinutes,

            @Value("${eduscope.security.login.block-minutes:5}")
            long blockMinutes) {

        this.maxFailures =
            maxFailures;

        this.failureWindow =
            Duration.ofMinutes(
                failureWindowMinutes
            );

        this.blockDuration =
            Duration.ofMinutes(
                blockMinutes
            );
    }


    /**
     * 현재 로그인 제한 상태인지 확인.
     */
    public boolean isBlocked(
            String loginId,
            String remoteAddr) {

        return getRemainingBlockSeconds(
            loginId,
            remoteAddr
        ) > 0;
    }


    /**
     * 로그인 제한 해제까지 남은 시간.
     *
     * 반환:
     * 0 = 제한 아님
     * 1 이상 = 남은 초
     */
    public long getRemainingBlockSeconds(
            String loginId,
            String remoteAddr) {

        String key =
            buildKey(
                loginId,
                remoteAddr
            );


        AttemptState state =
            attempts.get(
                key
            );


        if (
            state == null
            ||
            state.blockedUntil() == null
        ) {

            return 0;
        }


        Instant now =
            Instant.now();


        /*
         * 제한 시간이 끝났으면
         * 기록을 제거한다.
         */
        if (
            !now.isBefore(
                state.blockedUntil()
            )
        ) {

            attempts.remove(
                key,
                state
            );

            return 0;
        }


        /*
         * 남은 시간을 초 단위로 올림 처리.
         *
         * 예:
         * 299.2초 남음
         * → 300초 표시
         */
        long remainingMillis =
            Duration
                .between(
                    now,
                    state.blockedUntil()
                )
                .toMillis();


        return Math.max(
            1,
            (
                remainingMillis
                + 999
            )
            / 1000
        );
    }


    /**
     * 로그인 실패 기록.
     *
     * 이번 실패 때문에
     * 새롭게 로그인 제한이 시작되면 true.
     */
    public boolean recordFailure(
            String loginId,
            String remoteAddr) {

        String key =
            buildKey(
                loginId,
                remoteAddr
            );


        Instant now =
            Instant.now();


        AtomicBoolean blockStarted =
            new AtomicBoolean(
                false
            );


        attempts.compute(
            key,
            (ignored, current) -> {

                /*
                 * 기존 기록이 없거나
                 * 실패 집계 시간이 끝난 경우
                 * 새 Window 시작.
                 */
                if (
                    current == null
                    ||
                    (
                        current.blockedUntil() == null
                        &&
                        !now.isBefore(
                            current
                                .windowStartedAt()
                                .plus(
                                    failureWindow
                                )
                        )
                    )
                    ||
                    (
                        current.blockedUntil() != null
                        &&
                        !now.isBefore(
                            current.blockedUntil()
                        )
                    )
                ) {

                    current =
                        new AttemptState(
                            0,
                            now,
                            null
                        );
                }


                /*
                 * 이미 제한 중이면
                 * 제한 시간을 연장하지 않는다.
                 */
                if (
                    current.blockedUntil() != null
                    &&
                    now.isBefore(
                        current.blockedUntil()
                    )
                ) {

                    return current;
                }


                int nextFailureCount =
                    current.failureCount()
                    + 1;


                /*
                 * 최대 실패 횟수 도달.
                 */
                if (
                    nextFailureCount
                    >=
                    maxFailures
                ) {

                    blockStarted.set(
                        true
                    );


                    return new AttemptState(
                        nextFailureCount,
                        current.windowStartedAt(),
                        now.plus(
                            blockDuration
                        )
                    );
                }


                return new AttemptState(
                    nextFailureCount,
                    current.windowStartedAt(),
                    null
                );
            }
        );


        return blockStarted.get();
    }


    /**
     * 정상 로그인 성공 시
     * 해당 ID/IP의 실패 기록 제거.
     */
    public void recordSuccess(
            String loginId,
            String remoteAddr) {

        attempts.remove(
            buildKey(
                loginId,
                remoteAddr
            )
        );
    }


    /**
     * loginId + IP 조합을
     * 로그인 제한 기준으로 사용한다.
     */
    private String buildKey(
            String loginId,
            String remoteAddr) {

        String normalizedLoginId =
            loginId == null
                ? "<blank>"
                : loginId
                    .trim()
                    .toLowerCase(
                        Locale.ROOT
                    );


        if (
            normalizedLoginId.isBlank()
        ) {

            normalizedLoginId =
                "<blank>";
        }


        String normalizedAddress =
            remoteAddr == null
                ? "<unknown>"
                : remoteAddr.trim();


        if (
            normalizedAddress.isBlank()
        ) {

            normalizedAddress =
                "<unknown>";
        }


        return normalizedLoginId
            + "|"
            + normalizedAddress;
    }


    /**
     * 로그인 실패 상태.
     */
    private record AttemptState(

        int failureCount,

        Instant windowStartedAt,

        Instant blockedUntil

    ) {
    }
}