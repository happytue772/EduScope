package com.eduscope.web.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.user.entity.AppUser;
import com.eduscope.web.user.repository.AppUserRepository;
import com.eduscope.web.user.repository.AppUserRoleRepository;

/**
 * Oracle APP_USER / APP_ROLE을
 * Spring Security 인증정보로 변환한다.
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository
        userRepository;

    private final AppUserRoleRepository
        userRoleRepository;


    public CustomUserDetailsService(
            AppUserRepository userRepository,
            AppUserRoleRepository userRoleRepository) {

        this.userRepository =
            userRepository;

        this.userRoleRepository =
            userRoleRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        /*
         * LOGIN_ID 기준 실제 EduScope 사용자 조회.
         */
        AppUser user =
            userRepository
                .findByLoginId(
                    username
                )
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "등록된 사용자를 찾을 수 없습니다."
                    )
                );


        /*
         * APP_USER_ROLE → APP_ROLE
         * Spring Security Authority 변환.
         */
        List<GrantedAuthority> authorities =
            userRoleRepository
                .findAllWithRoleByUserId(
                    user.getUserId()
                )
                .stream()
                .map(userRole ->
                    (GrantedAuthority)
                        new SimpleGrantedAuthority(
                            "ROLE_"
                            +
                            userRole
                                .getRole()
                                .getRoleCode()
                        )
                )
                .toList();


        /*
         * ACTIVE만 로그인 허용.
         *
         * DISABLED 또는 알 수 없는 값은
         * enabled=false로 차단한다.
         */
        boolean enabled =
            "ACTIVE".equals(
                user.getAccountStatus()
            );


        /*
         * LOCKED는 별도 잠금 상태로 전달한다.
         */
        boolean accountNonLocked =
            !"LOCKED".equals(
                user.getAccountStatus()
            );


        return new User(
            user.getLoginId(),
            user.getPasswordHash(),
            enabled,
            true,
            true,
            accountNonLocked,
            authorities
        );
    }
}
