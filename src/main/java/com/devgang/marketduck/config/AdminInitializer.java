package com.devgang.marketduck.config;

import java.util.UUID;

import com.devgang.marketduck.constant.Authority;
import com.devgang.marketduck.constant.LoginType;
import com.devgang.marketduck.constant.UserStatus;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 어드민 계정 생성
        createUserIfNotExists(
                "admin@marketduck.com",
                "관리자",
                "1111!",
                Authority.ADMIN);

        // 테스트용 일반 계정 2개 생성
        createUserIfNotExists(
                "user1@marketduck.com",
                "테스트유저1",
                "1111!",
                Authority.USER);

        createUserIfNotExists(
                "user2@marketduck.com",
                "테스트유저2",
                "1111!",
                Authority.USER);
    }

    private void createUserIfNotExists(String email, String nickname, String password, Authority authority) {
        if (userRepository.findByEmail(email).isEmpty()) {
            log.info("{}({}) 계정이 존재하지 않습니다. 새로운 계정을 생성합니다.", nickname, email);

            User user = User.builder()
                    .username(email)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(password))
                    .profileImageUrl("")
                    .phoneNumber(UUID.randomUUID().toString())
                    .loginType(LoginType.BASIC)
                    .userStatus(UserStatus.ACTIVE)
                    .authority(authority)
                    .emailVerified(true)
                    .phoneVerified(true)
                    .roles(authority.getStringRole())
                    .build();

            userRepository.saveUser(user);
            log.info("{}({}) 계정이 성공적으로 생성되었습니다.", nickname, email);
        } else {
            log.info("{}({}) 계정이 이미 존재합니다.", nickname, email);
        }
    }
}