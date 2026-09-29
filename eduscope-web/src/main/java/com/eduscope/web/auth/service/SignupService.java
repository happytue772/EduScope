package com.eduscope.web.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.auth.dto.SignupRequest;
import com.eduscope.web.auth.repository.SignupRepository;

/**
 * 회원가입 Service.
 */
@Service
public class SignupService {

    private final SignupRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final boolean signupEnabled;

    public SignupService(
            SignupRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${eduscope.signup.enabled:true}")
            boolean signupEnabled) {

        this.repository = repository;
        this.passwordEncoder =
            passwordEncoder;
        this.signupEnabled = signupEnabled;
    }


    @Transactional
    public Long signup(
            SignupRequest request) {

        if (!signupEnabled) {
            throw new IllegalStateException(
                "공개 데모에서는 회원가입을 사용할 수 없습니다."
            );
        }

        String loginId =
            request.loginId().trim();

        String displayName =
            request.displayName().trim();


        if (
            !request.password()
                .equals(
                    request.passwordConfirm()
                )
        ) {

            throw new IllegalArgumentException(
                "비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }


        if (
            repository.existsByLoginId(
                loginId
            )
        ) {

            throw new IllegalArgumentException(
                "이미 사용 중인 로그인 ID입니다."
            );
        }


        Long userId =
            repository.nextUserId();


        String passwordHash =
            passwordEncoder.encode(
                request.password()
            );


        repository.insertUser(
            userId,
            loginId,
            passwordHash,
            displayName
        );


        /*
         * 가입 시 Role은 넣지 않는다.
         *
         * 관리자 승인 시
         * APP_USER_ROLE에 Role을 부여한다.
         */
        return userId;
    }
}
