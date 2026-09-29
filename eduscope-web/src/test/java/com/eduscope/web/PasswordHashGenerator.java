package com.eduscope.web;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 초기 관리자 비밀번호 BCrypt Hash 생성 도구.
 *
 * 비밀번호 자체는 코드에 저장하지 않고
 * 환경변수로 전달한다.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {

        String rawPassword =
            System.getenv("EDUSCOPE_BOOTSTRAP_PASSWORD");

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException(
                "EDUSCOPE_BOOTSTRAP_PASSWORD 환경변수가 없습니다."
            );
        }

        BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

        String hash =
            encoder.encode(rawPassword);

        System.out.println(
            "BCrypt Hash = " + hash
        );
    }
}