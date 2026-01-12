package com.mxxdone.miniproject.util;

import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("prod")
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args)  {

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .nickname("관리자")
                    .email("mxxdone@gmail.com")
                    .build();

            userRepository.save(admin);
            log.info("Admin user created.");
        }
    }
}
